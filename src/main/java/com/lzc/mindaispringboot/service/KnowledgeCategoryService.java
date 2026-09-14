package com.lzc.mindaispringboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lzc.mindaispringboot.entity.KnowledgeCategory;
import com.lzc.mindaispringboot.mappper.KnowledgeCategoryMapper;
import com.lzc.mindaispringboot.response.CategoryTreeNode;
import jakarta.annotation.Resource;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class KnowledgeCategoryService {
    @Resource
    private KnowledgeCategoryMapper categoryMapper;
    public List<CategoryTreeNode> getTree(){
        // ===== 第 1 步：查出所有"启用"的分类，按 sortOrder 升序 =====
        LambdaQueryWrapper<KnowledgeCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(KnowledgeCategory::getStatus, 1);
        queryWrapper.orderByAsc(KnowledgeCategory::getSortOrder);
        List<KnowledgeCategory> all = categoryMapper.selectList(queryWrapper);
        // ===== 第 2 步：给每个分类建一个树节点，放进 Map（id -> 节点）=====
        // 用 Map 是为了第 3 步能 O(1) 根据 parentId 找到父节点
        Map<Long, CategoryTreeNode> nodeMap = new LinkedHashMap<>();
        for (KnowledgeCategory c : all) {
            nodeMap.put(c.getId(), CategoryTreeNode.builder()
                            .id(c.getId())
                            .parentId(c.getParentId())
                            .categoryName(c.getCategoryName())
                            .categoryCode(c.getCategoryCode())
                            .description(c.getDescription())
                            .sortOrder(c.getSortOrder())
                            .children(new ArrayList<>())
                    .build());
        }
        // ===== 第 3 步：遍历每个节点，把它挂到父节点的 children 上 =====
        List<CategoryTreeNode> roots = new ArrayList<>();
        for (CategoryTreeNode node : nodeMap.values()) {
            Long parentId = node.getParentId();
            if (parentId == null || parentId == 0) {
                roots.add(node);
            }
            CategoryTreeNode parent = nodeMap.get(parentId);
            if (parent != null) {
                parent.getChildren().add(node);
            }else {
                roots.add(node);
            }
        }
        return roots;
    }
}
