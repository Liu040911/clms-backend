package com.clms.entity.bo;

import java.time.LocalDateTime;

import com.clms.entity.po.ClassTable;

import lombok.Data;

@Data
public class ClassBO {

    private String id;

    private String location;

    private Integer capacity;

    private String status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    public ClassBO(ClassTable table) {
        this.id = table.getId();
        this.location = table.getLocation();
        this.capacity = table.getCapacity();
        this.status = table.getStatus();
        this.createTime = table.getCreateTime();
        this.updateTime = table.getUpdateTime();
    }
}
