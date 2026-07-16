package com.im.common.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class BusinessException extends RuntimeException {
    private Integer code;
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    // 新增单参构造，默认错误码 500
    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }
}