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
import com.clms.entity.bo.TagBO;
import com.clms.entity.dto.TagDTO;
import com.clms.service.ITagService;

import cn.dev33.satoken.annotation.SaCheckLogin;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@RestController
@Tag(name = "标签管理接口")
@RequestMapping("/tag")
@SaCheckLogin
@Validated
public class TagController {

	@Resource
	private ITagService tagService;

	@Operation(summary = "创建标签")
	@PostMapping("/create")
	public ResponseEntity<Void> createTag(@RequestBody @Valid TagDTO tagDTO) {
		tagService.createTag(tagDTO);
		return ResponseEntity.ok();
	}

	@Operation(summary = "修改标签")
	@PostMapping("/update")
	public ResponseEntity<Void> updateTag(
			@RequestParam @NotBlank String tagId,
			@RequestBody @Valid TagDTO tagDTO) {
		tagService.updateTag(tagId, tagDTO);
		return ResponseEntity.ok();
	}

	@Operation(summary = "删除标签")
	@PostMapping("/delete")
	public ResponseEntity<Void> deleteTag(@RequestParam @NotBlank String tagId) {
		tagService.deleteTag(tagId);
		return ResponseEntity.ok();
	}

	@Operation(summary = "获取标签详情")
	@GetMapping("/info")
	public ResponseEntity<TagBO> getTagInfo(@RequestParam @NotBlank String tagId) {
		return ResponseEntity.ok(tagService.getTagInfo(tagId));
	}

	@Operation(summary = "获取标签列表")
	@GetMapping("/list")
	public ResponseEntity<Page<TagBO>> getTagList(
			@RequestParam(required = false) String tagName,
			@RequestParam(required = false) String tagType,
			@RequestParam(required = false) String tagStatus,
			@RequestParam(defaultValue = "1") Integer page,
			@RequestParam(defaultValue = "10") Integer size,
			@RequestParam(required = false) String sort,
			@RequestParam(required = false) String order) {
		return ResponseEntity.ok(tagService.getTagList(tagName, tagType, tagStatus, page, size, sort, order));
	}
}
