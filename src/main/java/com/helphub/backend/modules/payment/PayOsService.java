package com.helphub.backend.modules.payment;

import vn.payos.model.v2.paymentRequests.PaymentLink;
import vn.payos.model.webhooks.Webhook;
import vn.payos.model.webhooks.WebhookData;

import java.math.BigDecimal;

public interface PayOsService {
    PayOsPaymentLinkResult createPaymentLink(BigDecimal amount, String description);

    WebhookData verifyWebhook(Webhook webhook);

    PaymentLink getPaymentLink(Long orderCode);
}
