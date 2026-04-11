package com.clms;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;

import com.clms.service.IAiChatService;

import jakarta.annotation.Resource;

/**
 * 主应用测试类
 * 
 * @author clms-backend
 * @since 1.0.0
 */
@SpringBootTest
class ClmsBackendApplicationTests {
    public static void main(String[] args) {
        SpringApplication.run(ClmsBackendApplicationTests.class, args);
    }

    @Resource
    private IAiChatService aiChatService;
}
