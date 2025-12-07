package com.clms.handle;

import java.util.HashMap;
import java.util.Map;

import org.apache.coyote.BadRequestException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.HttpStatus;

import com.clms.entity.base.ResponseEntity;
import com.clms.exception.BusinessException;

import cn.dev33.satoken.exception.DisableServiceException;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import cn.dev33.satoken.exception.NotSafeException;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

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

        @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ResponseEntity<String> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException e) {
        log.debug("Method Not Allowed: {}", e.getLocalizedMessage());
        String message = "不支持的请求方法：" + e.getMethod();
        return ResponseEntity.error(405, message);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<String> handleNoResourceFoundException(NoResourceFoundException e) {
        log.debug("Resource Not Found: {}", e.getLocalizedMessage());
        String message = e.getMessage();
        return ResponseEntity.error(404, message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<String> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.debug("Bad request: {}", e.getLocalizedMessage());
        String message = e.getMessage();
        return ResponseEntity.error(400, message);
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<String> handleBadRequestException(BadRequestException e) {
        log.debug("Bad request: {}", e.getLocalizedMessage());
        String message = e.getMessage();
        return ResponseEntity.error(400, message);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<String> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        log.debug("Missing request parameter: {}", e.getLocalizedMessage());
        String message = "缺少必需参数: " + e.getParameterName();
        return ResponseEntity.error(400, message);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, String>> handleBodyValidException(MethodArgumentNotValidException exception) {
        Map<String, String> errorMsg = new HashMap<>();
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            errorMsg.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        String jsonMsg = JSONUtil.toJsonStr(errorMsg);
        log.debug("Invalid Parameter: {}", jsonMsg);
        return ResponseEntity.error(400, errorMsg);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, String>> handleConstraintViolationException(ConstraintViolationException exception) {
        Map<String, String> errorMsg = new HashMap<>();
        for (ConstraintViolation<?> violation : exception.getConstraintViolations()) {
            String propertyPath = violation.getPropertyPath().toString();
            // 从路径中提取参数名（去除方法名部分）
            String paramName = propertyPath.substring(propertyPath.lastIndexOf('.') + 1);
            errorMsg.put(paramName, violation.getMessage());
        }
        return ResponseEntity.error(400, errorMsg);
    }

    @ExceptionHandler(NotLoginException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<String> handlerNotLoginException(NotLoginException nle) {
        String message = "";
        if (nle.getType().equals(NotLoginException.NOT_TOKEN)) {
            message = "未提供Token";
        } else if (nle.getType().equals(NotLoginException.INVALID_TOKEN)) {
            message = "未提供有效的Token";
        } else if (nle.getType().equals(NotLoginException.TOKEN_TIMEOUT)) {
            message = "登录信息已过期，请重新登录";
        } else if (nle.getType().equals(NotLoginException.BE_REPLACED)) {
            message = "您的账户已在另一台设备上登录，如非本人操作，请立即修改密码";
        } else if (nle.getType().equals(NotLoginException.KICK_OUT)) {
            message = "已被系统强制下线";
        } else {
            message = "当前会话未登录";
        }
        return ResponseEntity.error(401, message);
    }

    @ExceptionHandler(NotRoleException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<String> handlerNotRoleException(NotRoleException e) {
        return ResponseEntity.error(403, "无此角色：" + e.getRole());
    }

    @ExceptionHandler(NotPermissionException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<String> handlerNotPermissionException(NotPermissionException e) {
        return ResponseEntity.error(403, "无此权限：" + e.getPermission());
    }

    @ExceptionHandler(DisableServiceException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<String> handlerDisableLoginException(DisableServiceException e) {
        return ResponseEntity.error(401, "账户被封禁：" + e.getDisableTime() + "秒后解封");
    }

    @ExceptionHandler(NotSafeException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<String> handlerNotSafeException(NotSafeException e) {
        return ResponseEntity.error(401, "二级认证异常：" + e.getMessage());
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public ResponseEntity<String> handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException e) {
        // 这是客户端错误，通常不需要上报到Sentry
        String message = "不支持的媒体类型：" + e.getContentType();
        return ResponseEntity.error(415, message);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<String> handleMissingRequestHeaderException(MissingRequestHeaderException e) {
        // 这是客户端错误，通常不需要上报到Sentry
        String message = "缺少必需的请求头：" + e.getHeaderName();
        return ResponseEntity.error(400, message);
    }

    @ExceptionHandler(org.springframework.web.multipart.MultipartException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<String> handleMultipartException(org.springframework.web.multipart.MultipartException e) {
        // 客户端未使用 multipart/form-data 格式上传文件，返回清晰错误提示
        log.warn("Multipart request parsing failed: {}", e.getMessage());
        String message = "文件上传格式错误，请使用 multipart/form-data 格式";
        return ResponseEntity.error(400, message);
    }


}
