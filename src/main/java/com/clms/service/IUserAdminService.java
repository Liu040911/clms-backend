package com.clms.service;

import com.clms.entity.bo.UserInfoBO;
import com.clms.entity.dto.UserAdminDTO;
import com.clms.entity.dto.UserAdminEditDTO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public interface IUserAdminService {

	void addAdmin(UserAdminDTO userAdminDTO);

	void disableAdminPermission(String userId);

	void enableAdminPermission(String userId);

	void editAdminInfo(UserAdminEditDTO userAdminEditDTO);

	Page<UserInfoBO> getAdminList(Long pageNum, Long pageSize);
}
