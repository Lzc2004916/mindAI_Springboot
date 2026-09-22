package com.lzc.mindaispringboot.mappper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lzc.mindaispringboot.entity.EmotionDiary;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface EmotionDiaryMapper extends BaseMapper<EmotionDiary> {
    /** 近 N 天每日情绪均分与日记数（趋势图用） */
    @Select("SELECT diary_date AS date, ROUND(AVG(mood_score),1) AS avgMood, COUNT(*) AS diaryCount " +
            "FROM emotion_diary WHERE diary_date >= DATE_SUB(CURDATE(), INTERVAL #{days} DAY) " +
            "GROUP BY diary_date ORDER BY diary_date")
    List<Map<String, Object>> selectMoodTrend(@Param("days") int days);
    @Select("select date_format(diary_date,'%Y-%m-%d') as `date` ," +
            " round(avg(mood_score),1) as avgMoodScore," +
            "count(*) as recordCount," +
            "count(distinct user_id) as diaryUsers " +
            "from emotion_diary where " +
            "diary_date >= date_sub(curdate(),interval 6 day ) " +
            "group by date_format(diary_date,'%Y-%m-%d') " +
            "order by date_format(diary_date,'%Y-%m-%d') asc ;")
    List<Map<String,Object>> selectDiaryDaily7d();
    @Select("select round(avg(mood_score)) from emotion_diary")
    Double selectAvgMood();
}
