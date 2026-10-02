package com.hmdp.tools;

import cn.hutool.json.JSONUtil;
import com.hmdp.dto.AiSkillRunRequest;
import com.hmdp.dto.Result;
import com.hmdp.service.IAiSkillService;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiSkillTool {

    private final IAiSkillService aiSkillService;

    @Tool("查询当前系统已启用的 AI 技能列表，返回技能编码、名称、描述等信息")
    public String queryEnabledAiSkills() {
        log.info("[AiSkillTool] 查询可用 AI 技能列表");
        Result result = aiSkillService.queryEnabledSkills();
        return JSONUtil.toJsonStr(result);
    }

    @Tool("根据技能编码调用 AI 技能，传入用户输入变量并返回生成结果。常用技能编码：review_summary（评价总结）、review_reply（商家回复）、blog_draft（探店笔记）、coupon_copy（优惠券文案）、short_video_script（短视频脚本）")
    public String invokeAiSkill(String skillCode, String userInput) {
        log.info("[AiSkillTool] 调用 AI 技能, skillCode={}, userInput={}", skillCode, userInput);
        if (skillCode == null || skillCode.trim().isEmpty()) {
            return JSONUtil.toJsonStr(Result.fail("技能编码不能为空"));
        }
        if (userInput == null || userInput.trim().isEmpty()) {
            return JSONUtil.toJsonStr(Result.fail("用户输入不能为空"));
        }
        Map<String, Object> input = Map.of(
                "input", userInput,
                "content", userInput,
                "keywords", userInput,
                "review", userInput
        );
        AiSkillRunRequest request = new AiSkillRunRequest();
        request.setInput(input);
        Result result = aiSkillService.runSkill(skillCode.trim(), request);
        return JSONUtil.toJsonStr(result);
    }
}
