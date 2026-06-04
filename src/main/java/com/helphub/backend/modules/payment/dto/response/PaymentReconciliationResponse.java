package com.helphub.backend.modules.payment.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class PaymentReconciliationResponse {
    private UUID id;
    private String paymentType;
    private Long orderCode;
    private String status;
    private String transactionCode;
}
