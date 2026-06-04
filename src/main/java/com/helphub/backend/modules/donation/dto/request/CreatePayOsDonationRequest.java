package com.helphub.backend.modules.donation.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class CreatePayOsDonationRequest {

    @NotNull(message = "Fund id is required")
    private UUID fundId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1000.00", message = "Amount must be at least 1000 VND")
    private BigDecimal amount;

    @Size(max = 500, message = "Note must not exceed 500 characters")
    private String note;
}
