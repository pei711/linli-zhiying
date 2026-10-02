package com.hmdp.config;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommonConfig {

    private final ChatMemoryStore redisChatMemoryStore;

    public CommonConfig(ChatMemoryStore redisChatMemoryStore) {
        this.redisChatMemoryStore = redisChatMemoryStore;
    }

    @Bean
    public ChatMemory chatMemory(){
        return MessageWindowChatMemory
                .builder()
                .maxMessages(20)//最多记忆20条消息
                .build();
    }

    @Bean
    public ChatMemoryProvider chatMemoryProvider(){
        //匿名内部类
        //根据记忆ID获取记忆对象
        //设置记忆存储组件
        return new ChatMemoryProvider(){//匿名内部类
            //根据记忆ID获取记忆对象
            @Override
            public ChatMemory get(Object memoryId){
                return MessageWindowChatMemory
                        .builder()
                        .maxMessages(20)
                        .id(memoryId)
                        .chatMemoryStore(redisChatMemoryStore)//设置记忆存储组件
                        .build();
            }
        };
    }
}
