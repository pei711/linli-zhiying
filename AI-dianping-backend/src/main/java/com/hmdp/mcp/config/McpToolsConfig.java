package com.hmdp.mcp.config;

import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.entity.ShopType;
import com.hmdp.service.IShopService;
import com.hmdp.service.IShopTypeService;
import com.hmdp.service.IVoucherService;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpStatelessServerFeatures;
import io.modelcontextprotocol.server.McpStatelessSyncServer;
import io.modelcontextprotocol.server.transport.HttpServletStatelessServerTransport;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class McpToolsConfig {

    private final IShopService shopService;
    private final IShopTypeService shopTypeService;
    private final IVoucherService voucherService;

    @Bean
    public McpStatelessSyncServer mcpStatelessSyncServer(HttpServletStatelessServerTransport transport) {
        McpStatelessSyncServer server = McpServer.sync(transport)
                .serverInfo("ai-dianping-mcp", "1.0.0")
                .capabilities(McpSchema.ServerCapabilities.builder()
                        .tools(true)
                        .build())
                .build();

        registerShopTools(server);
        registerVoucherTools(server);
        return server;
    }

    private void registerShopTools(McpStatelessSyncServer server) {
        server.addTool(new McpStatelessServerFeatures.SyncToolSpecification(
                new McpSchema.Tool("queryShopById",
                        "根据店铺ID查询店铺详细信息，包括名称、地址、类型、评分、营业状态、人均消费等",
                        """
                                { "type": "object",
                                  "properties": {
                                    "shopId": { "type": "integer", "description": "店铺ID" }
                                  },
                                  "required": ["shopId"] }
                                """),
                (exchange, request) -> {
                    Number shopId = (Number) request.arguments().get("shopId");
                    Result result = shopService.queryById(shopId == null ? null : shopId.longValue());
                    return new McpSchema.CallToolResult(JSONUtil.toJsonStr(result), false);
                }));

        server.addTool(new McpStatelessServerFeatures.SyncToolSpecification(
                new McpSchema.Tool("queryShopTypes",
                        "查询所有店铺类型分类，例如美食、娱乐、酒店等",
                        """
                                { "type": "object", "properties": {} }
                                """),
                (exchange, request) -> {
                    List<ShopType> list = shopTypeService.query().orderByAsc("sort").list();
                    return new McpSchema.CallToolResult(JSONUtil.toJsonStr(Result.ok(list)), false);
                }));

        server.addTool(new McpStatelessServerFeatures.SyncToolSpecification(
                new McpSchema.Tool("queryNearbyShops",
                        "根据当前用户位置（经纬度）和店铺类型ID，分页查询附近的店铺，按距离由近到远排序",
                        """
                                { "type": "object",
                                  "properties": {
                                    "typeId": { "type": "integer", "description": "店铺类型ID" },
                                    "current": { "type": "integer", "description": "页码，从1开始，默认1" },
                                    "x": { "type": "number", "description": "经度" },
                                    "y": { "type": "number", "description": "纬度" }
                                  },
                                  "required": ["typeId", "x", "y"] }
                                """),
                (exchange, request) -> {
                    Map<String, Object> args = request.arguments();
                    Number typeId = (Number) args.get("typeId");
                    Number current = (Number) args.get("current");
                    Number x = (Number) args.get("x");
                    Number y = (Number) args.get("y");
                    int page = current == null || current.intValue() < 1 ? 1 : current.intValue();
                    Result result = shopService.queryShopByType(typeId == null ? null : typeId.intValue(),
                            page, x == null ? null : x.doubleValue(), y == null ? null : y.doubleValue());
                    return new McpSchema.CallToolResult(JSONUtil.toJsonStr(result), false);
                }));

        server.addTool(new McpStatelessServerFeatures.SyncToolSpecification(
                new McpSchema.Tool("searchShopByName",
                        "根据关键词模糊搜索店铺名称，返回匹配的店铺列表",
                        """
                                { "type": "object",
                                  "properties": {
                                    "keyword": { "type": "string", "description": "店铺名称关键词" }
                                  },
                                  "required": ["keyword"] }
                                """),
                (exchange, request) -> {
                    String keyword = (String) request.arguments().get("keyword");
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
                    return new McpSchema.CallToolResult(JSONUtil.toJsonStr(Result.ok(dtoList)), false);
                }));
    }

    private void registerVoucherTools(McpStatelessSyncServer server) {
        server.addTool(new McpStatelessServerFeatures.SyncToolSpecification(
                new McpSchema.Tool("queryVouchersByShopId",
                        "查询指定店铺ID下的所有优惠券/代金券列表，包含普通券和秒杀券",
                        """
                                { "type": "object",
                                  "properties": {
                                    "shopId": { "type": "integer", "description": "店铺ID" }
                                  },
                                  "required": ["shopId"] }
                                """),
                (exchange, request) -> {
                    Number shopId = (Number) request.arguments().get("shopId");
                    Result result = voucherService.queryVoucherOfShop(shopId == null ? null : shopId.longValue());
                    return new McpSchema.CallToolResult(JSONUtil.toJsonStr(result), false);
                }));
    }

}
