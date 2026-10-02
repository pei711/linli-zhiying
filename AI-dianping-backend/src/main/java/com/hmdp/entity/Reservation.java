package com.hmdp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_reservation")
public class Reservation {
    /**
     * 主键
     */
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /** 用户手机号，便于预约通知和数据追踪。 */
    private String userPhone;

    /**
     * 店铺id
     */
    private Long shopId;

    /**
     * 预约时间
     */
    private LocalDateTime reservationTime;

    /**
     * 预约状态 0：待确认 1：已确认
     */
    private Integer status;

    /** 预约人数。 */
    private Integer numPeople;

    /** 用户给商家的备注。 */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
