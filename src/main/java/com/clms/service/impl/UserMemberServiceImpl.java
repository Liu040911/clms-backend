package com.clms.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.clms.entity.bo.UserInfoBO;
import com.clms.entity.dto.UserMemberEditDTO;
import com.clms.entity.po.PermissionTable;
import com.clms.entity.po.RoleTable;
import com.clms.entity.po.UserTable;
import com.clms.exception.BusinessException;
import com.clms.service.IUserMemberService;
import com.clms.service.IUserRoleService;
import com.clms.service.data.IRolePermissionTableService;
import com.clms.service.data.IRoleTableService;
import com.clms.service.data.IUserRoleTableService;
import com.clms.service.data.IUserTableService;

import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.core.util.StrUtil;
import cn.dev33.satoken.stp.StpUtil;
import jakarta.annotation.Resource;

@Service
public class UserMemberServiceImpl implements IUserMemberService {

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
	public Page<UserInfoBO> getMemberList(Long pageNum, Long pageSize) {
		long current = (pageNum == null || pageNum < 1) ? 1L : pageNum;
		long size = (pageSize == null || pageSize < 1) ? 10L : pageSize;

		LambdaQueryWrapper<UserTable> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.apply("NOT JSON_CONTAINS(user_roles, '\"admin\"')");
		queryWrapper.orderByDesc(UserTable::getCreateTime);

		Page<UserTable> page = new Page<>(current, size);
		Page<UserTable> memberPage = userTableService.page(page, queryWrapper);

		List<UserInfoBO> memberList = memberPage.getRecords().stream().map(user -> {
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

		Page<UserInfoBO> resultPage = new Page<>(memberPage.getCurrent(), memberPage.getSize(), memberPage.getTotal());
		resultPage.setRecords(memberList);
		return resultPage;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void editMemberInfo(UserMemberEditDTO dto) {
		String targetUserId = dto.getUserId();

		UserTable targetUser = userTableService.getById(targetUserId);
		if (targetUser == null) {
			throw new BusinessException(404, "用户不存在");
		}

		List<String> targetRoles = targetUser.getUserRoles() == null
			? List.of()
			: targetUser.getUserRoles().toList(String.class);
		if (targetRoles.contains("admin")) {
			throw new BusinessException(400, "不能编辑管理员用户，请使用管理员管理功能");
		}

		if (StrUtil.isNotBlank(dto.getPhone())
			&& !StrUtil.equals(dto.getPhone(), targetUser.getPhone())) {
			LambdaQueryWrapper<UserTable> phoneWrapper = new LambdaQueryWrapper<>();
			phoneWrapper.eq(UserTable::getPhone, dto.getPhone());
			phoneWrapper.ne(UserTable::getId, targetUserId);
			if (userTableService.exists(phoneWrapper)) {
				throw new BusinessException(400, "该手机号已被注册");
			}
		}

		if (StrUtil.isNotBlank(dto.getNickname())) {
			targetUser.setNickname(dto.getNickname());
		}
		if (StrUtil.isNotBlank(dto.getPhone())) {
			targetUser.setPhone(dto.getPhone());
		}
		if (StrUtil.isNotBlank(dto.getEmail())) {
			targetUser.setEmail(dto.getEmail());
		}
		if (StrUtil.isNotBlank(dto.getGender())) {
			targetUser.setGender(dto.getGender());
		}
		if (StrUtil.isNotBlank(dto.getAvatarUrl())) {
			targetUser.setAvatarUrl(dto.getAvatarUrl());
		}
		if (StrUtil.isNotBlank(dto.getPassword())) {
			targetUser.setPassword(DigestUtil.md5Hex(dto.getPassword() + targetUserId));
		}

		if (dto.getRoleIds() != null) {
			List<RoleTable> requestedRoles = roleTableService.listByIds(dto.getRoleIds());
			boolean hasAdminRole = requestedRoles.stream()
				.anyMatch(role -> "admin".equals(role.getRoleName()) || "superadmin".equals(role.getRoleName()));
			if (hasAdminRole) {
				throw new BusinessException(400, "不能分配管理员类角色");
			}

			LambdaQueryWrapper<com.clms.entity.po.UserRoleTable> deleteWrapper = new LambdaQueryWrapper<>();
			deleteWrapper.eq(com.clms.entity.po.UserRoleTable::getUserId, targetUserId);
			userRoleTableService.remove(deleteWrapper);

			userRoleService.bindRolesToUser(targetUserId, dto.getRoleIds());

			List<String> roleNames = requestedRoles.stream()
				.map(RoleTable::getRoleName)
				.filter(StrUtil::isNotBlank)
				.distinct()
				.toList();

			List<String> permissionStrings = dto.getRoleIds().isEmpty()
				? List.of()
				: rolePermissionTableService.getPermissionsByRoleIds(dto.getRoleIds())
					.stream()
					.map(PermissionTable::getPermissionString)
					.filter(StrUtil::isNotBlank)
					.distinct()
					.toList();

			targetUser.setUserRoles(new JSONArray(new ArrayList<>(roleNames)));
			targetUser.setUserPermissions(new JSONArray(permissionStrings));
		}

		boolean updated = userTableService.updateById(targetUser);
		if (!updated) {
			throw new BusinessException(500, "编辑用户信息失败");
		}
	}

	@Override
	public void enableMember(String userId) {
		if (StrUtil.isBlank(userId)) {
			throw new BusinessException(400, "用户ID不能为空");
		}

		UserTable user = userTableService.getById(userId);
		if (user == null) {
			throw new BusinessException(404, "用户不存在");
		}

		StpUtil.untieDisable(userId, "user");
	}

	@Override
	public void disableMember(String userId) {
		if (StrUtil.isBlank(userId)) {
			throw new BusinessException(400, "用户ID不能为空");
		}

		UserTable user = userTableService.getById(userId);
		if (user == null) {
			throw new BusinessException(404, "用户不存在");
		}

		StpUtil.disable(userId, "user", -1);
	}
}
