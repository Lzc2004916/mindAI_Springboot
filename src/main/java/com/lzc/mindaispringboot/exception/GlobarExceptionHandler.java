package com.lzc.mindaispringboot.exception;

import com.lzc.mindaispringboot.common.Result;
import com.lzc.mindaispringboot.common.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobarExceptionHandler {
    //参数校验异常
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<String> handlerException(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError :: getDefaultMessage)
                .collect(Collectors.joining(","));
        return Result.error(ResultCode.PARAM_ERROR.getCode(),ResultCode.PARAM_ERROR.getMessage(),msg);
    }
//    处理业务异常
    @ExceptionHandler(BusionessException.class)
    public Result<?> handleBusinessException(BusionessException e) {
        if (e.getData() != null){
            return Result.error(e.getCode(),e.getMessage(),e.getData());
        }
        return Result.error(e.getCode(),e.getMessage(),null);
    }
    //处理文件大小限制
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Result<String> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        return Result.error(ResultCode.FILE_SIZE_EXCEEDED.getCode(),ResultCode.FILE_SIZE_EXCEEDED.getMessage(), "最大支持10MB");
    }
    // 处理 Spring Security 权限不足异常（如 @PreAuthorize 校验失败、角色不匹配等）
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Result<Void>> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("权限不足：{}", e.getMessage());
        // 返回 403 状态码，与业务层统一使用 AUTHORIZED_ERROR 错误码
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Result.error(ResultCode.AUTHORIZED_ERROR.getCode(),
                                   "没有权限访问该资源", null));
    }
    /// 兜底：任何没被上面几个 handler 接住的异常都走这里
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleException(Exception e) throws Exception {
        // 框架级错误响应（404/405/415 等实现了 ErrorResponse 的异常）不要吞，
        // 直接 rethrow 让 Spring 原生渲染标准错误响应
        if (e instanceof org.springframework.web.ErrorResponse) {
            throw e;
        }
        log.error("未处理异常：{}",e.getMessage(),e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.error(ResultCode.SYSTEM_ERROR.getCode(),ResultCode.SYSTEM_ERROR.getMessage(),null));
    }
}