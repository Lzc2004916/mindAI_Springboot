package com.lzc.mindaispringboot.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.lzc.mindaispringboot.config.JwtConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Date;
@Component
public class JwtTokenUtil implements ApplicationContextAware {
    private static ApplicationContext applicationContext;
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
                    .withIssuer(userId +"mental-health-assistant")//签发者
                    .sign(algorithm);//签名算法
            return token;
        }catch (Exception e){
            throw new RuntimeException("生成token失败：" + e);
        }
    }
}