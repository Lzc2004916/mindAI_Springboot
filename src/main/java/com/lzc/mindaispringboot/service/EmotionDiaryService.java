package com.lzc.mindaispringboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lzc.mindaispringboot.common.Dto.EmotionDiaryAdminPageQuery;
import com.lzc.mindaispringboot.common.Dto.EmotionDiarySaveDTO;
import com.lzc.mindaispringboot.entity.EmotionDiary;
import com.lzc.mindaispringboot.exception.BusionessException;
import com.lzc.mindaispringboot.mappper.EmotionDiaryMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class EmotionDiaryService {
    @Resource
    private EmotionDiaryMapper emotionDiaryMapper;
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
        return emotionDiary;
    }
    /** 管理端：分页查询情绪日志 */
    public Page<EmotionDiary> adminPage(EmotionDiaryAdminPageQuery query){
        LambdaQueryWrapper<EmotionDiary> qw = new LambdaQueryWrapper<>();
        if (query.getUserId() != null) qw.eq(EmotionDiary :: getUserId,query.getUserId());
        if (query.getStartDate() != null) qw.ge(EmotionDiary :: getDiaryDate, query.getStartDate());
        if (query.getEndDate() != null) qw.le(EmotionDiary :: getDiaryDate, query.getEndDate());
        return emotionDiaryMapper.selectPage(new Page<>(query.getPageNum(),query.getPageSize()),qw);
    }
    /** 管理端：删除情绪日志 */
    public void adminDelete(Long userId){
        if (emotionDiaryMapper.selectById(userId) == null) throw new BusionessException("情绪日志不存在");
        emotionDiaryMapper.deleteById(userId);
    }
}
