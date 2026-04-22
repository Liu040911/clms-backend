package com.clms.entity.bo;

import java.sql.Timestamp;

import lombok.Data;

@Data
public class LectureCheckInQrCodeBO {

    private String lectureId;

    private String qrCodeBase64;

    private Long ttlSeconds;

    private Timestamp expireAt;
}
