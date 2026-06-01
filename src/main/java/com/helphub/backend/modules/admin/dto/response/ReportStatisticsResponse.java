package com.helphub.backend.modules.admin.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReportStatisticsResponse {

    private long totalReports;

    private long supportRequestReports;

    private long postReports;

    private long userReports;

    private long pending;

    private long reviewed;

    private long resolved;
}