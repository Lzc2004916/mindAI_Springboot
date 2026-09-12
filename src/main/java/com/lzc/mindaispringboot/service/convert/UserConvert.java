package com.lzc.mindaispringboot.service.convert;

import com.lzc.mindaispringboot.common.Dto.UserRegisterCommandDTO;
import com.lzc.mindaispringboot.entity.User;
import com.lzc.mindaispringboot.enumClass.UserStatus;
import com.lzc.mindaispringboot.response.UserLoginResponseDTO;

import java.time.LocalDateTime;

public class UserConvert {
    //响应DTO
    public static UserLoginResponseDTO.UserDetailResponseDTO entityToDetailResponse(User user){
        return UserLoginResponseDTO.UserDetailResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .phone(user.getPhone())
                .gender(user.getGender())
                .genderDisplayName(getGenderDisplayName(user.getGender()))
                .birthday(user.getBirthday())
                .userType(user.getUserType())
                .userTypeDisplayName(user.getUserTypeDisplayName())
                .status(user.getStatus())
                .statusDisplayName(user.getStatusDisplayName())
                .displayName(user.getDisplayName())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
    public static UserLoginResponseDTO entityToLoginResponse(String token,UserLoginResponseDTO.UserDetailResponseDTO userInfo){
        return UserLoginResponseDTO.builder()
                .token(token)
                .userInfo(userInfo)
                .roleType(userInfo.getUserType().toString())
                .build();
    }
    public static User RegisterCommandToEntity(UserRegisterCommandDTO userRegisterCommandDTO,String encodePassword){
        return User.builder()
                .username(userRegisterCommandDTO.getUsername())
                .email(userRegisterCommandDTO.getEmail())
                .password(encodePassword)
                .nickname(userRegisterCommandDTO.getNickname())
                .phone(userRegisterCommandDTO.getPhone())
                .gender(userRegisterCommandDTO.getGender())
                .birthday(userRegisterCommandDTO.getBirthday())
                .userType(userRegisterCommandDTO.getUserType())
                .status(UserStatus.NORMAL.getCode())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
    private static String getGenderDisplayName(Integer gender){
        if(gender == null){
            return "未知";
        }
        switch (gender){
            case 1:
                return "男";
                case 2:
                    return "女";
            default:
                return "未知";
        }
    }
}
