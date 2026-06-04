package com.helphub.backend.modules.payment;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PayOsPaymentLinkResult {
    private Long orderCode;
    private String paymentLinkId;
    private String checkoutUrl;
    private String qrCode;
}
