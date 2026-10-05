package com.hmdp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChatSendRequest {
    @NotNull
    private Long shopId;
    /** Required only when a mapped shop owner replies to a customer. */
    private Long customerUserId;
    @NotBlank
    @Size(max = 2000)
    private String content;
}
