package com.clms.controller.user;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.clms.entity.base.ResponseEntity;
import com.clms.entity.bo.RefreshTokenBO;
import com.clms.entity.bo.UserLoginBO;
import com.clms.entity.dto.AccountRegisterDTO;
import com.clms.entity.dto.UserCredentialDTO;
import com.clms.service.IUserAuthService;

import cn.dev33.satoken.annotation.SaIgnore;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


/**
 * App端认证控制器
 * 
 * @author Liu
 * @since 1.0.0
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "用户认证接口")
@Validated
public class AuthController {
    
    @Resource
    private IUserAuthService userAuthService;
    
    @SaIgnore
    @Operation(summary = "刷新Token（无感刷新）")
    @GetMapping("/refreshToken")
    public RefreshTokenBO refreshToken(@RequestParam String refreshToken) {
        return userAuthService.refreshToken(refreshToken);
    }

    @SaIgnore
    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public ResponseEntity<UserLoginBO> register(@Valid @RequestBody AccountRegisterDTO registerDTO) {
        UserLoginBO result = userAuthService.userRegister(registerDTO);
        return ResponseEntity.ok(result);
    }

    @SaIgnore
    @Operation(summary = "用户登录（密码/验证码）")
    @PostMapping("/login")
    public ResponseEntity<UserLoginBO> login(@Valid @RequestBody UserCredentialDTO credentialDTO) {
        UserLoginBO result = userAuthService.userLogin(credentialDTO);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "用户登出")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        userAuthService.userLogout();
        return ResponseEntity.ok();
    }
    
    @SaIgnore
    @Operation(summary = "获取手机验证码")
    @GetMapping("/phone/code")
    public ResponseEntity<Void> getPhoneCode(@RequestParam String phone) {
        userAuthService.getPhoneCode(phone);
        return ResponseEntity.ok();
    }

    @SaIgnore
    @Operation(summary = "获取邮箱验证码")
    @GetMapping("/email/code")
    public ResponseEntity<Void> getEmailCode(@RequestParam String email) {
        userAuthService.getEmailCode(email);
        return ResponseEntity.ok();
    }

    @SaIgnore
    @Operation(summary = "验证验证码并获取重置密码令牌")
    @PostMapping("/verify-code")
    public ResponseEntity<String> verifyCode(
            @RequestParam String credential,
            @RequestParam String code) {
        String stepToken = userAuthService.verifyCodeAndGetStepToken(credential, code);
        return ResponseEntity.ok(stepToken);
    }

    @SaIgnore
    @Operation(summary = "重置密码")
    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(
            @RequestParam String stepToken,
            @RequestParam String newPassword) {
        userAuthService.resetPassword(stepToken, newPassword);
        return ResponseEntity.ok();
    }
    
}
