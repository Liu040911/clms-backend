package com.clms.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CommonUtil 工具类测试
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("通用工具服务集成测试")
public class CommonUtilTest {
    
    @Test
    @DisplayName("生成UUID v7 32位字符串（不含横杠）")
    public void testGenerateUuidV7() {
        // Act
        String uuid = CommonUtil.generateUuidV7();
        
        // Assert
        assertNotNull(uuid);
        assertEquals(32, uuid.length(), "UUID v7 字符串长度应为 32");
        assertFalse(uuid.contains("-"), "UUID v7 字符串不应包含横杠");
        assertTrue(uuid.matches("[0-9a-f]{32}"), "UUID v7 字符串应为32个十六进制字符");
        
        System.out.println("Generated UUID v7 (without hyphen): " + uuid);
    }
}
