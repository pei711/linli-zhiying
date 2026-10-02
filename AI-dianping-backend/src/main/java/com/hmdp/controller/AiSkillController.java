package com.hmdp.controller;

import com.hmdp.dto.AiSkillRunRequest;
import com.hmdp.dto.Result;
import com.hmdp.service.IAiSkillService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai/skills")
public class AiSkillController {

    private final IAiSkillService aiSkillService;

    public AiSkillController(IAiSkillService aiSkillService) {
        this.aiSkillService = aiSkillService;
    }

    @GetMapping
    public Result queryEnabledSkills() {
        return aiSkillService.queryEnabledSkills();
    }

    @GetMapping("/{code}")
    public Result querySkillByCode(@PathVariable("code") String code) {
        return aiSkillService.querySkillByCode(code);
    }

    @PostMapping("/{code}/run")
    public Result runSkill(@PathVariable("code") String code, @RequestBody AiSkillRunRequest request) {
        return aiSkillService.runSkill(code, request);
    }

    @GetMapping("/runs")
    public Result queryMyRuns(@RequestParam(value = "skillCode", required = false) String skillCode) {
        return aiSkillService.queryMyRuns(skillCode);
    }
}
