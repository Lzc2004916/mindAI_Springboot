package com.lzc.mindaispringboot.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.lzc.mindaispringboot.entity.EmotionDiary;
import com.lzc.mindaispringboot.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmotionDiaryAdminVO {
    /** 正文预览最大字数，超过就截断加省略号 */
    private static final int PREVIEW_MAX = 50;

    /** 日记ID */
    private Long id;
    /** 用户ID */
    private Long userId;
    /** 用户名 */
    private String username;
    /** 用户昵称 */
    private String nickname;
    /** 日记日期 */
    private LocalDate diaryDate;
    /** 情绪评分 1-10 */
    private Integer moodScore;
    /** 主要情绪 */
    private String dominantEmotion;
    /** 情绪触发因素 */
    private String emotionTriggers;
    /** 日记正文 */
    private String diaryContent;
    /** 列表展示用的截断正文（超出 PREVIEW_MAX 字截断加省略号） */
    private String diaryContentPreview;
    /** 睡眠质量 1-5 */
    private Integer sleepQuality;
    /** 压力水平 1-5 */
    private Integer stressLevel;
    /** AI 情绪分析结果 JSON 字符串（原样透传，前端自行 JSON.parse） */
    private String aiEmotionAnalysis;
    /** AI 分析更新时间 */
    private LocalDateTime aiAnalysisUpdatedAt;
    /** 创建时间 */
    private LocalDateTime createdAt;
    /** 更新时间 */
    private LocalDateTime updatedAt;
    /** 是否有 AI 分析结果 */
    private Boolean hasAiEmotionAnalysis;
    /// /待处理/分析中/已完成/失败
    /// AI 分析状态：PENDING / ANALYZING / COMPLETED / FAILED *
    private String aiAnalysisStatus;
    /** 正文长度（字符数） */
    private Integer contentLength;
    public static EmotionDiaryAdminVO from(EmotionDiary d, User user){
        String content = d.getDiaryContent();
        int length = content == null ? 0 : content.length();
        boolean hasAi = StringUtils.hasText(d.getAiEmotionAnalysis());
        return EmotionDiaryAdminVO.builder()
                .id(d.getId())
                .userId(d.getUserId())
                .username(user == null ? null : user.getUsername())
                .nickname(user == null ? null : user.getNickname())
                .diaryDate(d.getDiaryDate())
                .moodScore(d.getMoodScore())
                .dominantEmotion(d.getDominantEmotion())
                .emotionTriggers(d.getEmotionTriggers())
                .diaryContent(content)
                .diaryContentPreview(preview(content))
                .sleepQuality(d.getSleepQuality())
                .stressLevel(d.getStressLevel())
                .aiEmotionAnalysis(d.getAiEmotionAnalysis())
                .aiAnalysisUpdatedAt(d.getAiAnalysisUpdatedAt())
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .hasAiEmotionAnalysis(hasAi)
                .aiAnalysisStatus(hasAi ? "COMPLETED" : "PENDING")
                .contentLength(length)
                .build();
    }
    private static String preview(String content){
        if (content == null) return "";
        if (content.length() <= PREVIEW_MAX) return content;
        return content.substring(0, PREVIEW_MAX) + "-";
    }
}