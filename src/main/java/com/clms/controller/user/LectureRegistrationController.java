package com.clms.controller.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clms.entity.base.ResponseEntity;
import com.clms.entity.bo.UserLectureAppointmentBO;
import com.clms.entity.bo.RegistrationBO;
import com.clms.entity.dto.LectureRegistrationDTO;
import com.clms.enums.RegistrationStatusEnum;
import com.clms.exception.BusinessException;
import com.clms.service.IUserLectureRegistrationService;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/user/registration")
@SaCheckLogin
@Tag(name = "用户讲座报名接口")
@Validated
public class LectureRegistrationController {

    @Resource
    private IUserLectureRegistrationService userLectureRegistrationService;

    @Operation(summary = "讲座报名")
    @PostMapping("/create")
    public ResponseEntity<RegistrationBO> registerLecture(@RequestBody @Valid LectureRegistrationDTO registrationDTO) {
        String userId = (String) StpUtil.getTokenInfo().getLoginId();
        return ResponseEntity.ok(userLectureRegistrationService.registerLecture(userId, registrationDTO));
    }

    @Operation(summary = "取消讲座报名")
    @PostMapping("/cancel")
    public ResponseEntity<RegistrationBO> cancelLectureRegistration(@RequestBody @Valid LectureRegistrationDTO registrationDTO) {
        String userId = (String) StpUtil.getTokenInfo().getLoginId();
        return ResponseEntity.ok(userLectureRegistrationService.cancelLectureRegistration(userId, registrationDTO));
    }

    @Operation(summary = "分页查询用户预约讲座列表")
    @GetMapping("/list")
    public ResponseEntity<Page<UserLectureAppointmentBO>> getUserLectureAppointmentList(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        if (status != null && !RegistrationStatusEnum.isValid(status)) {
            throw new BusinessException(400, "无效的预约状态");
        }
        String userId = (String) StpUtil.getTokenInfo().getLoginId();
        return ResponseEntity.ok(userLectureRegistrationService.getUserLectureAppointmentList(userId, status, page, size));
    }
}
