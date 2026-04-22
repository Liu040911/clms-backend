package com.clms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.clms.entity.bo.LectureCheckInQrCodeBO;
import com.clms.entity.bo.RegistrationBO;
import com.clms.entity.bo.UserLectureAppointmentBO;
import com.clms.entity.dto.LectureRegistrationDTO;

public interface IUserLectureRegistrationService {

    RegistrationBO registerLecture(String userId, LectureRegistrationDTO registrationDTO);

    RegistrationBO cancelLectureRegistration(String userId, LectureRegistrationDTO registrationDTO);

    Page<UserLectureAppointmentBO> getUserLectureAppointmentList(String userId, String status, Integer page, Integer size);

    LectureCheckInQrCodeBO getLectureCheckInQrCode(String operatorUserId, String lectureId);

    RegistrationBO checkInByQrCode(String userId, String token);
}
