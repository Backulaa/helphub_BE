package com.helphub.backend.modules.payment;

import com.helphub.backend.common.exception.ResourceNotFoundException;
import com.helphub.backend.common.payload.ApiResponse;
import com.helphub.backend.modules.payment.dto.response.PaymentReconciliationResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.payos.model.webhooks.Webhook;

@RestController
@RequiredArgsConstructor
@Validated
@Slf4j
public class PaymentController {

    private final PaymentProcessingService paymentProcessingService;

    @PostMapping({"/api/v1/payments/payos/webhook", "/webhook"})
    public ResponseEntity<String> handlePayOsWebhook(
            @RequestBody Webhook webhook) {

        try {
        PaymentReconciliationResponse response = paymentProcessingService.handlePayOsWebhook(webhook);
            log.info(
                    "PayOS webhook processed: orderCode={}, type={}, status={}",
                    response.getOrderCode(),
                    response.getPaymentType(),
                    response.getStatus());
            return ResponseEntity.ok("OK");
        } catch (ResourceNotFoundException ex) {
            log.warn("Verified PayOS webhook has no local payment target: {}", ex.getMessage());
            return ResponseEntity.ok("OK");
        } catch (Exception ex) {
            log.warn("Invalid PayOS webhook: {}", ex.getMessage());
            return ResponseEntity.badRequest().body("Invalid webhook");
        }
    }

    @GetMapping({"/api/v1/payments/payos/webhook", "/webhook"})
    public ResponseEntity<String> checkPayOsWebhook() {
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/api/v1/payments/payos/return")
    public ResponseEntity<ApiResponse<PaymentReconciliationResponse>> handlePayOsReturn(
            @RequestParam @NotNull Long orderCode) {

        PaymentReconciliationResponse response = paymentProcessingService.syncPayOsPayment(orderCode);

        return ResponseEntity.ok(ApiResponse.<PaymentReconciliationResponse>builder()
                .success(true)
                .message("PayOS payment synced successfully")
                .data(response)
                .build());
    }

    @GetMapping("/api/v1/payments/payos/cancel")
    public ResponseEntity<ApiResponse<PaymentReconciliationResponse>> handlePayOsCancel(
            @RequestParam @NotNull Long orderCode) {

        PaymentReconciliationResponse response = paymentProcessingService.cancelPayOsPayment(orderCode);

        return ResponseEntity.ok(ApiResponse.<PaymentReconciliationResponse>builder()
                .success(true)
                .message("PayOS payment cancelled successfully")
                .data(response)
                .build());
    }
}
