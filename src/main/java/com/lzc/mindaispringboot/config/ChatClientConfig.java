package com.lzc.mindaispringboot.config;

import com.lzc.mindaispringboot.AiService.PromptManage;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {
    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
                //保留最新30条消息
                .maxMessages(30)
                .build();
    }
    @Bean("open-ai")
    public ChatClient openAiChatClient(OpenAiChatModel openAiChatModel) {
        return ChatClient.builder(openAiChatModel)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory()).build())
                .defaultSystem(PromptManage.PSYCHOLOGICAL_SUPPORT_SYSTEM_PROMPT)
                .build();
    }

    /**
     * 一次性结构化分析专用（情绪日记分析、会话情绪分析）：不带 ChatMemory 顾问。
     * 带记忆的顾问强制要求每次调用都传 chat_memory_conversation_id，
     * 而这类调用是「喂一段文本 → 出 JSON」的一次性请求，没有多轮上下文，
     * 不该往会话记忆里塞日记正文，也不该因为缺这个参数直接抛异常。
     */
    @Bean("analysis")
    public ChatClient analysisChatClient(OpenAiChatModel openAiChatModel) {
        return ChatClient.builder(openAiChatModel).build();
    }
}
