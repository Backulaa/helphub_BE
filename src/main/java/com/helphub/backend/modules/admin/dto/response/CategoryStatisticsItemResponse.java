package com.helphub.backend.modules.admin.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class CategoryStatisticsItemResponse {

    private UUID categoryId;

    private String categoryName;

    private long supportRequestCount;
}