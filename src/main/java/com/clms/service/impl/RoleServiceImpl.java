package com.clms.service.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.clms.entity.bo.PermissionBO;
import com.clms.entity.bo.PermissionModuleBO;
import com.clms.entity.bo.RoleBO;
import com.clms.entity.dto.RoleDTO;
import com.clms.entity.po.ModelPermissionTable;
import com.clms.entity.po.ModelTable;
import com.clms.entity.po.PermissionTable;
import com.clms.entity.po.RolePermissionTable;
import com.clms.entity.po.RoleTable;
import com.clms.exception.BusinessException;
import com.clms.service.IPermissionService;
import com.clms.service.IRoleService;
import com.clms.service.data.IModelPermissionTableService;
import com.clms.service.data.IModelTableService;
import com.clms.service.data.IPermissionTableService;
import com.clms.service.data.IRolePermissionTableService;
import com.clms.service.data.IRoleTableService;
import com.clms.utils.CommonUtil;
import com.clms.utils.DataContainerConvertor;

import cn.hutool.core.util.StrUtil;
import jakarta.annotation.Resource;

@Service
public class RoleServiceImpl implements IRoleService {

	@Resource
	private IRoleTableService roleTableService;

	@Resource
	private IPermissionTableService permissionTableService;

    @Resource
    private IPermissionService permissionService;

	@Resource
	private IRolePermissionTableService rolePermissionTableService;

	@Resource
	private IModelPermissionTableService modelPermissionTableService;

	@Resource
	private IModelTableService modelTableService;

	@Override
	public void createRole(RoleDTO roleDTO) {
		LambdaQueryWrapper<RoleTable> roleNameWrapper = new LambdaQueryWrapper<>();
		roleNameWrapper.eq(RoleTable::getRoleName, roleDTO.getRoleName());
		if (roleTableService.exists(roleNameWrapper)) {
			throw new BusinessException(400, "角色名称已存在");
		}

		RoleTable role = new RoleTable();
		role.setId(CommonUtil.generateUuidV7());
		role.setRoleName(roleDTO.getRoleName());
		role.setRoleDescription(roleDTO.getRoleDescription());
		role.setRoleStatus(StrUtil.isBlank(roleDTO.getRoleStatus()) ? "active" : roleDTO.getRoleStatus());
		role.setDefaultRole(roleDTO.isDefaultRole());

		boolean saved = roleTableService.save(role);
		if (!saved) {
			throw new BusinessException(500, "创建角色失败");
		}
	}

	@Override
	public void updateRole(String roleId, RoleDTO roleDTO) {
		RoleTable existedRole = roleTableService.getById(roleId);
		if (existedRole == null) {
			throw new BusinessException(404, "角色不存在");
		}

		LambdaQueryWrapper<RoleTable> roleNameWrapper = new LambdaQueryWrapper<>();
		roleNameWrapper.eq(RoleTable::getRoleName, roleDTO.getRoleName());
		roleNameWrapper.ne(RoleTable::getId, roleId);
		if (roleTableService.exists(roleNameWrapper)) {
			throw new BusinessException(400, "角色名称已存在");
		}

		existedRole.setRoleName(roleDTO.getRoleName());
		existedRole.setRoleDescription(roleDTO.getRoleDescription());
		existedRole.setRoleStatus(StrUtil.isBlank(roleDTO.getRoleStatus()) ? "active" : roleDTO.getRoleStatus());
		existedRole.setDefaultRole(roleDTO.isDefaultRole());

		boolean updated = roleTableService.updateById(existedRole);
		if (!updated) {
			throw new BusinessException(500, "修改角色失败");
		}
	}

	@Override
	public void deleteRole(String roleId) {
		RoleTable existedRole = roleTableService.getById(roleId);
		if (existedRole == null) {
			throw new BusinessException(404, "角色不存在");
		}

		boolean removed = roleTableService.removeById(roleId);
		if (!removed) {
			throw new BusinessException(500, "删除角色失败");
		}
	}

	@Override
	public RoleBO getRole(String roleId) {
		RoleTable role = roleTableService.getById(roleId);
		if (role == null) {
			throw new BusinessException(404, "角色不存在");
		}

		return new RoleBO(role);
	}

    @Override
    public Page<RoleBO> getRoleList(String roleName, Integer page, Integer size, String sort, String order) {
        LambdaQueryChainWrapper<RoleTable> query = roleTableService.lambdaQuery();
        
        if (StrUtil.isNotBlank(roleName)) {
            query.like(RoleTable::getRoleDescription, roleName);
        }
        
        // 处理排序
        if (StrUtil.isNotBlank(sort) && StrUtil.isNotBlank(order)) {
            boolean isAsc = "asc".equalsIgnoreCase(order);
            if ("roleName".equals(sort)) {
                query = isAsc ? query.orderByAsc(RoleTable::getRoleName) : query.orderByDesc(RoleTable::getRoleName);
            } else if ("roleDescription".equals(sort)) {
                query = isAsc ? query.orderByAsc(RoleTable::getRoleDescription) : query.orderByDesc(RoleTable::getRoleDescription);
            } else if ("roleStatus".equals(sort)) {
                query = isAsc ? query.orderByAsc(RoleTable::getRoleStatus) : query.orderByDesc(RoleTable::getRoleStatus);
            } else if ("defaultRole".equals(sort)) {
                query = isAsc ? query.orderByAsc(RoleTable::isDefaultRole) : query.orderByDesc(RoleTable::isDefaultRole);
            } else if ("createTime".equals(sort)) {
                query = isAsc ? query.orderByAsc(RoleTable::getCreateTime) : query.orderByDesc(RoleTable::getCreateTime);
            } else if ("updateTime".equals(sort)) {
                query = isAsc ? query.orderByAsc(RoleTable::getUpdateTime) : query.orderByDesc(RoleTable::getUpdateTime);
            }
        } else {
            // 默认按照创建时间降序排序
            query = query.orderByDesc(RoleTable::getCreateTime);
        }
        
        Page<RoleTable> tablePage = query.page(new Page<>(page, size));
        Page<RoleBO> roleBOPage = DataContainerConvertor.convertPage(tablePage, RoleTable.class, RoleBO.class);
        
        return roleBOPage;
    }

	@Override
	public void bindPermissionToRole(String roleId, List<String> permissionIds) {
		RoleTable role = roleTableService.getById(roleId);
		if (role == null) {
			throw new BusinessException(404, "角色不存在");
		}

		if (permissionIds == null || permissionIds.isEmpty()) {
			return;
		}

		List<PermissionTable> permissions = permissionTableService.listByIds(permissionIds);
		if (permissions.size() != new HashSet<>(permissionIds).size()) {
			throw new BusinessException(400, "存在无效的权限ID");
		}

		LambdaQueryWrapper<RolePermissionTable> existingWrapper = new LambdaQueryWrapper<>();
		existingWrapper.eq(RolePermissionTable::getRoleId, roleId)
			.in(RolePermissionTable::getPermissionId, permissionIds);
		List<RolePermissionTable> existingBindings = rolePermissionTableService.list(existingWrapper);
		Set<String> existingPermissionIds = new HashSet<>(
			existingBindings.stream().map(RolePermissionTable::getPermissionId).toList()
		);

		List<RolePermissionTable> toSave = permissionIds.stream()
			.filter(permissionId -> !existingPermissionIds.contains(permissionId))
			.map(permissionId -> {
				RolePermissionTable rolePermission = new RolePermissionTable();
				rolePermission.setId(CommonUtil.generateUuidV7());
				rolePermission.setRoleId(roleId);
				rolePermission.setPermissionId(permissionId);
				return rolePermission;
			})
			.toList();

		if (!toSave.isEmpty()) {
			boolean saved = rolePermissionTableService.saveBatch(toSave);
			if (!saved) {
				throw new BusinessException(500, "角色绑定权限失败");
			}
		}
	}

	@Override
	public void unbindPermissionFromRole(String roleId, List<String> permissionIds) {
		RoleTable role = roleTableService.getById(roleId);
		if (role == null) {
			throw new BusinessException(404, "角色不存在");
		}

		if (permissionIds == null || permissionIds.isEmpty()) {
			return;
		}

		LambdaQueryWrapper<RolePermissionTable> removeWrapper = new LambdaQueryWrapper<>();
		removeWrapper.eq(RolePermissionTable::getRoleId, roleId)
			.in(RolePermissionTable::getPermissionId, permissionIds);
		rolePermissionTableService.remove(removeWrapper);
	}

    @Override
    public List<PermissionBO> getPermissionList(String roleId) {
        List<String> permissionIds = rolePermissionTableService.lambdaQuery()
                .eq(RolePermissionTable::getRoleId, roleId)
                .list()
                .stream()
                .map(RolePermissionTable::getPermissionId)
                .collect(Collectors.toList());
        
        if (permissionIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 批量查询所有权限信息，避免N+1查询问题
        Map<String, PermissionBO> permissionMap = permissionService.getBatchPermissions(permissionIds);
        
        // 按原始权限ID顺序返回结果，过滤掉不存在的权限
        return permissionIds.stream()
                .map(permissionMap::get)
                .filter(permission -> permission != null)
                .collect(Collectors.toList());
    }

	@Override
	public List<PermissionModuleBO> getPermissionModuleList(String roleId) {
		List<PermissionBO> permissions = getPermissionList(roleId);
		return groupPermissionsByModel(permissions);
	}

	@Override
	public Set<String> getPermissionModules(String roleId) {
		List<PermissionModuleBO> modules = getPermissionModuleList(roleId);
		return modules.stream()
				.map(PermissionModuleBO::getModuleName)
				.collect(Collectors.toCollection(TreeSet::new));
	}

	@Override
	public List<PermissionModuleBO> getAllPermissionModules() {
		List<PermissionBO> permissions = permissionService.getAllPermission();
		return groupPermissionsByModel(permissions);
	}

	private List<PermissionModuleBO> groupPermissionsByModel(List<PermissionBO> permissions) {
		if (permissions == null || permissions.isEmpty()) {
			return new ArrayList<>();
		}

		List<PermissionBO> validPermissions = permissions.stream()
				.filter(permission -> permission != null && StrUtil.isNotBlank(permission.getId()))
				.collect(Collectors.toList());

		if (validPermissions.isEmpty()) {
			return new ArrayList<>();
		}

		List<String> permissionIds = validPermissions.stream()
				.map(PermissionBO::getId)
				.collect(Collectors.toList());

		List<ModelPermissionTable> modelPermissions = modelPermissionTableService.lambdaQuery()
				.in(ModelPermissionTable::getPermissionId, permissionIds)
				.list();

		Map<String, Set<String>> permissionToModelIds = new LinkedHashMap<>();
		for (ModelPermissionTable relation : modelPermissions) {
			permissionToModelIds
					.computeIfAbsent(relation.getPermissionId(), key -> new HashSet<>())
					.add(relation.getModelId());
		}

		Set<String> modelIds = modelPermissions.stream()
				.map(ModelPermissionTable::getModelId)
				.collect(Collectors.toSet());

		Map<String, String> modelNameMap = new LinkedHashMap<>();
		if (!modelIds.isEmpty()) {
			List<ModelTable> models = modelTableService.listByIds(modelIds);
			modelNameMap = models.stream().collect(Collectors.toMap(
					ModelTable::getId,
					model -> resolveModelDisplayName(model),
					(oldValue, newValue) -> oldValue,
					LinkedHashMap::new));
		}

		Map<String, List<PermissionBO>> moduleMap = new LinkedHashMap<>();
		for (PermissionBO permission : validPermissions) {
			Set<String> relatedModelIds = permissionToModelIds.get(permission.getId());
			if (relatedModelIds == null || relatedModelIds.isEmpty()) {
				moduleMap.computeIfAbsent("未分组", key -> new ArrayList<>()).add(permission);
				continue;
			}

			for (String modelId : relatedModelIds) {
				String moduleName = modelNameMap.get(modelId);
				if (StrUtil.isBlank(moduleName)) {
					moduleName = "未分组";
				}
				moduleMap.computeIfAbsent(moduleName, key -> new ArrayList<>()).add(permission);
			}
		}

		return moduleMap.entrySet().stream()
				.map(entry -> {
					List<PermissionBO> sortedPermissions = entry.getValue().stream()
							.sorted(Comparator.comparing(PermissionBO::getPermissionName, Comparator.nullsLast(String::compareTo)))
							.collect(Collectors.toList());
					return new PermissionModuleBO(entry.getKey(), sortedPermissions);
				})
				.sorted(Comparator.comparing(PermissionModuleBO::getModuleName))
				.collect(Collectors.toList());
	}

	private String resolveModelDisplayName(ModelTable model) {
		if (model == null) {
			return "未分组";
		}
		if (StrUtil.isNotBlank(model.getModelName())) {
			return model.getModelName();
		}
		return "未分组";
	}

    
}
