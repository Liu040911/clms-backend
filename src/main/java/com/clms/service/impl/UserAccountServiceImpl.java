package com.clms.service.impl;

import java.util.List;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.clms.entity.dto.UserUpdateCredentialDTO;
import com.clms.entity.dto.UserUpdateInfoDTO;
import com.clms.entity.po.PermissionTable;
import com.clms.entity.po.RoleTable;
import com.clms.entity.bo.UserInfoBO;
import com.clms.entity.po.UserTable;
import com.clms.exception.BusinessException;
import com.clms.service.IUserAccountService;
import com.clms.service.data.IRolePermissionTableService;
import com.clms.service.data.IUserRoleTableService;
import com.clms.service.data.impl.UserTableServiceImpl;
import com.clms.utils.RedisConstants;

import cn.hutool.core.util.StrUtil;
import jakarta.annotation.Resource;

@Service
public class UserAccountServiceImpl implements IUserAccountService {
    
    @Resource
    private UserTableServiceImpl userTableService;

    @Resource
    private IUserRoleTableService userRoleTableService;

    @Resource
    private IRolePermissionTableService rolePermissionTableService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public UserInfoBO getCurrentUserInfo(String userId) {

        // 1. 查询用户信息
        UserTable user = userTableService.getById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        
        // 2. 构建返回对象
        UserInfoBO userInfo = new UserInfoBO();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getNickname()); // 使用用户昵称作为用户名
        userInfo.setNickname(user.getNickname());
        userInfo.setAvatar(user.getAvatarUrl());
        userInfo.setPhone(user.getPhone());
        userInfo.setEmail(user.getEmail());
        userInfo.setGender(user.getGender());
        userInfo.setCreateTime(user.getCreateTime());
        userInfo.setUpdateTime(user.getUpdateTime());
        
        List<RoleTable> roleTables = userRoleTableService.getRolesByUserId(user.getId());
        List<String> roleNames = roleTables.stream()
            .map(RoleTable::getRoleName)
            .filter(StrUtil::isNotBlank)
            .distinct()
            .toList();

        List<String> roleIds = roleTables.stream()
            .map(RoleTable::getId)
            .filter(StrUtil::isNotBlank)
            .distinct()
            .toList();

        List<String> permissionStrings = roleIds.isEmpty()
            ? List.of()
            : rolePermissionTableService.getPermissionsByRoleIds(roleIds)
                .stream()
                .map(PermissionTable::getPermissionString)
                .filter(StrUtil::isNotBlank)
                .distinct()
                .toList();

        userInfo.setRoles(roleNames);
        userInfo.setPermissions(permissionStrings);
        
        return userInfo;
    }

    @Override
    public void updateUserAvatar(String userId, String avatarUrl) {
        UserTable user = userTableService.getById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        user.setAvatarUrl(avatarUrl);
        userTableService.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserInfo(String userId, UserUpdateInfoDTO dto) {
        UserTable user = userTableService.getById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        if (StrUtil.isNotBlank(dto.getNickname())) {
            user.setNickname(dto.getNickname());
        }
        if (StrUtil.isNotBlank(dto.getGender())) {
            user.setGender(dto.getGender());
        }

        userTableService.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserCredential(String userId, UserUpdateCredentialDTO dto) {
        // 校验步骤令牌（证明用户已验证当前手机号/邮箱）
        String stepTokenKey = RedisConstants.STEP_TOKEN + dto.getStepToken();
        String stepTokenValue = stringRedisTemplate.opsForValue().get(stepTokenKey);
        if (StrUtil.isBlank(stepTokenValue)) {
            throw new BusinessException(400, "验证令牌已过期，请重新验证");
        }

        // 解析 stepToken: userId:sourceType
        int colonIdx = stepTokenValue.lastIndexOf(":");
        String sourceUserId = stepTokenValue.substring(0, colonIdx);
        String sourceType = stepTokenValue.substring(colonIdx + 1);

        if (!sourceUserId.equals(userId)) {
            throw new BusinessException(400, "验证令牌与当前用户不匹配");
        }

        // 校验凭证类型是否匹配
        if (!"phone".equals(dto.getType()) && !"email".equals(dto.getType())) {
            throw new BusinessException(400, "类型参数错误，必须为 phone 或 email");
        }

        UserTable user = userTableService.getById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        // 检查 stepToken 类型是否匹配更新类型
        // 更换手机号 → 必须用手机验证；更换邮箱 → 必须用邮箱验证
        // 唯一例外: 首次绑定邮箱（email 为空）允许用手机验证降级
        if (!sourceType.equals(dto.getType())) {
            boolean isFirstTimeEmailBind = "email".equals(dto.getType())
                && "phone".equals(sourceType)
                && StrUtil.isBlank(user.getEmail());
            if (!isFirstTimeEmailBind) {
                throw new BusinessException(400,
                    "验证方式与更新类型不匹配，请使用当前" +
                    ("phone".equals(dto.getType()) ? "手机号" : "邮箱") + "进行验证");
            }
        }

        stringRedisTemplate.delete(stepTokenKey);

        String value = dto.getValue();
        if ("phone".equals(dto.getType())) {
            if (!value.matches("^1[3-9]\\d{9}$")) {
                throw new BusinessException(400, "手机号格式不正确");
            }
        } else {
            if (!value.matches("^[\\w.-]+@[\\w.-]+\\.\\w+$")) {
                throw new BusinessException(400, "邮箱格式不正确");
            }
        }

        // 校验验证码
        String codeKey = RedisConstants.AUTH_CODE + value;
        String storedCode = stringRedisTemplate.opsForValue().get(codeKey);
        if (StrUtil.isBlank(storedCode) || !storedCode.equals(dto.getCode())) {
            throw new BusinessException(400, "验证码错误或已过期");
        }
        stringRedisTemplate.delete(codeKey);

        // 查重（排除自身）
        LambdaQueryWrapper<UserTable> wrapper = new LambdaQueryWrapper<>();
        wrapper.ne(UserTable::getId, userId);
        if ("phone".equals(dto.getType())) {
            wrapper.eq(UserTable::getPhone, value);
        } else {
            wrapper.eq(UserTable::getEmail, value);
        }
        if (userTableService.exists(wrapper)) {
            String fieldName = "phone".equals(dto.getType()) ? "手机号" : "邮箱";
            throw new BusinessException(409, "该" + fieldName + "已被其他用户使用");
        }

        if ("phone".equals(dto.getType())) {
            user.setPhone(value);
        } else {
            user.setEmail(value);
        }
        userTableService.updateById(user);
    }
}
