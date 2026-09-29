package com.lzc.mindaispringboot.security;

import com.lzc.mindaispringboot.enumClass.UserType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuthUtil {
    public static boolean isAdmin(){
        // 判断当前请求是否为管理员，是管理员返回 true，否则返回 false
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
         for (GrantedAuthority a : auth.getAuthorities()) {
            if(UserType.ROLE_ADMIN.equals(a.getAuthority())) return true;
        }
         return false;
    }
}