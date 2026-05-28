package com.helphub.backend.modules.report.dto.response;

import com.helphub.backend.common.enums.ReportStatus;
import com.helphub.backend.common.enums.ReportTargetType;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class ReportDetailResponse {

    private UUID id;

    private UUID reporterId;

    private String reporterName;

    private String reporterAvatarUrl;

    private ReportTargetType targetType;

    private UUID targetId;

    private String reason;

    private ReportStatus status;

    private UUID reviewedBy;

    private LocalDateTime reviewedAt;

    private String resolutionNote;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}