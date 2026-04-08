package com.clms.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.clms.entity.base.ResponseEntity;
import com.clms.entity.bo.LectureBO;
import com.clms.entity.dto.LectureDTO;
import com.clms.service.ILectureService;

import cn.dev33.satoken.annotation.SaCheckLogin;
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

	@Operation(summary = "创建讲座")
	@PostMapping("/create")
	public ResponseEntity<Void> createLecture(@RequestBody @Valid LectureDTO lectureDTO) {
		lectureService.createLecture(lectureDTO);
		return ResponseEntity.ok();
	}

	@Operation(summary = "修改讲座")
	@PostMapping("/update")
	public ResponseEntity<Void> updateLecture(
			@RequestParam @NotBlank String lectureId,
			@RequestBody @Valid LectureDTO lectureDTO) {
		lectureService.updateLecture(lectureId, lectureDTO);
		return ResponseEntity.ok();
	}

	@Operation(summary = "删除讲座")
	@PostMapping("/delete")
	public ResponseEntity<Void> deleteLecture(@RequestParam @NotBlank String lectureId) {
		lectureService.deleteLecture(lectureId);
		return ResponseEntity.ok();
	}

	@Operation(summary = "获取讲座信息")
	@GetMapping("/info")
	public ResponseEntity<LectureBO> getLectureInfo(@RequestParam @NotBlank String lectureId) {
		return ResponseEntity.ok(lectureService.getLectureInfo(lectureId));
	}

	@Operation(summary = "获取讲座列表")
	@GetMapping("/list")
	public ResponseEntity<Page<LectureBO>> getLectureList(
			@RequestParam(required = false) String title,
			@RequestParam(required = false) String status,
			@RequestParam(required = false) String teacherId,
			@RequestParam(defaultValue = "1") Integer page,
			@RequestParam(defaultValue = "10") Integer size,
			@RequestParam(required = false) String sort,
			@RequestParam(required = false) String order) {
		return ResponseEntity.ok(lectureService.getLectureList(title, status, teacherId, page, size, sort, order));
	}
}
