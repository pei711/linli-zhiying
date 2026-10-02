package com.hmdp.utils;

import cn.hutool.core.lang.UUID;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;

public class SimpleRedisLock implements ILock {

    private String keyName;
    private StringRedisTemplate stringRedisTemplate;
    private final static String PREFIX_LOCK = "lock:";
    private String ID_PREFIX = UUID.randomUUID().toString(true) + "-";

    public SimpleRedisLock(String keyName, StringRedisTemplate stringRedisTemplate) {
        this.keyName = keyName;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public boolean tryLock(long timeout) {
        String threadId = ID_PREFIX + Thread.currentThread().getId();
        Boolean success = stringRedisTemplate.opsForValue()
                .setIfAbsent(PREFIX_LOCK + keyName, threadId, timeout, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(success);
    }

    @Override
    public void unlock() {
        //判断当前锁是否是自己的锁
        //获取线程标识
        String threadId = ID_PREFIX + Thread.currentThread().getId();
        //获取redis中的标识
        String id = stringRedisTemplate.opsForValue().get(PREFIX_LOCK + keyName);
        //判断是否相等
        if(threadId.equals(id)) {
            //相等，删除锁
            stringRedisTemplate.delete(PREFIX_LOCK + keyName);
            //不相等，不操作（别人的锁）
        }
    }
}
