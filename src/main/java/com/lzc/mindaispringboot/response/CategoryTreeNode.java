package com.lzc.mindaispringboot.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 知识库分类树节点（响应 DTO）。
 * 用于把多级分类（knowledge_category）组装成树形结构返回给前端，
 * 每个节点通过 children 递归持有其子分类。
 */
@Data
@Builder
public class CategoryTreeNode {
    /** 分类 ID（对应 knowledge_category.id） */
    private Long id;

    /** 父分类 ID（对应 knowledge_category.parent_id） */
    private Long parentId;

    /** 分类名称 */
    private String categoryName;

    /** 分类编码（程序内部唯一标识） */
    private String categoryCode;

    /** 分类描述 */
    private String description;

    /** 排序号；数值越小越靠前，用于控制同层级下的展示顺序 */
    private Integer sortOrder;

    /** 子分类节点列表；无子节点时为 null 或空列表（递归结构，自身类型） */
    private List<CategoryTreeNode> children;
}
