package com.helphub.backend.modules.expense.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
public class ExpenseResponse {
    private UUID id;
    private UUID fundId;
    private String fundName;
    private UUID supportRequestId;
    private String supportRequestTitle;
    private UUID createdBy;
    private String createdByName;
    private BigDecimal amount;
    private String description;
    private LocalDateTime createdAt;
}