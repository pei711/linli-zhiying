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
@TableName("tb_ai_skill")
public class AiSkill implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String code;

    private String name;

    /**
     * consumer / merchant / admin
     */
    private String scene;

    private String description;

    /**
     * JSON string describing required input fields.
     */
    private String inputSchema;

    /**
     * Fixed prompt template. Skills execute once with user-provided fields.
     */
    private String promptTemplate;

    /**
     * JSON string describing the expected output shape.
     */
    private String outputSchema;

    private Boolean enabled;

    private Integer sort;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
