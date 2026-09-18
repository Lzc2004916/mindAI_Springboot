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
            String overallMood,        // 总体情绪：平静/焦虑/低落/积极/愤怒/混乱
            List<String> emotions,     // 出现过的情绪（2-5 个）
            Double positiveRatio,      // 积极情绪占比 0~1
            String summary,            // 100 字内总结
            List<String> suggestions   // 2-4 条温和建议
    ){}
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
