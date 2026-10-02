package com.hmdp.tools;

import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.Voucher;
import com.hmdp.service.IVoucherService;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class VoucherTool {

    private final IVoucherService voucherService;

    @Tool("查询指定店铺ID下的所有优惠券/代金券列表，包含普通券和秒杀券")
    public String queryVouchersByShopId(Long shopId) {
        log.info("[VoucherTool] 查询店铺优惠券, shopId={}", shopId);
        if (shopId == null || shopId <= 0) {
            return JSONUtil.toJsonStr(Result.fail("店铺ID不能为空"));
        }
        Result result = voucherService.queryVoucherOfShop(shopId);
        return JSONUtil.toJsonStr(result);
    }
}
