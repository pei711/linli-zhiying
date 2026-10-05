package com.hmdp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_chat_message")
public class ChatMessage {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String messageId;
    private Long shopId;
    private Long customerUserId;
    private Long senderUserId;
    private String senderRole;
    private String content;
    private LocalDateTime createTime;
}
