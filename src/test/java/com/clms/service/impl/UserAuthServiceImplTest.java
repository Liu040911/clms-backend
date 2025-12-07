package com.clms.service.impl;

import com.clms.service.IUserAuthService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * UserAuthServiceImpl 集成测试类
 * 使用真实服务进行测试，需要配置测试数据库
 * 
 * @author clms-backend
 * @since 1.0.0
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("用户认证服务集成测试")
class UserAuthServiceImplTest {

    @Resource
    private IUserAuthService userAuthService;

}
