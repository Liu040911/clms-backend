package com.clms.handle;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.clms.entity.base.ResponseEntity;
import com.clms.exception.BusinessException;

import lombok.extern.slf4j.Slf4j;

/**
 * 基础全局异常处理器
 * 处理通用的异常类型，具体业务模块可以继承并扩展
 */
@Slf4j
@RestControllerAdvice
public class BaseGlobalExceptionAdvice {
    @ExceptionHandler(BusinessException.class)
    public org.springframework.http.ResponseEntity<ResponseEntity<String>> handleBusinessException(BusinessException e) {
        log.debug("Error code: {} Details: {}", e.getCode(), e.getLocalizedMessage());
        String message = e.getMessage();
        
        // 创建自定义ResponseEntity
        ResponseEntity<String> customResponse = 
            ResponseEntity.error(e.getCode(), message);
        
        // 根据业务错误码映射到HTTP状态码
        org.springframework.http.HttpStatus httpStatus;
        if (e.getCode() == 400) {
            httpStatus = org.springframework.http.HttpStatus.BAD_REQUEST;
        } else if (e.getCode() == 401) {
            httpStatus = org.springframework.http.HttpStatus.UNAUTHORIZED;
        } else if (e.getCode() == 403) {
            httpStatus = org.springframework.http.HttpStatus.FORBIDDEN;
        } else if (e.getCode() == 404) {
            httpStatus = org.springframework.http.HttpStatus.NOT_FOUND;
        } else if (e.getCode() == 409) {
            httpStatus = org.springframework.http.HttpStatus.CONFLICT;
        } else if (e.getCode() >= 400 && e.getCode() < 500) {
            httpStatus = org.springframework.http.HttpStatus.BAD_REQUEST;
        } else if (e.getCode() >= 500) {
            httpStatus = org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
        } else if (e.getCode() >= 300 && e.getCode() < 400) {
            // 3xx状态码映射到BAD_REQUEST，因为这通常表示客户端需要采取行动
            httpStatus = org.springframework.http.HttpStatus.BAD_REQUEST;
        } else {
            httpStatus = org.springframework.http.HttpStatus.OK;
        }
        
        // 返回标准Spring ResponseEntity并设置HTTP状态码
        return new org.springframework.http.ResponseEntity<>(customResponse, httpStatus);
    }
}
