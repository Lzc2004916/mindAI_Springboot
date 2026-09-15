package com.lzc.mindaispringboot.common.Dto;

import lombok.Data;

@Data
public class SessionPageQuery {
    private Long pargeNum = 1L;
    private Long pageNum = 1L;
    //仅管理员生效👇
    private Long userId;
}
