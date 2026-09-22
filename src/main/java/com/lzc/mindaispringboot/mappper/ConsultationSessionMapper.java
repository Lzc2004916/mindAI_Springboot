package com.lzc.mindaispringboot.mappper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lzc.mindaispringboot.entity.ConsultationSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface ConsultationSessionMapper extends BaseMapper<ConsultationSession> {
    @Select("select DATE_FORMAT(started_at, '%Y-%m-%d') as `date`, " +
            "count(*) as sessionCount, " +
            "count(distinct user_id) as userCount " +
            "from consultation_session " +
            "where started_at >= DATE_SUB(CURDATE(), INTERVAL 6 DAY) " +
            "group by date_format(started_at,'%Y-%m-%d') " +
            "order by date_format(started_at,'%Y-%m-%d') asc")
    List<Map<String,Object>> selectSessionDaily7d();
}