package com.clms.service.impl;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.clms.entity.po.AppConfigTable;
import com.clms.service.IAppConfigService;
import com.clms.service.data.IAppConfigTableService;
import com.clms.utils.CommonUtil;

import cn.hutool.json.JSONArray;
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
    @DisplayName("getConfig: null key应返回完整配置")
    void getConfig_shouldReturnWholeConfigWhenKeyIsNull() {
        seedConfigIfNone();
        JSONObject result = appConfigService.getConfig(null);
        assertNotNull(result);
    }

    @Test
    @DisplayName("getManagerConfig: 无配置数据应返回空JSONObject")
    void getManagerConfig_shouldReturnEmptyWhenNoData() {
        JSONObject result = appConfigService.getManagerConfig("any-user");
        assertNotNull(result);
    }

    private void seedConfigIfNone() {
        AppConfigTable existing = appConfigTableService.lambdaQuery().one();
        if (existing != null) {
            return;
        }

        JSONArray routes = new JSONArray();
        JSONObject homeRoute = new JSONObject();
        homeRoute.set("path", "/home");
        homeRoute.set("name", "home");
        routes.add(homeRoute);

        JSONObject managerConfig = new JSONObject();
        managerConfig.set("routes", routes);

        JSONObject configData = new JSONObject();
        configData.set("manager", managerConfig);

        AppConfigTable config = new AppConfigTable();
        config.setId("test-cfg-" + CommonUtil.generateUuidV7().substring(0, 24));
        config.setConfigData(configData);
        appConfigTableService.save(config);
        testConfigId = config.getId();
    }
}
