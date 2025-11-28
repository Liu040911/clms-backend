package com.clms.entity.base;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "返回实体类")
public class ResponseEntity<T> {
    @Schema(description = "返回码")
    private int code;
    @Schema(description = "返回信息, 无返回数据或报错则显示此信息")
    private String msg;
    @Schema(description = "返回数据, 无返回信息则为null")
    private T data;

    public static <T> ResponseEntity<T> ok() {
        return new ResponseEntity<T>().code(200).msg("OK");
    }

    public static <T> ResponseEntity<T> ok(T data) {
        if (data == null) {
            return ok();
        }
        return new ResponseEntity<T>().code(200).data(data);
    }

    public static <T> ResponseEntity<T> error(int code) {
        return new ResponseEntity<T>().code(code);
    }

    public static <T> ResponseEntity<T> error(int code, T data) {
        return new ResponseEntity<T>().code(code).data(data);
    }

    public static <T> ResponseEntity<T> error(int code, String msg) {
        return new ResponseEntity<T>().code(code).data(null).msg(msg);
    }

    public static <T> ResponseEntity<T> badRequest() {
        return new ResponseEntity<T>().code(400).msg("Bad Request");
    }

    public static <T> ResponseEntity<T> unauthorized() {
        return new ResponseEntity<T>().code(401).msg("Unauthorized");
    }

    public static <T> ResponseEntity<T> forbidden() {
        return new ResponseEntity<T>().code(403).msg("Forbidden");
    }

    public static <T> ResponseEntity<T> notFound() {
        return new ResponseEntity<T>().code(404).msg("Not Found");
    }

    public static <T> ResponseEntity<T> serverError() {
        return new ResponseEntity<T>().code(500).msg("Server Error");
    }

    public static <T> ResponseEntity<T> serviceUnavailable() {
        return new ResponseEntity<T>().code(503).msg("Service Unavailable");
    }

    private ResponseEntity<T> code(int code) {
        this.code = code;
        return this;
    }

    private ResponseEntity<T> msg(String msg) {
        this.msg = msg;
        return this;
    }

    private ResponseEntity<T> data(T data) {
        this.data = data;
        return this;
    }
}
