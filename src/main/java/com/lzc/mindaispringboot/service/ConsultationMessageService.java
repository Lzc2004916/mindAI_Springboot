package com.lzc.mindaispringboot.service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lzc.mindaispringboot.entity.ConsultationMessage;
import com.lzc.mindaispringboot.mappper.ConsultionMessageMapper;
import com.lzc.mindaispringboot.response.ConsultationMessageResponseDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ConsultationMessageService {
    @Resource
    public ConsultionMessageMapper consultionMessageMapper;
    //保存的是用户的消息
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
    public ConsultationMessage saveAiMessage(Long sessionId,String content , String ai_model) {
        ConsultationMessage AiMessage = ConsultationMessage.builder()
                .sessionId(sessionId)
                .senderType(2)
                .messageType(1)
                .content(content)
                .aiModel(ai_model)
                .createdAt(LocalDateTime.now())
                .build();
        consultionMessageMapper.insert(AiMessage);
        return AiMessage;
    }
    //保存Ai消息
    public Integer getMessageCount(Long sessionId){
        LambdaQueryWrapper<ConsultationMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ConsultationMessage :: getSessionId,sessionId);
        return consultionMessageMapper.selectCount(queryWrapper).intValue();
    }
    /// 获取会话中最新的消息数据
    public ConsultationMessageResponseDTO getLastMessageBySessionId(Long sessionId){
        // 构建查询条件：按 sessionId 匹配，按创建时间倒序，取最新一条
        LambdaQueryWrapper<ConsultationMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ConsultationMessage::getSessionId, sessionId)
                .orderByDesc(ConsultationMessage::getCreatedAt)
                .last("limit 1");
        // 执行查询，获取最新的消息记录
        ConsultationMessage lastMessage = consultionMessageMapper.selectOne(queryWrapper);
        // 将实体转换为响应 DTO 后返回
        return lastMessage != null ? convertToResponseDTO(lastMessage) : null;
    }
    //按时间升序返回会话全部消息
    public List<ConsultationMessageResponseDTO> listBySession(Long sessionId){
        LambdaQueryWrapper<ConsultationMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ConsultationMessage :: getSessionId,sessionId)
                .orderByAsc(ConsultationMessage::getCreatedAt);
        List<ConsultationMessage> consultationMessages = consultionMessageMapper.selectList(queryWrapper);
        ArrayList<ConsultationMessageResponseDTO> result = new ArrayList<>();
        for (ConsultationMessage message : consultationMessages) {
            result.add(convertToResponseDTO(message));
        }
        return result;
    }
    //删除会话全部消息
    public void deleteBySessionId(Long sessionId){
        LambdaQueryWrapper<ConsultationMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ConsultationMessage::getSessionId, sessionId);
        consultionMessageMapper.delete(queryWrapper);
    }
    public ConsultationMessageResponseDTO convertToResponseDTO(ConsultationMessage message) {
        if (message == null) return null;
        ConsultationMessageResponseDTO dto = ConsultationMessageResponseDTO.builder()
                .id(message.getId())
                .sessionId(message.getSessionId())
                .senderType(message.getSenderType())
                .senderTypeDesc(message.getSenderTypeDesc())
                .messageType(message.getMessageType())
                .messageTypeDesc(message.getMessageTypeDesc())
                .content(message.getContent())
                .emotionTag(message.getEmotionTag())
                .aiModel(message.getAiModel())
                .createdAt(message.getCreatedAt())
                .build();
        dto.calculateContentLength();
        return dto;
    }
}