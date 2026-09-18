package com.lzc.mindaispringboot.AiService;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class EmotionDiaryAnalysisService {
    /** 日记内容为空时，回退取该用户最近几个会话的消息作为分析素材 */
    private static final int FALLBACK_SESSIONS = 3;
    /** 送给 AI 的文本上限 */
    private static final int MAX_TEXT_LENGTH = 8000;
}
