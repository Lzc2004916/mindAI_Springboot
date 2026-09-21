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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("ai_analysis_task")
public class AiAnalysisTask {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 日记ID（外键 → emotion_diary.id，删除日记会级联删除任务） */
    @TableField("diary_id")
    private Long diaryId;

    /** 用户ID */
    @TableField("user_id")
    private Long userId;

    /** 任务状态：PENDING（待处理）/ PROCESSING（处理中）/ COMPLETED（已完成）/ FAILED（失败） */
    @TableField("status")
    private String status;

    /** 任务触发类型：AUTO（自动触发）/ MANUAL（用户手动）/ ADMIN（管理员触发）/ BATCH（批量任务） */
    @TableField("task_type")
    private String taskType;

    /** 1-低 2-正常 3-高 4-紧急 */
    @TableField("priority")
    private Integer priority;

    @TableField("retry_count")
    private Integer retryCount;

    @TableField("max_retry_count")
    private Integer maxRetryCount;

    /** 失败原因（成功时留空） */
    @TableField("error_message")
    private String errorMessage;

    /** 任务开始执行的时间（PENDING → PROCESSING 时写入） */
    @TableField("started_at")
    private LocalDateTime startedAt;

    /** 任务完成的时间（PROCESSING → COMPLETED/FAILED 时写入） */
    @TableField("completed_at")
    private LocalDateTime completedAt;

    /** 任务创建时间（插入数据库时自动写入） */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** 最后更新时间（每次修改任务记录时刷新） */
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}