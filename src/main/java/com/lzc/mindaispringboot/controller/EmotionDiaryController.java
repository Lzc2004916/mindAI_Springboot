package com.lzc.mindaispringboot.controller;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lzc.mindaispringboot.Aop.GetToken;
import com.lzc.mindaispringboot.Aop.Token_Aspect;
import com.lzc.mindaispringboot.common.Dto.EmotionDiaryAdminPageQuery;
import com.lzc.mindaispringboot.common.Dto.EmotionDiarySaveDTO;
import com.lzc.mindaispringboot.common.Result;
import com.lzc.mindaispringboot.entity.EmotionDiary;
import com.lzc.mindaispringboot.service.EmotionDiaryService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
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
    /// 管理端
    @GetToken
    @PreAuthorize("hasRole('2')")
    @GetMapping
    public Result<Page<EmotionDiary>> adminPage(EmotionDiaryAdminPageQuery query){
        return Result.success(emotionDiaryService.adminPage(query));
    }
    @GetToken
    @PreAuthorize("hasRole('2')")
    @DeleteMapping("/admin/{id}")
    public Result<Void> admindelete(@PathVariable Long id){
        emotionDiaryService.adminDelete(id);
        return Result.success();
    }
}
