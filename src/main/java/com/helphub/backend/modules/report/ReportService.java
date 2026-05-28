package com.helphub.backend.modules.report;

import com.helphub.backend.modules.report.dto.request.CreateReportRequest;
import com.helphub.backend.modules.report.dto.request.ResolveReportRequest;
import com.helphub.backend.modules.report.dto.request.ReviewReportRequest;
import com.helphub.backend.modules.report.dto.response.ReportDetailResponse;
import com.helphub.backend.modules.report.dto.response.ReportSummaryResponse;

import java.util.List;
import java.util.UUID;

public interface ReportService {

    ReportDetailResponse createReport(UUID currentUserId, CreateReportRequest request);

    List<ReportSummaryResponse> getMyReports(UUID currentUserId);

    List<ReportSummaryResponse> getAllReports();

    List<ReportSummaryResponse> getPendingReports();

    ReportDetailResponse getReportById(UUID reportId);

    ReportDetailResponse reviewReport(
            UUID adminId,
            UUID reportId,
            ReviewReportRequest request);

    ReportDetailResponse resolveReport(
            UUID adminId,
            UUID reportId,
            ResolveReportRequest request);
}