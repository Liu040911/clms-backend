package com.clms.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.temp.SaTempUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.clms.entity.bo.AdminLoginBO;
import com.clms.entity.bo.RefreshTokenBO;
import com.clms.entity.bo.UserLoginBO;
import com.clms.entity.dto.AccountRegisterDTO;
import com.clms.entity.dto.UserCredentialDTO;
import com.clms.entity.po.PermissionTable;
import com.clms.entity.po.RolePermissionTable;
import com.clms.entity.po.RoleTable;
import com.clms.entity.po.UserRoleTable;
import com.clms.entity.po.UserTable;
import com.clms.exception.BusinessException;
import com.clms.service.IUserAuthService;
import com.clms.service.ISmsService;
import com.clms.service.data.IPermissionTableService;
import com.clms.service.data.IRolePermissionTableService;
import com.clms.service.data.IRoleTableService;
import com.clms.service.data.IUserRoleTableService;
import com.clms.service.data.IUserTableService;
import com.clms.utils.RedisConstants;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * UserAuthServiceImpl 集成测试类
 * 使用真实服务进行测试，需要配置测试数据库
 * 
 * @author clms-backend
 * @since 1.0.0
 */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
@DisplayName("用户认证服务集成测试")
public class UserAuthServiceImplTest {

    private static final String PHONE_CODE_TEST_PHONE = "13800000001";
    private static final String EMAIL_CODE_TEST_EMAIL = "integration-test@clms.local";
    private static final String PASSWORD = "Pass@123";

    private String defaultRoleId;
    private String defaultPermissionId;
    private String defaultRolePermissionId;

    @Resource
    private IUserAuthService userAuthService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private IUserTableService userTableService;

    @Resource
    private IUserRoleTableService userRoleTableService;

    @Resource
    private IRoleTableService roleTableService;

    @Resource
    private IPermissionTableService permissionTableService;

    @Resource
    private IRolePermissionTableService rolePermissionTableService;

    /**
     * 外部接口（阿里云短信）使用 Mock，避免测试触达真实第三方服务。
     */
    @MockBean
    private ISmsService smsService;

    @BeforeEach
    void setUp() {
        StpUtil.logout();
        clearRedisKeys();
        cleanupTestData();
        seedDefaultRoleAndPermission();
    }

    @AfterEach
    void tearDown() {
        StpUtil.logout();
        clearRedisKeys();
        cleanupTestData();
    }

    @Test
    @DisplayName("获取手机验证码: 内部Redis和RabbitMQ走真实链路, 外部短信服务走Mock")
    void getPhoneCode_shouldUseRealInternalFlowAndMockExternalSms() throws Exception {
        String phone = PHONE_CODE_TEST_PHONE;
        String codeKey = RedisConstants.AUTH_CODE + phone;
        String limitKey = RedisConstants.SEND_CODE_LIMIT + phone;

        stringRedisTemplate.delete(codeKey);
        stringRedisTemplate.delete(limitKey);

        userAuthService.getPhoneCode(phone);

        String code = stringRedisTemplate.opsForValue().get(codeKey);
        String sendLimit = stringRedisTemplate.opsForValue().get(limitKey);
        assertFalse(code == null || code.isBlank(), "验证码应写入Redis");
        assertTrue(code.matches("^\\d{4}$"), "验证码应为4位数字");
        assertTrue("1".equals(sendLimit), "发送频率限制标记应写入Redis");

        // 验证 RabbitMQ 消费后会调用短信服务（该服务已被 Mock）
        verify(smsService, timeout(5000)).sendSms(eq(phone), anyString());
    }

    @Test
    @DisplayName("手机验证码限流: 1分钟内重复请求应被拒绝")
    void getPhoneCode_shouldThrottleWithinOneMinute() {
        String phone = PHONE_CODE_TEST_PHONE;

        userAuthService.getPhoneCode(phone);

        BusinessException ex = assertThrows(BusinessException.class, () -> userAuthService.getPhoneCode(phone));
        assertTrue(ex.getCode() == 429, "应返回429限流错误码");
    }

    @Test
    @DisplayName("获取邮箱验证码: 内部Redis和RabbitMQ走真实链路")
    void getEmailCode_shouldUseRealInternalFlow() {
        String email = EMAIL_CODE_TEST_EMAIL;
        String codeKey = RedisConstants.AUTH_CODE + email;
        String limitKey = RedisConstants.SEND_CODE_LIMIT + email;

        stringRedisTemplate.delete(codeKey);
        stringRedisTemplate.delete(limitKey);

        userAuthService.getEmailCode(email);

        String code = stringRedisTemplate.opsForValue().get(codeKey);
        String sendLimit = stringRedisTemplate.opsForValue().get(limitKey);
        assertFalse(code == null || code.isBlank(), "验证码应写入Redis");
        assertTrue(code.matches("^\\d{4}$"), "验证码应为4位数字");
        assertTrue("1".equals(sendLimit), "发送频率限制标记应写入Redis");
    }

    @Test
    @DisplayName("管理员登录成功")
    void adminLogin_shouldSuccessWhenUserHasAdminRole() {
        UserTable user = createUser(
                "admin-" + randomId() + "@clms.local",
                "139" + randomDigits(8),
                PASSWORD,
                List.of("admin", "user"),
                List.of("system:read")
        );

        UserCredentialDTO dto = new UserCredentialDTO();
        dto.setCredential(user.getEmail());
        dto.setPassword(PASSWORD);

        AdminLoginBO result = userAuthService.adminLogin(dto);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertNotNull(result.getData().getAccessToken());
        assertNotNull(result.getData().getRefreshToken());
        assertTrue(result.getData().getRoles().contains("admin"));
    }

    @Test
    @DisplayName("管理员登录失败: 非管理员角色")
    void adminLogin_shouldFailWhenUserIsNotAdmin() {
        UserTable user = createUser(
                "user-" + randomId() + "@clms.local",
                "137" + randomDigits(8),
                PASSWORD,
                List.of("user"),
                List.of("system:read")
        );

        UserCredentialDTO dto = new UserCredentialDTO();
        dto.setCredential(user.getPhone());
        dto.setPassword(PASSWORD);

        BusinessException ex = assertThrows(BusinessException.class, () -> userAuthService.adminLogin(dto));
        assertEquals(403, ex.getCode());
    }

    @Test
    @DisplayName("刷新Token成功")
    void refreshToken_shouldSuccessWhenTokenValid() {
        UserTable user = createUser(
                "refresh-" + randomId() + "@clms.local",
                "136" + randomDigits(8),
                PASSWORD,
                List.of("user"),
                List.of("system:read")
        );
        String oldRefreshToken = SaTempUtil.createToken(user.getId(), 60 * 5);

        RefreshTokenBO result = userAuthService.refreshToken(oldRefreshToken);

        assertTrue(result.isSuccess());
        assertNotNull(result.getData().getAccessToken());
        assertNotNull(result.getData().getRefreshToken());
        assertNotEquals(oldRefreshToken, result.getData().getRefreshToken());
    }

    @Test
    @DisplayName("刷新Token失败: 空token")
    void refreshToken_shouldFailWhenTokenBlank() {
        BusinessException ex = assertThrows(BusinessException.class, () -> userAuthService.refreshToken(""));
        assertEquals(401, ex.getCode());
    }

    @Test
    @DisplayName("用户密码登录成功")
    void userLogin_shouldSuccessByPassword() {
        UserTable user = createUser(
                "pwd-login-" + randomId() + "@clms.local",
                "135" + randomDigits(8),
                PASSWORD,
                List.of("user"),
                List.of("system:read")
        );

        UserCredentialDTO dto = new UserCredentialDTO();
        dto.setCredential(user.getPhone());
        dto.setPassword(PASSWORD);

        UserLoginBO result = userAuthService.userLogin(dto);
        assertNotNull(result.getAccessToken());
        assertNotNull(result.getRefreshToken());
        assertTrue(result.getExpires() == null || result.getExpires().after(new Date()));
    }

    @Test
    @DisplayName("用户验证码登录成功")
    void userLogin_shouldSuccessByCode() {
        UserTable user = createUser(
                "code-login-" + randomId() + "@clms.local",
                "134" + randomDigits(8),
                PASSWORD,
                List.of("user"),
                List.of("system:read")
        );

        String codeKey = RedisConstants.AUTH_CODE + user.getEmail();
        stringRedisTemplate.opsForValue().set(codeKey, "1234", 5, TimeUnit.MINUTES);

        UserCredentialDTO dto = new UserCredentialDTO();
        dto.setCredential(user.getEmail());
        dto.setCode("1234");

        UserLoginBO result = userAuthService.userLogin(dto);
        assertNotNull(result.getAccessToken());
        assertNull(stringRedisTemplate.opsForValue().get(codeKey));
    }

    @Test
    @DisplayName("用户注册失败: 手机号已存在")
    void userRegister_shouldFailWhenPhoneAlreadyRegistered() {
        String phone = "133" + randomDigits(8);
        createUser(
                "register-exists-" + randomId() + "@clms.local",
                phone,
                PASSWORD,
                List.of("user"),
                List.of("system:read")
        );

        AccountRegisterDTO dto = new AccountRegisterDTO();
        dto.setPhone(phone);
        dto.setNickname("reg-user-" + randomId());
        dto.setPassword(PASSWORD);
        dto.setVerificationCode("5678");

        BusinessException ex = assertThrows(BusinessException.class, () -> userAuthService.userRegister(dto));
        assertEquals(400, ex.getCode());
    }

    @Test
    @DisplayName("用户登出成功")
    void userLogout_shouldClearCurrentSession() {
        StpUtil.login("logout-" + randomId());
        assertTrue(StpUtil.isLogin());

        userAuthService.userLogout();

        assertFalse(StpUtil.isLogin());
    }

    @Test
    @DisplayName("验证验证码并获取步骤令牌成功")
    void verifyCodeAndGetStepToken_shouldSuccess() {
        UserTable user = createUser(
                "step-" + randomId() + "@clms.local",
                "132" + randomDigits(8),
                PASSWORD,
                List.of("user"),
                List.of("system:read")
        );

        String codeKey = RedisConstants.AUTH_CODE + user.getPhone();
        stringRedisTemplate.opsForValue().set(codeKey, "9999", 5, TimeUnit.MINUTES);

        String stepToken = userAuthService.verifyCodeAndGetStepToken(user.getPhone(), "9999");
        assertNotNull(stepToken);
        assertNull(stringRedisTemplate.opsForValue().get(codeKey));

        String stepTokenKey = RedisConstants.STEP_TOKEN + stepToken;
        assertEquals(user.getId(), stringRedisTemplate.opsForValue().get(stepTokenKey));
    }

    @Test
    @DisplayName("重置密码成功")
    void resetPassword_shouldSuccess() {
        UserTable user = createUser(
                "reset-" + randomId() + "@clms.local",
                "131" + randomDigits(8),
                PASSWORD,
                List.of("user"),
                List.of("system:read")
        );

        String stepToken = "step-" + randomId();
        String stepTokenKey = RedisConstants.STEP_TOKEN + stepToken;
        stringRedisTemplate.opsForValue().set(stepTokenKey, user.getId(), 10, TimeUnit.MINUTES);

        String newPassword = "NewPass@456";
        userAuthService.resetPassword(stepToken, newPassword);

        UserTable updated = userTableService.getById(user.getId());
        assertNotNull(updated);
        assertEquals(DigestUtil.md5Hex(newPassword + user.getId()), updated.getPassword());
        assertNull(stringRedisTemplate.opsForValue().get(stepTokenKey));
    }

    private void clearRedisKeys() {
        stringRedisTemplate.delete(RedisConstants.AUTH_CODE + PHONE_CODE_TEST_PHONE);
        stringRedisTemplate.delete(RedisConstants.SEND_CODE_LIMIT + PHONE_CODE_TEST_PHONE);
        stringRedisTemplate.delete(RedisConstants.AUTH_CODE + EMAIL_CODE_TEST_EMAIL);
        stringRedisTemplate.delete(RedisConstants.SEND_CODE_LIMIT + EMAIL_CODE_TEST_EMAIL);

        if (stringRedisTemplate.keys(RedisConstants.STEP_TOKEN + "*") != null) {
            stringRedisTemplate.delete(stringRedisTemplate.keys(RedisConstants.STEP_TOKEN + "*"));
        }
    }

    private void cleanupTestData() {
        userRoleTableService.remove(new LambdaQueryWrapper<UserRoleTable>().like(UserRoleTable::getUserId, "test-user-"));
        userTableService.remove(new LambdaQueryWrapper<UserTable>().like(UserTable::getEmail, "@clms.local"));

        if (defaultRolePermissionId != null) {
            rolePermissionTableService.removeById(defaultRolePermissionId);
        }
        if (defaultPermissionId != null) {
            permissionTableService.removeById(defaultPermissionId);
        }
        if (defaultRoleId != null) {
            roleTableService.removeById(defaultRoleId);
        }
    }

    private void seedDefaultRoleAndPermission() {
        defaultRoleId = "test-role-" + randomId();
        defaultPermissionId = "test-perm-" + randomId();
        defaultRolePermissionId = "test-role-perm-" + randomId();

        RoleTable role = new RoleTable();
        role.setId(defaultRoleId);
        role.setRoleName("test-default-role-" + randomId());
        role.setRoleDescription("test-default-role-desc");
        role.setRoleStatus("enabled");
        role.setDefaultRole(true);
        roleTableService.save(role);

        PermissionTable permission = new PermissionTable();
        permission.setId(defaultPermissionId);
        String permissionValue = "test:default:perm:" + randomId();
        permission.setPermissionName(permissionValue);
        permission.setPermissionString(permissionValue);
        permissionTableService.save(permission);

        RolePermissionTable relation = new RolePermissionTable();
        relation.setId(defaultRolePermissionId);
        relation.setRoleId(defaultRoleId);
        relation.setPermissionId(defaultPermissionId);
        rolePermissionTableService.save(relation);
    }

    private UserTable createUser(String email, String phone, String rawPassword, List<String> roles, List<String> permissions) {
        UserTable user = new UserTable();
        user.setId("test-user-" + randomId());
        user.setEmail(email);
        user.setPhone(phone);
        user.setNickname("test-" + randomId());
        user.setPassword(DigestUtil.md5Hex(rawPassword + user.getId()));
        user.setAvatarUrl("https://example.com/avatar.png");
        user.setUserRoles(new JSONArray(roles));
        user.setUserPermissions(new JSONArray(permissions));
        userTableService.save(user);

        for (String roleName : roles) {
            RoleTable role = roleTableService.lambdaQuery()
                    .eq(RoleTable::getRoleName, roleName)
                    .one();
            if (role == null) {
                role = new RoleTable();
                role.setId("test-role-" + randomId());
                role.setRoleName(roleName);
                role.setRoleDescription("test-role-desc-" + roleName);
                role.setRoleStatus("enabled");
                role.setDefaultRole(false);
                roleTableService.save(role);
            }

            UserRoleTable userRole = new UserRoleTable();
            userRole.setId("test-user-role-" + randomId());
            userRole.setUserId(user.getId());
            userRole.setRoleId(role.getId());
            userRoleTableService.save(userRole);
        }

        return user;
    }

    private String randomId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    private String randomDigits(int size) {
        String seed = UUID.randomUUID().toString().replace("-", "");
        StringBuilder digits = new StringBuilder();
        for (int i = 0; i < seed.length() && digits.length() < size; i++) {
            char c = seed.charAt(i);
            if (Character.isDigit(c)) {
                digits.append(c);
            }
        }
        while (digits.length() < size) {
            digits.append('0');
        }
        return digits.substring(0, size);
    }
}
