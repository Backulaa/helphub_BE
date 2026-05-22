package com.helphub.backend.modules.donation.dto.request;

import com.helphub.backend.common.enums.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class CreateDonationRequest {

    @NotNull(message = "Fund id is required")
    private UUID fundId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1000.00", message = "Amount must be at least 1000 VND")
    private BigDecimal amount;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    @Size(max = 100, message = "Transaction code must not exceed 100 characters")
    private String transactionCode;

    @Size(max = 500, message = "Note must not exceed 500 characters")
    private String note;
}