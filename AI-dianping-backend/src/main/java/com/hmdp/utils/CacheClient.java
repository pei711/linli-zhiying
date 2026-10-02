package com.hmdp.utils;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.hash.BloomFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * <p>
 * Redis工具类
 * </p>
 *
 * @author 超大王
 * @since 2025-09-19
 */
@Slf4j
@Component
public class CacheClient {

    private static final long LOCAL_CACHE_MAX_SIZE = 10_000L;
    private static final long LOCAL_CACHE_TTL_SECONDS = 300L;

    // 布隆过滤器
    @Resource
    private BloomFilter<Long> bloomFilter;

    private final Cache<String, LocalCacheValue> localCache = CacheBuilder.newBuilder()
            .maximumSize(LOCAL_CACHE_MAX_SIZE)
            .build();

    private final StringRedisTemplate stringRedisTemplate;

    public CacheClient(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    //将任意Java对象序列化为json并存储在string类型的key中，并且可以设置TTL过期时间
    public void set(String key, Object value, Long time, TimeUnit unit){
        setJson(key, JSONUtil.toJsonStr(value), time, unit);
    }

    public String get(String key, Long time, TimeUnit unit) {
        LocalCacheValue localValue = getLocalValue(key);
        if (localValue != null) {
            return localValue.value;
        }
        String json = stringRedisTemplate.opsForValue().get(key);
        if (json != null) {
            putLocalValue(key, json, time, unit);
        }
        return json;
    }

    public void delete(String key) {
        localCache.invalidate(key);
        stringRedisTemplate.delete(key);
    }

    //根据指定的key查询缓存，并反序列化为指定类型，利用缓存空值的方式解决缓存穿透问题,利用互斥锁解决缓存击穿问题
    public <R, ID> R queryWithPassThroughAndMutex(
           final String keyPrefix, final String lockKeyPrefix, ID id, Class<R> type, Function<ID, R> dbFallback,
            Long time, TimeUnit unit, Long nullValueTTL, TimeUnit nullValueUnit){
        if (id == null) {
            return null;
        }
        // 1.布隆过滤器判断id是否存在
        if (id instanceof Long && !bloomFilter.mightContain((Long)id)) {
            //不存在，直接返回错误
            log.info("布隆过滤器拦截，id:{}不存在", id);
            return null;
        }
        String key = keyPrefix + id;
        String lockKey = lockKeyPrefix + id;
        while (true) {
            // 2.先查本地缓存，再查Redis缓存
            String json = getFromLocalOrRedis(key, time, unit, nullValueTTL, nullValueUnit);
            // 3.判断是否存在
            if (StrUtil.isNotBlank(json)) {
                // 存在，直接返回
                R result = JSONUtil.toBean(json, type);
                return result;
            }

            // 4.判断是否位空值（缓存空值）
            if (json != null) {
                // 是空值，返回不存在
                return null;
            }
            // 5.不存在，去数据库查找
            R result = null;
            boolean isLock = false;
            try {
                isLock = tryLock(lockKey);
                //判断是否获取锁成功
                if (!isLock) {
                    //失败，休眠并重试
                    Thread.sleep(50);
                    continue;
                } else {
                    // 再次检查缓存（Double Check）, 避免重复查询数据库
                    json = getFromLocalOrRedis(key, time, unit, nullValueTTL, nullValueUnit);
                    if (StrUtil.isNotBlank(json)) {
                        R cached = JSONUtil.toBean(json, type);
                        return cached;
                    }
                    if (json != null) {
                        return null;
                    }
                }
                //成功，根据id查询数据库
                result = dbFallback.apply(id);
                // 6.数据库不存在，返回错误信息
                if (result == null) {
                    //将空值写入redis（设置较短的过期时间）
                    setJson(key, "", nullValueTTL, nullValueUnit);
                    return null;
                }
                // 7.存在，写入redis
                // 生成TTL随机数,防止缓存雪崩
                Long random = (long) RandomUtil.randomInt(3, 10);
                set(key, result, time + random, unit);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            } finally {
                //释放锁
                if (isLock) {
                    unlock(lockKey);
                }
            }
            // 8.返回
            return result;
        }
    }

    //获取互斥锁
    private boolean tryLock(String key){
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag);
    }
    //释放锁
    private void unlock(String key){
        stringRedisTemplate.delete(key);
    }

    private String getFromLocalOrRedis(String key, Long time, TimeUnit unit, Long nullValueTTL, TimeUnit nullValueUnit) {
        LocalCacheValue localValue = getLocalValue(key);
        if (localValue != null) {
            return localValue.value;
        }
        String json = stringRedisTemplate.opsForValue().get(key);
        if (json == null) {
            return null;
        }
        if (StrUtil.isNotBlank(json)) {
            putLocalValue(key, json, time, unit);
        } else {
            putLocalValue(key, json, nullValueTTL, nullValueUnit);
        }
        return json;
    }

    private void setJson(String key, String json, Long time, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, json, time, unit);
        putLocalValue(key, json, time, unit);
    }

    private LocalCacheValue getLocalValue(String key) {
        LocalCacheValue value = localCache.getIfPresent(key);
        if (value == null) {
            return null;
        }
        if (value.isExpired()) {
            localCache.invalidate(key);
            return null;
        }
        return value;
    }

    private void putLocalValue(String key, String value, Long time, TimeUnit unit) {
        if (time == null || unit == null || time <= 0) {
            localCache.invalidate(key);
            return;
        }
        long ttlNanos = Math.min(unit.toNanos(time), TimeUnit.SECONDS.toNanos(LOCAL_CACHE_TTL_SECONDS));
        localCache.put(key, new LocalCacheValue(value, System.nanoTime() + ttlNanos));
    }

    private static class LocalCacheValue {
        private final String value;
        private final long expireAtNanos;

        private LocalCacheValue(String value, long expireAtNanos) {
            this.value = value;
            this.expireAtNanos = expireAtNanos;
        }

        private boolean isExpired() {
            return System.nanoTime() - expireAtNanos >= 0;
        }
    }

}
