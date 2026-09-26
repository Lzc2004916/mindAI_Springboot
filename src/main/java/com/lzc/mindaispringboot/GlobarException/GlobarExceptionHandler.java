package com.lzc.mindaispringboot.GlobarException;

import com.lzc.mindaispringboot.common.Result;
import com.lzc.mindaispringboot.common.ResultCode;
import com.lzc.mindaispringboot.exception.BusionessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
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

    /**
     * 权限不足（`@PreAuthorize("hasRole('2')")` 不通过时，方法级鉴权会抛 `AuthorizationDeniedException`，
     * 它是 `AccessDeniedException` 的子类，所以这里处理父类即可全覆盖）。
     *
     * ⚠️ 必须要单独处理：否则它会落到下面的 `Exception` 兜底里，被当成"系统异常"返回
     *    **HTTP 200 + code=500「系统错误」**，前端就会把"没权限"显示成"系统错误"，
     *    而且拦截器里那个「403 → 没有权限访问」的分支永远走不到。
     *
     * 这里用 `ResponseEntity` 显式指定 403 —— 其余 handler 保持原来「HTTP 200 + 业务 code」的风格不动。
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Result<Void>> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("权限不足：{}", e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Result.error(ResultCode.AUTHORIZED_ERROR.getCode(),
                                   "没有权限访问该资源", null));
    }

    /**
     * 兜底：所有未被上面接住的异常，都转成统一的 Result 结构，
     * 免得前端拿到 Spring 默认的错误 JSON（它的字段名是 timestamp/status/error，没有 code）。
     *
     * 两个关键点：
     * ① 完整堆栈只写日志，**不要把 e.getMessage() 返回给前端** ——
     *    数据库异常的消息里可能带表名、SQL、字段名，属于信息泄露。
     * ② `ErrorResponse` 是 Spring 自己的"带 HTTP 状态码的异常"（如 404 找不到路径、
     *    405 方法不支持、400 请求体解析失败）。这些直接原样抛出交给 Spring 处理，
     *    保留正确的状态码；否则所有打错的 URL 都会变成"系统错误 500"，反而难排查。
     */
    @ExceptionHandler(Exception.class)
    public Object handleException(Exception e) throws Exception {
        if (e instanceof ErrorResponse) {
            throw e;   // 交由 Spring 按它自带的状态码处理（404/405/400 等）
        }
        log.error("系统异常", e);
        return Result.error(ResultCode.SYSTEM_ERROR.getCode(),
                            ResultCode.SYSTEM_ERROR.getMessage(), null);
    }
}
