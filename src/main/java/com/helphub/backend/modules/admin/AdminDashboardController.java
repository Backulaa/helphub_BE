package com.helphub.backend.modules.admin;

import com.helphub.backend.common.payload.ApiResponse;
import com.helphub.backend.modules.admin.dto.response.CategoryStatisticsResponse;
import com.helphub.backend.modules.admin.dto.response.PostStatisticsResponse;
import com.helphub.backend.modules.admin.dto.response.ReportStatisticsResponse;
import com.helphub.backend.modules.admin.dto.response.SupportRequestStatisticsResponse;
import com.helphub.backend.modules.admin.dto.response.UserStatisticsResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<UserStatisticsResponse>> getUserStatistics() {
        UserStatisticsResponse response = adminDashboardService.getUserStatistics();

        return ResponseEntity.ok(ApiResponse.<UserStatisticsResponse>builder()
                .success(true)
                .message("User statistics fetched successfully")
                .data(response)
                .build());
    }

    @GetMapping("/support-requests")
    public ResponseEntity<ApiResponse<SupportRequestStatisticsResponse>> getSupportRequestStatistics() {
        SupportRequestStatisticsResponse response = adminDashboardService.getSupportRequestStatistics();

        return ResponseEntity.ok(ApiResponse.<SupportRequestStatisticsResponse>builder()
                .success(true)
                .message("Support request statistics fetched successfully")
                .data(response)
                .build());
    }

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<CategoryStatisticsResponse>> getCategoryStatistics() {
        CategoryStatisticsResponse response = adminDashboardService.getCategoryStatistics();

        return ResponseEntity.ok(ApiResponse.<CategoryStatisticsResponse>builder()
                .success(true)
                .message("Category statistics fetched successfully")
                .data(response)
                .build());
    }

    @GetMapping("/posts")
    public ResponseEntity<ApiResponse<PostStatisticsResponse>> getPostStatistics() {
        PostStatisticsResponse response = adminDashboardService.getPostStatistics();

        return ResponseEntity.ok(ApiResponse.<PostStatisticsResponse>builder()
                .success(true)
                .message("Post statistics fetched successfully")
                .data(response)
                .build());
    }

    @GetMapping("/reports")
    public ResponseEntity<ApiResponse<ReportStatisticsResponse>> getReportStatistics() {
        ReportStatisticsResponse response = adminDashboardService.getReportStatistics();

        return ResponseEntity.ok(ApiResponse.<ReportStatisticsResponse>builder()
                .success(true)
                .message("Report statistics fetched successfully")
                .data(response)
                .build());
    }
}