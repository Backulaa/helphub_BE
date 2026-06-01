package com.helphub.backend.modules.admin.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PostStatisticsResponse {

    private long totalPosts;

    private long active;

    private long underReview;

    private long hidden;

    private long removed;
}