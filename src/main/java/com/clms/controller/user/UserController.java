package com.clms.controller.user;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clms.entity.base.ResponseEntity;
import com.clms.entity.bo.UserInfoBO;
import com.clms.service.IUserAccountService;
import com.clms.service.IUserAuthService;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;

/**
 * 用户控制器
 * 
 * @author Liu
 * @since 1.0.0
 */
@RestController
@RequestMapping("/user")
@SaCheckLogin
@Tag(name = "用户接口")
public class UserController {
    
    @Resource
    private IUserAccountService userAccountService;
    
    @Operation(summary = "获取当前登录用户信息")
    @GetMapping("/info")
    public ResponseEntity<UserInfoBO> getUserInfo() {
        String userId = (String) StpUtil.getTokenInfo().getLoginId();
        UserInfoBO userInfo = userAccountService.getCurrentUserInfo(userId);
        return ResponseEntity.ok(userInfo);
    }
}
