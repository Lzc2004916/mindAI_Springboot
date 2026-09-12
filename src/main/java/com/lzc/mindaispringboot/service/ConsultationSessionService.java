package com.lzc.mindaispringboot.service;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.lzc.mindaispringboot.common.Dto.ConsultationSessionCreateDto;
import com.lzc.mindaispringboot.entity.ConsultationSession;
import com.lzc.mindaispringboot.entity.User;
import com.lzc.mindaispringboot.mappper.ConsultationSessionMapper;
import com.lzc.mindaispringboot.mappper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ConsultationSessionService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ConsultationSessionMapper consultationSessionMapper;
    public ConsultationSession createSession(Long userId, ConsultationSessionCreateDto SessionCreateDto){
        //验证用户是否存在
        User user = userMapper.selectById(userId);
        if (user != null){
            //创建会话记录
            ConsultationSession session = ConsultationSession.builder()
                    .userId(userId)
                    .sessionTitle(SessionCreateDto.getSessionTitle())
                    .startedAt(LocalDateTime.now())
                    .build();
            //如果未提供标题
            if (!StrUtil.isBlank(SessionCreateDto.getSessionTitle())){
                session.setSessionTitle("宁渡AI助手 - " + DateUtil.format(LocalDateTime.now(),"MM-dd-yyyy HH:mm:ss"));
            }
            //插入记录
            consultationSessionMapper.insert(session);
            return session;
        }
        return null;
    }
}
