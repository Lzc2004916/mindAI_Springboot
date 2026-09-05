package com.lzc.mindaispringboot.common;

import lombok.Data;

import java.io.Serializable;
@Data
public class Result<T> implements Serializable {
    private String code;
    private String msg;
    private T data;
    public static <T> Result<T> success() {
        Result<T> result = new Result<T>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMsg(ResultCode.SUCCESS.getMessage());
        return result;
    }
    public static <T> Result<T> success(T data) {
       Result<T> result = success();
       result.setData(data);
        return result;
    }
    public static <T> Result<T> error(String code,String msg,T data) {
        Result result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        result.setData(data);
        return result;
    }
    public static <T> Result<T> error() {
        Result result = new Result<>();
        result.setCode(ResultCode.ERROR.getCode());
        result.setMsg(ResultCode.ERROR.getMessage());
        return result;
    }
}