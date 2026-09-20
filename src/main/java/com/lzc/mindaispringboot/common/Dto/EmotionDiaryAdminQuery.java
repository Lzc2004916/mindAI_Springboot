package com.lzc.mindaispringboot.common.Dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class EmotionDiaryAdminQuery {
    private Long userId;

    /** 情绪评分区间（前端把 moodScoreRange 拆成这两个发过来） */
    private Integer minMoodScore;
    private Integer maxMoodScore;

    /** 起始日期，格式 yyyy-MM-dd */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;
    /** 结束日期，格式 yyyy-MM-dd */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;
}