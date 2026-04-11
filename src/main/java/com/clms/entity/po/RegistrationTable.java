package com.clms.entity.po;

import java.sql.Timestamp;

import com.baomidou.mybatisplus.annotation.TableName;
import com.clms.entity.base.BasePO;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "registration_table", autoResultMap = true)
public class RegistrationTable extends BasePO {

    private String userId;

    private String lectureId;

    private Timestamp registrationTime;

    private String status;

    private Timestamp checkInTime;
}
