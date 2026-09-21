package com.lzc.mindaispringboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lzc.mindaispringboot.entity.AiAnalysisTask;
import com.lzc.mindaispringboot.enumClass.AiReturnResult;
import com.lzc.mindaispringboot.mappper.AiAnalysisTaskMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AnalysisTaskService {
    @Resource
    private AiAnalysisTaskMapper aiAnalysisTaskMapper;
    public AiAnalysisTask createPending(Long diaryId,Long userId, String taskType){
        AiAnalysisTask task = AiAnalysisTask.builder()
                .diaryId(diaryId)
                .userId(userId)
                .status(AiReturnResult.PENDING.getCode())
                .taskType(taskType)
                .priority(2)
                .retryCount(0)
                .maxRetryCount(3)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        aiAnalysisTaskMapper.insert(task);
        return task;
    }
    public boolean markProcessing(Long taskId){
        return aiAnalysisTaskMapper.markProcessing(taskId) == 1;
    }
    /// 成功
    public void markCompleted(Long taskId){
        AiAnalysisTask task = new AiAnalysisTask();
        task.setId(taskId);
        task.setStatus(AiReturnResult.COMPLETED.getCode());
        task.setCompletedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        aiAnalysisTaskMapper.updateById(task);
    }
    ///  失败：未超上限退回 PENDING（待处理），超了标记 FAILED（失败）
    public void markFailed(Long taskId,String errorMessage){
        String msg = errorMessage == null ? "未知错误"
                : errorMessage.substring(0,Math.min(errorMessage.length(),1000));
        aiAnalysisTaskMapper.retryOrFail(taskId,msg);
    }
    /// 定时扫描
    public List<AiAnalysisTask> pickPending(int limit){
        return aiAnalysisTaskMapper.pickPendingTasks(limit);
    }
    /// 防止用户重复放入分析队列，一篇日记最多一个待处理任务
    public AiAnalysisTask enqueue(Long diaryId,Long userId){
        LambdaQueryWrapper<AiAnalysisTask> last = new LambdaQueryWrapper<AiAnalysisTask>()
                .eq(AiAnalysisTask::getDiaryId, diaryId)
                .eq(AiAnalysisTask::getStatus, AiReturnResult.PENDING.getCode())
                .orderByDesc(AiAnalysisTask::getId)
                .last("limit 1");
        AiAnalysisTask pending = aiAnalysisTaskMapper.selectOne(last);
        if (pending != null) return pending;
        return createPending(diaryId,userId,AiReturnResult.TYPE_AUTO.getCode());
    }
    public int recycleStuck(){
        return aiAnalysisTaskMapper.recycleStuck();
    }
    public int markStuckAsFailed(){
        return aiAnalysisTaskMapper.markStuckAsFailed();
    }
}
