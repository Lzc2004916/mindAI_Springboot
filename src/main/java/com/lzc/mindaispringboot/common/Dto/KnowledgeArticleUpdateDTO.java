package com.lzc.mindaispringboot.common.Dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class KnowledgeArticleUpdateDTO {
    private Long categoryId;
    @Size(max = 200, message = "标题最多200字符")
    private String title;
    @Size(max = 2000, message = "摘要最多2000字符")
    private String summary;
    private String content;
    @Size(max = 500, message = "封面图片地址最多500字符")
    private String coverImage;
    @Size(max = 500, message = "标签最多500字符")
    private String tags;
}
