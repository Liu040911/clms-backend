package com.clms.entity.bo;

import java.sql.Timestamp;

import com.clms.entity.base.BaseBO;
import com.clms.entity.po.RegistrationTable;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class RegistrationBO extends BaseBO {

    private String userId;

    private String lectureId;

    private Timestamp registrationTime;

    private String status;

    private Timestamp checkInTime;

    public RegistrationBO(RegistrationTable table) {
        this.setId(table.getId());
        this.setCreateTime(table.getCreateTime());
        this.setUpdateTime(table.getUpdateTime());
        this.userId = table.getUserId();
        this.lectureId = table.getLectureId();
        this.registrationTime = table.getRegistrationTime();
        this.status = table.getStatus();
        this.checkInTime = table.getCheckInTime();
    }
}
