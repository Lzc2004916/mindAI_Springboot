package com.lzc.mindaispringboot.util;

import com.lzc.mindaispringboot.common.ResultCode;
import com.lzc.mindaispringboot.config.SecurityConfig;
import com.lzc.mindaispringboot.enumClass.UserStatus;
import com.lzc.mindaispringboot.response.UserLoginResponseDTO;
import com.lzc.mindaispringboot.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

public class JwtAuthticationFilter extends OncePerRequestFilter {
    @Resource
    public UserService userService;
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String requestURI = request.getRequestURI();
        //检查是否为公开路径
        return SecurityConfig.isPublicPATH(requestURI);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 取值统一走 JwtTokenUtil（读 yml 配置的头 + 前缀，并兼容旧的 token 头）
        String token = JwtTokenUtil.extractTokenFromRequest(request);
        // 1. 无 token，拒绝
        if (!StringUtils.hasText(token)) {
            clearSecurityContext();
            ResponseUtil.writeResponse(response, ResultCode.ACCESS_UNAUTHORIZED);
            return;
        }
        // 说明：本项目采用「无状态登出」，不做 token 黑名单
        //      —— 登出由前端清掉本地 token 完成，服务端不让已签发的 token 失效（见指南第 3 章）
        // 2. 验证 token
        JwtTokenUtil.TokenVerificationResult TokenResult = JwtTokenUtil.validateToken(token);
        // 3. claims 残缺，拒绝
        if (TokenResult == null || !TokenResult.isToken()) {
            clearSecurityContext();
            ResponseUtil.writeResponse(response, ResultCode.TOKEN_INVALID);
            return;
        }
        // 4. 查用户，状态异常则拒绝
        //    这里必须 catch：getUserById 在"用户已被删除"时会抛 BusionessException，
        //    而过滤器在 DispatcherServlet 之前执行，全局异常处理器管不到它 ——
        //    不接住的话会变成 500，而不是干净的 401/403。
        UserLoginResponseDTO.UserDetailResponseDTO user;
        try {
            user = userService.getUserById(TokenResult.getUserId());
        } catch (Exception e) {
            user = null;
        }
        if (user == null || !UserStatus.NORMAL.getCode().equals(user.getStatus())) {
            clearSecurityContext();
            ResponseUtil.writeResponse(response, ResultCode.TOKEN_ACCESS_FORBIDDEN);
            return;
        }

        // 5. 认证通过，设置 Spring Security 上下文
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                TokenResult.getUsername(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + TokenResult.getRoleType()))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }
    //清理spring security上下文
    private void clearSecurityContext(){
        SecurityContextHolder.clearContext();
    }

}