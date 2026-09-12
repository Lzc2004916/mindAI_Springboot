package com.lzc.mindaispringboot.service;

import com.lzc.mindaispringboot.entity.ConsultationMessage;
import com.lzc.mindaispringboot.mappper.ConsultionMessageMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ConsultationMessageService {
    @Resource
    public ConsultionMessageMapper consultionMessageMapper;
    public ConsultationMessage saveUserMessage(Long sessionId,String content ,String emotion_tag) {
        ConsultationMessage userMessage = ConsultationMessage.builder()
                .sessionId(sessionId)
                .senderType(1)
                .messageType(1)
                .content(content)
                .emotionTag(emotion_tag)
                .createdAt(LocalDateTime.now())
                .build();
        consultionMessageMapper.insert(userMessage);
        return userMessage;
    }

}
