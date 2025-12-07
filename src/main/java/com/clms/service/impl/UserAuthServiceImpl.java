package com.clms.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.clms.config.RabbitMQConfig;
import com.clms.entity.bo.AdminLoginBO;
import com.clms.entity.bo.PermissionBO;
import com.clms.entity.bo.RefreshTokenBO;
import com.clms.entity.bo.RoleBO;
import com.clms.entity.bo.UserInfoBO;
import com.clms.entity.bo.UserLoginBO;
import com.clms.entity.bo.VerificationCodeBO;
import com.clms.entity.dto.AccountRegisterDTO;
import com.clms.entity.dto.UserCredentialDTO;
import com.clms.entity.po.PermissionTable;
import com.clms.entity.po.RoleTable;
import com.clms.entity.po.UserTable;
import com.clms.exception.BusinessException;
import com.clms.service.IUserAuthService;
import com.clms.service.IUserRoleService;
import com.clms.service.data.IRolePermissionTableService;
import com.clms.service.data.IRoleTableService;
import com.clms.service.data.IUserTableService;
import com.clms.utils.CommonUtil;
import com.clms.utils.RedisConstants;

import cn.dev33.satoken.exception.DisableServiceException;
import cn.dev33.satoken.exception.NotRoleException;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.temp.SaTempUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONArray;
import jakarta.annotation.Resource;

@Service
public class UserAuthServiceImpl implements IUserAuthService {

    @Resource
    private IUserTableService userTableService;

    @Resource
    private IUserRoleService userRoleService;

    @Resource
    private IRolePermissionTableService rolePermissionTableService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Override
    public List<RoleBO> getUserRoleListByUserId(String userId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getUserRoleListByUserId'");
    }

    @Override
    public List<PermissionBO> getPermissionListByUserId(String userId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getPermissionListByUserId'");
    }

    @Override
    public AdminLoginBO adminLogin(UserCredentialDTO userCredentialDTO) {
        // 1. 判断用户是邮箱登录还是手机号登录
        LambdaQueryWrapper<UserTable> queryWrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(userCredentialDTO.getCredential())) {
            if (StrUtil.contains(userCredentialDTO.getCredential(), "@")) {
                queryWrapper.eq(UserTable::getEmail, userCredentialDTO.getCredential());
            } else {
                queryWrapper.eq(UserTable::getPhone, userCredentialDTO.getCredential());
            }
        } else {
            throw new BusinessException(400,"邮箱或手机号不能为空");
        }
        
        UserTable userTable = userTableService.getOne(queryWrapper);
        
        if (userTable == null) {
            throw new BusinessException(400,"用户不存在");
        }
        
        // 2. 验证密码
        if (StrUtil.isNotBlank(userCredentialDTO.getPassword())) {
            if (!userTable.getPassword().equals(DigestUtil.md5Hex(userCredentialDTO.getPassword() + userTable.getId()))) {
                throw new BusinessException(400,"密码错误");
            }
        } else {
            throw new BusinessException(400,"密码不能为空");
        }

        try {
            StpUtil.checkDisable(userTable.getId());
        } catch (DisableServiceException e) {
            throw new BusinessException(403, "用户被禁用");
        }
     
        // 3. Sa-Token 登录
        try {
            StpUtil.checkRole("admin");
        } catch (NotRoleException e) {
            throw new BusinessException(403, "用户无管理员权限");
        }
        StpUtil.login(userTable.getId());
        
        // 4. 获取角色和权限
        List<String> roles = userTable.getUserRoles().toList(String.class);
        List<String> permissions = userTable.getUserPermissions().toList(String.class);
        
        // 5. 构建返回数据
        AdminLoginBO result = new AdminLoginBO();
        AdminLoginBO.DataContent data = new AdminLoginBO.DataContent();
        data.setAvatar(userTable.getAvatarUrl());
        data.setUsername(userTable.getNickname()); // 使用昵称作为用户名
        data.setNickname(userTable.getNickname());
        data.setRoles(roles);
        data.setPermissions(permissions);
        data.setAccessToken(StpUtil.getTokenValue()); // 会话Token
        data.setRefreshToken(SaTempUtil.createToken(userTable.getId(), 60 * 60 * 24 * 7)); // 刷新Token，有效期7天
        
        // 获取 token 过期时间
        long timeout = StpUtil.getTokenTimeout();
        if (timeout > 0) {
            data.setExpires(new Date(System.currentTimeMillis() + timeout * 1000));
        }

        result.setSuccess(true);
        result.setData(data);
        
        return result;
    }
    
    @Override
    public RefreshTokenBO refreshToken(String refreshToken) {
        
        // 1. 验证 refreshToken 是否有效
        if (StrUtil.isBlank(refreshToken)) {
            throw new BusinessException(401, "refreshToken不能为空");
        }
        
        // 2. 解析 refreshToken 获取用户ID
        Object userId = SaTempUtil.parseToken(refreshToken);
        if (userId == null) {
            throw new BusinessException(401, "refreshToken无效或已过期");
        }
        
        // 3. 删除旧的 refreshToken
        SaTempUtil.deleteToken(refreshToken);
        
        // 4. 检查用户是否被禁用
        try {
            StpUtil.checkDisable(userId);
        } catch (DisableServiceException e) {
            throw new BusinessException(403, "用户已被禁用");
        }
        
        // 5. 重新登录生成新的 accessToken
        StpUtil.login(userId);
        
        // 6. 生成新的 refreshToken
        String newRefreshToken = SaTempUtil.createToken(userId, 60 * 60 * 24 * 7); // 7天有效期
        
        // 7. 获取新的 accessToken 过期时间
        long timeout = StpUtil.getTokenTimeout();
        Date expires = null;
        if (timeout > 0) {
            expires = new Date(System.currentTimeMillis() + timeout * 1000);
        }
        
        // 8. 构建返回结果
        RefreshTokenBO result = new RefreshTokenBO();
        RefreshTokenBO.DataContent data = new RefreshTokenBO.DataContent();
        data.setAccessToken(StpUtil.getTokenValue());
        data.setRefreshToken(newRefreshToken);
        data.setExpires(expires);
        
        result.setSuccess(true);
        result.setData(data);
        
        return result;
    }

    @Override
    public UserLoginBO userLogin(UserCredentialDTO userCredentialDTO) {
        // 1. 验证凭证不能为空
        if (StrUtil.isBlank(userCredentialDTO.getCredential())) {
            throw new BusinessException(400, "手机号或邮箱不能为空");
        }

        // 2. 查询用户
        LambdaQueryWrapper<UserTable> queryWrapper = new LambdaQueryWrapper<>();
        if (StrUtil.contains(userCredentialDTO.getCredential(), "@")) {
            queryWrapper.eq(UserTable::getEmail, userCredentialDTO.getCredential());
        } else {
            queryWrapper.eq(UserTable::getPhone, userCredentialDTO.getCredential());
        }
        
        UserTable user = userTableService.getOne(queryWrapper);
        if (user == null) {
            throw new BusinessException(400, "用户不存在");
        }

        // 3. 验证方式：验证码或密码
        if (StrUtil.isNotBlank(userCredentialDTO.getCode())) {
            // 验证码登录
            String codeKey = RedisConstants.AUTH_CODE + userCredentialDTO.getCredential();
            String storedCode = stringRedisTemplate.opsForValue().get(codeKey);
            
            if (StrUtil.isBlank(storedCode) || !storedCode.equals(userCredentialDTO.getCode())) {
                throw new BusinessException(400, "验证码错误或已过期");
            }
            // 删除已使用的验证码
            stringRedisTemplate.delete(codeKey);
        } else if (StrUtil.isNotBlank(userCredentialDTO.getPassword())) {
            // 密码登录
            String encryptedPassword = DigestUtil.md5Hex(userCredentialDTO.getPassword() + user.getId());
            if (!encryptedPassword.equals(user.getPassword())) {
                throw new BusinessException(400, "密码错误");
            }
        } else {
            throw new BusinessException(400, "请输入密码或验证码");
        }

        // 4. 检查用户是否被禁用
        try {
            StpUtil.checkDisable(user.getId());
        } catch (DisableServiceException e) {
            throw new BusinessException(403, "用户已被禁用");
        }

        // 5、为用户绑定最新的默认角色关系
        List<RoleTable> defaultRoles = userRoleService.getDefaultRoles();
        userRoleService.bindRolesToUser(user.getId(), defaultRoles.stream().map(RoleTable::getId).toList());

        // 6. 获取默认角色关联的权限列表
        List<String> defaultPermissions = rolePermissionTableService.getPermissionsByRoleIds(
            defaultRoles.stream().map(RoleTable::getId).toList()
        ).stream().map(PermissionTable::getPermissionName).toList();
        user.setUserRoles(new JSONArray(
            defaultRoles.stream().map(RoleTable::getRoleName).toList()
        ));
        user.setUserPermissions(new JSONArray(defaultPermissions));
        // 6. 执行登录
        StpUtil.login(user.getId());

        // 7. 构建返回数据
        return buildUserLoginBO(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserLoginBO userRegister(AccountRegisterDTO registerDTO) {
        // 1. 验证手机号是否已注册
        LambdaQueryWrapper<UserTable> phoneWrapper = new LambdaQueryWrapper<>();
        phoneWrapper.eq(UserTable::getPhone, registerDTO.getPhone());
        if (userTableService.exists(phoneWrapper)) {
            throw new BusinessException(400, "该手机号已被注册");
        }

        // 2. 验证验证码
        String codeKey = RedisConstants.AUTH_CODE + registerDTO.getPhone();
        String storedCode = stringRedisTemplate.opsForValue().get(codeKey);
        
        if (StrUtil.isBlank(storedCode)) {
            throw new BusinessException(400, "验证码已过期，请重新获取");
        }
        
        if (!storedCode.equals(registerDTO.getVerificationCode())) {
            throw new BusinessException(400, "验证码错误");
        }
        
        // 删除已使用的验证码
        stringRedisTemplate.delete(codeKey);

        // 3. 创建新用户
        UserTable newUser = new UserTable();
        newUser.setId(CommonUtil.generateUuidV7());
        newUser.setPhone(registerDTO.getPhone());
        newUser.setNickname(registerDTO.getNickname());

        // 4. 使用用户ID加密密码并更新
        String encryptedPassword = DigestUtil.md5Hex(registerDTO.getPassword() + newUser.getId());
        newUser.setPassword(encryptedPassword);

        // 5. 获取默认角色
        List<RoleTable> defaultRoles = userRoleService.getDefaultRoles();

        // 6. 设置默认角色字符串列表
        newUser.setUserRoles(new JSONArray(
            defaultRoles.stream().map(RoleTable::getRoleName).toList()
        ));

        // 7. 获取默认角色关联的权限列表
        List<String> defaultPermissions = rolePermissionTableService.getPermissionsByRoleIds(
            defaultRoles.stream().map(RoleTable::getId).toList()
        ).stream().map(PermissionTable::getPermissionName).toList();
        newUser.setUserPermissions(new JSONArray(defaultPermissions));

        // 8. 保存用户信息
        boolean saved = userTableService.save(newUser);
        if (!saved) {
            throw new BusinessException(500, "注册失败，请稍后重试");
        }

        // 9. 为新用户绑定默认角色(必须在用户保存后执行,避免外键约束失败)
        userRoleService.bindRolesToUser(newUser.getId(), defaultRoles.stream().map(RoleTable::getId).toList());

        // 10. 自动登录
        StpUtil.login(newUser.getId());

        // 11. 返回登录结果
        return buildUserLoginBO(newUser);
    }

    @Override
    public void userLogout() {
        StpUtil.logout();
    }

    /**
     * 构建用户登录返回对象（只包含token信息）
     */
    private UserLoginBO buildUserLoginBO(UserTable user) {
        UserLoginBO result = new UserLoginBO();

        // Token信息
        result.setAccessToken(StpUtil.getTokenValue());
        result.setRefreshToken(SaTempUtil.createToken(user.getId(), 60 * 60 * 24 * 7)); // 7天
        
        long timeout = StpUtil.getTokenTimeout();
        if (timeout > 0) {
            result.setExpires(new Date(System.currentTimeMillis() + timeout * 1000));
        }

        return result;
    }

    @Override
    public void getPhoneCode(String phone) {
        // 1. 验证手机号格式
        if (StrUtil.isBlank(phone)) {
            throw new BusinessException(400, "手机号不能为空");
        }
        
        if (!isValidPhone(phone)) {
            throw new BusinessException(400, "手机号格式不正确");
        }
        
        // 2. 检查是否在1分钟内发送过验证码（频率限制）
        String limitKey = RedisConstants.SEND_CODE_LIMIT + phone;
        Boolean hasLimit = stringRedisTemplate.hasKey(limitKey);
        if (Boolean.TRUE.equals(hasLimit)) {
            throw new BusinessException(429, "操作过于频繁，请稍后再试");
        }
        
        // 3. 生成4位随机验证码
        String code = generateCode(4);
        
        // 4. 存储验证码到Redis（5分钟过期，如果存在则覆盖）
        String codeKey = RedisConstants.AUTH_CODE + phone;
        stringRedisTemplate.opsForValue().set(codeKey, code, RedisConstants.AUTH_CODE_TTL, TimeUnit.MINUTES);
        
        // 5. 设置发送频率限制（1分钟）
        stringRedisTemplate.opsForValue().set(limitKey, "1", RedisConstants.SEND_CODE_LIMIT_TTL, TimeUnit.MINUTES);
        
        // 6. 发送消息到RabbitMQ队列
        VerificationCodeBO message = new VerificationCodeBO(phone, code);
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.SMS_EXCHANGE,
            RabbitMQConfig.SMS_ROUTING_KEY,
            message
        ); 
    }

    @Override
    public void getEmailCode(String email) {
        // 1. 验证邮箱格式
        if (StrUtil.isBlank(email)) {
            throw new BusinessException(400, "邮箱不能为空");
        }
        
        if (!isValidEmail(email)) {
            throw new BusinessException(400, "邮箱格式不正确");
        }
        
        // 2. 检查是否在1分钟内发送过验证码（频率限制）
        String limitKey = RedisConstants.SEND_CODE_LIMIT + email;
        Boolean hasLimit = stringRedisTemplate.hasKey(limitKey);
        if (Boolean.TRUE.equals(hasLimit)) {
            throw new BusinessException(429, "操作过于频繁，请稍后再试");
        }
        
        // 3. 生成4位随机验证码
        String code = generateCode(4);
        
        // 4. 存储验证码到Redis（5分钟过期，如果存在则覆盖）
        String codeKey = RedisConstants.AUTH_CODE + email;
        stringRedisTemplate.opsForValue().set(codeKey, code, RedisConstants.AUTH_CODE_TTL, TimeUnit.MINUTES);
        
        // 5. 设置发送频率限制（1分钟）
        stringRedisTemplate.opsForValue().set(limitKey, "1", RedisConstants.SEND_CODE_LIMIT_TTL, TimeUnit.MINUTES);
        
        // 6. 发送消息到RabbitMQ队列
        VerificationCodeBO message = new VerificationCodeBO(email, code);
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.EMAIL_EXCHANGE,
            RabbitMQConfig.EMAIL_ROUTING_KEY,
            message
        );
    }

    /**
     * 生成指定位数的随机数字验证码
     * 
     * @param length 验证码长度
     * @return 验证码字符串
     */
    private String generateCode(int length) {
        StringBuilder code = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }

    /**
     * 验证手机号格式
     * 
     * @param phone 手机号
     * @return 是否有效
     */
    private boolean isValidPhone(String phone) {
        // 简单的中国手机号验证：11位数字，1开头
        return phone != null && phone.matches("^1[3-9]\\d{9}$");
    }

    /**
     * 验证邮箱格式
     * 
     * @param email 邮箱
     * @return 是否有效
     */
    private boolean isValidEmail(String email) {
        // 简单的邮箱格式验证
        return email != null && email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");
    }

    @Override
    public String verifyCodeAndGetStepToken(String credential, String code) {
        // 1. 验证参数
        if (StrUtil.isBlank(credential) || StrUtil.isBlank(code)) {
            throw new BusinessException(400, "凭证和验证码不能为空");
        }

        // 2. 从 Redis 中获取验证码
        String codeKey = RedisConstants.AUTH_CODE + credential;
        String storedCode = stringRedisTemplate.opsForValue().get(codeKey);
        
        if (StrUtil.isBlank(storedCode)) {
            throw new BusinessException(400, "验证码已过期或不存在");
        }

        // 3. 验证码比对
        if (!code.equals(storedCode)) {
            throw new BusinessException(400, "验证码错误");
        }

        // 4. 验证用户是否存在
        LambdaQueryWrapper<UserTable> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserTable::getPhone, credential)
               .or()
               .eq(UserTable::getEmail, credential);
        UserTable user = userTableService.getOne(wrapper);
        
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        // 5. 删除已使用的验证码
        stringRedisTemplate.delete(codeKey);

        // 6. 生成步骤令牌（10分钟有效期）
        String stepToken = IdUtil.fastSimpleUUID();
        String stepTokenKey = RedisConstants.STEP_TOKEN + stepToken;
        
        // 将用户ID存储在stepToken中，用于后续重置密码
        stringRedisTemplate.opsForValue().set(
            stepTokenKey, 
            user.getId(), 
            RedisConstants.STEP_TOKEN_TTL, 
            TimeUnit.MINUTES
        );

        return stepToken;
    }

    @Override
    public void resetPassword(String stepToken, String newPassword) {
        // 1. 验证参数
        if (StrUtil.isBlank(stepToken) || StrUtil.isBlank(newPassword)) {
            throw new BusinessException(400, "令牌和新密码不能为空");
        }

        // 2. 验证密码格式（6-20位）
        if (newPassword.length() < 6 || newPassword.length() > 20) {
            throw new BusinessException(400, "密码长度必须在6-20位之间");
        }

        // 3. 从 Redis 获取步骤令牌对应的用户ID
        String stepTokenKey = RedisConstants.STEP_TOKEN + stepToken;
        String userId = stringRedisTemplate.opsForValue().get(stepTokenKey);
        
        if (StrUtil.isBlank(userId)) {
            throw new BusinessException(400, "令牌已过期或无效");
        }

        // 4. 获取用户信息
        UserTable user = userTableService.getById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        // 5. 加密新密码并更新（使用 密码 + 用户ID 的方式加密，与登录验证保持一致）
        String encryptedPassword = DigestUtil.md5Hex(newPassword + user.getId());
        user.setPassword(encryptedPassword);
        boolean updated = userTableService.updateById(user);
        
        if (!updated) {
            throw new BusinessException(500, "密码重置失败");
        }

        // 6. 删除已使用的步骤令牌
        stringRedisTemplate.delete(stepTokenKey);
    }

    
    
}
