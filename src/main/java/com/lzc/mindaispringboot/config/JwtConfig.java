package com.lzc.mindaispringboot.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {
    /** HMAC256 签名密钥，只能由环境变量 JWT_SECRET 注入（JwtTokenUtil 签发/验签都用它）。 */
    private String secret;
    private long expiration;
    private long refreshExpiration;
    private String header;
    private String tokenPrefix;

    /**
     * 启动自检：密钥缺失或过弱 → 直接拒绝启动。
     *
     * 为什么必须有这一段：application.yml 原先写的是 ${JWT_SECRET:某个默认值}，而本机从未设置过
     * JWT_SECRET，于是应用一直用「仓库里人人可见」的默认密钥在签名 —— 任何拿到源码的人都能离线
     * 伪造一个 roleType=2 的 token 直接拿到管理员权限（安全审查 S-01，已实测复现）。
     * 现在把这种情况变成「启动即失败」，它就再也不可能静默发生。
     */
    @PostConstruct
    void validateSecret() {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "启动失败：环境变量 JWT_SECRET 未设置，拒绝以弱密钥启动。\n"
                    + "  生成：python -c \"import secrets; print(secrets.token_urlsafe(48))\"\n"
                    + "  然后设为用户环境变量（HKCU\\Environment），并在 IDEA 运行配置里也加一条，最后重启 IDE。");
        }
        if (secret.length() < 32) {
            throw new IllegalStateException(
                    "启动失败：JWT_SECRET 长度只有 " + secret.length() + " 位，至少需要 32 位。");
        }
    }
}
