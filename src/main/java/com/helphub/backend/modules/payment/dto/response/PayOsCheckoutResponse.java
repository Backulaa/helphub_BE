package com.helphub.backend.modules.payment.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
public class PayOsCheckoutResponse {
    private UUID id;
    private String paymentType;
    private BigDecimal amount;
    private Long orderCode;
    private String paymentLinkId;
    private String checkoutUrl;
    private String qrCode;
}
