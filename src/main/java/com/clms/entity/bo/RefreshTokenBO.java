package com.clms.entity.bo;

import java.util.Date;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 刷新Token响应业务对象
 * 
 * @author Liu
 * @since 1.0.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "RefreshTokenBO对象", description = "刷新Token响应业务对象")
public class RefreshTokenBO {
    
    @Schema(description = "是否成功")
    private boolean success;
    
    @Schema(description = "返回数据内容")
    private DataContent data;
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DataContent {
        @Schema(description = "新的访问token")
        private String accessToken;
        
        @Schema(description = "新的刷新token")
        private String refreshToken;
        
        @Schema(description = "accessToken过期时间")
        private Date expires;
    }
}
