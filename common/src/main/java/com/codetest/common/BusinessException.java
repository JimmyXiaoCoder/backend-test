package com.codetest.common;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final Integer code;
    private final String message;

    public BusinessException(ResultCode code) {
        super(code.getMessage());
        this.code = code.getCode();
        this.message = code.getMessage();
    }

    public BusinessException(ResultCode code, String customMessage) {
        super(customMessage);
        this.code = code.getCode();
        this.message = customMessage;
    }

    public BusinessException(String customMessage) {
        super(customMessage);
        this.code = ResultCode.INTERNAL_ERROR.getCode();
        this.message = customMessage;
    }
}
