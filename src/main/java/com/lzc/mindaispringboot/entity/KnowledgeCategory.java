package com.lzc.mindaispringboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 知识库分类实体，对应数据库表 knowledge_category。
 * 分类支持多级（通过 parent_id 关联自身形成分类树）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("knowledge_category")
public class KnowledgeCategory {
    /** 分类主键 ID（数据库自增） */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 父分类 ID；为 0 或 null 时表示一级分类，用于构建多级分类树 */
    @TableField("parent_id")
    private Long parentId;

    /** 分类名称（展示用，如「焦虑自助」「亲子沟通」） */
    @TableField("category_name")
    private String categoryName;

    /** 分类编码（程序内部唯一标识，如 CATEGORY_ANXIETY，便于代码区分而非依赖名称） */
    @TableField("category_code")
    private String categoryCode;

    /** 分类描述（可选，用于后台说明该分类的用途） */
    private String description;

    /** 排序号；数值越小越靠前，用于控制分类在前端的展示顺序 */
    @TableField("sort_order")
    private Integer sortOrder;

    /** 状态；一般 0=禁用，1=启用（具体取值以项目约定为准） */
    private Integer status;

    /** 创建时间 */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
