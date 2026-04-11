package com.clms.entity.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.clms.entity.base.BasePO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "lecture_audit_table", autoResultMap = true)
@Schema(name = "LectureAuditTable对象", description = "讲座审核记录表")
public class LectureAuditTable extends BasePO {

    @Schema(description = "讲座ID")
    private String lectureId;

    @Schema(description = "讲座标题快照")
    private String lectureTitle;

    @Schema(description = "审核动作：approve/reject")
    private String auditAction;

    @Schema(description = "审核前状态")
    private String beforeStatus;

    @Schema(description = "审核后状态")
    private String afterStatus;

    @Schema(description = "审核原因/驳回原因")
    private String reason;

    @Schema(description = "审核人ID")
    private String auditorId;

    @Schema(description = "审核人昵称")
    private String auditorName;
}
