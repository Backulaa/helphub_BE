package com.helphub.backend.modules.donation.dto.response;

import com.helphub.backend.common.enums.DonationStatus;
import com.helphub.backend.common.enums.PaymentMethod;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
public class DonationResponse {
    private UUID id;
    private UUID fundId;
    private String fundName;
    private UUID donorId;
    private String donorName;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private DonationStatus status;
    private String transactionCode;
    private String note;
    private LocalDateTime createdAt;
}