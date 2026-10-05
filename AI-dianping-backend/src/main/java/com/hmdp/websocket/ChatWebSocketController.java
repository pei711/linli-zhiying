package com.hmdp.websocket;

import com.hmdp.dto.ChatMessageEvent;
import com.hmdp.dto.ChatSendRequest;
import com.hmdp.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {
    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.send")
    public void send(ChatSendRequest request, Principal principal) {
        Long senderId = Long.valueOf(principal.getName());
        ChatMessageEvent event = chatMessageService.enqueue(senderId, request);
        messagingTemplate.convertAndSendToUser(senderId.toString(), "/queue/chat-ack",
                Map.of("messageId", event.getMessageId(), "status", "QUEUED"));
    }
}
