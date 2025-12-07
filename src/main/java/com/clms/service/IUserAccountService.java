package com.clms.service;

import com.clms.entity.bo.UserInfoBO;

public interface IUserAccountService {

    /**
     * 获取当前登录用户信息
     * @return 用户信息
    */
    UserInfoBO getCurrentUserInfo(String userId);
}
