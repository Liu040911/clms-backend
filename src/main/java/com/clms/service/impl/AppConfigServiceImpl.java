package com.clms.service.impl;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.clms.entity.po.AppConfigTable;
import com.clms.service.IAppConfigService;
import com.clms.service.IUserRoleService;
import com.clms.service.data.IAppConfigTableService;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import jakarta.annotation.Resource;

@Service
public class AppConfigServiceImpl implements IAppConfigService {

    /**
     * 应用配置表服务，用于操作app_config_table表
     */
    @Resource
    private IAppConfigTableService appConfigTableService;

    /**
     * 用户角色服务，用于获取用户的角色信息
     */
    @Resource
    private IUserRoleService userRoleService;


    @Override
    public JSONObject getConfig(String configKey) {
        // 查询配置是否存在
        AppConfigTable appConfigTable = appConfigTableService.lambdaQuery().one();
        
        // 如果配置不存在，返回空JSONObject
        if (appConfigTable == null) {
            return new JSONObject();
        }
        
        // 如果configKey为null，返回整个配置对象
        if (configKey == null) {
            return appConfigTable.getConfigData();
        }
        
        // 返回指定configKey对应的配置项
        return appConfigTable.getConfigData().getJSONObject(configKey);
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * 具体实现逻辑：
     * 1. 根据appKey和appChannel查询配置
     * 2. 如果配置不存在，返回空JSONObject
     * 3. 获取manager配置，如果不存在，返回空JSONObject
     * 4. 获取用户角色
     * 5. 根据用户角色过滤路由，只保留用户有权限访问的路由
     * 6. 返回过滤后的路由配置
     * </p>
     */
    @Override
    public JSONObject getManagerConfig(String userId) {
        // 查询配置是否存在
        AppConfigTable appConfigTable = appConfigTableService.lambdaQuery().one();
                
        // 如果配置不存在，返回空JSONObject
        if (appConfigTable == null) {
            return new JSONObject();
        }

        // 获取整个配置数据
        JSONObject configData = appConfigTable.getConfigData();
        
        // 获取manager配置
        JSONObject managerConfig = configData.getJSONObject("manager");
        
        // 如果manager配置不存在，返回空JSONObject
        if (managerConfig == null) {
            return new JSONObject();
        }

        // 获取用户所有角色
        Set<String> userRoles = userRoleService.getUserRoles(userId).stream()
            .map(role -> role.getRoleName())
            .filter(StrUtil::isNotBlank)
            .collect(Collectors.toSet());

        // 克隆routes数组以避免修改原始数据
        JSONArray routes = managerConfig.getJSONArray("routes");

        // 过滤用户没有权限的路由
        JSONArray filteredRoutes = filterRoutesByUserRoles(routes, userRoles);

        // 创建返回结果
        JSONObject result = new JSONObject();
        result.set("success", true);
        result.set("data", filteredRoutes);

        return result;
    }

    /**
     * 递归过滤路由，只保留用户有权限的路由，并移除roles字段
     * 
     * <p>
     * 该方法递归处理路由树，对每个路由节点执行以下操作：
     * 1. 检查用户是否有权限访问该路由（通过比对用户角色和路由要求的角色）
     * 2. 如果有权限，创建路由副本，移除roles字段
     * 3. 如果路由有子路由，递归处理子路由
     * 4. 将过滤后的路由添加到结果列表
     * </p>
     * 
     * @param routes 原始路由列表
     * @param userRoles 用户拥有的角色集合
     * @return 过滤后的路由列表，只包含用户有权限访问的路由
     */
    private JSONArray filterRoutesByUserRoles(JSONArray routes, Set<String> userRoles) {
        // 创建过滤后的路由列表
        JSONArray filteredRoutes = new JSONArray();
        
        // 遍历所有路由
        for (int i = 0; i < routes.size(); i++) {
            JSONObject route = routes.getJSONObject(i);

            // 检查用户是否有权限访问该路由
            boolean hasPermission = false;
            
            // 如果路由没有设置roles，默认所有用户都有权限
            if (!route.containsKey("roles")) {
                hasPermission = true;
            } else {
                // 获取路由需要的角色列表
                JSONArray routeRoles = route.getJSONArray("roles");
                
                // 如果角色列表不为空，检查用户是否拥有任一所需角色
                if (routeRoles != null && routeRoles.size() > 0) {
                    for (int j = 0; j < routeRoles.size(); j++) {
                        String role = routeRoles.getStr(j);
                        // 只要用户拥有一个所需角色，就有权限访问
                        if (userRoles.contains(role)) {
                            hasPermission = true;
                            break;
                        }
                    }
                } else {
                    // 如果roles是空数组，则所有用户都有权限
                    hasPermission = true;
                }
            }

            // 如果用户有权限访问该路由
            if (hasPermission) {
                // 创建路由副本以避免修改原始数据
                JSONObject filteredRoute = new JSONObject(route.toString());

                // 移除roles字段，客户端不需要知道角色权限设置
                filteredRoute.remove("roles");

                // 如果有子路由，递归过滤
                if (filteredRoute.containsKey("children")) {
                    JSONArray children = filteredRoute.getJSONArray("children");
                    if (children != null && !children.isEmpty()) {
                        // 递归处理子路由
                        filteredRoute.set("children", filterRoutesByUserRoles(children, userRoles));
                    }
                }

                // 将过滤后的路由添加到结果列表
                filteredRoutes.add(filteredRoute);
            }
        }

        return filteredRoutes;
    }
}
