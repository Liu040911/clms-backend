package com.clms.utils;

import org.springframework.stereotype.Component;
import com.github.f4b6a3.uuid.UuidCreator;

/**
 * 通用工具类
 */
@Component
public class CommonUtil {
    
    /**
     * 生成 UUID v7 32位字符串（不含横杠）
     * 
     * @return 32位 UUID v7 字符串，格式示例: 1f0ce8898f486f429eb9a1a0c761ea7b
     */
    public static String generateUuidV7() {
        java.util.UUID uuid = UuidCreator.getTimeOrdered();
        return uuid.toString().replace("-", "");
    }
}
