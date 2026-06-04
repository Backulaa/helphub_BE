package com.helphub.backend.modules.moneytransfer.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateMoneyTransferTicketRequest {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1000.00", message = "Amount must be at least 1000 VND")
    private BigDecimal amount;

    @NotBlank(message = "Reason is required")
    @Size(max = 1000, message = "Reason must not exceed 1000 characters")
    private String reason;
}
