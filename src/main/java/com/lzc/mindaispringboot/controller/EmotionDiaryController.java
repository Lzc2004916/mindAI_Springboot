package com.lzc.mindaispringboot.controller;
import com.lzc.mindaispringboot.Aop.GetToken;
import com.lzc.mindaispringboot.Aop.Token_Aspect;
import com.lzc.mindaispringboot.common.Dto.EmotionDiaryAdminQuery;
import com.lzc.mindaispringboot.common.Dto.EmotionDiarySaveDTO;
import com.lzc.mindaispringboot.common.Result;
import com.lzc.mindaispringboot.entity.EmotionDiary;
import com.lzc.mindaispringboot.response.EmotionDiaryAdminVO;
import com.lzc.mindaispringboot.service.EmotionDiaryService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/emotion-diary")
public class EmotionDiaryController {
    @Resource
    private EmotionDiaryService emotionDiaryService;
    /// 用户端
    @GetToken
    @PostMapping
    public Result<EmotionDiary> save(@Valid @RequestBody EmotionDiarySaveDTO emotionDiarySaveDTO){
        Long userId = Token_Aspect.getUserId();
        return Result.success(emotionDiaryService.saveOrUpdate(userId, emotionDiarySaveDTO));
    }
    /**
     * 用户端：查看自己的日记（前端"我的情绪花园 / 我的日记"页用）
     * 可选按月份过滤，如 ?month=2026-09
     */
    @GetToken
    @GetMapping("/mine")
    public Result<List<EmotionDiary>> mine(@RequestParam(required = false) String month){
        return Result.success(emotionDiaryService.listMine(Token_Aspect.getUserId(), month));
    }
    /// 管理端
    @GetToken
    @PreAuthorize("hasRole('2')")
    @GetMapping("/admin/page")
    public Result<List<EmotionDiaryAdminVO>> adminPage(EmotionDiaryAdminQuery query){
        return Result.success(emotionDiaryService.adminList(query));
    }
    @GetToken
    @PreAuthorize("hasRole('2')")
    @DeleteMapping("/admin/{id}")
    public Result<Void> admindelete(@PathVariable Long id){
        emotionDiaryService.adminDelete(id);
        return Result.success();
    }
}
