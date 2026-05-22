package com.helphub.backend.modules.expense.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class CreateExpenseRequest {

    @NotNull(message = "Fund id is required")
    private UUID fundId;

    private UUID supportRequestId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1000.00", message = "Amount must be at least 1000 VND")
    private BigDecimal amount;

    @NotBlank(message = "Description is required")
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
}