package com.lzc.mindaispringboot.AiService;

import com.lzc.mindaispringboot.common.Dto.ConsultationSessionCreateDto;
import com.lzc.mindaispringboot.entity.ConsultationMessage;
import com.lzc.mindaispringboot.entity.ConsultationSession;
import com.lzc.mindaispringboot.mappper.ConsultionMessageMapper;
import com.lzc.mindaispringboot.response.ConsultationMessageResponseDTO;
import com.lzc.mindaispringboot.service.ConsultationMessageService;
import com.lzc.mindaispringboot.service.ConsultationSessionService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class PsychologicalSupportService {
    @Resource
    private ConsultationSessionService consultationSessionService;
    @Resource
    private ConsultationMessageService consultationMessageService;

    public StructOutPut.StreamChatSession startSession(Long userId, ConsultationSessionCreateDto consultationSessionCreateDto){
        //创建数据库会话记录
        ConsultationSession session = consultationSessionService.createSession(userId, consultationSessionCreateDto);
        // 将初始用户消息保存到message表里
        ConsultationMessage message = consultationMessageService.saveUserMessage(session.getId(), consultationSessionCreateDto.getInitialMessage(), null);
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
    public Flux<String> streamPsychologicalChat(String sessionId , String userMessage){
//        创建响应流
        return Flux.create(sink -> {
            // sink.next("数据1") //发布数据
            // sink.complete() //完成流
            // sink.error() //发布错误
            Long dbSession = extractSessionId(sessionId);
            if(dbSession == null){
                sink.error(new RuntimeException("会话ID格式错误"));
            }
            //判断是否是初始会话用户
            boolean isInitMessage = false;
            Integer messageCount = consultationMessageService.getMessageCount(dbSession);
            if (messageCount == 1){
                //获取用户最新的消息数据
                ConsultationMessageResponseDTO lastMessage = consultationMessageService.getLastMessageBySessionId(dbSession);
                if (lastMessage != null && lastMessage.getSenderType().equals(1) && userMessage.equals(lastMessage.getContent())){
                    isInitMessage = true;
                }
            }
            if (!isInitMessage){
                //保存用户消息到数据库
                consultationMessageService.saveUserMessage(dbSession,userMessage,null);
            }
        });
    }
    public Long extractSessionId(String sessionId){
        if (sessionId == null || !sessionId.startsWith("session_")) return null;
        return Long.parseLong(sessionId.replace("session_",""));
    }
}