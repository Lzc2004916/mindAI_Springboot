package com.lzc.mindaispringboot.service;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lzc.mindaispringboot.common.Dto.ConsultationSessionCreateDto;
import com.lzc.mindaispringboot.common.Dto.SessionPageQuery;
import com.lzc.mindaispringboot.entity.ConsultationMessage;
import com.lzc.mindaispringboot.entity.ConsultationSession;
import com.lzc.mindaispringboot.entity.User;
import com.lzc.mindaispringboot.exception.BusionessException;
import com.lzc.mindaispringboot.mappper.ConsultationSessionMapper;
import com.lzc.mindaispringboot.mappper.ConsultionMessageMapper;
import com.lzc.mindaispringboot.mappper.UserMapper;
import com.lzc.mindaispringboot.response.SessionAdminVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ConsultationSessionService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ConsultationSessionMapper consultationSessionMapper;
    @Autowired
    private ConsultionMessageMapper consultionMessageMapper;
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
    public Page<SessionAdminVO> pageSessions(SessionPageQuery query){
        LambdaQueryWrapper<ConsultationSession> qw = new LambdaQueryWrapper<>();
        // userId为空表示查全部
        if (query.getUserId() != null){
            qw.eq(ConsultationSession :: getUserId,query.getUserId());
        }
        // 标题模糊搜索
        if (StrUtil.isNotBlank(query.getKeyword())){
            qw.like(ConsultationSession :: getSessionTitle,query.getKeyword());
        }
        qw.orderByDesc(ConsultationSession :: getStartedAt);
        // ① 分页查会话
        Page<ConsultationSession> page = consultationSessionMapper.selectPage(new Page<>(query.resolvePage(), query.resolvePageSize()), qw);
        //当前页的数据
        List<ConsultationSession> records = page.getRecords();
        // 构建 VO 分页结果，保留分页元信息
        Page<SessionAdminVO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        // 无数据直接返回空页
        if (records.isEmpty()){
            result.setRecords(List.of());
            return result;
        }
        // ② 批量查出这批会话的所有消息
        List<Long> sessionIds = records.stream().map(ConsultationSession::getId).toList();
        List<ConsultationMessage> allMessages = consultionMessageMapper.selectList(
                new LambdaQueryWrapper<ConsultationMessage>()
                        .in(ConsultationMessage::getSessionId, sessionIds)
                        .orderByAsc(ConsultationMessage::getCreatedAt)
        );
        // 按 sessionId 分组，一个会话对应多条消息
        Map<Long,List<ConsultationMessage>> msgGroup = allMessages.stream()
                .collect(Collectors.groupingBy(ConsultationMessage:: getSessionId));
        // ③ 批量查出这批会话所属的用户
        List<Long> userIds = records.stream().map(ConsultationSession :: getUserId).distinct().toList();
        Map<Long, User> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));
        // ④ 组装 VO 列表
        ArrayList<SessionAdminVO> voList = new ArrayList<>();
        for (ConsultationSession s : records) {
            // 当前会话的消息列表
            List<ConsultationMessage> msgs = msgGroup.get(s.getId());
            if (msgs == null) msgs = List.of();
            // 最后一条消息作为预览
            ConsultationMessage last = msgs.isEmpty() ? null : msgs.get(msgs.size() - 1);
            User u = userMap.get(s.getUserId());
            voList.add(
                    SessionAdminVO.builder()
                            .id(s.getId())
                            .userId(s.getUserId())
                            .userNickname(u ==null ? null : u.getNickname())
                            .sessionTitle(s.getSessionTitle())
                            .lastMessageContent(last == null ? null : SessionAdminVO.preview(last.getContent()))
                            .messageCount((long) msgs.size())
                            .lastMessageTime(last == null ? null : last.getCreatedAt())
                            .startedAt(s.getStartedAt())
                            .build()
            );
        }
        result.setRecords(voList);
        return result;
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