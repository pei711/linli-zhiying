package com.hmdp.utils;

import cn.hutool.json.JSONUtil;
import com.hmdp.dto.ReservationDTO;
import com.hmdp.dto.Result;
import com.hmdp.entity.Reservation;
import com.hmdp.service.IReservationService;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationTool {

    private final IReservationService reservationService;

    @Tool("根据预约ID查询预约详情，包括店铺、预约时间、人数、状态等")
    public String queryReservationById(Long reservationId) {
        log.info("[ReservationTool] 查询预约详情, reservationId={}", reservationId);
        if (reservationId == null || reservationId <= 0) {
            return JSONUtil.toJsonStr(Result.fail("预约ID不能为空"));
        }
        Reservation reservation = reservationService.getById(reservationId);
        Long userId = UserHolder.getUser() != null ? UserHolder.getUser().getId() : null;
        if (reservation == null || !Objects.equals(reservation.getUserId(), userId)) {
            return JSONUtil.toJsonStr(Result.fail("预约不存在"));
        }
        return JSONUtil.toJsonStr(Result.ok(reservation));
    }

    @Tool("查询当前登录用户的预约列表，按创建时间倒序返回")
    public String queryMyReservations() {
        Long userId = UserHolder.getUser() != null ? UserHolder.getUser().getId() : null;
        log.info("[ReservationTool] 查询我的预约, userId={}", userId);
        if (userId == null) {
            return JSONUtil.toJsonStr(Result.fail("用户未登录，无法查询预约"));
        }
        List<Reservation> list = reservationService.lambdaQuery()
                .eq(Reservation::getUserId, userId)
                .orderByDesc(Reservation::getCreateTime)
                .list();
        return JSONUtil.toJsonStr(Result.ok(list, (long) list.size()));
    }

    @Tool("为当前登录用户创建一条店铺预约。参数：店铺ID(shopId)、预约时间(reservationTime，格式yyyy-MM-dd HH:mm)、人数(numPeople)。创建前会校验时间是否合法及是否冲突。")
    public String createReservation(Long shopId, String reservationTime, Integer numPeople) {
        Long userId = UserHolder.getUser() != null ? UserHolder.getUser().getId() : null;
        log.info("[ReservationTool] 创建预约, userId={}, shopId={}, reservationTime={}, numPeople={}",
                userId, shopId, reservationTime, numPeople);
        if (userId == null) {
            return JSONUtil.toJsonStr(Result.fail("用户未登录，无法创建预约"));
        }
        if (shopId == null || shopId <= 0) {
            return JSONUtil.toJsonStr(Result.fail("店铺ID不能为空"));
        }
        if (reservationTime == null || reservationTime.trim().isEmpty()) {
            return JSONUtil.toJsonStr(Result.fail("预约时间不能为空"));
        }
        if (numPeople == null || numPeople <= 0) {
            return JSONUtil.toJsonStr(Result.fail("预约人数必须大于0"));
        }
        LocalDateTime time;
        try {
            time = LocalDateTime.parse(reservationTime.trim(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        } catch (Exception e) {
            return JSONUtil.toJsonStr(Result.fail("预约时间格式错误，请使用 yyyy-MM-dd HH:mm"));
        }
        if (time.isBefore(LocalDateTime.now())) {
            return JSONUtil.toJsonStr(Result.fail("预约时间不能早于当前时间"));
        }
        ReservationDTO dto = new ReservationDTO();
        dto.setShopId(shopId);
        dto.setReservationTime(time);
        dto.setNumPeople(numPeople);
        return JSONUtil.toJsonStr(reservationService.createReservation(dto));
    }
}
