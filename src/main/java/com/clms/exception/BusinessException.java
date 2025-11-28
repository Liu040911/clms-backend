package com.clms.exception;

import com.clms.enums.ResponseCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(ResponseCode resp) {
        super(resp.getMessage());
        this.code = resp.getCode();
    }

    public BusinessException(ResponseCode resp,String message) {
        super(message);
        this.code = resp.getCode();
    }

    public BusinessException(String msg) {
        super(msg);
        this.code = ResponseCode.FAILURE.getCode();
    }
}
