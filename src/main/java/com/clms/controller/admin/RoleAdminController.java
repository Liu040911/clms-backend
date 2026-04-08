package com.clms.controller.admin;

import java.util.List;
import java.util.Set;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.clms.entity.base.ResponseEntity;
import com.clms.entity.bo.PermissionBO;
import com.clms.entity.bo.PermissionModuleBO;
import com.clms.entity.bo.RoleBO;
import com.clms.entity.dto.BatchPermissionUpdateDTO;
import com.clms.entity.dto.RoleDTO;
import com.clms.service.IRoleService;

import cn.dev33.satoken.annotation.SaCheckDisable;
import cn.dev33.satoken.annotation.SaCheckLogin;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/admin/role/")
@SaCheckDisable("admin")
@SaCheckLogin
@Tag(name = "角色管理")
@Validated
public class RoleAdminController {

    @Resource
    private IRoleService roleService;

    @Operation(summary = "创建角色")
    // @SaCheckPermission("role:create")
    @PostMapping("create")
    public ResponseEntity<Void> createRole(@RequestBody @Valid RoleDTO roleDTO) {
        roleService.createRole(roleDTO);
        return ResponseEntity.ok();
    }

    @Operation(summary = "修改角色")
    // @SaCheckPermission("role:update")
    @PostMapping("update")
    public ResponseEntity<Void> updateRole(
            @RequestParam @NotBlank String roleId,
            @RequestBody @Valid RoleDTO roleDTO) {
        roleService.updateRole(roleId, roleDTO);
        return ResponseEntity.ok();
    }

    @Operation(summary = "删除角色")
    // @SaCheckPermission("role:delete")
    @PostMapping("delete")
    public ResponseEntity<Void> deleteRole(@RequestParam @NotBlank String roleId) {
        roleService.deleteRole(roleId);
        return ResponseEntity.ok();
    }

    @Operation(summary = "获取角色信息")
    // @SaCheckPermission("role:info")
    @GetMapping("info")
    public ResponseEntity<RoleBO> getRole(@RequestParam @NotBlank String roleId) {
        return ResponseEntity.ok(roleService.getRole(roleId));
    }

    @Operation(summary = "获取角色列表")
    // @SaCheckPermission("role:list")
    @GetMapping("list")
    public ResponseEntity<Page<RoleBO>> getRoleList(
            @RequestParam(required = false) String roleName,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String order) {
        return ResponseEntity.ok(roleService.getRoleList(roleName, page, size, sort, order));
    }

    @Operation(summary = "为角色绑定权限")
    // @SaCheckPermission("role:bindPermission")
    @PostMapping("bindPermission")
    public ResponseEntity<Void> bindPermissionToRole(
            @RequestParam @NotBlank String roleId,
            @RequestBody List<String> permissionIds) {
        roleService.bindPermissionToRole(roleId, permissionIds);
        return ResponseEntity.ok();
    }

    @Operation(summary = "解除角色权限绑定")
    // @SaCheckPermission("role:unbindPermission")
    @PostMapping("unbindPermission")
    public ResponseEntity<Void> unbindPermissionFromRole(
            @RequestParam @NotBlank String roleId,
            @RequestBody List<String> permissionIds) {
        roleService.unbindPermissionFromRole(roleId, permissionIds);
        return ResponseEntity.ok();
    }

    @Operation(summary = "获取指定角色的权限字符串列表")
    // @SaCheckPermission("role:permissionList")
    @GetMapping("permissionList")
    public ResponseEntity<List<PermissionBO>> getPermissionStringList(@RequestParam @NotBlank String roleId) {
        return ResponseEntity.ok(roleService.getPermissionList(roleId));
    }

    @Operation(summary = "获取指定角色的权限模块分组列表")
    // @SaCheckPermission("role:permissionList")
    @GetMapping("permissionModuleList")
    public ResponseEntity<List<PermissionModuleBO>> getPermissionModuleList(
            @RequestParam @NotBlank String roleId) {
        return ResponseEntity.ok(roleService.getPermissionModuleList(roleId));
    }

    @Operation(summary = "获取指定角色的权限模块集合")
    // @SaCheckPermission("role:permissionList")
    @GetMapping("permissionModules")
    public ResponseEntity<Set<String>> getPermissionModules(
            @RequestParam @NotBlank String roleId) {
        return ResponseEntity.ok(roleService.getPermissionModules(roleId));
    }

    @Operation(summary = "获取全模块全权限点聚合列表")
    // @SaCheckPermission("role:permissionList")
    @GetMapping("allPermissionModules")
    public ResponseEntity<List<PermissionModuleBO>> getAllPermissionModules() {
        return ResponseEntity.ok(roleService.getAllPermissionModules());
    }


    @Operation(summary = "批量更新角色权限")
    // @SaCheckPermission(value = {"role:bindPermission", "role:unbindPermission"}, mode = SaMode.AND)
    @PostMapping("batchUpdatePermissions")
    @Transactional
    public ResponseEntity<String> batchUpdateRolePermissions(
            @RequestParam @NotBlank String roleId,
            @RequestBody BatchPermissionUpdateDTO permissionUpdateDTO) {
        
        // 先解绑需要移除的权限
        if (permissionUpdateDTO.getPermissionsToUnbind() != null && !permissionUpdateDTO.getPermissionsToUnbind().isEmpty()) {
            roleService.unbindPermissionFromRole(roleId, permissionUpdateDTO.getPermissionsToUnbind());
        }
        
        // 再绑定需要添加的权限
        if (permissionUpdateDTO.getPermissionsToBind() != null && !permissionUpdateDTO.getPermissionsToBind().isEmpty()) {
            roleService.bindPermissionToRole(roleId, permissionUpdateDTO.getPermissionsToBind());
        }
        
        return ResponseEntity.ok();
    }
}
