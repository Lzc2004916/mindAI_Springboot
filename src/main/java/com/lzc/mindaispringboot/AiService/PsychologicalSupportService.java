package com.lzc.mindaispringboot.AiService;

import cn.hutool.core.text.StrBuilder;
import cn.hutool.json.JSONUtil;
import com.lzc.mindaispringboot.common.Dto.ConsultationSessionCreateDto;
import com.lzc.mindaispringboot.entity.ConsultationSession;
import com.lzc.mindaispringboot.exception.BusionessException;
import com.lzc.mindaispringboot.response.ConsultationMessageResponseDTO;
import com.lzc.mindaispringboot.service.ConsultationMessageService;
import com.lzc.mindaispringboot.service.ConsultationSessionService;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PsychologicalSupportService {
    @Autowired
    @Qualifier("open-ai")
    private ChatClient chatClient;
    /// 一次性分析专用：不带 ChatMemory 顾问，不需要 conversationId
    @Autowired
    @Qualifier("analysis")
    private ChatClient analysisChatClient;
    @Resource
    private ConsultationSessionService consultationSessionService;
    @Resource
    private ConsultationMessageService consultationMessageService;

    public StructOutPut.StreamChatSession startSession(Long userId, ConsultationSessionCreateDto consultationSessionCreateDto){
        //创建数据库会话记录
        ConsultationSession session = consultationSessionService.createSession(userId, consultationSessionCreateDto);
        // 将初始用户消息保存到message表里
        consultationMessageService.saveUserMessage(session.getId(), consultationSessionCreateDto.getInitialMessage(), null);
        //创建会话信息
        String sessionId = "session_" + session.getId();
        return new StructOutPut.StreamChatSession(
                sessionId,
                userId,
                consultationSessionCreateDto.getInitialMessage(),
                System.currentTimeMillis(),
                System.currentTimeMillis() + 24 * 60 * 60 * 1000, //24小时
                1,
                "ACTIVE"
        );
    }
    /**
     * 心理支持流式对话
     */
    public Flux<String> streamPsychologicalChat(String sessionId, String userMessage) {
        return Flux.create(sink -> {
            // 1. 解析会话 ID，获取数据库中的会话主键
            Long dbSession = extractSessionId(sessionId);
            if (dbSession == null) {
                sink.error(new RuntimeException("会话ID格式错误"));
                return;
            }

            // 2. 判断是否为会话的第一条消息（防止 startSession 时的初始消息被重复保存）
            boolean isInitMessage = false;
            Integer messageCount = consultationMessageService.getMessageCount(dbSession);
            if (messageCount == 1) {
                ConsultationMessageResponseDTO lastMessage =
                        consultationMessageService.getLastMessageBySessionId(dbSession);
                if (lastMessage != null
                        && lastMessage.getSenderType().equals(1)
                        && userMessage.equals(lastMessage.getContent())) {
                    isInitMessage = true;
                }
            }

            // 3. 非初始消息才写入数据库（初始消息已在 startSession 中保存）
            if (!isInitMessage) {
                consultationMessageService.saveUserMessage(dbSession, userMessage, null);
            }

            // 4. 生成对话记忆的 conversationId，用于 ChatMemory 隔离不同会话的历史
            String conversationId = "conversation_" + sessionId;

            // 5. 拼接 AI 流式回复的完整内容
            StrBuilder fullResponse = new StrBuilder();

            // 6. 调用 AI 进行流式对话
            //    - MessageChatMemoryAdvisor 会自动携带历史消息并保存本轮对话到 ChatMemory
            //    - chat_memory_conversation_id 参数指定当前对话的上下文
            chatClient.prompt()
                    .user(userMessage)
                    .advisors(advisorSpec ->
                            advisorSpec.param("chat_memory_conversation_id", conversationId))
                    .stream()
                    .content()
                    .doOnNext(fragment -> {
                        // 每收到一个文本片段，追加到完整回复并推送给前端
                        fullResponse.append(fragment);
                        sink.next(fragment);
                    })
                    .doOnComplete(() -> {
                        // 流式回复完成后，将完整回复存入数据库
                        String completeRes = fullResponse.toString();
                        consultationMessageService.saveAiMessage(dbSession, completeRes, "openai");
                        sink.complete();
                    })
                    .doOnError(sink::error)
                    .subscribe();
        });
    }
    public Long extractSessionId(String sessionId){
        if (sessionId == null || !sessionId.startsWith("session_")) return null;
        return Long.parseLong(sessionId.replace("session_",""));
    }
    public StructOutPut.EmotionAnalysis getEmotionAnalysis(String sessionId,Long userId,boolean isAdmin){
        Long dbsession = extractSessionId(sessionId);
        if (dbsession == null) throw new BusionessException("会话Id格式错误");
        ConsultationSession session = consultationSessionService.getById(dbsession);
        if (session == null) throw new BusionessException("会话不存在");
        if (!isAdmin && !session.getUserId().equals(userId)){
            throw new BusionessException("无权访问该会话");
        }
        if (session.getLastEmotionAnalysis() != null
                && session.getLastEmotionUpdatedAt() != null
                && session.getLastEmotionUpdatedAt().isAfter(LocalDateTime.now().minusHours(24))
        ){
            return JSONUtil.toBean(session.getLastEmotionAnalysis(), StructOutPut.EmotionAnalysis.class);
        }
        StructOutPut.EmotionAnalysis analysis = analyzeSessionEmotion(dbsession);
        session.setLastEmotionAnalysis(JSONUtil.toJsonStr(analysis));
        session.setLastEmotionUpdatedAt(LocalDateTime.now());
        consultationSessionService.updateById(session);
        return analysis;
    }
    private StructOutPut.EmotionAnalysis analyzeSessionEmotion(Long dbSession){
        List<ConsultationMessageResponseDTO> message = consultationMessageService.listBySession(dbSession);
        if (message.isEmpty()) throw new BusionessException("该会话暂无消息，无法进行情绪分析");
        StrBuilder dialogue = new StrBuilder();
        for (ConsultationMessageResponseDTO m : message) {
            dialogue.append(m.getSenderType() == 1 ? "用户：" : "AI：").append(m.getContent()).append("\n");
        }
        String text = dialogue.length() > 8000
                ? dialogue.subString(dialogue.length() - 8000)
                : dialogue.toString();
        return analysisChatClient.prompt()
                .system(PromptManage.EMOTION_ANALYSIS_PROMPT)
                .user(text)
                .call()// ④ 发送请求，等待 AI 返回（阻塞式，非流式）
                .entity(StructOutPut.EmotionAnalysis.class);
    }
}