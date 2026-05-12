package com.clms.controller;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.clms.entity.base.ResponseEntity;
import com.clms.entity.bo.HotLectureBO;
import com.clms.entity.bo.LectureAnalyticsOverviewBO;
import com.clms.entity.bo.LectureAnalyticsTagTopBO;
import com.clms.entity.bo.LectureAnalyticsTrendPointBO;
import com.clms.entity.bo.LectureAuditBO;
import com.clms.entity.bo.LectureBO;
import com.clms.entity.bo.LectureCheckInQrCodeBO;
import com.clms.entity.bo.LectureTagBO;
import com.clms.entity.dto.LectureDTO;
import com.clms.service.IUserLectureRegistrationService;
import com.clms.service.ILectureService;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.stp.StpUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@RestController
@Tag(name = "讲座管理接口")
@RequestMapping("/lecture")
@SaCheckLogin
@Validated
public class LectureController {

	@Resource
	private ILectureService lectureService;

	@Resource
	private IUserLectureRegistrationService userLectureRegistrationService;

	@Operation(summary = "创建讲座")
	@SaCheckPermission("lecture:create")
	@PostMapping("/create")
	public ResponseEntity<Void> createLecture(@RequestBody @Valid LectureDTO lectureDTO) {
		lectureService.createLecture(lectureDTO);
		return ResponseEntity.ok();
	}

	@Operation(summary = "修改讲座")
	@SaCheckPermission("lecture:update")
	@PostMapping("/update")
	public ResponseEntity<Void> updateLecture(
			@RequestParam @NotBlank String lectureId,
			@RequestBody @Valid LectureDTO lectureDTO) {
		lectureService.updateLecture(lectureId, lectureDTO);
		return ResponseEntity.ok();
	}

	@Operation(summary = "删除讲座")
	@SaCheckPermission("lecture:delete")
	@PostMapping("/delete")
	public ResponseEntity<Void> deleteLecture(@RequestParam @NotBlank String lectureId) {
		lectureService.deleteLecture(lectureId);
		return ResponseEntity.ok();
	}

	@Operation(summary = "讲座通过")
	@SaCheckPermission("lecture:approve")
	@PostMapping("/approve")
	public ResponseEntity<Void> approveLecture(@RequestParam @NotBlank String lectureId) {
		lectureService.approveLecture(lectureId);
		return ResponseEntity.ok();
	}

	@Operation(summary = "讲座驳回")
	@SaCheckPermission("lecture:reject")
	@PostMapping("/reject")
	public ResponseEntity<Void> rejectLecture(
			@RequestParam @NotBlank String lectureId,
			@RequestParam @NotBlank String reason) {
		lectureService.rejectLecture(lectureId, reason);
		return ResponseEntity.ok();
	}

	@Operation(summary = "获取讲座信息")
	@GetMapping("/info")
	@SaIgnore
	public ResponseEntity<LectureBO> getLectureInfo(@RequestParam @NotBlank String lectureId) {
		return ResponseEntity.ok(lectureService.getLectureInfo(lectureId));
	}

	@Operation(summary = "获取讲座审核记录")
	@SaCheckPermission("lecture:auditList")
	@GetMapping("/audit/list")
	public ResponseEntity<Page<LectureAuditBO>> getLectureAuditList(
			@RequestParam @NotBlank String lectureId,
			@RequestParam(defaultValue = "1") Integer page,
			@RequestParam(defaultValue = "10") Integer size) {
		return ResponseEntity.ok(lectureService.getLectureAuditList(lectureId, page, size));
	}

	@Operation(summary = "获取讲座列表")
	@SaCheckPermission("lecture:list")
	@GetMapping("/list")
	public ResponseEntity<Page<LectureBO>> getLectureList(
			@RequestParam(required = false) String title,
			@RequestParam(required = false) String status,
			@RequestParam(required = false) String teacherId,
			@RequestParam(required = false) String tagId,
			@RequestParam(defaultValue = "1") Integer page,
			@RequestParam(defaultValue = "10") Integer size,
			@RequestParam(required = false) String sort,
			@RequestParam(required = false) String order) {
		return ResponseEntity.ok(lectureService.getLectureList(title, status, teacherId, tagId, page, size, sort, order));
	}

	@SaIgnore
	@Operation(summary = "获取讲座标签列表")
	@GetMapping("/tag/list")
	public ResponseEntity<List<LectureTagBO>> getLectureTagList() {
		// 首页分类入口：仅返回启用状态标签，由服务层按sort升序输出。
		return ResponseEntity.ok(lectureService.getLectureTagList());
	}

	@SaIgnore
	@Operation(summary = "获取热门讲座列表")
	@GetMapping("/hot/list")
	public ResponseEntity<List<HotLectureBO>> getHotLectureList(
			@RequestParam(required = false) String tagId,
			@RequestParam(defaultValue = "6") Integer limit) {
		// V1热门：按报名量排序，支持可选标签过滤。
		return ResponseEntity.ok(lectureService.getHotLectureList(tagId, limit));
	}

	@Operation(summary = "手动结束讲座（仅教师或管理员）")
	@SaCheckPermission("lecture:end")
	@PostMapping("/end")
	public ResponseEntity<Void> endLecture(@RequestParam @NotBlank String lectureId) {
		String userId = (String) StpUtil.getTokenInfo().getLoginId();
		lectureService.endLecture(userId, lectureId);
		return ResponseEntity.ok();
	}

	@Operation(summary = "教师端获取讲座签到二维码")
	@SaCheckPermission("lecture:checkInQrCode")
	@GetMapping("/check-in/qrcode")
	public ResponseEntity<LectureCheckInQrCodeBO> getLectureCheckInQrCode(@RequestParam @NotBlank String lectureId) {
        String userId = (String) StpUtil.getTokenInfo().getLoginId();
		return ResponseEntity.ok(userLectureRegistrationService.getLectureCheckInQrCode(userId, lectureId));
	}

	@Operation(summary = "讲座分析概览")
	@SaCheckPermission("lecture:analyticsOverview")
	@GetMapping("/analytics/overview")
	public ResponseEntity<LectureAnalyticsOverviewBO> getLectureAnalyticsOverview(
			@RequestParam(required = false) String startTime,
			@RequestParam(required = false) String endTime,
			@RequestParam(required = false) String teacherId,
			@RequestParam(required = false) String tagId,
			@RequestParam(required = false) String classId) {
		return ResponseEntity.ok(lectureService.getLectureAnalyticsOverview(startTime, endTime, teacherId, tagId, classId));
	}

	@Operation(summary = "讲座分析趋势")
	@SaCheckPermission("lecture:analyticsTrend")
	@GetMapping("/analytics/trend")
	public ResponseEntity<List<LectureAnalyticsTrendPointBO>> getLectureAnalyticsTrend(
			@RequestParam(required = false) String startTime,
			@RequestParam(required = false) String endTime,
			@RequestParam(defaultValue = "day") String granularity,
			@RequestParam(required = false) String teacherId,
			@RequestParam(required = false) String tagId) {
		return ResponseEntity.ok(lectureService.getLectureAnalyticsTrend(startTime, endTime, granularity, teacherId, tagId));
	}

	@Operation(summary = "讲座标签热度Top")
	@SaCheckPermission("lecture:analyticsTagTop")
	@GetMapping("/analytics/tag-top")
	public ResponseEntity<List<LectureAnalyticsTagTopBO>> getLectureAnalyticsTagTop(
			@RequestParam(required = false) String startTime,
			@RequestParam(required = false) String endTime,
			@RequestParam(defaultValue = "10") Integer topN,
			@RequestParam(defaultValue = "registration") String metric) {
		return ResponseEntity.ok(lectureService.getLectureAnalyticsTagTop(startTime, endTime, topN, metric));
	}
}
