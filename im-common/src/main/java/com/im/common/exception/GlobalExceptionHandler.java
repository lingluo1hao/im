package com.im.common.exception;


import com.im.common.result.R;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // 自定义业务异常
    @ExceptionHandler(BusinessException.class)
    public R<?> businessException(BusinessException e) {
        return R.fail(e.getCode(), e.getMessage());
    }

    // 参数校验异常
    @ExceptionHandler(ConstraintViolationException.class)
    public R<?> validException(ConstraintViolationException e) {
        String msg = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(","));
        return R.fail(400, msg);
    }

    // 系统未知异常
    @ExceptionHandler(Exception.class)
    public R<?> exception(Exception e) {
        log.error("系统异常: {}", e.getMessage());
        return R.fail(500, "服务器内部异常");
    }
}
