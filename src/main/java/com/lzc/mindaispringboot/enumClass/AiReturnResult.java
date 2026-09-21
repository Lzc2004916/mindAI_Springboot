package com.lzc.mindaispringboot.enumClass;

import lombok.Getter;

/// AI 分析任务的统一枚举：包含任务状态 + 任务触发类型
@Getter
public enum AiReturnResult {

    // ==================== 任务状态 ====================

    /** 待处理：任务已创建，等待执行 */
    PENDING("PENDING", "待处理"),

    /** 处理中：AI 正在分析 */
    PROCESSING("PROCESSING", "处理中"),

    /** 已完成：AI 分析成功，结果已写回 */
    COMPLETED("COMPLETED", "已完成"),

    /** 失败：AI 分析出错（网络超时 / 模型异常等） */
    FAILED("FAILED", "失败"),

    // ==================== 任务触发类型 ====================

    /** 自动触发：写日记后系统自动发起分析 */
    TYPE_AUTO("AUTO", "自动触发"),

    /** 管理员触发：管理员在后台手动发起分析 */
    TYPE_ADMIN("ADMIN", "管理员触发");

    //TODO 待扩展：TYPE_MANUAL("MANUAL", "用户手动")、TYPE_BATCH("BATCH", "批量任务")

    private final String code;
    private final String desc;

    AiReturnResult(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /// 根据数据库存的 code 字符串反查枚举
    public static AiReturnResult fromCode(String code) {
        for (AiReturnResult v : values()) {
            if (v.code.equals(code)) {
                return v;
            }
        }
        throw new IllegalArgumentException("未知的 AI 返回状态: " + code);
    }
}