package com.helphub.backend.modules.payment;

import vn.payos.model.v2.paymentRequests.PaymentLink;
import vn.payos.model.webhooks.Webhook;
import vn.payos.model.webhooks.WebhookData;

import java.math.BigDecimal;
import java.util.Map;

public interface PayOsService {
    PayOsPaymentLinkResult createPaymentLink(BigDecimal amount, String description);

    PayOsPaymentLinkResult createPaymentLink(
            BigDecimal amount,
            String description,
            Map<String, String> redirectParams);

    PayOsPaymentLinkResult createPaymentLink(
            BigDecimal amount,
            String description,
            Map<String, String> redirectParams,
            String returnUrl,
            String cancelUrl);

    WebhookData verifyWebhook(Webhook webhook);

    PaymentLink getPaymentLink(Long orderCode);
}
