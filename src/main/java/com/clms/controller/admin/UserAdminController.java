package com.clms.controller.admin;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clms.entity.bo.AdminLoginBO;
import com.clms.entity.dto.UserCredentialDTO;
import com.clms.service.IUserAuthService;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaIgnore;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/admin/user")
@Tag(name = "用户管理接口")
@SaCheckLogin
@Validated
public class UserAdminController {
    @Resource
    private IUserAuthService userAuthService;

    @SaIgnore
    @Operation(summary = "管理员登录（邮箱或手机号+密码）")
    @PostMapping("/login")
    public AdminLoginBO login(@RequestBody UserCredentialDTO userCredentialDTO) {
        return userAuthService.adminLogin(userCredentialDTO);
    }
}
