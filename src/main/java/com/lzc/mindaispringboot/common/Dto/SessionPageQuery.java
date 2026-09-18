package com.lzc.mindaispringboot.common.Dto;

import lombok.Data;

@Data
public class SessionPageQuery {
    /** 页码，从 1 开始 */
    private Long pargeNum = 1L;
    /** 每页条数 */
    private Long pageSize = 10L;
    //仅管理员生效👇
    private Long userId;
    /** 标题关键字模糊搜索（可选） */
    private String keyword;
}
