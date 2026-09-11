package com.lzc.mindaispringboot.util;

import cn.hutool.json.JSONUtil;
import com.lzc.mindaispringboot.common.Result;
import com.lzc.mindaispringboot.common.ResultCode;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

public class ResponseUtil {
    // 过滤器中的异常响应：鉴权失败时直接写入 JSON 错误信息，不交由全局异常处理器
    public static void writeResponse(HttpServletResponse response, ResultCode resultCode){
        // 1. 根据不同的 ResultCode 映射对应的 HTTP 状态码
        int status = switch (resultCode){
            // 401：未登录 / token 无效 / 过期 / 被拉黑
            case UNAUTHORIZED,ACCESS_UNAUTHORIZED,TOKEN_INVALID,TOKEN_EXPIRED,TOKEN_BLOCKED ->
                HttpStatus.UNAUTHORIZED.value();

            // 403：token 有效但无权限访问该资源
            case TOKEN_ACCESS_FORBIDDEN ->
                HttpStatus.FORBIDDEN.value();

            // 其他异常一律返回 400
            default -> HttpStatus.BAD_REQUEST.value();
        };

        // 2. 设置响应头
        response.setStatus(status);                                         // HTTP 状态码
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);          // Content-Type: application/json
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());       // 编码: UTF-8

        // 3. 写入 JSON 错误信息到响应体
        try(PrintWriter writer = response.getWriter()) {                    // try-with-resources 自动关闭流
            String jsonStr = JSONUtil.toJsonStr(Result.error(resultCode.getCode(), resultCode.getMessage(), null));
            writer.print(jsonStr);                                          // 写入 JSON
            writer.flush();                                                 // 强制刷出，确保客户端收到
        }catch (IOException e){
            System.out.println("写入响应失败：" + e.getMessage());
        }
    }
}