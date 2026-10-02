package com.hmdp.dto;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class AiSkillRunRequest {

    /** MCP 等无状态调用方可显式传入用户ID；普通 HTTP 请求优先使用登录态。 */
    private Long userId;

    private Long merchantId;

    private Map<String, Object> input = new HashMap<>();
}
