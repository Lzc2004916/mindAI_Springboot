package com.lzc.mindaispringboot.Aop;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.lzc.mindaispringboot.exception.BusionessException;
import com.lzc.mindaispringboot.util.JwtTokenUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;


@Aspect
@Component
public class Token_Aspect {
    /** ThreadLocal 缓存当前请求解析出的 userId（请求结束必须清掉，否则线程复用会串号） */
    private static final ThreadLocal<Long> USER_ID_HOLDER = new ThreadLocal<>();

    /** 供 Controller 取当前登录用户：方法标 @GetToken，方法体内调 Token_Aspect.getUserId() */
    public static Long getUserId() {
        return USER_ID_HOLDER.get();
    }

    @Around("@annotation(com.lzc.mindaispringboot.Aop.GetToken)")
    public Object resolveToken(ProceedingJoinPoint joinPoint) throws Throwable {
        Long userId = currentUserId();//从安全认证上下文取出当前线程的userId
        if (userId == null) throw new BusionessException("未登录");
        try {
            USER_ID_HOLDER.set(userId);
            //调用目标方法
            return joinPoint.proceed();
        }finally {
            //调用完目标方法return后执行
            USER_ID_HOLDER.remove();
        }
    }
    public static Long currentUserId(){
        // ① 从 SecurityContextHolder 中取出当前线程绑定的认证信息
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // ② 判空 + 类型检查：principal 必须是 Long 类型（即 userId）
        if (auth == null || !(auth.getPrincipal() instanceof Long id)) return null;
        return id;
    }
}