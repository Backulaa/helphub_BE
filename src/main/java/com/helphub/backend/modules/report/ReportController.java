package com.helphub.backend.modules.report;

import com.helphub.backend.common.payload.ApiResponse;
import com.helphub.backend.modules.report.dto.request.CreateReportRequest;
import com.helphub.backend.modules.report.dto.request.ResolveReportRequest;
import com.helphub.backend.modules.report.dto.request.ReviewReportRequest;
import com.helphub.backend.modules.report.dto.response.ReportDetailResponse;
import com.helphub.backend.modules.report.dto.response.ReportSummaryResponse;
import com.helphub.backend.security.model.CustomUserDetails;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Validated
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    public ResponseEntity<ApiResponse<ReportDetailResponse>> createReport(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody CreateReportRequest request) {

        ReportDetailResponse response = reportService.createReport(
                currentUser.getUserId(),
                request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<ReportDetailResponse>builder()
                        .success(true)
                        .message("Report created successfully")
                        .data(response)
                        .build());
    }

    @GetMapping("/my-reports")
    public ResponseEntity<ApiResponse<List<ReportSummaryResponse>>> getMyReports(
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        List<ReportSummaryResponse> response = reportService.getMyReports(
                currentUser.getUserId());

        return ResponseEntity.ok(
                ApiResponse.<List<ReportSummaryResponse>>builder()
                        .success(true)
                        .message("My reports fetched successfully")
                        .data(response)
                        .build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ReportSummaryResponse>>> getAllReports() {

        List<ReportSummaryResponse> response = reportService.getAllReports();

        return ResponseEntity.ok(
                ApiResponse.<List<ReportSummaryResponse>>builder()
                        .success(true)
                        .message("Reports fetched successfully")
                        .data(response)
                        .build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<ReportSummaryResponse>>> getPendingReports() {

        List<ReportSummaryResponse> response = reportService.getPendingReports();

        return ResponseEntity.ok(
                ApiResponse.<List<ReportSummaryResponse>>builder()
                        .success(true)
                        .message("Pending reports fetched successfully")
                        .data(response)
                        .build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReportDetailResponse>> getReportById(
            @PathVariable @NotNull UUID id) {

        ReportDetailResponse response = reportService.getReportById(id);

        return ResponseEntity.ok(
                ApiResponse.<ReportDetailResponse>builder()
                        .success(true)
                        .message("Report fetched successfully")
                        .data(response)
                        .build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/review")
    public ResponseEntity<ApiResponse<ReportDetailResponse>> reviewReport(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable @NotNull UUID id,
            @Valid @RequestBody ReviewReportRequest request) {

        ReportDetailResponse response = reportService.reviewReport(
                currentUser.getUserId(),
                id,
                request);

        return ResponseEntity.ok(
                ApiResponse.<ReportDetailResponse>builder()
                        .success(true)
                        .message("Report reviewed successfully")
                        .data(response)
                        .build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/resolve")
    public ResponseEntity<ApiResponse<ReportDetailResponse>> resolveReport(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable @NotNull UUID id,
            @Valid @RequestBody ResolveReportRequest request) {

        ReportDetailResponse response = reportService.resolveReport(
                currentUser.getUserId(),
                id,
                request);

        return ResponseEntity.ok(
                ApiResponse.<ReportDetailResponse>builder()
                        .success(true)
                        .message("Report resolved successfully")
                        .data(response)
                        .build());
    }
}