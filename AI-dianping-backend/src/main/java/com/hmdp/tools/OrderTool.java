package com.hmdp.tools;

import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.VoucherOrder;
import com.hmdp.service.IVoucherOrderService;
import com.hmdp.utils.UserHolder;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTool {

    private final IVoucherOrderService voucherOrderService;

    @Tool("查询当前登录用户的优惠券订单列表，支持指定订单状态：1-未使用，2-已使用，3-已退款，不传则查询全部")
    public String queryMyOrders(Integer status) {
        Long userId = UserHolder.getUser() != null ? UserHolder.getUser().getId() : null;
        log.info("[OrderTool] 查询我的订单, userId={}, status={}", userId, status);
        if (userId == null) {
            return JSONUtil.toJsonStr(Result.fail("用户未登录，无法查询订单"));
        }
        var query = voucherOrderService.lambdaQuery()
                .eq(VoucherOrder::getUserId, userId)
                .orderByDesc(VoucherOrder::getCreateTime);
        if (status != null) {
            query.eq(VoucherOrder::getStatus, status);
        }
        List<VoucherOrder> list = query.list();
        List<VoucherOrder> dtoList = list.stream()
                .map(order -> {
                    VoucherOrder vo = new VoucherOrder();
                    vo.setId(order.getId());
                    vo.setVoucherId(order.getVoucherId());
                    vo.setPayTime(order.getPayTime());
                    vo.setStatus(order.getStatus());
                    vo.setCreateTime(order.getCreateTime());
                    return vo;
                })
                .collect(Collectors.toList());
        return JSONUtil.toJsonStr(Result.ok(dtoList, (long) dtoList.size()));
    }
}
