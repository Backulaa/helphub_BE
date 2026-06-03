package com.helphub.backend.modules.report;

import com.helphub.backend.modules.report.dto.response.ReportDetailResponse;
import com.helphub.backend.modules.report.dto.response.ReportSummaryResponse;
import com.helphub.backend.persistence.entity.Report;
import com.helphub.backend.persistence.entity.User;

import org.springframework.stereotype.Component;

@Component
public class ReportMapper {

    public ReportSummaryResponse toSummaryResponse(Report report) {

        User reporter = report.getReporter();

        return ReportSummaryResponse.builder()
                .id(report.getId())
                .reporterId(reporter.getId())
                .reporterName(reporter.getFullName())
                .targetType(report.getTargetType())
                .targetId(report.getTargetId())
                .reason(report.getReason())
                .status(report.getStatus())
                .createdAt(report.getCreatedAt())
                .build();
    }

    public ReportDetailResponse toDetailResponse(Report report) {

        User reporter = report.getReporter();
        User reviewedBy = report.getReviewedBy();

        return ReportDetailResponse.builder()
                .id(report.getId())
                .reporterId(reporter.getId())
                .reporterName(reporter.getFullName())
                .reporterAvatarUrl(reporter.getAvatarUrl())
                .targetType(report.getTargetType())
                .targetId(report.getTargetId())
                .reason(report.getReason())
                .status(report.getStatus())
                .reviewedBy(reviewedBy != null ? reviewedBy.getId() : null)
                .reviewedAt(report.getReviewedAt())
                .resolutionNote(report.getResolutionNote())
                .createdAt(report.getCreatedAt())
                .updatedAt(report.getUpdatedAt())
                .build();
    }
}