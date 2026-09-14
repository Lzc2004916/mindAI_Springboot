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
 * 知识库文章实体，对应数据库表 knowledge_article。
 * 每篇文章归属一个分类（category_id），并关联作者（author_id）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("knowledge_article")
public class KnowledgeArticle {
    /** 文章主键 ID；使用 IdType.INPUT，即主键由调用方手动指定（如雪花 ID / UUID 字符串），非数据库自增 */
    @TableId(type = IdType.INPUT)
    private String id;

    /** 所属分类 ID，关联 knowledge_category.id */
    @TableField("category_id")
    private Long categoryId;

    /** 文章标题 */
    private String title;

    /** 文章摘要（列表页展示的简介，非正文） */
    private String summary;

    /** 文章内容正文（可存储富文本或 Markdown） */
    private String content;

    /** 封面图 URL（列表/详情页头图） */
    @TableField("cover_image")
    private String coverImage;

    /** 标签；多个标签可用逗号分隔存储，如「焦虑,睡眠,冥想」 */
    private String tags;

    /** 作者 ID，关联 user.id（发布该文章的用户） */
    @TableField("author_id")
    private Long authorId;

    /** 阅读次数（每被查看一次累加） */
    @TableField("read_count")
    private Integer readCount;

    /** 状态；一般 0=草稿，1=已发布（具体取值以项目约定为准） */
    private Integer status;

    /** 发布时间（文章对外可见的时间点） */
    @TableField("published_at")
    private LocalDateTime publishedAt;

    /** 创建时间 */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
