package com.hmdp.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReservationDTO {

    private Long shopId;

    private LocalDateTime reservationTime;

    private Integer numPeople;

    private String remark;
}
