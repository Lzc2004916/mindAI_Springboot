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
    /** 全库情绪均分 */
    @Select("SELECT IFNULL(ROUND(AVG(mood_score),1),0) FROM emotion_diary")
    Double selectAvgMood();
}
