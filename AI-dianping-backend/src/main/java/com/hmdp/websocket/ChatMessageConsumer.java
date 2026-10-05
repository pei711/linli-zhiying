package com.hmdp.websocket;

import com.hmdp.dto.ChatMessageEvent;
import com.hmdp.entity.ChatMessage;
import com.hmdp.mapper.ChatMessageMapper;
import com.hmdp.mapper.ShopOwnerMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatMessageConsumer {
    private final ChatMessageMapper messageMapper;
    private final ShopOwnerMapper shopOwnerMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${app.chat.store-topic}", groupId = "${app.chat.store-group}")
    public void persist(String payload) throws Exception {
        ChatMessageEvent event = objectMapper.readValue(payload, ChatMessageEvent.class);
        ChatMessage message = new ChatMessage();
        message.setMessageId(event.getMessageId());
        message.setShopId(event.getShopId());
        message.setCustomerUserId(event.getCustomerUserId());
        message.setSenderUserId(event.getSenderUserId());
        message.setSenderRole(event.getSenderRole());
        message.setContent(event.getContent());
        message.setCreateTime(event.getCreateTime());
        try {
            messageMapper.insert(message);
        } catch (DuplicateKeyException duplicateDelivery) {
            log.info("Ignoring duplicate chat event, messageId={}", event.getMessageId());
        }
    }

    @KafkaListener(topics = "${app.chat.push-topic}", groupId = "${app.chat.push-group}")
    public void push(String payload) throws Exception {
        ChatMessageEvent event = objectMapper.readValue(payload, ChatMessageEvent.class);
        Set<Long> recipients = new LinkedHashSet<>();
        recipients.add(event.getCustomerUserId());
        recipients.add(shopOwnerMapper.findOwnerId(event.getShopId()));
        recipients.remove(null);
        for (Long recipientId : recipients) {
            messagingTemplate.convertAndSendToUser(recipientId.toString(), "/queue/chat", event);
        }
    }
}
