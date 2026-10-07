package com.lzc.mindaispringboot.Aop;

import com.lzc.mindaispringboot.common.Result;
import com.lzc.mindaispringboot.common.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {
    private final Map<String, Window> counters = new ConcurrentHashMap<>();
    private record Window(long startAt,int count){}
    private static final long WINDOW_MS = 60_000L;
    // 每分钟清一次过期 key
    @Scheduled(fixedRate = WINDOW_MS)
    public void evictExpired(){
        long now = System.currentTimeMillis();
        counters.entrySet().removeIf(e -> now - e.getValue().startAt() > WINDOW_MS * 2);
    }
    private String resolveKey(HttpServletRequest request){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Long userId){
            return "u:" + userId + ":" + request.getRequestURI();
        }
        return "ip:" + request.getRemoteAddr() + ":" + request.getRequestURI();
    }
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //如果当前请求不是 Controller 方法（比如是静态资源 CSS/JS/图片），直接放行，不检查限流
      if (!(handler instanceof HandlerMethod method)) return true;
        RateLimit rateLimit = method.getMethodAnnotation(RateLimit.class);
        if (rateLimit == null) return true;
        long now = System.currentTimeMillis();
        String key = resolveKey(request);
        Window window = counters.compute(key,(k,old)->
                        (old == null || now - old.startAt() > WINDOW_MS
                                ? new Window(now, 1)
                                : new Window(old.startAt(),old.count() + 1))
                );
        if (window.count > rateLimit.qps()){
            response.setStatus(429);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(
                    new ObjectMapper().writeValueAsString(
                            Result.error(ResultCode.TOO_MANY_REQUESTS.getCode(),"请求过于频繁，请稍后再试",null)
                    )
            );
            return false;
        }
        return true;
    }
}