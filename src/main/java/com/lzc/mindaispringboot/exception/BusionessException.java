package com.lzc.mindaispringboot.exception;

import com.lzc.mindaispringboot.common.ResultCode;
import lombok.Getter;

@Getter
public class BusionessException extends RuntimeException {
    private final String code;
    private final String message;
    private final Object data;
    public BusionessException(String message) {
        this(ResultCode.BUSINESS_ERROR.getCode(),message,null);
    }
    public BusionessException(String code, String message) {
        this (code,message,null);
    }
    public BusionessException(String code, String message, Object data) {
        super(message);
        this.code = code;
        this.message = message;
        this.data = data;
    }
}
