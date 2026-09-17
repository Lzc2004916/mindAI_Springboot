package com.lzc.mindaispringboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lzc.mindaispringboot.entity.KnowledgeCategory;
import com.lzc.mindaispringboot.mappper.KnowledgeCategoryMapper;
import com.lzc.mindaispringboot.response.CategoryTreeVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class KnowledgeCategoryService {
    @Resource
    private KnowledgeCategoryMapper categoryMapper;

    /**
     * 返回所有"启用"的分类（平铺列表，按 sortOrder 升序）。
     */
    public List<KnowledgeCategory> getEnabledCategories() {
        LambdaQueryWrapper<KnowledgeCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(KnowledgeCategory::getStatus, 1);
        queryWrapper.orderByAsc(KnowledgeCategory::getSortOrder);
        return categoryMapper.selectList(queryWrapper);
    }
    public List<CategoryTreeVO> getCategoryTree() {
        LambdaQueryWrapper<KnowledgeCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(KnowledgeCategory :: getStatus , 1);
        queryWrapper.orderByAsc(KnowledgeCategory :: getSortOrder);
        List<KnowledgeCategory> categoryList = categoryMapper.selectList(queryWrapper);
        if(categoryList.isEmpty()) return new ArrayList<>();
        // 2. 实体 → VO，并按 parentId 分组
        //    用 LinkedHashMap 而不是 HashMap：保证分组的遍历顺序 = 数据库返回顺序
        Map<Long, List<CategoryTreeVO>> groupByParent = new LinkedHashMap<>();
        for (KnowledgeCategory c : categoryList) {
            // parentId 可能是 null（表结构允许），统一成 0 表示"一级分类"，避免后面 map.get(null) 出岔子
            long parentId = c.getParentId() == null ? 0L : c.getParentId();
            if (!groupByParent.containsKey(parentId)){
                groupByParent.put(parentId, new ArrayList<>());
            }
            groupByParent.get(parentId).add(toVo(c));
        }
        return buildChildren(0L,groupByParent);
    }
    /** 递归：把 groupByParent 中 parentId 对应的节点挂上它们各自的 children */
    private List<CategoryTreeVO> buildChildren(Long parentId, Map<Long, List<CategoryTreeVO>> groupByParent) {
        List<CategoryTreeVO> nodes = groupByParent.get(parentId);
        if (nodes == null || nodes.isEmpty()) return  new ArrayList<>();
        for (CategoryTreeVO node : nodes) {
            node.setChildren(buildChildren(node.getId(),groupByParent));
        }
        return nodes;
    }
    private CategoryTreeVO toVo(KnowledgeCategory c) {
        return CategoryTreeVO.builder()
                .id(c.getId())
                .categoryName(c.getCategoryName())
                .categoryCode(c.getCategoryCode())
                .sortOrder(c.getSortOrder())
                .children(new  ArrayList<>())// 先给空数组，后面由 buildChildren 填充
                .build();
    }
}