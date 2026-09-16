package com.lzc.mindaispringboot.common.Dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class EmotionDiarySaveDTO {
    @NotNull(message = "日记日期不能为空")
    private LocalDate diaryDate;
    @NotNull(message = "情绪评分不能为空")
    @Min(value = 1, message = "情绪评分最小为1")
    @Max(value = 10, message = "情绪评分最大为10")
    private Integer moodScore;
    @Size(max = 50, message = "主要情绪最多50字符")
    private String dominantEmotion;
    @Size(max = 2000, message = "情绪触发因素最多2000字符")
    private String emotionTriggers;
    @Size(max = 10000, message = "日记内容最多10000字符")
    private String diaryContent;
    @Min(value = 1, message = "睡眠质量最小为1")
    @Max(value = 5, message = "睡眠质量最大为5")
    private Integer sleepQuality;
    @Min(value = 1, message = "压力水平最小为1")
    @Max(value = 5, message = "压力水平最大为5")
    private Integer stressLevel;
}