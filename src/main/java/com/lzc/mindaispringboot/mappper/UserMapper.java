package com.lzc.mindaispringboot.mappper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lzc.mindaispringboot.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    /** 近 7 日每日新增用户 */
    @Select("select date_format(created_at,'%Y-%m-%d') as date ,count(*) as newUsers  from user " +
            "where created_at >= date_sub(curdate(),interval 6 day ) " +
            "GROUP BY DATE_FORMAT(created_at, '%Y-%m-%d') " +
            " ORDER BY DATE_FORMAT(created_at, '%Y-%m-%d') ASC ")
    List<Map<String, Object>> selectNewUserDaily7d();
    /** 近 7 日活跃用户：按天「写日记 ∪ 发起咨询」去重 */
    @Select("SELECT `date`, COUNT(DISTINCT uid) AS activeUsers FROM ( " +
            "  SELECT DATE_FORMAT(diary_date, '%Y-%m-%d') AS `date`, user_id AS uid " +
            "    FROM emotion_diary WHERE diary_date >= DATE_SUB(CURDATE(), INTERVAL 6 DAY) " +
            "  UNION ALL " +
            "  SELECT DATE_FORMAT(started_at, '%Y-%m-%d') AS `date`, user_id AS uid " +
            "    FROM consultation_session WHERE started_at >= DATE_SUB(CURDATE(), INTERVAL 6 DAY) " +
            ") t GROUP BY `date` ORDER BY `date` ASC")
    List<Map<String, Object>> selectActiveUserDaily7d();
    /** 近 7 日活跃用户总数（整段去重，给概览卡用） */
    @Select("SELECT COUNT(DISTINCT uid) FROM ( " +
            "  SELECT user_id AS uid FROM emotion_diary " +
            "   WHERE diary_date >= DATE_SUB(CURDATE(), INTERVAL 6 DAY) " +
            "  UNION ALL " +
            "  SELECT user_id AS uid FROM consultation_session " +
            "   WHERE started_at >= DATE_SUB(CURDATE(), INTERVAL 6 DAY) " +
            ") t")
    Long countActiveUsers7d();
}
