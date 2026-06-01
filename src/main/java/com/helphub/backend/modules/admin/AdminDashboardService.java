package com.helphub.backend.modules.admin;

import com.helphub.backend.modules.admin.dto.response.CategoryStatisticsResponse;
import com.helphub.backend.modules.admin.dto.response.PostStatisticsResponse;
import com.helphub.backend.modules.admin.dto.response.ReportStatisticsResponse;
import com.helphub.backend.modules.admin.dto.response.SupportRequestStatisticsResponse;
import com.helphub.backend.modules.admin.dto.response.UserStatisticsResponse;

public interface AdminDashboardService {

    UserStatisticsResponse getUserStatistics();

    SupportRequestStatisticsResponse getSupportRequestStatistics();

    PostStatisticsResponse getPostStatistics();

    ReportStatisticsResponse getReportStatistics();

    CategoryStatisticsResponse getCategoryStatistics();
}