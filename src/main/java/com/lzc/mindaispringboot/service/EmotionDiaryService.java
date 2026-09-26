package com.lzc.mindaispringboot.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lzc.mindaispringboot.common.Dto.EmotionDiaryAdminQuery;
import com.lzc.mindaispringboot.common.Dto.EmotionDiarySaveDTO;
import com.lzc.mindaispringboot.entity.EmotionDiary;
import com.lzc.mindaispringboot.entity.User;
import com.lzc.mindaispringboot.exception.BusionessException;
import com.lzc.mindaispringboot.mappper.EmotionDiaryMapper;
import com.lzc.mindaispringboot.mappper.UserMapper;
import com.lzc.mindaispringboot.response.EmotionDiaryAdminVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EmotionDiaryService {
    @Resource
    private EmotionDiaryMapper emotionDiaryMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private AnalysisTaskService analysisTaskService;
    private static final int MAX_ROWS = 500;
/// 用户端：按 用户+日期 幂等创建或更新（表有 user_date_unique 唯一键
    public EmotionDiary saveOrUpdate(Long userId, EmotionDiarySaveDTO emotionDiarySaveDTO){
        LambdaQueryWrapper<EmotionDiary> qw = new LambdaQueryWrapper<>();
        qw.eq(EmotionDiary :: getUserId, userId).eq(EmotionDiary :: getDiaryDate,emotionDiarySaveDTO.getDiaryDate());
        EmotionDiary emotionDiary = emotionDiaryMapper.selectOne(qw);
        LocalDateTime now = LocalDateTime.now();
        if (emotionDiary == null){
            emotionDiary = EmotionDiary.builder()
                    .userId(userId)
                    .diaryDate(emotionDiarySaveDTO.getDiaryDate())
                    .moodScore(emotionDiarySaveDTO.getMoodScore())
                    .dominantEmotion(emotionDiarySaveDTO.getDominantEmotion())
                    .emotionTriggers(emotionDiarySaveDTO.getEmotionTriggers())
                    .diaryContent(emotionDiarySaveDTO.getDiaryContent())
                    .sleepQuality(emotionDiarySaveDTO.getSleepQuality())
                    .stressLevel(emotionDiarySaveDTO.getStressLevel())
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
            emotionDiaryMapper.insert(emotionDiary);
        }else {
            emotionDiary.setMoodScore(emotionDiarySaveDTO.getMoodScore());
            emotionDiary.setDominantEmotion(emotionDiarySaveDTO.getDominantEmotion());
            emotionDiary.setEmotionTriggers(emotionDiarySaveDTO.getEmotionTriggers());
            emotionDiary.setDiaryContent(emotionDiarySaveDTO.getDiaryContent());
            emotionDiary.setSleepQuality(emotionDiarySaveDTO.getSleepQuality());
            emotionDiary.setStressLevel(emotionDiarySaveDTO.getStressLevel());
            emotionDiary.setUpdatedAt(now);
            emotionDiaryMapper.updateById(emotionDiary);
        }
        try {
            analysisTaskService.enqueue(emotionDiary.getId(),userId);
        }catch (Exception e){
            log.warn("AI分析任务入队失败，diaryId={}", emotionDiary.getId(),e);
        }
        return emotionDiary;
    }
    /**
     * 用户端：查询"自己的"日记（按日期倒序，可选按月份过滤）
     *
     * @param userId 从 token 取，前端传不了别人的
     * @param month  形如 "2026-09"，不传就返回全部
     */
    public List<EmotionDiary> listMine(Long userId, String month) {
        LambdaQueryWrapper<EmotionDiary> qw = new LambdaQueryWrapper<>();
        qw.eq(EmotionDiary::getUserId, userId);
        if (StrUtil.isNotBlank(month)) {
            // 前端传的是 "2026-09" 这种月份字符串，解析成"当月第一天 ~ 当月最后一天"
            YearMonth ym = YearMonth.parse(month);
            qw.ge(EmotionDiary::getDiaryDate, ym.atDay(1))
              .le(EmotionDiary::getDiaryDate, ym.atEndOfMonth());
        }
        qw.orderByDesc(EmotionDiary::getDiaryDate);
        // 这里不用 LIMIT：查的是自己的日记，数量天然有限（管理端那条全站接口才需要加）
        return emotionDiaryMapper.selectList(qw);
    }
    /** 管理端：分页查询情绪日志 */
    public List<EmotionDiaryAdminVO> adminList(EmotionDiaryAdminQuery query){
        // 1. 构建动态查询条件（LambdaQueryWrapper 用法：只有前端传了值，才拼接对应的 WHERE 条件）
        LambdaQueryWrapper<EmotionDiary> qw = new LambdaQueryWrapper<>();
        // 按用户ID精确筛选
        if (query.getUserId() != null) qw.eq(EmotionDiary :: getUserId,query.getUserId());
        // 按日记日期范围筛选：startDate <= diaryDate
        if (query.getStartDate() != null) qw.ge(EmotionDiary :: getDiaryDate, query.getStartDate());
        // 按日记日期范围筛选：diaryDate <= endDate
        if (query.getEndDate() != null) qw.le(EmotionDiary :: getDiaryDate, query.getEndDate());
        // 按心情分数范围筛选：moodScore >= minMoodScore
        if (query.getMinMoodScore() != null) qw.ge(EmotionDiary :: getMoodScore, query.getMinMoodScore());
        // 按心情分数范围筛选：moodScore <= maxMoodScore
        if (query.getMaxMoodScore() != null) qw.le(EmotionDiary :: getMoodScore, query.getMaxMoodScore());
        // 排序规则：先按创建时间降序，再按ID降序（保证最新的日记排在前面）
        qw.orderByDesc(EmotionDiary :: getCreatedAt).orderByDesc(EmotionDiary :: getId);
        // 限制最大返回行数，防止一次性查询过多数据导致内存压力
        qw.last("LIMIT " + MAX_ROWS);

        // 2. 执行查询，拿到"情绪日记"列表
        List<EmotionDiary> list = emotionDiaryMapper.selectList(qw);
        // 如果查不到任何数据，直接返回空列表，避免后续多余操作
        if (list.isEmpty()) return List.of();

        // 3. 从日记列表中收集所有不重复的用户ID
        Set<Long> userIds = list.stream()
                .map(EmotionDiary::getUserId)      // 取出每条日记的 userId
                .filter(Objects::nonNull)           // 过滤掉 null（防御性编程）
                .collect(Collectors.toSet());       // 收集到 Set 去重

        // 4. 批量查询用户信息，构建 userId -> User 的映射表，用于后续 VO 组装
        Map<Long, User> userMap = Collections.emptyMap();
        if (!userIds.isEmpty()){
            // selectByIds 一次性查询多个用户，避免 N+1 问题
            userMap = userMapper
                    .selectByIds(userIds).stream()
                    .collect(Collectors.toMap(
                            User :: getId,    // key：用户ID
                            u -> u,           // value：用户对象本身
                            (a,b) -> a));     // 如果有重复key（理论上不会），取第一个
        }

        // 5. 将 EmotionDiary（数据库实体）转换为 EmotionDiaryAdminVO（给前端看的视图对象）
        Map<Long,User> finalUserMap = userMap; // lambda 里只能用 final/effectively-final 变量
        List<EmotionDiaryAdminVO> emotionDiaryAdminVOList = list
                .stream()
                .map(d -> EmotionDiaryAdminVO.from(
                        d,                               // 日记实体
                        finalUserMap.get(d.getUserId())))// 从映射表中取出对应的用户信息
                .toList();

        return emotionDiaryAdminVOList;
    }
    /** 管理端：删除情绪日志 */
    public void adminDelete(Long userId){
        if (emotionDiaryMapper.selectById(userId) == null) throw new BusionessException("情绪日志不存在");
        emotionDiaryMapper.deleteById(userId);
    }
}