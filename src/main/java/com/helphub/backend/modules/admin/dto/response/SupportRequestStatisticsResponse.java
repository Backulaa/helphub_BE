package com.helphub.backend.modules.admin.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SupportRequestStatisticsResponse {

    private long totalSupportRequests;

    private long pending;

    private long approved;

    private long inProgress;

    private long rejected;

    private long completed;

    private long cancelled;
}