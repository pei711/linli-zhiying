package com.hmdp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hmdp.dto.ChatMessageEvent;
import com.hmdp.dto.ChatSendRequest;
import com.hmdp.entity.ChatMessage;
import com.hmdp.mapper.ChatMessageMapper;
import com.hmdp.mapper.ShopOwnerMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class ChatMessageService {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ChatMessageMapper messageMapper;
    private final ShopOwnerMapper shopOwnerMapper;
    private final IShopService shopService;
    private final ObjectMapper objectMapper;
    private final String pushTopic;
    private final String storeTopic;

    public ChatMessageService(KafkaTemplate<String, String> kafkaTemplate,
                              ChatMessageMapper messageMapper,
                              ShopOwnerMapper shopOwnerMapper,
                              IShopService shopService,
                              ObjectMapper objectMapper,
                              @Value("${app.chat.push-topic}") String pushTopic,
                              @Value("${app.chat.store-topic}") String storeTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.messageMapper = messageMapper;
        this.shopOwnerMapper = shopOwnerMapper;
        this.shopService = shopService;
        this.objectMapper = objectMapper;
        this.pushTopic = pushTopic;
        this.storeTopic = storeTopic;
    }

    public ChatMessageEvent enqueue(Long actorId, ChatSendRequest request) {
        if (actorId == null || request == null || request.getShopId() == null || request.getShopId() <= 0) {
            throw new IllegalArgumentException("Invalid chat request");
        }
        String content = request.getContent() == null ? "" : request.getContent().trim();
        if (content.isEmpty() || content.length() > 2000) {
            throw new IllegalArgumentException("Message must contain 1 to 2000 characters");
        }
        if (shopService.getById(request.getShopId()) == null) {
            throw new IllegalArgumentException("Shop does not exist");
        }
        Long ownerId = shopOwnerMapper.findOwnerId(request.getShopId());
        if (ownerId == null) {
            throw new IllegalArgumentException("This shop has no merchant chat account configured");
        }

        boolean isMerchant = actorId.equals(ownerId);
        Long customerId = isMerchant ? request.getCustomerUserId() : actorId;
        if (customerId == null || customerId <= 0 || isMerchant && customerId.equals(ownerId)) {
            throw new IllegalArgumentException("A valid customer is required for merchant replies");
        }
        if (isMerchant && messageMapper.selectCount(new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getShopId, request.getShopId())
                .eq(ChatMessage::getCustomerUserId, customerId)) == 0) {
            throw new IllegalArgumentException("Customer conversation does not exist");
        }

        ChatMessageEvent event = new ChatMessageEvent();
        event.setMessageId(UUID.randomUUID().toString());
        event.setShopId(request.getShopId());
        event.setCustomerUserId(customerId);
        event.setSenderUserId(actorId);
        event.setSenderRole(isMerchant ? "merchant" : "customer");
        event.setContent(content);
        event.setCreateTime(LocalDateTime.now());
        String key = request.getShopId() + ":" + customerId;
        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.executeInTransaction(operations -> {
                try {
                    operations.send(pushTopic, key, payload).get(5, TimeUnit.SECONDS);
                    operations.send(storeTopic, key, payload).get(5, TimeUnit.SECONDS);
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Interrupted while publishing chat message", interrupted);
                } catch (Exception sendFailure) {
                    throw new IllegalStateException("Failed to publish chat message", sendFailure);
                }
                return null;
            });
            return event;
        } catch (Exception e) {
            log.error("Failed to enqueue merchant chat message, messageId={}", event.getMessageId(), e);
            throw new IllegalStateException("Message service is temporarily unavailable", e);
        }
    }

    public List<ChatMessage> history(Long actorId, Long shopId, Long requestedCustomerId) {
        Long ownerId = shopOwnerMapper.findOwnerId(shopId);
        Long customerId;
        if (actorId != null && actorId.equals(ownerId)) {
            if (requestedCustomerId == null || requestedCustomerId <= 0) {
                throw new IllegalArgumentException("Customer ID is required for merchant chat history");
            }
            customerId = requestedCustomerId;
            if (messageMapper.selectCount(new LambdaQueryWrapper<ChatMessage>()
                    .eq(ChatMessage::getShopId, shopId)
                    .eq(ChatMessage::getCustomerUserId, customerId)) == 0) {
                return List.of();
            }
        } else {
            customerId = actorId;
        }
        if (shopService.getById(shopId) == null) {
            throw new IllegalArgumentException("Shop does not exist");
        }
        List<ChatMessage> messages = messageMapper.selectList(new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getShopId, shopId)
                .eq(ChatMessage::getCustomerUserId, customerId)
                .orderByDesc(ChatMessage::getId)
                .last("LIMIT 100"));
        java.util.Collections.reverse(messages);
        return messages;
    }

    public List<Long> merchantCustomers(Long actorId, Long shopId) {
        if (shopService.getById(shopId) == null || !actorId.equals(shopOwnerMapper.findOwnerId(shopId))) {
            throw new IllegalArgumentException("You are not authorized to access this shop's conversations");
        }
        return messageMapper.findCustomerIdsByShopId(shopId);
    }
}
