package com.helphub.backend.modules.admin.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CategoryStatisticsResponse {

    private long totalCategories;

    private long activeCategories;

    private List<CategoryStatisticsItemResponse> categories;
}