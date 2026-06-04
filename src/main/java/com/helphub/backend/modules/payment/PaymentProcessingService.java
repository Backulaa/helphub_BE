package com.helphub.backend.modules.payment;

import com.helphub.backend.modules.payment.dto.response.PaymentReconciliationResponse;
import vn.payos.model.webhooks.Webhook;

public interface PaymentProcessingService {
    PaymentReconciliationResponse handlePayOsWebhook(Webhook webhook);

    PaymentReconciliationResponse syncPayOsPayment(Long orderCode);

    PaymentReconciliationResponse cancelPayOsPayment(Long orderCode);
}
