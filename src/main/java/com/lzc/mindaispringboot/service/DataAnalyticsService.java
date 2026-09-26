package com.lzc.mindaispringboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lzc.mindaispringboot.mappper.ConsultationSessionMapper;
import com.lzc.mindaispringboot.mappper.EmotionDiaryMapper;
import com.lzc.mindaispringboot.mappper.UserMapper;
import com.lzc.mindaispringboot.response.DataAnalyticsVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DataAnalyticsService {
    /** 趋势窗口天数（含今天） */
    private static final int DAYS = 7;
    /// 2026-09-22
    private static final DateTimeFormatter  FNT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    @Resource
    private UserMapper userMapper;
    @Resource
    private ConsultationSessionMapper consultationSessionMapper;
    @Resource
    private EmotionDiaryMapper emotionDiaryMapper;
    public DataAnalyticsVO overview(){
        LocalDate today = LocalDate.now();
        List<String> days = new ArrayList<>();
        for (int i = DAYS - 1; i >= 0; i--){
            days.add(today.minusDays(i).format(FNT));
        }
        Map<String, Map<String, Object>> diaryDaily = toMap(emotionDiaryMapper.selectDiaryDaily7d());
        Map<String, Map<String, Object>> sessionDaily = toMap(consultationSessionMapper.selectSessionDaily7d());
        Map<String, Map<String, Object>> newUserDaily = toMap(userMapper.selectNewUserDaily7d());
        Map<String, Map<String, Object>> activeDaily = toMap(userMapper.selectActiveUserDaily7d());

        ArrayList<DataAnalyticsVO.EmotionTrendPoint> emotionTrend = new ArrayList<>();
        ArrayList<DataAnalyticsVO.DailySessionPoint> dailyTrend = new ArrayList<>();
        ArrayList<DataAnalyticsVO.UserActivityPoint> userActivity = new ArrayList<>();
        ///封装返回结果
        for (String day : days) {
            Map<String, Object> d = diaryDaily.get(day);
            Map<String, Object> s = sessionDaily.get(day);
            Map<String, Object> n = newUserDaily.get(day);
            Map<String, Object> a = activeDaily.get(day);
            //统计一周每天的平均情感分、记录条数
            // 计数类字段统一用 count()：没有数据的那天补 0，不能是 null，否则 ECharts 折线会断点
            emotionTrend.add(DataAnalyticsVO.EmotionTrendPoint.builder()
                            .date(day)
                            .avgMoodScore(toDouble(d == null ? null : d.get("avgMoodScore")))   // 均分允许为 null
                            .recordCount(count(d == null ? null : d.get("recordCount")))
                    .build());
            //统计一周每天的对话数、参与参与会话数
            dailyTrend.add(DataAnalyticsVO.DailySessionPoint.builder()
                            .date(day)
                            .sessionCount(count(s == null ? null : s.get("sessionCount")))
                            .userCount(count(s == null ? null : s.get("userCount")))
                    .build());
            //统计一周每天的活跃用户、新增用户、写日记用户数、发起咨询用户数
            userActivity.add(DataAnalyticsVO.UserActivityPoint.builder()
                            .date(day)
                            .activeUsers(count(a == null ? null : a.get("activeUsers")))
                            .newUsers(count(n == null ? null : n.get("newUsers")))
                            .diaryUsers(count(d == null ? null : d.get("diaryUsers")))
                            .consultationUsers(count(s == null ? null : s.get("userCount")))
                    .build());
        }
        //概览卡片👇
        //当天时间
        String todayKey = today.format(FNT);
        //查全表数量总和
        Long totalSessions = consultationSessionMapper.selectCount(new LambdaQueryWrapper<>());
        DataAnalyticsVO.SystemOverview overview = DataAnalyticsVO.SystemOverview.builder()
                .totalUsers(userMapper.selectCount(new LambdaQueryWrapper<>()))
                .activeUsers(userMapper.countActiveUsers7d())
                .totalDiaries(emotionDiaryMapper.selectCount(new LambdaQueryWrapper<>()))
                .todayNewDiaries(fromMap(diaryDaily, todayKey, "recordCount"))
                .totalSessions(totalSessions)
                .todayNewSessions(fromMap(sessionDaily, todayKey, "sessionCount"))
                .avgMoodScore(emotionDiaryMapper.selectAvgMood())
                .build();
        /// 咨询统计块
        DataAnalyticsVO.ConsultationStats stats = DataAnalyticsVO.ConsultationStats.builder()
                .totalSessions(totalSessions)
                .avgDurationMinutes(0L)
                .dailyTrend(dailyTrend)
                .build();
        return DataAnalyticsVO.builder()
                .systemOverview(overview)
                .emotionTrend(emotionTrend)
                .consultationStats(stats)
                .userActivity(userActivity)
                .build();
    }
    /** 从"按日分组"的结果里取某天的某个统计值；没有该天数据时返回 0（不能返回 null，否则前端图表断点） */
    private Long fromMap(Map<String, Map<String, Object>> daily,String day,String key){
        Map<String, Object> row = daily.get(day);
        if (row == null) return 0L;
        Long value = toLong(row.get(key));
        return value == null ? 0L : value;
    }
    private Map<String,Map<String,Object>> toMap(List<Map<String,Object>> rows){
        Map<String,Map<String,Object>> map = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Object date = row.get("date");
            if (date != null) map.put(String.valueOf(date), row);
        }
        return map;
    }
    private static Long toLong(Object o){
        return o == null ? null : ((Number) o).longValue();
    }
    /** 计数类取值：null 一律当 0（图表要的是 0 而不是空点） */
    private static Long count(Object o){
        return o == null ? 0L : ((Number) o).longValue();
    }
    private static Double toDouble(Object o){
        return o == null ? null : ((Number) o).doubleValue();
    }
}
