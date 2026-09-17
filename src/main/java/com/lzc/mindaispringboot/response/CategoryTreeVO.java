package com.lzc.mindaispringboot.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
/**
 * 分类树节点。
 * 只保留前端需要的字段，不直接返回 KnowledgeCategory 实体——
 * 实体里的 description / createdAt / updatedAt 对树形下拉毫无用处，还会让 JSON 变臃肿。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryTreeVO {
    private Long id; //分类id
    private String categoryName; //分类名称
    private String categoryCode; //类别
    private Integer sortOrder; //排序
    /** 子分类；没有子级时是空数组（不要返回 null，前端 v-for 会报错） */
    @Builder.Default
    private List<CategoryTreeVO> children = new ArrayList<>();
}
