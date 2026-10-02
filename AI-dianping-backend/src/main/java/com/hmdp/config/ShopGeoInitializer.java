package com.hmdp.config;

import com.hmdp.entity.Shop;
import com.hmdp.service.IShopService;
import com.hmdp.utils.RedisConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** 启动时把店铺坐标预热到 Redis GEO，支撑附近店铺分页查询。 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ShopGeoInitializer implements ApplicationRunner {

    private final IShopService shopService;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void run(ApplicationArguments args) {
        try {
            List<Shop> shops = shopService.list();
            Set<Long> typeIds = shops.stream()
                    .map(Shop::getTypeId)
                    .filter(typeId -> typeId != null)
                    .collect(Collectors.toSet());
            typeIds.forEach(typeId -> stringRedisTemplate.delete(RedisConstants.SHOP_GEO_KEY + typeId));

            int loaded = 0;
            for (Shop shop : shops) {
                if (shop.getId() == null || shop.getTypeId() == null
                        || shop.getX() == null || shop.getY() == null) {
                    continue;
                }
                stringRedisTemplate.opsForGeo().add(
                        RedisConstants.SHOP_GEO_KEY + shop.getTypeId(),
                        new Point(shop.getX(), shop.getY()),
                        shop.getId().toString());
                loaded++;
            }
            log.info("店铺 GEO 坐标预热完成，共加载{}家店铺", loaded);
        } catch (Exception e) {
            log.warn("店铺 GEO 坐标预热失败，附近店铺功能将在 Redis 恢复后重试", e);
        }
    }
}
