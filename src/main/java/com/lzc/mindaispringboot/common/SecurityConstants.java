package com.lzc.mindaispringboot.common;

import com.lzc.mindaispringboot.enumClass.UserType;

public final class SecurityConstants {
    private SecurityConstants(){}
    public static final String ROLE_PREFIX = "ROLE_";
    public static String role(Integer userType){
        return ROLE_PREFIX + userType;
    }
    public static final String ROLE_ADMIN = role(UserType.ADMIN.getCode());
}
