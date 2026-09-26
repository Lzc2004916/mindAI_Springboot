package com.lzc.mindaispringboot.mappper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lzc.mindaispringboot.entity.EmotionDiary;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface EmotionDiaryMapper extends BaseMapper<EmotionDiary> {
    /** 近 7 天每日：情绪均分、日记条数、写日记的用户数（趋势图用） */
    @Select("select date_format(diary_date,'%Y-%m-%d') as `date` ," +
            " round(avg(mood_score),1) as avgMoodScore," +
            "count(*) as recordCount," +
            "count(distinct user_id) as diaryUsers " +
            "from emotion_diary where " +
            "diary_date >= date_sub(curdate(),interval 6 day ) " +
            "group by date_format(diary_date,'%Y-%m-%d') " +
            "order by date_format(diary_date,'%Y-%m-%d') asc ;")
    List<Map<String,Object>> selectDiaryDaily7d();

    /** 全表情绪均分（保留 1 位小数，前端显示成 6.4 而不是 6） */
    @Select("select round(avg(mood_score), 1) from emotion_diary")
    Double selectAvgMood();
}
