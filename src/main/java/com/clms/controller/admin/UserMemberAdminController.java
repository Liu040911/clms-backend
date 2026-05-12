package com.clms.controller.admin;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clms.entity.base.ResponseEntity;
import com.clms.entity.bo.UserInfoBO;
import com.clms.entity.dto.UserMemberEditDTO;
import com.clms.service.IUserMemberService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckDisable;
import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/admin/member")
@Tag(name = "用户成员管理接口")
@SaCheckLogin
@SaCheckDisable("admin")
@Validated
public class UserMemberAdminController {

    @Resource
    private IUserMemberService userMemberService;

    @Operation(summary = "获取用户列表（排除管理员）")
    @SaCheckPermission("user:list")
    @GetMapping("/list")
    public ResponseEntity<Page<UserInfoBO>> getMemberList(
            @RequestParam("pageNum") Long pageNum,
            @RequestParam("pageSize") Long pageSize) {
        return ResponseEntity.ok(userMemberService.getMemberList(pageNum, pageSize));
    }

    @Operation(summary = "编辑用户信息")
    @SaCheckPermission("user:edit")
    @PostMapping("/edit")
    public ResponseEntity<Void> editMemberInfo(@Valid @RequestBody UserMemberEditDTO dto) {
        userMemberService.editMemberInfo(dto);
        return ResponseEntity.ok();
    }

    @Operation(summary = "启用用户")
    @SaCheckPermission("user:enable")
    @PostMapping("/enable")
    public ResponseEntity<Void> enableMember(@RequestParam("userId") String userId) {
        userMemberService.enableMember(userId);
        return ResponseEntity.ok();
    }

    @Operation(summary = "停用用户")
    @SaCheckPermission("user:disable")
    @PostMapping("/disable")
    public ResponseEntity<Void> disableMember(@RequestParam("userId") String userId) {
        userMemberService.disableMember(userId);
        return ResponseEntity.ok();
    }
}
