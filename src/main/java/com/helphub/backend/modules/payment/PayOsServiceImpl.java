package com.helphub.backend.modules.payment;

import com.helphub.backend.common.exception.BadRequestException;
import com.helphub.backend.config.PayOsProperties;
import com.helphub.backend.config.PaymentProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.PaymentLink;
import vn.payos.model.webhooks.Webhook;
import vn.payos.model.webhooks.WebhookData;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class PayOsServiceImpl implements PayOsService {

    private static final int PAYOS_DESCRIPTION_MAX_LENGTH = 25;

    private final PayOsProperties payOsProperties;
    private final PaymentProperties paymentProperties;

    @Override
    public PayOsPaymentLinkResult createPaymentLink(BigDecimal amount, String description) {
        Long orderCode = generateOrderCode();

        CreatePaymentLinkRequest request = CreatePaymentLinkRequest.builder()
                .orderCode(orderCode)
                .amount(toPayOsAmount(amount))
                .description(normalizeDescription(description))
                .returnUrl(requiredPaymentUrl(paymentProperties.getReturnUrl(), "PayOS return URL is not configured"))
                .cancelUrl(requiredPaymentUrl(paymentProperties.getCancelUrl(), "PayOS cancel URL is not configured"))
                .build();

        CreatePaymentLinkResponse response = client().paymentRequests().create(request);

        return PayOsPaymentLinkResult.builder()
                .orderCode(response.getOrderCode())
                .paymentLinkId(response.getPaymentLinkId())
                .checkoutUrl(response.getCheckoutUrl())
                .qrCode(response.getQrCode())
                .build();
    }

    @Override
    public WebhookData verifyWebhook(Webhook webhook) {
        return client().webhooks().verify(webhook);
    }

    @Override
    public PaymentLink getPaymentLink(Long orderCode) {
        if (orderCode == null) {
            throw new BadRequestException("PayOS order code is required");
        }

        return client().paymentRequests().get(orderCode);
    }

    private PayOS client() {
        if (!StringUtils.hasText(payOsProperties.getClientId())
                || !StringUtils.hasText(payOsProperties.getApiKey())
                || !StringUtils.hasText(payOsProperties.getChecksumKey())) {
            throw new BadRequestException("PayOS credentials are not configured");
        }

        return new PayOS(
                payOsProperties.getClientId(),
                payOsProperties.getApiKey(),
                payOsProperties.getChecksumKey());
    }

    private Long toPayOsAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Amount must be greater than 0");
        }

        try {
            return amount.setScale(0, RoundingMode.UNNECESSARY).longValueExact();
        } catch (ArithmeticException ex) {
            throw new BadRequestException("PayOS amount must be a whole VND amount");
        }
    }

    private String normalizeDescription(String description) {
        String normalized = StringUtils.hasText(description) ? description.trim() : "HelpHub payment";

        if (normalized.length() <= PAYOS_DESCRIPTION_MAX_LENGTH) {
            return normalized;
        }

        return normalized.substring(0, PAYOS_DESCRIPTION_MAX_LENGTH);
    }

    private String requiredPaymentUrl(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new BadRequestException(message);
        }

        return value.trim();
    }

    private Long generateOrderCode() {
        return Instant.now().toEpochMilli();
    }
}
