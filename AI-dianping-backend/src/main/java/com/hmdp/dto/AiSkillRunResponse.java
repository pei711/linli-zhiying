package com.hmdp.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

@Data
@Accessors(chain = true)
public class AiSkillRunResponse {

    private Long runId;

    private String skillCode;

    private String skillName;

    private String content;

    private Map<String, Object> output;

    private Boolean fallback;
}
