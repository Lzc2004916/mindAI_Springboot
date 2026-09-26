package com.lzc.mindaispringboot.Aop;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.lzc.mindaispringboot.util.JwtTokenUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class Token_Aspect {
    private static final ThreadLocal<Long> USER_ID_HOLDER = new ThreadLocal<>();
    public static Long getUserId() {
        return USER_ID_HOLDER.get();
    }
    @Around("@annotation(com.lzc.mindaispringboot.Aop.GetToken)")
    public Object resolveToken(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = getCurrentRequest();
        String token = request.getHeader("token");
        if (!StringUtils.hasText(token)) return null;
        DecodedJWT jwt = JwtTokenUtil.verifyToken(token);
        Long userId = jwt.getClaim("userId").asLong();
        try {
            USER_ID_HOLDER.set(userId);
            return joinPoint.proceed();
        }finally {
            USER_ID_HOLDER.remove();
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