package com.lzc.mindaispringboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lzc.mindaispringboot.AiService.EmotionDiaryAnalysisService;
import com.lzc.mindaispringboot.common.Dto.EmotionDiaryAdminQuery;
import com.lzc.mindaispringboot.common.Dto.EmotionDiarySaveDTO;
import com.lzc.mindaispringboot.entity.EmotionDiary;
import com.lzc.mindaispringboot.entity.User;
import com.lzc.mindaispringboot.exception.BusionessException;
import com.lzc.mindaispringboot.mappper.EmotionDiaryMapper;
import com.lzc.mindaispringboot.mappper.UserMapper;
import com.lzc.mindaispringboot.response.EmotionDiaryAdminVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EmotionDiaryService {
    @Resource
    private EmotionDiaryMapper emotionDiaryMapper;
    @Resource
    private EmotionDiaryAnalysisService emotionDiaryAnalysisService;
    @Resource
    private UserMapper userMapper;
    private static final int MAX_ROWS = 500;
/// 用户端：按 用户+日期 幂等创建或更新（表有 user_date_unique 唯一键
    public EmotionDiary saveOrUpdate(Long userId, EmotionDiarySaveDTO emotionDiarySaveDTO){
        LambdaQueryWrapper<EmotionDiary> qw = new LambdaQueryWrapper<>();
        qw.eq(EmotionDiary :: getUserId, userId).eq(EmotionDiary :: getDiaryDate,emotionDiarySaveDTO.getDiaryDate());
        EmotionDiary emotionDiary = emotionDiaryMapper.selectOne(qw);
        LocalDateTime now = LocalDateTime.now();
        boolean exists = emotionDiary != null;
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
            emotionDiaryAnalysisService.analyzeEmotionDiary(emotionDiary.getId(),exists);
        }catch (Exception e){
            System.err.println("AI情绪分析失败，diaryId=" + emotionDiary.getId() + "：" + e.getMessage());
        }
        return emotionDiary;
    }
    /** 管理端：分页查询情绪日志 */
    public List<EmotionDiaryAdminVO> adminList(EmotionDiaryAdminQuery query){
        LambdaQueryWrapper<EmotionDiary> qw = new LambdaQueryWrapper<>();
        if (query.getUserId() != null) qw.eq(EmotionDiary :: getUserId,query.getUserId());
        if (query.getStartDate() != null) qw.ge(EmotionDiary :: getDiaryDate, query.getStartDate());
        if (query.getEndDate() != null) qw.le(EmotionDiary :: getDiaryDate, query.getEndDate());
        if (query.getMinMoodScore() != null) qw.ge(EmotionDiary :: getMoodScore, query.getMinMoodScore());
        if (query.getMaxMoodScore() != null) qw.le(EmotionDiary :: getMoodScore, query.getMaxMoodScore());
        qw.orderByDesc(EmotionDiary :: getCreatedAt).orderByDesc(EmotionDiary :: getId);
        qw.last("LIMIT" + MAX_ROWS);
        List<EmotionDiary> list = emotionDiaryMapper.selectList(qw);
        if (list.isEmpty()) return List.of(); // 空就早返回
        Set<Long> userIds = list.stream()
                .map(EmotionDiary::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, User> userMap = Collections.emptyMap();
        if (!userIds.isEmpty()){
            userMap = userMapper
                    .selectByIds(userIds).stream()
                    .collect(Collectors.toMap(User :: getId, u -> u, (a,b) -> a));
        }
        Map<Long,User> finalUserMap = userMap;
        List<EmotionDiaryAdminVO> emotionDiaryAdminVOList = list.stream()
                .map(d -> EmotionDiaryAdminVO.from(d, finalUserMap.get(d.getUserId())))
                .toList();
        return emotionDiaryAdminVOList;
    }
    /** 管理端：删除情绪日志 */
    public void adminDelete(Long userId){
        if (emotionDiaryMapper.selectById(userId) == null) throw new BusionessException("情绪日志不存在");
        emotionDiaryMapper.deleteById(userId);
    }
}