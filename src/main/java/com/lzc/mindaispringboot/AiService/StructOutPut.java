package com.lzc.mindaispringboot.AiService;

import java.util.List;

public class StructOutPut {
    public record  StreamChatSession(
            String sessionId,
            Long userHash,
            String initialMessage,
            Long startTime,
            Long expiryTime,
            Integer messageCount,
            String status
    ){
    }
    //会话情绪分析结构结果
    public record EmotionAnalysis(
            String primaryEmotion,             // 主要情绪：快乐/平静/兴奋/满足/愤怒/悲伤/焦虑/恐惧/沮丧/压力
            Integer emotionScore,              // 情绪强度 0~100（整数）
            Boolean isNegative,                // 是否负面情绪
            Integer riskLevel,                 // 风险等级：0正常 1关注 2预警 3危机
            List<String> keywords,             // 关键词 3~5 个
            String suggestion,                 // 专业建议（一句话）
            String icon,                       // 情绪 emoji
            String label,                      // 英文标识，如 disappointed
            String riskDescription,            // 风险描述（一句话）
            List<String> improvementSuggestions, // 改善建议 2~4 条
            Long timestamp                     // 分析时间，毫秒时间戳（由后端填，不是 AI 生成）
    ){}
    //日志
    public record DiaryEmotionAnalysis(
            String primaryEmotion,              // 主要情绪：快乐/平静/兴奋/满足/愤怒/悲伤/焦虑/恐惧/沮丧/压力
            Integer emotionScore,               // 情绪强度 0~100（越高越负面/越强烈）
            Integer riskLevel,                  // 风险等级：0正常 1关注 2预警 3危机
            Boolean isNegative,                 // 是否负面情绪
            String suggestion,                  // 专业建议（一句话）
            String riskDescription,             // 风险描述（一句话）
            List<String> improvementSuggestions // 改善建议 2~4 条
    ) {}
}
