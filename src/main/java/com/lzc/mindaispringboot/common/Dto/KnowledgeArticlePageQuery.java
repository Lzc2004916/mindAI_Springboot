package com.lzc.mindaispringboot.common.Dto;

import lombok.Data;

@Data
public class KnowledgeArticlePageQuery {
    private Long pageNum = 1L;
    private Long pageSize = 10L;
    private Long categoryId;
    private String keyword;// 标题/摘要模糊
    private Integer status;   // 管理端可传 0/1；用户端强制按 1 处理
}
