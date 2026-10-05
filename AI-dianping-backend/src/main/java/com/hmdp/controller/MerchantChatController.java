package com.hmdp.controller;

import com.hmdp.dto.Result;
import com.hmdp.service.ChatMessageService;
import com.hmdp.utils.UserHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class MerchantChatController {
    private final ChatMessageService chatMessageService;

    @GetMapping("/shops/{shopId}/messages")
    public Result history(@PathVariable Long shopId,
                          @RequestParam(required = false) Long customerId) {
        return Result.ok(chatMessageService.history(UserHolder.getUser().getId(), shopId, customerId));
    }

    @GetMapping("/merchant/shops/{shopId}/customers")
    public Result customers(@PathVariable Long shopId) {
        return Result.ok(chatMessageService.merchantCustomers(UserHolder.getUser().getId(), shopId));
    }
}
