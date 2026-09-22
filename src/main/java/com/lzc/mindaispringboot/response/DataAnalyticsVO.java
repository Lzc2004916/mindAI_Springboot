package com.lzc.mindaispringboot.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
public class DataAnalyticsVO {
    /** 概览卡片（Dashboard 顶部 4 张卡） */
    private SystemOverview systemOverview;

    /** 情绪趋势（折线图，近 7 日） */
    private List<EmotionTrendPoint> emotionTrend;

    /** 咨询会话统计（柱状图 + 2 个数字） */
    private ConsultationStats consultationStats;

    /** 用户活跃度趋势（折线图，近 7 日） */
    private List<UserActivityPoint> userActivity;
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SystemOverview {
        /** 总用户数 */
        private Long totalUsers;
        /** 活跃用户数（近 7 日内写过日记 或 发起过咨询的去重用户） */
        private Long activeUsers;
        /** 日记总数 */
        private Long totalDiaries;
        /** 今日新增日记 */
        private Long todayNewDiaries;
        /** 会话总数 */
        private Long totalSessions;
        /** 今日新增会话 */
        private Long todayNewSessions;
        /** 平均情绪分（1-10，一位小数） */
        private Double avgMoodScore;
    }
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmotionTrendPoint {
        /** yyyy-MM-dd */
        private String date;
        /** 当日平均情绪分（当天无记录时 null） */
        private Double avgMoodScore;
        /** 当日记录条数（当天无记录时 0） */
        private Long recordCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConsultationStats {
        /** 总会话数 */
        private Long totalSessions;
        /** 平均时长（分钟） */
        private Long avgDurationMinutes;
        /** 近 7 日咨询活动 */
        private List<DailySessionPoint> dailyTrend;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailySessionPoint {
        /** yyyy-MM-dd */
        private String date;
        /** 当日会话数 */
        private Long sessionCount;
        /** 当日参与用户数（去重） */
        private Long userCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserActivityPoint {
        /** yyyy-MM-dd */
        private String date;
        /** 当日活跃用户（写日记 ∪ 发起咨询，去重） */
        private Long activeUsers;
        /** 当日新增用户 */
        private Long newUsers;
        /** 当日写日记的用户数（去重） */
        private Long diaryUsers;
        /** 当日发起咨询的用户数（去重） */
        private Long consultationUsers;
    }
}
