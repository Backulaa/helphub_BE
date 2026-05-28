package com.helphub.backend.modules.report.dto.response;

import com.helphub.backend.common.enums.ReportStatus;
import com.helphub.backend.common.enums.ReportTargetType;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class ReportSummaryResponse {

    private UUID id;

    private UUID reporterId;

    private String reporterName;

    private ReportTargetType targetType;

    private UUID targetId;

    private ReportStatus status;

    private LocalDateTime createdAt;
}