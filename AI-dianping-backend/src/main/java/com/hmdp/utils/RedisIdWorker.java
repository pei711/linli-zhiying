package com.hmdp.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * Redis分布式ID生成器
 *  生成全局唯一ID
 */
@Slf4j
@Component
public class RedisIdWorker {

    /*
     * 开始时间戳 2025-01-01 00:00:00 UTC
     */
    private static final long BEGIN_TIMESTAMP = 1735689600L; // 2025-01-01 00:00:00 UTC in seconds
    private static final int COUNT_BITS = 32; // 序列号位数

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public long nextId(String keyPrefix) {
        // 1.生成时间戳
        LocalDateTime now = LocalDateTime.now();
        long nowSecond = now.toEpochSecond(ZoneOffset.UTC);
        long timeStamp = nowSecond - BEGIN_TIMESTAMP;

        // 2.生成序列号
        // 2.1 获取当前日期（天）
        String day = now.format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
        // 2.2 自增长
        long count = stringRedisTemplate.opsForValue().increment("icr:" + keyPrefix + ":" + day);

        // 3.拼接并返回
        return timeStamp << COUNT_BITS | count;
    }


    //获取当前时间戳，单位秒
//    public static void main(String[] args) {
//        LocalDateTime time = LocalDateTime.of(2025, 1, 1, 0, 0, 0);
//        Long second = time.toEpochSecond(java.time.ZoneOffset.UTC);
//        System.out.println(second);
//    }
}
