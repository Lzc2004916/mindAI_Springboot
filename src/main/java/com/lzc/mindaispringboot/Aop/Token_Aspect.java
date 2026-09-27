package com.lzc.mindaispringboot.Aop;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.lzc.mindaispringboot.exception.BusionessException;
import com.lzc.mindaispringboot.util.JwtTokenUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

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
        HttpServletRequest request = getCurrentRequest();
        String token = JwtTokenUtil.extractTokenFromRequest(request);
        if (!StringUtils.hasText(token)) {
            throw new BusionessException("未登录");
        }
        DecodedJWT jwt = JwtTokenUtil.verifyToken(token);
        Long userId = jwt.getClaim("userId").asLong();
        try {
            USER_ID_HOLDER.set(userId);
            return joinPoint.proceed();
        } finally {
            USER_ID_HOLDER.remove();   // 用完必须清理
        }
    }

    private HttpServletRequest getCurrentRequest(){
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new IllegalStateException("当前线程没有绑定上下文");
        }
        return attributes.getRequest();
    }
}
