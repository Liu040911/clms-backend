package com.clms.service;

import com.clms.entity.bo.PermissionBO;
import com.clms.entity.bo.PermissionModuleBO;
import com.clms.entity.bo.RoleBO;
import com.clms.entity.dto.RoleDTO;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;
import java.util.Set;

public interface IRoleService {

	void createRole(RoleDTO roleDTO);

	void updateRole(String roleId, RoleDTO roleDTO);

	void deleteRole(String roleId);

	RoleBO getRole(String roleId);

	Page<RoleBO> getRoleList(String roleName, Integer page, Integer size, String sort, String order);

	void bindPermissionToRole(String roleId, List<String> permissionIds);

	void unbindPermissionFromRole(String roleId, List<String> permissionIds);

    List<PermissionBO> getPermissionList(String roleId);

	List<PermissionModuleBO> getPermissionModuleList(String roleId);

	Set<String> getPermissionModules(String roleId);

	List<PermissionModuleBO> getAllPermissionModules();
}
