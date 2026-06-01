package com.helphub.backend.modules.admin.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserStatisticsResponse {

    private long totalUsers;

    private long activeUsers;

    private long inactiveUsers;

    private long requesters;

    private long volunteers;

    private long collaborators;

    private long admins;
}