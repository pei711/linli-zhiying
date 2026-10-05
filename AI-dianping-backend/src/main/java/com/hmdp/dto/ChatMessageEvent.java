package com.hmdp.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatMessageEvent {
    private String messageId;
    private Long shopId;
    private Long customerUserId;
    private Long senderUserId;
    private String senderRole;
    private String content;
    private LocalDateTime createTime;
}
