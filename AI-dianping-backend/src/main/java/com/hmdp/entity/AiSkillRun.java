package com.hmdp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_ai_skill_run")
public class AiSkillRun implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String skillCode;

    private Long userId;

    private Long merchantId;

    private String inputJson;

    private String outputJson;

    /**
     * SUCCESS / FAILED
     */
    private String status;

    private String errorMsg;

    private LocalDateTime createTime;
}
