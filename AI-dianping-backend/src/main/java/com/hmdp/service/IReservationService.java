package com.hmdp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hmdp.dto.ReservationDTO;
import com.hmdp.dto.Result;
import com.hmdp.entity.Reservation;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author ShenCodr
 * @since 2025-10-07
 */
public interface IReservationService extends IService<Reservation> {

    /**
     * 创建预约
     *
     * @param dto 预约信息
     * @return 预约结果
     */
    Result createReservation(ReservationDTO dto);
}
