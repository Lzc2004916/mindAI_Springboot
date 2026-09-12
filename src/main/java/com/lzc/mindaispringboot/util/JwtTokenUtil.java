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

    //获取token
    public static String extractTokenFromRequest(HttpServletRequest request){
        if (request == null) return null;
        String token = request.getHeader("token");   // 从请求头取 token
        if (StringUtils.hasText(token)) {             // token 不为 null，不是空串，不是纯空格
            return token;                             // 有效，返回
        }
        return null;                                  // 无效，返回 null
    }
    //两种获取token方式(附加知识点)
//    public static String getCurrentToken() {
//        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
//        if (attributes != null){
//            HttpServletRequest request = attributes.getRequest();
//            String jwtToken = (String) request.getAttribute("jwtToken");
//            if (jwtToken != null) {
//                return jwtToken;
//            }
//            String headerToken = extractTokenFromRequest(request);
//            return headerToken;
//        }
//        return null;
//    }
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