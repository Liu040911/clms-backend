package com.clms.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.clms.entity.base.ResponseEntity;
import com.clms.service.IAppConfigService;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.json.JSONObject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;

@RestController
@Tag(name = "应用配置接口")
@RequestMapping("/app/config")
@SaCheckLogin
public class AppConfigController {

    @Resource
    private IAppConfigService appConfigService;

    
    @Operation(summary = "获取配置")
    @GetMapping("/get")
    public ResponseEntity<JSONObject> getConfigValue(
            @RequestParam(required = false) String key) {
        return ResponseEntity.ok(appConfigService.getConfig(key));
    }

    @Operation(summary = "获取管理面板路由配置")
    @GetMapping("/get/manager")
    @SaCheckLogin
    public JSONObject getManagerConfigValue() {
        String userId = (String) StpUtil.getTokenInfo().getLoginId();
        return appConfigService.getManagerConfig(userId);
    }
}
