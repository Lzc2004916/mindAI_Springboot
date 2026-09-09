package com.lzc.mindaispringboot.exception;

import lombok.Getter;

@Getter
public class BusionessException extends RuntimeException {
    private final String code;
    private final String message;
    private final Object data;
    public BusionessException(String message) {
        super(message);
        this.code = "BUSINESS_ERROR";
        this.message = message;
        this.data = null;
    }
}
