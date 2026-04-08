package com.clms.entity.bo;

import java.util.Date;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户登录/注册返回BO
 * 只返回token信息，用户详细信息通过单独的用户信息接口获取
 */
@Data
@Schema(description = "用户登录返回BO")
public class UserLoginBO {
    
    @Schema(description = "访问令牌")
    private String accessToken;
    
    @Schema(description = "刷新令牌")
    private String refreshToken;
    
    @Schema(description = "过期时间")
    private Date expires;

    @Schema(description = "当前登录用户的角色")
    private List<String> roles;

    @Schema(description = "按钮级别权限")
    private List<String> permissions;
}
