package com.helphub.backend.modules.supportneed.dto.response;

import com.helphub.backend.common.enums.PaymentMethod;
import com.helphub.backend.common.enums.SupportNeedContributionStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
public class SupportNeedContributionResponse {
    private UUID id;
    private UUID supportNeedId;
    private String needName;
    private UUID contributorId;
    private String contributorName;
    private BigDecimal quantity;
    private PaymentMethod paymentMethod;
    private SupportNeedContributionStatus status;
    private String transactionCode;
    private Long payosOrderCode;
    private String payosPaymentLinkId;
    private String checkoutUrl;
    private String note;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
}