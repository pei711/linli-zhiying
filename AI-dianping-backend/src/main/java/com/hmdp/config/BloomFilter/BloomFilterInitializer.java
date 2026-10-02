package com.hmdp.config.BloomFilter;

import com.google.common.hash.BloomFilter;
import com.hmdp.entity.Shop;
import com.hmdp.service.IShopService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.List;

@Slf4j
@Component
public class BloomFilterInitializer implements ApplicationRunner {
    
    @Resource
    private IShopService shopService;
    
    @Resource
    private BloomFilter<Long> shopBloomFilter;
    
    @Override
    public void run(ApplicationArguments args) {
        initBloomFilter();
    }
    
    public void initBloomFilter() {
        log.info("初始化布隆过滤器");
        
        // 获取所有商铺ID
        List<Shop> shops = shopService.list();
        for (Shop shop : shops) {
            shopBloomFilter.put(shop.getId());
        }
        
        log.info("布隆过滤器初始化完成，共加载{}个店铺", shops.size());
    }
}
