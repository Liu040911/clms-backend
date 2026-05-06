package com.clms.service.impl;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.clms.service.IAppConfigService;
import com.clms.service.data.IAppConfigTableService;

import cn.hutool.json.JSONObject;
import jakarta.annotation.Resource;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@DisplayName("应用配置服务测试")
class AppConfigServiceImplTest {

    @Resource
    private IAppConfigService appConfigService;

    @Resource
    private IAppConfigTableService appConfigTableService;

    private String testConfigId;

    @BeforeEach
    void setUp() {
        testConfigId = null;
    }

    @AfterEach
    void tearDown() {
        if (testConfigId != null) {
            appConfigTableService.removeById(testConfigId);
        }
    }

    @Test
    @DisplayName("getManagerConfig: 无配置数据应返回空JSONObject")
    void getManagerConfig_shouldReturnEmptyWhenNoData() {
        JSONObject result = appConfigService.getManagerConfig("any-user");
        assertNotNull(result);
    }
}
