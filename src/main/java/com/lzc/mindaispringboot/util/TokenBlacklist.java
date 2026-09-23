package com.lzc.mindaispringboot.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
public class TokenBlacklist {
    private static final Map<String,Long> BLACKLIST = new ConcurrentHashMap<>();
    /// token无法解析过期时间的兜底保留时长24小时
    private static final long DEFAULT_KEEP_MILLIS = 24 * 60 * 60 * 1000L;
    /// 加入黑名单
    public static void add(String token){
        if (token == null || token.isBlank()) return;
        Long exp = extractExpiration(token);
        BLACKLIST.put(token,exp == null ? System.currentTimeMillis() + DEFAULT_KEEP_MILLIS : exp);
        cleanExpired();
    }
    public static boolean contains(String token){
        if (token == null || token.isBlank()) return false;
        Long exp = BLACKLIST.get(token);
        if (exp == null) return false;
        /// 已过期的token本来验证不过，直接当不在黑名单，省一次判断
        if (exp < System.currentTimeMillis()){
            BLACKLIST.remove(token);
            return false;
        }
        return true;
    }
    /// 清理已过期的记录
    public static void cleanExpired(){
        long now = System.currentTimeMillis();
        ///entrySet().iterator()转成Set集合，然后遍历元素
        Iterator<Map.Entry<String, Long>> it = BLACKLIST.entrySet().iterator();
        ///hasNext检查迭代器还有没有下一个元素，有就循环没有就结束
        while (it.hasNext()){
            /// next取出下一个元素的value
            if (it.next().getValue() < now) it.remove();
        }
    }
    private static Long extractExpiration(String token){
        try {
            DecodedJWT jwt = JWT.decode(token);
            Date expiresAt = jwt.getExpiresAt();
            /// 普通解码，获取token中的过期时间
            return expiresAt == null ? null : expiresAt.getTime();
        }catch (Exception e){
            return null;
        }
    }
}