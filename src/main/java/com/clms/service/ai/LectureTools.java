package com.clms.service.ai;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.clms.entity.bo.HotLectureBO;
import com.clms.entity.bo.LectureBO;
import com.clms.entity.bo.LectureTagBO;
import com.clms.service.ILectureService;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.annotation.Resource;

@Component("lectureTools")
public class LectureTools {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Resource
    private ILectureService lectureService;

    @Tool("获取最热门的讲座列表。当用户请求推荐、热门趋势或受欢迎的讲座时使用此工具。")
    public String getHotLectures(
            @P("可选的标签ID，用于按分类筛选。留空或传null则获取不分类的热门讲座。")
            String tagId,
            @P("返回的最大讲座数量，默认6条，最多50条。")
            Integer limit) {
        int effectiveLimit = (limit != null && limit > 0 && limit <= 50) ? limit : 6;
        try {
            List<HotLectureBO> list = lectureService.getHotLectureList(
                    (tagId != null && !tagId.isBlank()) ? tagId : null,
                    effectiveLimit);
            if (list == null || list.isEmpty()) {
                return "暂无热门讲座。";
            }
            StringBuilder sb = new StringBuilder("热门讲座列表：\n");
            for (int i = 0; i < list.size(); i++) {
                HotLectureBO lecture = list.get(i);
                sb.append(i + 1).append(". 讲座ID: ").append(lecture.getId())
                        .append(", 标题: ").append(lecture.getTitle())
                        .append(", 分类: ").append(lecture.getTag() != null ? lecture.getTag() : "未分类")
                        .append(", 热度: ").append(lecture.getHotValue())
                        .append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            return "获取热门讲座时发生错误: " + e.getMessage();
        }
    }

    @Tool("按关键词搜索讲座。当用户描述讲座主题或提出'帮我找关于XX的讲座'时使用此工具。")
    public String searchLectures(
            @P("搜索关键词，用于匹配讲座标题。")
            String keywords,
            @P("页码，从1开始，默认第1页。")
            Integer page,
            @P("每页条数，默认5条，最多20条。为便于阅读建议保持较小值。")
            Integer size) {
        int effectivePage = (page != null && page > 0) ? page : 1;
        int effectiveSize = (size != null && size > 0 && size <= 20) ? size : 5;
        try {
            Page<LectureBO> result = lectureService.getLectureList(
                    keywords, null, null, null,
                    effectivePage, effectiveSize, null, null);
            if (result == null || result.getRecords().isEmpty()) {
                return "未找到与 \"" + keywords + "\" 相关的讲座。";
            }
            StringBuilder sb = new StringBuilder("搜索 \"" + keywords + "\" 结果（第")
                    .append(effectivePage).append("页，共").append(result.getTotal()).append("条）：\n");
            for (int i = 0; i < result.getRecords().size(); i++) {
                LectureBO lecture = result.getRecords().get(i);
                sb.append(i + 1).append(". 讲座ID: ").append(lecture.getId())
                        .append(", 标题: ").append(lecture.getTitle())
                        .append(", 状态: ").append(lecture.getStatus())
                        .append(", 讲师: ").append(lecture.getTeacherName() != null ? lecture.getTeacherName() : "未知")
                        .append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            return "搜索讲座时发生错误: " + e.getMessage();
        }
    }

    @Tool("根据ID获取指定讲座的详细信息。当用户询问某个讲座的详情时使用此工具。")
    public String getLectureInfo(
            @P("讲座ID（无连字符的UUID）。")
            String lectureId) {
        try {
            LectureBO bo = lectureService.getLectureInfo(lectureId);
            if (bo == null) {
                return "未找到ID为 " + lectureId + " 的讲座。";
            }
            StringBuilder sb = new StringBuilder();
            sb.append("讲座详情：\n");
            sb.append("- ID: ").append(bo.getId()).append("\n");
            sb.append("- 标题: ").append(bo.getTitle()).append("\n");
            if (bo.getDescription() != null && !bo.getDescription().isBlank()) {
                String desc = bo.getDescription();
                if (desc.length() > 200) {
                    desc = desc.substring(0, 200) + "...";
                }
                sb.append("- 简介: ").append(desc).append("\n");
            }
            sb.append("- 讲师: ").append(bo.getTeacherName() != null ? bo.getTeacherName() : "未知").append("\n");
            if (bo.getTags() != null && !bo.getTags().isEmpty()) {
                String tags = bo.getTags().stream()
                        .map(LectureTagBO::getName)
                        .collect(Collectors.joining("、"));
                sb.append("- 标签: ").append(tags).append("\n");
            }
            if (bo.getLocation() != null) {
                sb.append("- 地点: ").append(bo.getLocation()).append("\n");
            }
            if (bo.getLectureStartTime() != null) {
                sb.append("- 开始时间: ").append(bo.getLectureStartTime().toLocalDateTime().format(DT_FMT)).append("\n");
            }
            if (bo.getLectureEndTime() != null) {
                sb.append("- 结束时间: ").append(bo.getLectureEndTime().toLocalDateTime().format(DT_FMT)).append("\n");
            }
            if (bo.getRegistrationStartsTime() != null) {
                sb.append("- 报名开始: ").append(bo.getRegistrationStartsTime().toLocalDateTime().format(DT_FMT)).append("\n");
            }
            if (bo.getRegistrationEndsTime() != null) {
                sb.append("- 报名截止: ").append(bo.getRegistrationEndsTime().toLocalDateTime().format(DT_FMT)).append("\n");
            }
            sb.append("- 剩余名额: ").append(bo.getRemaining()).append("\n");
            sb.append("- 状态: ").append(bo.getStatus()).append("\n");
            return sb.toString();
        } catch (Exception e) {
            return "获取讲座信息时发生错误: " + e.getMessage();
        }
    }
}
