package com.lzc.mindaispringboot.service;

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
            emotionTrend.add(DataAnalyticsVO.EmotionTrendPoint.builder()
                            .date(day)
                            .avgMoodScore(toDouble(d == null ? null : d.get("avgMoodScore")))
                            .recordCount(toLong(d == null ? null : d.get("recordCount")))
                    .build());
            dailyTrend.add(DataAnalyticsVO.DailySessionPoint.builder()
                            .date(day)
                            .sessionCount(toLong(s == null ? null : s.get("sessionCount")))
                            .userCount(toLong(s == null ? null : s.get("userCount")))
                    .build());
            userActivity.add(DataAnalyticsVO.UserActivityPoint.builder()
                            .date(day)
                            .activeUsers(toLong(a == null ? null : a.get("activeUsers")))
                            .newUsers(toLong(n == null ? null : n.get("newUsers")))
                            .diaryUsers(toLong(d == null ? null : d.get("diaryUsers")))
                            .consultationUsers(toLong(s == null ? null : s.get("userCount")))
                    .build());
        }
        return null;
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
    private static Double toDouble(Object o){
        return o == null ? null : ((Number) o).doubleValue();
    }
}
