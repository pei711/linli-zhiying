package com.hmdp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hmdp.dto.AiSkillRunRequest;
import com.hmdp.dto.Result;
import com.hmdp.entity.AiSkill;

public interface IAiSkillService extends IService<AiSkill> {

    Result queryEnabledSkills();

    Result querySkillByCode(String code);

    Result runSkill(String code, AiSkillRunRequest request);

    Result queryMyRuns(String skillCode);
}
