package com.clms.service;

import com.clms.entity.bo.UserInfoBO;
import com.clms.entity.dto.UserMemberEditDTO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public interface IUserMemberService {

	Page<UserInfoBO> getMemberList(Long pageNum, Long pageSize);

	void editMemberInfo(UserMemberEditDTO dto);

	void enableMember(String userId);

	void disableMember(String userId);
}
