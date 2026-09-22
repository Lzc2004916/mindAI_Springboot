package com.lzc.mindaispringboot.common.Dto;

import lombok.Data;

@Data
public class SessionPageQuery {
    /// 管理端传的页码
    private Long currentPage;
    /** 用户端传的页码，从 1 开始 */
    private Long pageNum;
    /** 每页条数 */
    private Long pageSize = 10L;
    //仅管理员生效👇
    private Long userId;
    /** 标题关键字模糊搜索（可选） */
    private String keyword;

    /**
     * 取有效页码：管理端传 currentPage、用户端传 pageNum，两边都能接住
     * （同一个 /sessions 接口服务两端，前端两套命名）
     */
    public Long resolvePage(){
        if (currentPage != null && currentPage > 0) return currentPage;
        if (pageNum != null && pageNum > 0) return pageNum;
        return 1L;
    }

    /** 取有效每页条数：非法值兜底 10 */
    public Long resolvePageSize(){
        return (pageSize != null && pageSize > 0) ? pageSize : 10L;
    }
}
