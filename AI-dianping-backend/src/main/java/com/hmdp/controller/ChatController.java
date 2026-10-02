package com.hmdp.controller;

import com.hmdp.service.ConsultantService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ConsultantService consultantService;

    public ChatController(ConsultantService consultantService) {
        this.consultantService = consultantService;
    }

    @GetMapping(produces = "text/plain;charset=utf-8")
    public Flux<String> chat(@RequestParam("memoryId") String memoryId,
                             @RequestParam("message") String message) {
        return consultantService.chat(memoryId, message);
    }

}
