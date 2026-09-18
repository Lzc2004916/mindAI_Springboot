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
@TableName("sys_file_info")
public class SysFileInfo {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 原始文件名 */
    @TableField("original_name")
    private String originalName;

    /** 访问路径，如 /files/2026/09/17/uuid.png */
    @TableField("file_path")
    private String filePath;

    /** 文件大小（字节） */
    @TableField("file_size")
    private Long fileSize;

    /** 文件类型：IMG/PDF/TXT/DOC/XLS/OTHER */
    @TableField("file_type")
    private String fileType;

    /** 业务类型：avatar / article_cover / attachment */
    @TableField("business_type")
    private String businessType;

    /** 业务对象 ID */
    @TableField("business_id")
    private String businessId;

    /** 业务字段名 */
    @TableField("business_field")
    private String businessField;

    /** 上传用户 ID */
    @TableField("upload_user_id")
    private Long uploadUserId;

    /** 是否临时文件 0:否 1:是 */
    @TableField("is_temp")
    private Integer isTemp;

    /** 状态 0:删除 1:正常 */
    private Integer status;

    @TableField("create_time")
    private LocalDateTime createTime;

    /** 过期时间（仅临时文件） */
    @TableField("expire_time")
    private LocalDateTime expireTime;
}
