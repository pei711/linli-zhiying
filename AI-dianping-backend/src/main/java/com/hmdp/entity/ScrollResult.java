package com.hmdp.entity;


import lombok.Data;
import java.util.List;

@Data
public class ScrollResult {
    private List<?> list;
    private Long minTime; // 最小时间戳
    private Integer offset; // 偏移量
}
