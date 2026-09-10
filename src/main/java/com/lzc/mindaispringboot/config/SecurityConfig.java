package com.lzc.mindaispringboot.config;

import cn.hutool.core.text.AntPathMatcher;
import com.lzc.mindaispringboot.util.JwtAuthticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private static final AntPathMatcher antPathMatcher = new AntPathMatcher();
    private static final String[] PUBLIC_MATCHERS = {
            "/",
            "/api/test",
            "/api/user/login",
            "/api/user/add",
    };
    public static Boolean isPublicPATH(String requestURI){
        for(String str:PUBLIC_MATCHERS){
            if(antPathMatcher.match(str,requestURI)){
                return true;
            }
        }
        return false;
    }
    @Bean
    public JwtAuthticationFilter jwtAuthticationFilter(){
        return new JwtAuthticationFilter();
    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity)throws Exception{
        //禁止CSRF保护（API服务通常不需要）
        httpSecurity.csrf(AbstractHttpConfigurer :: disable)
                //配置会话管理为无状态
                .sessionManagement(
                        session -> session.sessionCreationPolicy(
                                (SessionCreationPolicy.STATELESS)
                        ))
                .authorizeHttpRequests(auth ->auth
                //公开路径，无需登录即可访问
                                .requestMatchers(PUBLIC_MATCHERS).permitAll()
                        //其他请求都需要认证
                                .anyRequest().authenticated()
                )
                //添加JWT验证
                .addFilterBefore(jwtAuthticationFilter(), UsernamePasswordAuthenticationFilter.class);
        return httpSecurity.build();
    }
}
