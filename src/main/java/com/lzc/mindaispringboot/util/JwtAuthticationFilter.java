package com.lzc.mindaispringboot.util;

import cn.hutool.json.JSONUtil;
import com.lzc.mindaispringboot.common.ResultCode;
import com.lzc.mindaispringboot.config.SecurityConfig;
import com.lzc.mindaispringboot.entity.User;
import com.lzc.mindaispringboot.enumClass.UserStatus;
import com.lzc.mindaispringboot.response.UserLoginResponseDTO;
import com.lzc.mindaispringboot.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

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
        String token = JwtTokenUtil.extractTokenFromRequest(request);
        if (StringUtils.hasText(token)){
            // 验证 token：验签失败会抛异常，claims 不全返回 null
            try {
                JwtTokenUtil.TokenVerificationResult validationResult = JwtTokenUtil.validateToken(token);
                if (validationResult != null && validationResult.isToken()) {
                    UserLoginResponseDTO.UserDetailResponseDTO user = userService.getUserById(validationResult.getUserId());
                    if (user != null && UserStatus.NORMAL.getCode().equals(user.getStatus())) {
                        //创建Spring Security认证对象
                        List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                                new SimpleGrantedAuthority("ROLE_" + validationResult.getRoleType())
                        );
                        //创建UsernamePasswordAuthenticationToken
                        UsernamePasswordAuthenticationToken authcation = new UsernamePasswordAuthenticationToken(
                                validationResult.getUsername(),
                                null,
                                authorities
                        );
                        //设置认证信息到spring securtity上下文
                        SecurityContextHolder.getContext().setAuthentication(authcation);
                        //将token存储到请求属性中
//                        request.setAttribute("jwtToken", token);
                    }else {
                        clearSecurityContext();
                        ResponseUtil.writeResponse(response,ResultCode.TOKEN_ACCESS_FORBIDDEN);
                    }
                } else {
                    // claims 残缺，token 无效
                    clearSecurityContext();
                    ResponseUtil.writeResponse(response, ResultCode.TOKEN_INVALID);
                    return;
                }
            } catch (Exception e) {
                // 验签失败（签名不对 / 过期 / issuer 不匹配）
                clearSecurityContext();
                ResponseUtil.writeResponse(response, ResultCode.TOKEN_INVALID);
                return;
            }
        }else {
            //token异常处理
            clearSecurityContext();
            ResponseUtil.writeResponse(response, ResultCode.ACCESS_UNAUTHORIZED);
            return;
        }
        filterChain.doFilter(request,response);
    }
    //清理spring security上下文
    private void clearSecurityContext(){
        SecurityContextHolder.clearContext();
    }

}