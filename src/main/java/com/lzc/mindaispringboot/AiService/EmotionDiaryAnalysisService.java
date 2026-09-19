package com.lzc.mindaispringboot.AiService;

import cn.hutool.core.text.StrBuilder;
import tools.jackson.databind.ObjectMapper;
import com.lzc.mindaispringboot.entity.EmotionDiary;
import com.lzc.mindaispringboot.exception.BusionessException;
import com.lzc.mindaispringboot.mappper.EmotionDiaryMapper;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.ai.chat.client.ChatClient;

import java.time.LocalDateTime;
import java.util.List;

/// 情绪日记的 AI 分析：分析一条日记，把结构化结果写回 emotion_diary.ai_emotion_analysis。
@Service
public class EmotionDiaryAnalysisService {
    /// 发给AI的文本上限
    private static final int MAX_TEXT_LENGTH = 8000;
    private final ChatClient chatClient;
    public EmotionDiaryAnalysisService(@Qualifier("analysis") ChatClient chatClient) {
        this.chatClient = chatClient;
    }
    @Resource
    private EmotionDiaryMapper emotionDiaryMapper;
    @Resource
    private ObjectMapper objectMapper;
    public StructOutPut.DiaryEmotionAnalysis analyzeEmotionDiary(Long diaryId, boolean force) {
        EmotionDiary diary  = emotionDiaryMapper.selectById(diaryId);
        if (diary == null) throw new BusionessException("情绪日记不存在");
        if (!force
                && diary.getAiAnalysisUpdatedAt() != null
                && diary.getAiAnalysisUpdatedAt().isAfter(LocalDateTime.now().minusHours(24))){
            StructOutPut.DiaryEmotionAnalysis cached = readCache(diary.getAiEmotionAnalysis());
            if (cached != null) return cached;
        }
        String buildText = buildText(diary);
        if (buildText.isBlank()) throw new BusionessException("该日记暂无可分析的内容");
        StructOutPut.DiaryEmotionAnalysis result = chatClient.prompt()
                .system(PromptManage.DIARY_ANALYSIS_PROMPT)
                .user(buildText)
                .call()
                .entity(StructOutPut.DiaryEmotionAnalysis.class);
        //兜底AI 偶尔漏字段或缺省，保证前端不会渲染出 undefined
        result = normalize(result);
        diary.setAiEmotionAnalysis(objectMapper.writeValueAsString(result));
        diary.setAiAnalysisUpdatedAt(LocalDateTime.now());
        emotionDiaryMapper.updateById(diary);
        return result;
    }
    ///AI 漏字段时补默认值，避免前端显示 undefined 或进度条异常
    private StructOutPut.DiaryEmotionAnalysis normalize(StructOutPut.DiaryEmotionAnalysis r){
        if (r == null){
            return new StructOutPut.DiaryEmotionAnalysis("平静", 30, 0, false, "保持当下的节奏就好", "情绪稳定", List.of());
        }
        Integer score = r.emotionScore() == null ? 50 : Math.max(0, Math.min(100, r.emotionScore()));
        Integer level = r.riskLevel() == null ? 0 : Math.max(0, Math.min(3, r.riskLevel()));
        // 保证 riskLevel 与 isNegative 不矛盾
        boolean negative = r.isNegative() != null ? r.isNegative() : level >= 2;
        if (level >= 2) negative = true;
        if (level == 0) negative = false;
        return new StructOutPut.DiaryEmotionAnalysis(
                r.primaryEmotion(),
                score,
                level,
                negative,
                r.suggestion(),
                r.riskDescription(),
                r.improvementSuggestions() == null ? List.of() : r.improvementSuggestions()
        );
    }
    ///组装送给 AI 的文本：只取日记自身的内容，不去读会话消息
    private String buildText(EmotionDiary diary){
        if (!hasText(diary.getDiaryContent()) && !hasText(diary.getEmotionTriggers()))return "";
        StrBuilder strBuilder = new StrBuilder();
        strBuilder.append("【日记日期】").append(diary.getDiaryDate()).append("\n");
        if (hasText(diary.getDominantEmotion()))
            strBuilder.append("【自评情绪】").append(diary.getDominantEmotion()).append("\n");
        if (diary.getMoodScore() != null)
            strBuilder.append("【情绪评分(1-10，越高越好)】").append(diary.getMoodScore()).append("\n");
        if (diary.getSleepQuality() != null)
            strBuilder.append("【睡眠质量(1-5)】").append(diary.getSleepQuality()).append("\n");
        if (diary.getStressLevel() != null)
            strBuilder.append("【压力水平(1-5)】").append(diary.getStressLevel()).append("\n");
        if (hasText(diary.getEmotionTriggers()))
            strBuilder.append("【情绪触发因素】").append(diary.getEmotionTriggers()).append("\n");
        if (hasText(diary.getDiaryContent()))
            strBuilder.append("【日记内容】").append(diary.getDiaryContent()).append("\n");
        return strBuilder.length() > MAX_TEXT_LENGTH
                ? strBuilder.subString(strBuilder.length() - MAX_TEXT_LENGTH)
                : strBuilder.toString();
    }
    private boolean hasText(String s){
        return s != null && !s.isBlank();
    }

    private StructOutPut.DiaryEmotionAnalysis readCache(String json){
        if (!hasText(json)) return null;
        try {
            StructOutPut.DiaryEmotionAnalysis cached =
                    objectMapper.readValue(json,StructOutPut.DiaryEmotionAnalysis.class);
            /// primaryEmotion 为空 = 旧格式脏数据（Hutool 只会写出 {"isNegative":false}）
            return hasText(cached.primaryEmotion()) ? cached : null;
        }catch (Exception e){
            return null;
        }
    }
}