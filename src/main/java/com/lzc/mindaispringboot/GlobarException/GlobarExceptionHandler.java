package com.lzc.mindaispringboot.GlobarException;

import com.lzc.mindaispringboot.common.Result;
import com.lzc.mindaispringboot.common.ResultCode;
import com.lzc.mindaispringboot.exception.BusionessException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.stream.Collectors;

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
}
