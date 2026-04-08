package com.clms.controller.admin;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clms.entity.base.ResponseEntity;
import com.clms.entity.bo.AdminLoginBO;
import com.clms.entity.bo.UserInfoBO;
import com.clms.entity.dto.UserCredentialDTO;
import com.clms.entity.dto.UserAdminDTO;
import com.clms.entity.dto.UserAdminEditDTO;
import com.clms.service.IUserAdminService;
import com.clms.service.IUserAuthService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckDisable;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaIgnore;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/admin")
@Tag(name = "用户管理接口")
@SaCheckLogin
@SaCheckDisable("admin")
@Validated
public class UserAdminController {
    @Resource
    private IUserAuthService userAuthService;

    @Resource
    private IUserAdminService userAdminService;

    @SaIgnore
    @Operation(summary = "管理员登录（邮箱或手机号+密码）")
    @PostMapping("/login")
    public AdminLoginBO login(@RequestBody UserCredentialDTO userCredentialDTO) {
        return userAuthService.adminLogin(userCredentialDTO);
    }

    @Operation(summary = "新增管理员")
    @SaCheckPermission("admin:addAdmin")
    @PostMapping("/addAdmin")
    public ResponseEntity<Void> addAdmin(@Valid @RequestBody UserAdminDTO userAdminDTO) {
        userAdminService.addAdmin(userAdminDTO);
        return ResponseEntity.ok();
    }

    @Operation(summary = "停用管理员权限")
    @SaCheckPermission("admin:disableAdmin")
    @PostMapping("/disableAdminPermission")
    public ResponseEntity<Void> disableAdminPermission(@RequestParam("userId") String userId) {
        userAdminService.disableAdminPermission(userId);
        return ResponseEntity.ok();
    }

    @Operation(summary = "启用管理员权限")
    @SaCheckPermission("admin:enableAdmin")
    @PostMapping("/enableAdminPermission")
    public ResponseEntity<Void> enableAdminPermission(@RequestParam("userId") String userId) {
        userAdminService.enableAdminPermission(userId);
        return ResponseEntity.ok();
    }

    @Operation(summary = "编辑管理员信息")
    @PostMapping("/editAdminInfo")
    public ResponseEntity<Void> editAdminInfo(@Valid @RequestBody UserAdminEditDTO userAdminEditDTO) {
        userAdminService.editAdminInfo(userAdminEditDTO);
        return ResponseEntity.ok();
    }

    @Operation(summary = "获取管理员列表")
    @SaCheckPermission("admin:getAdminList")
    @GetMapping("/getAdminList")
    public ResponseEntity<Page<UserInfoBO>> getAdminList(
            @RequestParam("pageNum") Long pageNum,
            @RequestParam("pageSize") Long pageSize) {
        return ResponseEntity.ok(userAdminService.getAdminList(pageNum, pageSize));
    }

    
}
