package com.hmdp.websocket;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.hmdp.dto.UserDTO;
import com.hmdp.utils.RedisConstants;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class ChatStompAuthInterceptor implements ChannelInterceptor {
    private final StringRedisTemplate redis;

    public ChatStompAuthInterceptor(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = accessor.getFirstNativeHeader("authorization");
            if (token != null && token.regionMatches(true, 0, "Bearer ", 0, 7)) {
                token = token.substring(7).trim();
            }
            if (StrUtil.isBlank(token)) {
                throw new MessageDeliveryException(message, "Login token is required");
            }
            Map<Object, Object> userMap = redis.opsForHash().entries(RedisConstants.LOGIN_USER_KEY + token);
            UserDTO user = BeanUtil.fillBeanWithMap(userMap, new UserDTO(), false);
            if (user.getId() == null) {
                throw new MessageDeliveryException(message, "Login token is invalid or expired");
            }
            redis.expire(RedisConstants.LOGIN_USER_KEY + token, RedisConstants.LOGIN_USER_TTL, TimeUnit.MINUTES);
            String userId = user.getId().toString();
            accessor.setUser((Principal) () -> userId);
        } else if (StompCommand.SEND.equals(accessor.getCommand())) {
            if (accessor.getUser() == null || !"/app/chat.send".equals(accessor.getDestination())) {
                throw new MessageDeliveryException(message, "Only authenticated chat messages are accepted");
            }
        } else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            if (accessor.getUser() == null || !"/user/queue/chat".equals(accessor.getDestination())
                    && !"/user/queue/chat-ack".equals(accessor.getDestination())) {
                throw new MessageDeliveryException(message, "Subscription is not allowed");
            }
        }
        return message;
    }
}
