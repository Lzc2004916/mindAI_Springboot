package com.lzc.mindaispringboot.common;

import lombok.Data;

import java.io.Serializable;
@Data
public class Result<T> implements Serializable {
    private String code;
    private String msg;
    private T data;
    /// 是否成功
    private Boolean success;
    /// 提示信息
    private String message;
    public static <T> Result<T> success() {
        Result<T> result = new Result<T>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMsg(ResultCode.SUCCESS.getMessage());
        result.setMessage(ResultCode.SUCCESS.getMessage());
        result.setSuccess(true);
        return result;
    }
    public static <T> Result<T> success(T data) {
        Result<T> result = success();
        result.setData(data);
        return result;
    }

    public static <T> Result<T> error(String code, String msg, T data) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        result.setMessage(msg);
        result.setSuccess(false);
        result.setData(data);
        return result;
    }
    public static <T> Result<T> error() {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.ERROR.getCode());
        result.setMsg(ResultCode.ERROR.getMessage());
        result.setMessage(ResultCode.ERROR.getMessage());
        result.setSuccess(false);
        return result;
    }
}