package com.lzc.mindaispringboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lzc.mindaispringboot.common.Dto.EmotionDiarySaveDTO;
import com.lzc.mindaispringboot.entity.EmotionDiary;
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
        if (emotionDiary != null){
            //TODO 待完善
        }
        return null;
    }
}