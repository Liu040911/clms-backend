package com.clms.entity.bo;

import java.util.Date;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 管理员登录业务对象
 * 
 * @author Liu
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "AdminLoginBO对象", description = "管理员登录业务对象")
public class AdminLoginBO {
    @Schema(description = "是否成功")
    private boolean success;

    @Schema(description = "返回数据内容")
    private DataContent data;

    @Data
    public static class DataContent {
        @Schema(description = "头像")
        private String avatar;

        @Schema(description = "用户名")
        private String username;

        @Schema(description = "昵称")
        private String nickname;

        @Schema(description = "当前登录用户的角色")
        private List<String> roles;

        @Schema(description = "按钮级别权限")
        private List<String> permissions;

        @Schema(description = "token字符串")
        private String accessToken;

        @Schema(description = "刷新token字符串")
        private String refreshToken;

        @Schema(description = "accessToken过期时间")
        private Date expires;
    }
}
