package com.lzc.mindaispringboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lzc.mindaispringboot.Aop.GetToken;
import com.lzc.mindaispringboot.Aop.Token_Aspect;
import com.lzc.mindaispringboot.common.Dto.KnowledgeArticleCreateDTO;
import com.lzc.mindaispringboot.common.Dto.KnowledgeArticlePageQuery;
import com.lzc.mindaispringboot.common.Dto.KnowledgeArticleStatusDTO;
import com.lzc.mindaispringboot.common.Dto.KnowledgeArticleUpdateDTO;
import com.lzc.mindaispringboot.common.Result;
import com.lzc.mindaispringboot.entity.KnowledgeArticle;
import com.lzc.mindaispringboot.entity.KnowledgeCategory;
import com.lzc.mindaispringboot.response.CategoryTreeVO;
import com.lzc.mindaispringboot.service.KnowledgeArticleService;
import com.lzc.mindaispringboot.service.KnowledgeCategoryService;
import com.lzc.mindaispringboot.util.AuthUtil;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeController {
    @Resource
    private KnowledgeArticleService knowledgeArticleService;
    @Resource
    private KnowledgeCategoryService categoryService;

    @GetToken
    @GetMapping("/article/page")
    public Result<Page<KnowledgeArticle>> articlePage(KnowledgeArticlePageQuery query) {
        boolean skipPublishFilter = AuthUtil.isAdmin();
        return Result.success(knowledgeArticleService.page(query, skipPublishFilter));
    }
    @GetToken
    @PreAuthorize("hasRole('2')")
    @PostMapping("/article")
    public Result<KnowledgeArticle> addArticle(@Valid @RequestBody KnowledgeArticleCreateDTO knowledgeArticleCreateDTO){
        Long userId = Token_Aspect.getUserId();
        knowledgeArticleService.create(knowledgeArticleCreateDTO,userId);
        return Result.success();
    }
    /** 管理端：更新文章（只覆盖非 null 字段） */
    @GetToken
    @PreAuthorize("hasRole('2')")
    @PutMapping("/article/{id}")
    public Result<KnowledgeArticle> updateArticle(
            @PathVariable String id,
            @Valid @RequestBody KnowledgeArticleUpdateDTO dto) {
        knowledgeArticleService.update(id, dto);
        return Result.success();
    }
    /** 管理端：发布 / 下架 */
    @GetToken
    @PreAuthorize("hasRole('2')")
    @PutMapping("/article/{id}/status")
    public Result<Void> updateArticleStatus(
            @PathVariable String id,
            @Valid @RequestBody KnowledgeArticleStatusDTO dto) {
        knowledgeArticleService.updateStatus(id, dto.getStatus());
        return Result.success();
    }
    //查看详情
    @GetToken
    @GetMapping("/article/{id}")
    public Result<KnowledgeArticle> articleDetail(@PathVariable String id){
        return Result.success(knowledgeArticleService.detail(id,AuthUtil.isAdmin()));
    }
    //删除文章
    @GetToken
    @PreAuthorize("hasRole('2')")
    @DeleteMapping("/article/{id}")
    public Result<Void> deleteArticle(@PathVariable String id){
        knowledgeArticleService.delete(id);
        return Result.success();
    }
    /** 分类平铺列表（仅启用状态），前端用于分类筛选 / 下拉 */
    @GetToken
    @GetMapping("/categories")
    public Result<List<KnowledgeCategory>> categories() {
        return Result.success(categoryService.getEnabledCategories());
    }
    @GetToken
    @GetMapping("/category/tree")
    public Result<List<CategoryTreeVO>> categoryTree(){
        return Result.success(categoryService.getCategoryTree());
    }
}
