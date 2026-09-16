package com.lzc.mindaispringboot.common.Dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class EmotionDiaryAdminPageQuery {
    private Long pageNum = 1L;
    private Long pageSize = 10L;
    private Long userId;
    /** 起始日期，格式 yyyy-MM-dd */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;
    /** 结束日期，格式 yyyy-MM-dd */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;
}