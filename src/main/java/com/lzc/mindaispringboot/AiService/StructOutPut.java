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
}
