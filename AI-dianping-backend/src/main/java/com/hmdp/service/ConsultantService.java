package com.hmdp.service;

import com.hmdp.tools.AiSkillTool;
import com.hmdp.tools.OrderTool;
import com.hmdp.tools.ShopTool;
import com.hmdp.tools.VoucherTool;
import com.hmdp.utils.ReservationTool;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;
import reactor.core.publisher.Flux;

@AiService(
        wiringMode = AiServiceWiringMode.EXPLICIT,
        chatModel = "openAiChatModel",
        streamingChatModel = "openAiStreamingChatModel",
        chatMemoryProvider = "chatMemoryProvider",
        tools = {"shopTool", "voucherTool", "orderTool", "reservationTool", "aiSkillTool"}
)
public interface ConsultantService {
    @SystemMessage(fromResource = "system.txt")
    Flux<String> chat(@MemoryId String memoryId, @UserMessage String message);
}
