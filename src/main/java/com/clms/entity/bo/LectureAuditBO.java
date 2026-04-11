package com.clms.entity.bo;

import com.clms.entity.base.BaseBO;
import com.clms.entity.po.LectureAuditTable;

import cn.hutool.core.bean.BeanUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Schema(name = "LectureAuditBO对象", description = "讲座审核记录BO")
public class LectureAuditBO extends BaseBO {

    public LectureAuditBO(LectureAuditTable lectureAuditTable) {
        BeanUtil.copyProperties(lectureAuditTable, this);
    }

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