package com.clms.service;

import com.clms.entity.bo.UserInfoBO;
import com.clms.entity.dto.UserUpdateCredentialDTO;
import com.clms.entity.dto.UserUpdateInfoDTO;

public interface IUserAccountService {

    /**
     * 获取当前登录用户信息
     * @return 用户信息
    */
    UserInfoBO getCurrentUserInfo(String userId);

    /**
     * 更新用户头像
     */
    void updateUserAvatar(String userId, String avatarUrl);

    /**
     * 更新用户个人信息（昵称、性别）
     */
    void updateUserInfo(String userId, UserUpdateInfoDTO dto);

    /**
     * 更新用户手机号或邮箱（需验证码）
     */
    void updateUserCredential(String userId, UserUpdateCredentialDTO dto);
}
