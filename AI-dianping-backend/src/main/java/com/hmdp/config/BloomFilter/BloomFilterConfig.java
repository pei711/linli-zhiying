package com.hmdp.config.BloomFilter;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BloomFilterConfig {

    @Bean
    public BloomFilter<Long> bloomFilter() {
        // 创建布隆过滤器（预计元素数量，误判率）
        return BloomFilter.create(
                Funnels.longFunnel(),
                10000,
                0.01);
    }

}
