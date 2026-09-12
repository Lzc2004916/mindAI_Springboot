package com.lzc.mindaispringboot.AiService;

import com.lzc.mindaispringboot.common.Dto.ConsultationSessionCreateDto;
import com.lzc.mindaispringboot.entity.ConsultationMessage;
import com.lzc.mindaispringboot.entity.ConsultationSession;
import com.lzc.mindaispringboot.service.ConsultationMessageService;
import com.lzc.mindaispringboot.service.ConsultationSessionService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

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
}