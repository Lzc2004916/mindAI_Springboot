package com.lzc.mindaispringboot.mappper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lzc.mindaispringboot.entity.AiAnalysisTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface AiAnalysisTaskMapper extends BaseMapper<AiAnalysisTask> {
    @Select("select status,count(*) from ai_analysis_task group by status")
    List<Map<String,Object>> countGroupByStatus();

    @Select("SELECT * FROM ai_analysis_task WHERE status = 'PENDING' " +
            "ORDER BY priority DESC, id ASC LIMIT #{limit}")
    List<AiAnalysisTask> pickPendingTasks(@Param("limit") int limit);
    //待处理 > 处理中
    @Update("update ai_analysis_task set status = 'PROCESSING',started_at = NOW(),updated_at = NOW() "
    + "where id = #{id} and status = 'PENDING'"
    )
    int markProcessing(@Param("id") Long id);

    @Update("update ai_analysis_task set retry_count = retry_count + 1, " +
            "status = if(retry_count + 1 < max_retry_count,'PENDING','FAILED')," +
            "error_message = #{msg},updated_at = NOW() " +
            "where id = #{id}")
    int retryOrFail(@Param("id") Long id,@Param("msg") String msg);
    /// 回收卡死但还能重试的任务：退回 PENDING
    @Update("update ai_analysis_task set status = 'PENDING'," +
            "retry_count = retry_count + 1," +
            "updated_at = NOW(),error_message = '执行超时（可能因服务重启中断）,已自动退回重试'" +
            " where status = 'PROCESSING' " +
            "and started_at < date_sub(NOW(),interval 5 minute ) and  retry_count < max_retry_count")
    int recycleStuck();

    /// 回收卡死且重试已耗尽的任务：直接标记为 FAILED
    @Update("update ai_analysis_task set status = 'FAILED'," +
            "updated_at = NOW(),completed_at = NOW()," +
            "error_message = '执行超时且重试次数已耗尽，标记为失败'" +
            " where status = 'PROCESSING' " +
            "and started_at < date_sub(NOW(),interval 5 minute ) and retry_count >= max_retry_count")
    int markStuckAsFailed();
}