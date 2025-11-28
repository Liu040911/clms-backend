package com.clms.entity.bo;


import lombok.Data;
import lombok.EqualsAndHashCode;

import com.clms.entity.base.BaseBO;
import com.clms.entity.po.RoleTable;

import cn.hutool.core.bean.BeanUtil;

@Data
@EqualsAndHashCode(callSuper = true)
public class RoleBO extends BaseBO {
    private String roleName;
    private String roleDescription;
    private String roleStatus;
    private boolean defaultRole;
    
    // 添加最后操作人信息
    private String lastOperatorId;
    private String lastOperatorName;

    public RoleBO(RoleTable roleTable) {
        BeanUtil.copyProperties(roleTable, this);
    }
}
