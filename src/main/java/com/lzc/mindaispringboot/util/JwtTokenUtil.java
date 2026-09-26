package com.lzc.mindaispringboot.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.lzc.mindaispringboot.config.JwtConfig;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Date;
@Component
public class JwtTokenUtil implements ApplicationContextAware {
    private static final String ISSUER = "L-Z-C";
    private static ApplicationContext applicationContext;
    //token验证结果封装类
    @Getter
    public static class TokenVerificationResult {
        private final Long userId;
        private final String username;
        private final Integer roleType;
        private final boolean token;

        public TokenVerificationResult(Long userId, String username, Integer roleType, boolean token) {
            this.userId = userId;
            this.username = username;
            this.roleType = roleType;
            this.token = token;
        }
    }
    @Override
    public void setApplicationContext(ApplicationContext applicationContext){
        JwtTokenUtil.applicationContext = applicationContext;
    }
    private static JwtConfig getJwtConfig(){
        return JwtTokenUtil.applicationContext.getBean(JwtConfig.class);
    }
    //生成token方法
    public static String generateToken(Long userId,String username,Integer roleType ) {
        try {
            JwtConfig jwtConfig = getJwtConfig();
            //生成签名算法
            Algorithm algorithm = Algorithm.HMAC256(jwtConfig.getSecret());
            //生成过期时间
            Date expiration = new Date(System.currentTimeMillis() + jwtConfig.getExpiration());
            String token = JWT.create()
                    .withClaim("userId", userId)
                    .withClaim("username", username)
                    .withClaim("roleType", roleType)
                    .withExpiresAt(expiration)//过期时间
                    .withIssuedAt(new Date())//签发时间
                    .withIssuer(ISSUER)//签发者
                    .sign(algorithm);//签名算法
            return token;
        }catch (Exception e){
            throw new RuntimeException("生成token失败：" + e);
        }
    }
    /**
     * 从请求头解析 token —— 全项目唯一的取值入口（过滤器与 AOP 都调它，别在别处再写一份）。
     *
     * 优先读 application.yml 里配置的请求头（jwt.header，默认 Authorization），并自动去掉前缀
     * （jwt.token-prefix，默认 "Bearer "）；读不到时兼容旧的 "token" 头（当前前端就是这个写法），
     * 所以前端不改也能用。
     *
     * 注意：这里只负责"把字符串取出来"，真正的合法性校验在 validateToken 里做。
     */
    public static String extractTokenFromRequest(HttpServletRequest request){
        if (request == null) return null;
        JwtConfig jwtConfig = getJwtConfig();

        // ① 优先读配置里指定的请求头
        String headerName = jwtConfig.getHeader();
        if (StringUtils.hasText(headerName)) {
            String header = request.getHeader(headerName);
            if (StringUtils.hasText(header)) {
                String prefix = jwtConfig.getTokenPrefix();
                if (prefix != null && !prefix.isEmpty() && header.startsWith(prefix)) {
                    header = header.substring(prefix.length()).trim();   // 去掉 "Bearer "
                }
                if (StringUtils.hasText(header)) return header;
            }
        }
        // ② 兼容旧的 token 头（前端统一成 Authorization 之后可以删掉这一段）
        String legacy = request.getHeader("token");
        return StringUtils.hasText(legacy) ? legacy : null;
    }
    //验证token
    public static TokenVerificationResult validateToken(String token){
        try {
            // 1. 先验签（签名 + issuer + 过期时间），拿到解码后的 JWT
            DecodedJWT jwt = verifyToken(token);
            if (jwt == null) return null;
            // 2. 从 claims 中提取用户信息
            Long userId = jwt.getClaim("userId").asLong();
            String username = jwt.getClaim("username").asString();
            Integer roleType = jwt.getClaim("roleType").asInt();
            // 4. 三个字段都有效才返回结果，否则返回 null
            if (userId != null && StringUtils.hasText(username) && roleType != null) {
                return new TokenVerificationResult(userId, username, roleType, true);
            }
            return null;
        }catch (Exception e){
            return null;
        }
    }
    // 底层验签：校验 token 的签名、签发者、过期时间
    public static DecodedJWT verifyToken(String token){
        // 空 token 直接拒绝
        if (!StringUtils.hasText(token)){
            throw new JWTVerificationException("Token不能为空");
        }
        JwtConfig jwtConfig = getJwtConfig();
        // 用配置的 secret 构建 HMAC256 算法
        Algorithm algorithm = Algorithm.HMAC256(jwtConfig.getSecret());
        // 构建验证器：验签名 + 验签发者
        JWTVerifier verifier = JWT.require(algorithm)
                .withIssuer(ISSUER)
                .build();
        // 执行验证，通过则返回 DecodedJWT，失败则抛异常
        return verifier.verify(token);
    }
}