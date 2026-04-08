package com.clms.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.clms.entity.base.ResponseEntity;
import com.clms.utils.QiNiuUtils;

import cn.dev33.satoken.annotation.SaCheckLogin;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;


@RestController
@RequestMapping("/api")
@Tag(name = "工具类API")
@SaCheckLogin
public class CommonController {
    private List<String> safeFileTypes = List.of("image/jpeg", "image/png", "image/gif", "image/bmp", "image/webp");
    private List<String> safeVideoTypes = List.of("video/mp4", "video/*");
    private List<String> safeAudioTypes = List.of("audio/wav", "audio/mp3", "audio/aac", "audio/ogg", "audio/flac", "audio/m4a", "audio/*");

    @Autowired
    private QiNiuUtils qiNiuUtils;

    @GetMapping("/upload/token")
    @Operation(summary = "获取上传凭证", responses = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "401", description = "未登录"),
            @ApiResponse(responseCode = "403", description = "无权限")
    })
    public ResponseEntity<String> getUploadToken(@RequestParam(required = false) String key) {
        return ResponseEntity.ok(qiNiuUtils.getUploadToken(key));
    }
}
