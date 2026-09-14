package com.lzc.mindaispringboot.common.Dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class KnowledgeArticleCreateDTO {
    @NotNull(message = "分类ID不能为空")
    private Long categoryId;
    @NotBlank(message = "文章标题不能为空")
    @Size(max = 200, message = "标题最多200字符")
    private String title;
    @Size(max = 2000, message = "摘要最多2000字符")
    private String summary;
    @NotBlank(message = "文章内容不能为空")
    private String content;
    @Size(max = 500, message = "封面图片地址最多500字符")
    private String coverImage;
    @Size(max = 500, message = "标签最多500字符")
    private String tags;
    /** 1:发布 0:下架，不传默认 1 */
    private Integer status;
}
