package com.clms.controller;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.clms.entity.base.ResponseEntity;
import com.clms.entity.bo.ClassBO;
import com.clms.entity.dto.ClassDTO;
import com.clms.service.IClassService;

import cn.dev33.satoken.annotation.SaCheckLogin;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@RestController
@Tag(name = "教室管理接口")
@RequestMapping("/class")
@SaCheckLogin
@Validated
public class ClassController {

	@Resource
	private IClassService classService;

	@Operation(summary = "创建教室")
	@PostMapping("/create")
	public ResponseEntity<Void> createClass(@RequestBody @Valid ClassDTO classDTO) {
		classService.createClass(classDTO);
		return ResponseEntity.ok();
	}

	@Operation(summary = "修改教室")
	@PostMapping("/update")
	public ResponseEntity<Void> updateClass(
			@RequestParam @NotBlank String classId,
			@RequestBody @Valid ClassDTO classDTO) {
		classService.updateClass(classId, classDTO);
		return ResponseEntity.ok();
	}

	@Operation(summary = "删除教室")
	@PostMapping("/delete")
	public ResponseEntity<Void> deleteClass(@RequestParam @NotBlank String classId) {
		classService.deleteClass(classId);
		return ResponseEntity.ok();
	}

	@Operation(summary = "获取教室信息")
	@GetMapping("/info")
	public ResponseEntity<ClassBO> getClassInfo(@RequestParam @NotBlank String classId) {
		return ResponseEntity.ok(classService.getClassInfo(classId));
	}

	@Operation(summary = "获取教室列表")
	@GetMapping("/list")
	public ResponseEntity<Page<ClassBO>> getClassList(
			@RequestParam(required = false) String location,
			@RequestParam(required = false) String status,
			@RequestParam(defaultValue = "1") Integer page,
			@RequestParam(defaultValue = "10") Integer size,
			@RequestParam(required = false) String sort,
			@RequestParam(required = false) String order) {
		return ResponseEntity.ok(classService.getClassList(location, status, page, size, sort, order));
	}

	@Operation(summary = "获取可用教室列表")
	@GetMapping("/available/list")
	public ResponseEntity<List<ClassBO>> getAvailableClassList() {
		return ResponseEntity.ok(classService.getAvailableClassList());
	}
}
