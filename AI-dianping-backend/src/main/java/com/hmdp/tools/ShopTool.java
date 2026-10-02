package com.hmdp.tools;

import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.entity.ShopType;
import com.hmdp.service.IShopService;
import com.hmdp.service.IShopTypeService;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShopTool {

    private final IShopService shopService;
    private final IShopTypeService shopTypeService;

    @Tool("根据店铺ID查询店铺详细信息，包括名称、地址、类型、评分、营业状态、人均消费等")
    public String queryShopById(Long shopId) {
        log.info("[ShopTool] 查询店铺详情, shopId={}", shopId);
        if (shopId == null || shopId <= 0) {
            return JSONUtil.toJsonStr(Result.fail("店铺ID不能为空"));
        }
        Result result = shopService.queryById(shopId);
        return JSONUtil.toJsonStr(result);
    }

    @Tool("查询所有店铺类型分类，例如美食、娱乐、酒店等")
    public String queryShopTypes() {
        log.info("[ShopTool] 查询店铺类型列表");
        List<ShopType> list = shopTypeService.query().orderByAsc("sort").list();
        return JSONUtil.toJsonStr(Result.ok(list));
    }

    @Tool("根据当前用户位置（经纬度）和店铺类型ID，分页查询附近的店铺，按距离由近到远排序")
    public String queryNearbyShops(Integer typeId, Integer current, Double x, Double y) {
        log.info("[ShopTool] 附近店铺查询, typeId={}, current={}, x={}, y={}", typeId, current, x, y);
        if (typeId == null || typeId <= 0 || x == null || y == null) {
            return JSONUtil.toJsonStr(Result.fail("店铺类型、经度、纬度均不能为空"));
        }
        if (current == null || current < 1) {
            current = 1;
        }
        Result result = shopService.queryShopByType(typeId, current, x, y);
        return JSONUtil.toJsonStr(result);
    }

    @Tool("根据关键词模糊搜索店铺名称，返回匹配的店铺列表")
    public String searchShopByName(String keyword) {
        log.info("[ShopTool] 店铺关键词搜索, keyword={}", keyword);
        if (keyword == null || keyword.trim().isEmpty()) {
            return JSONUtil.toJsonStr(Result.fail("搜索关键词不能为空"));
        }
        List<Shop> list = shopService.query()
                .like("name", keyword)
                .last("limit 20")
                .list();
        List<Shop> dtoList = list.stream()
                .map(shop -> {
                    Shop s = new Shop();
                    s.setId(shop.getId());
                    s.setName(shop.getName());
                    s.setTypeId(shop.getTypeId());
                    s.setArea(shop.getArea());
                    s.setAddress(shop.getAddress());
                    s.setScore(shop.getScore());
                    s.setAvgPrice(shop.getAvgPrice());
                    s.setOpenHours(shop.getOpenHours());
                    return s;
                })
                .collect(Collectors.toList());
        return JSONUtil.toJsonStr(Result.ok(dtoList));
    }
}
