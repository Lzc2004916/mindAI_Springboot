package com.lzc.mindaispringboot.AiService;

import com.lzc.mindaispringboot.entity.AiAnalysisTask;
import com.lzc.mindaispringboot.entity.EmotionDiary;
import com.lzc.mindaispringboot.mappper.EmotionDiaryMapper;
import com.lzc.mindaispringboot.service.AnalysisTaskService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
/// AI 分析任务的后台消费者：每 30 秒捞一批 PENDING 任务执行
public class AnalysisTaskScheduler {
    private static final int BATCH_SIZE = 20;
    @Resource
    private AnalysisTaskService analysisTaskService;
    @Resource
    private EmotionDiaryMapper emotionDiaryMapper;
    @Resource
    private EmotionDiaryAnalysisService emotionDiaryAnalysisService;
    /// 初始化10秒后才开始调度，等bean加载完
    @Scheduled(fixedDelay = 30_000,initialDelay = 10_100)
    public void scan(){
        int recycled = analysisTaskService.recycleStuck();
        if (recycled > 0) log.info("回收卡死的 PROCESSING 任务 {} 条",recycled);
        int dead = analysisTaskService.markStuckAsFailed();
        if (dead > 0) log.warn("僵死且重试耗尽的 PROCESSING 任务 {} 条已标记 FAILED", dead);
        //如果一个集合中没有数据，会自然跳过循环
        for (AiAnalysisTask task : analysisTaskService.pickPending(BATCH_SIZE)) {
            // 如果一条数据中没有待处理状态就跳过
            if (!analysisTaskService.markProcessing(task.getId())) continue;
            try {
                //ai分析任务表中如果有待处理或处理中，利用日记id从日记表中取出来
                EmotionDiary diary = emotionDiaryMapper.selectById(task.getDiaryId());
                if (diary == null){
                    analysisTaskService.markCompleted(task.getId());
                    continue;
                }
                emotionDiaryAnalysisService.analyzeEmotionDiary(diary.getId(),true);
                analysisTaskService.markCompleted(task.getId());
            }catch (Exception e){
                // 不往外抛：一条失败不能连累这一批剩下的任务
                log.warn("AI 分析任务失败，taskId={}, diaryId={}", task.getId(), task.getDiaryId(), e);
                analysisTaskService.markFailed(task.getId(), e.getMessage());
            }
        }
    }
}
