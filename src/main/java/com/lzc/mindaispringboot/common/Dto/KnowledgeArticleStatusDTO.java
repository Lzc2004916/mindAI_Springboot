package com.lzc.mindaispringboot.common.Dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class KnowledgeArticleStatusDTO {
    @NotNull(message = "状态不能为空")
    private Integer status; // 1:发布 0:下架
}
