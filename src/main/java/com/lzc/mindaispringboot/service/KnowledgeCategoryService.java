package com.lzc.mindaispringboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lzc.mindaispringboot.entity.KnowledgeCategory;
import com.lzc.mindaispringboot.mappper.KnowledgeCategoryMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KnowledgeCategoryService {
    @Resource
    private KnowledgeCategoryMapper categoryMapper;

    /**
     * 返回所有"启用"的分类（平铺列表，按 sortOrder 升序）。
     * 不做树形嵌套，前端如需多级菜单可自行按 parentId 组装。
     */
    public List<KnowledgeCategory> getEnabledCategories() {
        LambdaQueryWrapper<KnowledgeCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(KnowledgeCategory::getStatus, 1);
        queryWrapper.orderByAsc(KnowledgeCategory::getSortOrder);
        return categoryMapper.selectList(queryWrapper);
    }
}
