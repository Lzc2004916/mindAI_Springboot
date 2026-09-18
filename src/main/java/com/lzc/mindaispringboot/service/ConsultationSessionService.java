package com.lzc.mindaispringboot.service;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lzc.mindaispringboot.common.Dto.ConsultationSessionCreateDto;
import com.lzc.mindaispringboot.common.Dto.SessionPageQuery;
import com.lzc.mindaispringboot.entity.ConsultationSession;
import com.lzc.mindaispringboot.entity.User;
import com.lzc.mindaispringboot.exception.BusionessException;
import com.lzc.mindaispringboot.mappper.ConsultationSessionMapper;
import com.lzc.mindaispringboot.mappper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ConsultationSessionService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ConsultationSessionMapper consultationSessionMapper;
    @Autowired
    private ConsultationMessageService consultationMessageService;
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
            //标题为空使用默认值
            if (StrUtil.isBlank(SessionCreateDto.getSessionTitle())){
                session.setSessionTitle("宁渡AI助手 - " + DateUtil.format(LocalDateTime.now(),"MM-dd-yyyy HH:mm:ss"));
            }
            //插入记录
            consultationSessionMapper.insert(session);
            return session;
        }
        return null;
    }
    //查询会话列表
    public List<ConsultationSession> listSessions(Long userId){
        LambdaQueryWrapper<ConsultationSession> queryWrapper = new LambdaQueryWrapper<>();
        if (userId != null) queryWrapper.eq(ConsultationSession::getUserId,userId);
        queryWrapper.orderByDesc(ConsultationSession :: getStartedAt);
        return consultationSessionMapper.selectList(queryWrapper);
    }
    /** 分页查询会话列表 */
    public Page<ConsultationSession> pageSessions(SessionPageQuery query){
        LambdaQueryWrapper<ConsultationSession> qw = new LambdaQueryWrapper<>();
        //userId为空表示查全部
        if (query.getUserId() != null){
            qw.eq(ConsultationSession :: getUserId,query.getUserId());
        }
        //标题模糊
        if (StrUtil.isNotBlank(query.getKeyword())){
            qw.like(ConsultationSession :: getSessionTitle,query.getKeyword());
        }
        qw.orderByDesc(ConsultationSession :: getStartedAt);
        return consultationSessionMapper.selectPage(new Page<>(query.getPargeNum(),query.getPageSize()),qw);
    }
    //按主键取单条
    public ConsultationSession getById(Long sessionId){
        return consultationSessionMapper.selectById(sessionId);
    }
    //删除会话及其全部消息
    public void deleteSession(Long sessionId){
        if (consultationSessionMapper.selectById(sessionId) == null){
            throw new BusionessException("会话不存在");
        }
        //先删除消息
        consultationMessageService.deleteBySessionId(sessionId);
        //再删会话
        consultationSessionMapper.deleteById(sessionId);
    }

    public void updateById(ConsultationSession session) {
        consultationSessionMapper.updateById(session);
    }
}