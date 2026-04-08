package com.clms.service.impl;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.clms.entity.bo.UserInfoBO;
import com.clms.entity.dto.UserAdminDTO;
import com.clms.entity.dto.UserAdminEditDTO;
import com.clms.entity.po.PermissionTable;
import com.clms.entity.po.RoleTable;
import com.clms.entity.po.UserTable;
import com.clms.exception.BusinessException;
import com.clms.service.IUserAdminService;
import com.clms.service.IUserRoleService;
import com.clms.service.data.IRolePermissionTableService;
import com.clms.service.data.IRoleTableService;
import com.clms.service.data.IUserRoleTableService;
import com.clms.service.data.IUserTableService;
import com.clms.utils.CommonUtil;

import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.core.util.StrUtil;
import cn.dev33.satoken.stp.StpUtil;
import jakarta.annotation.Resource;

@Service
public class UserAdminServiceImpl implements IUserAdminService {

	@Resource
	private IUserTableService userTableService;

	@Resource
	private IUserRoleService userRoleService;

	@Resource
	private IRoleTableService roleTableService;

	@Resource
	private IRolePermissionTableService rolePermissionTableService;

	@Resource
	private IUserRoleTableService userRoleTableService;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void addAdmin(UserAdminDTO userAdminDTO) {
		LambdaQueryWrapper<RoleTable> roleWrapper = new LambdaQueryWrapper<>();
		roleWrapper.eq(RoleTable::getRoleName, "admin");
		RoleTable adminRole = roleTableService.getOne(roleWrapper);
		if (adminRole == null) {
			throw new BusinessException(500, "管理员角色不存在");
		}

		LambdaQueryWrapper<UserTable> phoneWrapper = new LambdaQueryWrapper<>();
		phoneWrapper.eq(UserTable::getPhone, userAdminDTO.getPhone());
		UserTable existedUser = userTableService.getOne(phoneWrapper);
		if (existedUser != null) {
			List<String> existedRoles = existedUser.getUserRoles() == null
				? new ArrayList<>()
				: new ArrayList<>(existedUser.getUserRoles().toList(String.class));
			if (existedRoles.contains("admin")) {
				return;
			}

			List<String> adminPermissions = rolePermissionTableService.getPermissionsByRoleIds(List.of(adminRole.getId()))
				.stream()
				.map(PermissionTable::getPermissionString)
				.toList();

			Set<String> roleSet = new LinkedHashSet<>(existedRoles);
			roleSet.add("admin");
			existedUser.setUserRoles(new JSONArray(new ArrayList<>(roleSet)));

			List<String> existedPermissions = existedUser.getUserPermissions() == null
				? new ArrayList<>()
				: new ArrayList<>(existedUser.getUserPermissions().toList(String.class));
			Set<String> permissionSet = new LinkedHashSet<>(existedPermissions);
			permissionSet.addAll(adminPermissions);
			existedUser.setUserPermissions(new JSONArray(new ArrayList<>(permissionSet)));

			boolean updated = userTableService.updateById(existedUser);
			if (!updated) {
				throw new BusinessException(500, "升级管理员失败");
			}

			userRoleService.bindRolesToUser(existedUser.getId(), List.of(adminRole.getId()));
			return;
		}

		List<RoleTable> defaultRoles = userRoleService.getDefaultRoles();
		Set<String> allRoleIds = new LinkedHashSet<>();
		Set<String> allRoleNames = new LinkedHashSet<>();
		for (RoleTable defaultRole : defaultRoles) {
			if (defaultRole != null && StrUtil.isNotBlank(defaultRole.getId())) {
				allRoleIds.add(defaultRole.getId());
			}
			if (defaultRole != null && StrUtil.isNotBlank(defaultRole.getRoleName())) {
				allRoleNames.add(defaultRole.getRoleName());
			}
		}
		allRoleIds.add(adminRole.getId());
		allRoleNames.add(adminRole.getRoleName());

		List<String> allPermissions = rolePermissionTableService.getPermissionsByRoleIds(new ArrayList<>(allRoleIds))
			.stream()
			.map(PermissionTable::getPermissionString)
			.distinct()
			.toList();

		UserTable newUser = new UserTable();
		newUser.setId(CommonUtil.generateUuidV7());
		newUser.setNickname(userAdminDTO.getNickname());
		newUser.setPhone(userAdminDTO.getPhone());
		newUser.setEmail(userAdminDTO.getEmail());
		newUser.setGender(userAdminDTO.getGender());
		newUser.setAvatarUrl(userAdminDTO.getAvatarUrl());
		newUser.setPassword(DigestUtil.md5Hex(userAdminDTO.getPassword() + newUser.getId()));
		newUser.setUserRoles(new JSONArray(new ArrayList<>(allRoleNames)));
		newUser.setUserPermissions(new JSONArray(allPermissions));

		boolean saved = userTableService.save(newUser);
		if (!saved) {
			throw new BusinessException(500, "新增管理员失败");
		}

		userRoleService.bindRolesToUser(newUser.getId(), new ArrayList<>(allRoleIds));
	}

	@Override
	public void disableAdminPermission(String userId) {
		if (StrUtil.isBlank(userId)) {
			throw new BusinessException(400, "用户ID不能为空");
		}

		UserTable user = userTableService.getById(userId);
		if (user == null) {
			throw new BusinessException(404, "用户不存在");
		}

		List<String> roles = user.getUserRoles() == null ? List.of() : user.getUserRoles().toList(String.class);
		if (!roles.contains("admin")) {
			return;
		}

		// 分类封禁：仅封禁 admin 管理能力，不影响普通用户能力。
		StpUtil.disable(userId, "admin", -1);
	}

	@Override
	public void enableAdminPermission(String userId) {
		if (StrUtil.isBlank(userId)) {
			throw new BusinessException(400, "用户ID不能为空");
		}

		UserTable user = userTableService.getById(userId);
		if (user == null) {
			throw new BusinessException(404, "用户不存在");
		}

		List<String> roles = user.getUserRoles() == null ? List.of() : user.getUserRoles().toList(String.class);
		if (!roles.contains("admin")) {
			return;
		}

		// 分类解封：恢复 admin 管理能力，不影响普通用户能力。
		StpUtil.untieDisable(userId, "admin");
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void editAdminInfo(UserAdminEditDTO userAdminEditDTO) {
		String operatorId = String.valueOf(StpUtil.getLoginId());
		String targetUserId = userAdminEditDTO.getUserId();

		UserTable targetUser = userTableService.getById(targetUserId);
		if (targetUser == null) {
			throw new BusinessException(404, "用户不存在");
		}

		List<String> targetRoles = targetUser.getUserRoles() == null ? List.of() : targetUser.getUserRoles().toList(String.class);
		if (!targetRoles.contains("admin")) {
			throw new BusinessException(400, "目标用户不是管理员");
		}

		boolean isSelf = StrUtil.equals(operatorId, targetUserId);
		List<RoleTable> operatorRoles = userRoleTableService.getRolesByUserId(operatorId);
		boolean operatorIsSuperAdmin = operatorRoles.stream()
			.anyMatch(role -> StrUtil.equals(role.getRoleName(), "superadmin"));

		if (!isSelf && !operatorIsSuperAdmin) {
			throw new BusinessException(403, "仅系统超管可编辑其他管理员");
		}

		boolean upgradeToSuperAdmin = Boolean.TRUE.equals(userAdminEditDTO.getRoleIds());
		if (upgradeToSuperAdmin && !operatorIsSuperAdmin) {
			throw new BusinessException(403, "仅系统超管可升级为系统超管");
		}

		if (StrUtil.isNotBlank(userAdminEditDTO.getPhone())
			&& !StrUtil.equals(userAdminEditDTO.getPhone(), targetUser.getPhone())) {
			LambdaQueryWrapper<UserTable> phoneWrapper = new LambdaQueryWrapper<>();
			phoneWrapper.eq(UserTable::getPhone, userAdminEditDTO.getPhone());
			phoneWrapper.ne(UserTable::getId, targetUserId);
			if (userTableService.exists(phoneWrapper)) {
				throw new BusinessException(400, "该手机号已被注册");
			}
		}

		if (StrUtil.isNotBlank(userAdminEditDTO.getNickname())) {
			targetUser.setNickname(userAdminEditDTO.getNickname());
		}
		if (StrUtil.isNotBlank(userAdminEditDTO.getPhone())) {
			targetUser.setPhone(userAdminEditDTO.getPhone());
		}
		if (StrUtil.isNotBlank(userAdminEditDTO.getEmail())) {
			targetUser.setEmail(userAdminEditDTO.getEmail());
		}
		if (StrUtil.isNotBlank(userAdminEditDTO.getGender())) {
			targetUser.setGender(userAdminEditDTO.getGender());
		}
		if (StrUtil.isNotBlank(userAdminEditDTO.getAvatarUrl())) {
			targetUser.setAvatarUrl(userAdminEditDTO.getAvatarUrl());
		}
		if (StrUtil.isNotBlank(userAdminEditDTO.getPassword())) {
			targetUser.setPassword(DigestUtil.md5Hex(userAdminEditDTO.getPassword() + targetUserId));
		}

		if (upgradeToSuperAdmin) {
			LambdaQueryWrapper<RoleTable> superAdminWrapper = new LambdaQueryWrapper<>();
			superAdminWrapper.eq(RoleTable::getRoleName, "superadmin");
			RoleTable superAdminRole = roleTableService.getOne(superAdminWrapper);
			if (superAdminRole == null) {
				throw new BusinessException(500, "系统超管角色不存在");
			}

			List<RoleTable> targetRoleTables = userRoleTableService.getRolesByUserId(targetUserId);
			boolean targetAlreadySuperAdmin = targetRoleTables.stream()
				.anyMatch(role -> StrUtil.equals(role.getRoleName(), "superadmin"));

			if (!targetAlreadySuperAdmin) {
				userRoleService.bindRolesToUser(targetUserId, List.of(superAdminRole.getId()));
				targetRoleTables = userRoleTableService.getRolesByUserId(targetUserId);
			}

			List<String> resolvedRoleIds = targetRoleTables.stream()
				.map(RoleTable::getId)
				.filter(StrUtil::isNotBlank)
				.distinct()
				.toList();

			List<String> resolvedRoleNames = targetRoleTables.stream()
				.map(RoleTable::getRoleName)
				.filter(StrUtil::isNotBlank)
				.distinct()
				.toList();

			List<String> permissionStrings = resolvedRoleIds.isEmpty()
				? List.of()
				: rolePermissionTableService.getPermissionsByRoleIds(resolvedRoleIds)
					.stream()
					.map(PermissionTable::getPermissionString)
					.filter(StrUtil::isNotBlank)
					.distinct()
					.toList();

			targetUser.setUserRoles(new JSONArray(resolvedRoleNames));
			targetUser.setUserPermissions(new JSONArray(permissionStrings));
		}

		boolean updated = userTableService.updateById(targetUser);
		if (!updated) {
			throw new BusinessException(500, "编辑管理员信息失败");
		}
	}

	@Override
	public Page<UserInfoBO> getAdminList(Long pageNum, Long pageSize) {
		long current = (pageNum == null || pageNum < 1) ? 1L : pageNum;
		long size = (pageSize == null || pageSize < 1) ? 10L : pageSize;

		LambdaQueryWrapper<UserTable> adminQueryWrapper = new LambdaQueryWrapper<>();
		adminQueryWrapper.apply("JSON_CONTAINS(user_roles, '\"admin\"')");
		adminQueryWrapper.orderByDesc(UserTable::getCreateTime);

		Page<UserTable> page = new Page<>(current, size);
		Page<UserTable> adminPage = userTableService.page(page, adminQueryWrapper);

		List<UserInfoBO> adminList = adminPage.getRecords().stream().map(user -> {
			List<RoleTable> roleTables = userRoleTableService.getRolesByUserId(user.getId());
			List<String> roleNames = roleTables.stream()
				.map(RoleTable::getRoleName)
				.filter(StrUtil::isNotBlank)
				.distinct()
				.toList();

			List<String> roleIds = roleTables.stream()
				.map(RoleTable::getId)
				.filter(StrUtil::isNotBlank)
				.distinct()
				.toList();

			List<String> permissionStrings = roleIds.isEmpty()
				? List.of()
				: rolePermissionTableService.getPermissionsByRoleIds(roleIds)
					.stream()
					.map(PermissionTable::getPermissionString)
					.filter(StrUtil::isNotBlank)
					.distinct()
					.toList();
			UserInfoBO userInfo = new UserInfoBO();
			userInfo.setId(user.getId());
			userInfo.setUsername(user.getNickname());
			userInfo.setNickname(user.getNickname());
			userInfo.setAvatar(user.getAvatarUrl());
			userInfo.setPhone(user.getPhone());
			userInfo.setEmail(user.getEmail());
			userInfo.setGender(user.getGender());
			userInfo.setCreateTime(user.getCreateTime());
			userInfo.setUpdateTime(user.getUpdateTime());
			userInfo.setRoles(roleNames);
			userInfo.setPermissions(permissionStrings);
			return userInfo;
		}).toList();

		Page<UserInfoBO> resultPage = new Page<>(adminPage.getCurrent(), adminPage.getSize(), adminPage.getTotal());
		resultPage.setRecords(adminList);
		return resultPage;
	}
}
