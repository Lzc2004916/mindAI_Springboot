package com.lzc.mindaispringboot.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class UserLoginResponseDTO {
    private String token;
    private String roleType;
    private UserDetailResponseDTO userInfo;
    @Builder
    @Data
    public static class UserDetailResponseDTO {
        private Long id;                     // 用户ID
        private String username;            // 用户名
        private String email;               // 邮箱
        private String nickname;            // 昵称
        private String avatar;              // 头像
        private String phone;               // 手机号
        private Integer gender;             // 性别（0:未知 1:男 2:女）
        private String genderDisplayName;   // 性别显示名称
        private LocalDate birthday;         // 生日
        private Integer userType;           // 用户类型（1:普通用户 2:管理员）
        private String userTypeDisplayName; // 用户类型显示名称
        private Integer status;             // 状态（0:禁用 1:正常）
        private String statusDisplayName;   // 状态显示名称
        private String displayName;         // 显示名称
        private LocalDateTime createdAt;           // 创建时间
        private LocalDateTime updatedAt;           // 更新时间
    }
}