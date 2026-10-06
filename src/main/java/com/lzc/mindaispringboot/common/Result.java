package com.lzc.mindaispringboot.common;

import lombok.Data;

import java.io.Serializable;
@Data
public class Result<T> implements Serializable {
    private String code;
    private String msg;
    private T data;
    private Boolean success;
    public static <T> Result<T> success() {
        Result<T> result = new Result<T>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMsg(ResultCode.SUCCESS.getMessage());
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
        result.setSuccess(false);
        result.setData(data);
        return result;
    }
    public static <T> Result<T> error(String msg) {
        return error(ResultCode.ERROR.getCode(), msg, null);
    }
    public static <T> Result<T> error() {
        return error(ResultCode.ERROR.getCode(),ResultCode.ERROR.getMessage(),null);
    }
}