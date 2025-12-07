package com.clms.service.data;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.clms.entity.po.RoleTable;
import com.clms.entity.po.UserRoleTable;

/**
 * 用户角色关联表 Service 接口
 * 
 * @author Liu
 * @since 1.0.0
 */
public interface IUserRoleTableService extends IService<UserRoleTable> {
    // 根据用户id获取该用户的角色列表
    public List<RoleTable> getRolesByUserId(String userId);
}
