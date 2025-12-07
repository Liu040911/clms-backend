package com.clms.service;

import java.util.List;

import com.clms.entity.bo.AdminLoginBO;
import com.clms.entity.bo.PermissionBO;
import com.clms.entity.bo.RefreshTokenBO;
import com.clms.entity.bo.RoleBO;
import com.clms.entity.bo.UserLoginBO;
import com.clms.entity.dto.AccountRegisterDTO;
import com.clms.entity.dto.UserCredentialDTO;

public interface IUserAuthService {

    /**
     * 管理员用户登录
     * @param userCredentialDTO 用户登录凭证
     * @return 登录结果
     */
    AdminLoginBO adminLogin(UserCredentialDTO userCredentialDTO);

    /**
     * 用户登录（密码/验证码登录）
     * @param userCredentialDTO 用户登录凭证
     * @return 登录结果
     */
    UserLoginBO userLogin(UserCredentialDTO userCredentialDTO);

    /**
     * 用户注册
     * @param registerDTO 注册信息
     * @return 登录结果（注册成功后自动登录）
     */
    UserLoginBO userRegister(AccountRegisterDTO registerDTO);

    /**
     * 用户登出
     */
    void userLogout();

    /**
     * 刷新Token
     * 使用refreshToken获取新的accessToken和refreshToken
     * @param refreshToken 刷新token字符串
     * @return 新的token信息
     */
    RefreshTokenBO refreshToken(String refreshToken);

    /**
     * 获取用户角色列表
     * 
     * @param userId 用户ID
     * @return 角色列表
     */
    List<RoleBO> getUserRoleListByUserId(String userId);

    /**
     * 获取用户的权限列表
     * 
     * @param userId 用户ID
     * @return 权限列表
    */
    List<PermissionBO> getPermissionListByUserId(String userId);

    /**
     * 获取手机号验证码
     */
    void getPhoneCode(String phone);

    /**
     * 获取邮箱验证码
     */
    void getEmailCode(String email);

    /**
     * 验证码验证并获取重置密码令牌
     * @param credential 手机号或邮箱
     * @param code 验证码
     * @return stepToken 用于下一步重置密码
     */
    String verifyCodeAndGetStepToken(String credential, String code);

    /**
     * 重置密码
     * @param stepToken 步骤令牌
     * @param newPassword 新密码
     */
    void resetPassword(String stepToken, String newPassword);


}
