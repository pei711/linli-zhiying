package com.hmdp.utils;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Repository
public class RedisChatMemoryStore implements ChatMemoryStore {

    private final StringRedisTemplate stringRedisTemplate;

    public RedisChatMemoryStore(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        //从Redis中获取消息列表，反序列化为ChatMessage对象列表并返回
        String json = stringRedisTemplate.opsForValue().get(memoryId.toString());
        //把json转换为list
        List<ChatMessage> list = ChatMessageDeserializer.messagesFromJson(json);
        return list;
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> list) {
        //将消息列表序列化为字符串并存储到Redis中
        //把list转换为json
        String json = ChatMessageSerializer.messagesToJson(list);
        //存储到Redis中
        stringRedisTemplate.opsForValue().set(memoryId.toString(), json, 1L, TimeUnit.DAYS);
    }

    @Override
    public void deleteMessages(Object memoryId) {
        //从Redis中删除指定ID的消息列表
        stringRedisTemplate.delete(memoryId.toString());
    }
}