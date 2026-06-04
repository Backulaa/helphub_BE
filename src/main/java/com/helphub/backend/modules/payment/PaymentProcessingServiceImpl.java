package com.helphub.backend.modules.payment;

import com.helphub.backend.common.enums.DonationStatus;
import com.helphub.backend.common.enums.SupportNeedContributionStatus;
import com.helphub.backend.common.enums.SupportRequestStatus;
import com.helphub.backend.common.exception.BadRequestException;
import com.helphub.backend.common.exception.ResourceNotFoundException;
import com.helphub.backend.modules.payment.dto.response.PaymentReconciliationResponse;
import com.helphub.backend.persistence.entity.CommunityFund;
import com.helphub.backend.persistence.entity.Donation;
import com.helphub.backend.persistence.entity.SupportNeed;
import com.helphub.backend.persistence.entity.SupportNeedContribution;
import com.helphub.backend.persistence.entity.SupportRequest;
import com.helphub.backend.persistence.repository.CommunityFundRepository;
import com.helphub.backend.persistence.repository.DonationRepository;
import com.helphub.backend.persistence.repository.SupportNeedContributionRepository;
import com.helphub.backend.persistence.repository.SupportNeedRepository;
import com.helphub.backend.persistence.repository.SupportRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import vn.payos.model.v2.paymentRequests.PaymentLink;
import vn.payos.model.v2.paymentRequests.PaymentLinkStatus;
import vn.payos.model.webhooks.Webhook;
import vn.payos.model.webhooks.WebhookData;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentProcessingServiceImpl implements PaymentProcessingService {

    private final PayOsService payOsService;
    private final DonationRepository donationRepository;
    private final CommunityFundRepository communityFundRepository;
    private final SupportNeedContributionRepository supportNeedContributionRepository;
    private final SupportNeedRepository supportNeedRepository;
    private final SupportRequestRepository supportRequestRepository;

    @Override
    @Transactional
    public PaymentReconciliationResponse handlePayOsWebhook(Webhook webhook) {
        WebhookData data = payOsService.verifyWebhook(webhook);
        Long orderCode = data.getOrderCode();

        if (orderCode == null) {
            throw new BadRequestException("PayOS webhook is missing order code");
        }

        boolean paid = Boolean.TRUE.equals(webhook.getSuccess()) && "00".equals(data.getCode());
        String transactionCode = normalizeTransactionCode(data.getReference());

        if (paid) {
            return markPaid(orderCode, BigDecimal.valueOf(data.getAmount()), transactionCode);
        }

        return markFailed(orderCode, transactionCode);
    }

    @Override
    @Transactional
    public PaymentReconciliationResponse syncPayOsPayment(Long orderCode) {
        PaymentLink paymentLink = payOsService.getPaymentLink(orderCode);

        if (paymentLink.getStatus() == PaymentLinkStatus.PAID) {
            return markPaid(
                    orderCode,
                    BigDecimal.valueOf(paymentLink.getAmountPaid()),
                    normalizeTransactionCode(paymentLink.getId()));
        }

        if (paymentLink.getStatus() == PaymentLinkStatus.CANCELLED
                || paymentLink.getStatus() == PaymentLinkStatus.EXPIRED) {
            return markCancelled(orderCode, normalizeTransactionCode(paymentLink.getId()));
        }

        if (paymentLink.getStatus() == PaymentLinkStatus.FAILED) {
            return markFailed(orderCode, normalizeTransactionCode(paymentLink.getId()));
        }

        return currentStatus(orderCode);
    }

    @Override
    @Transactional
    public PaymentReconciliationResponse cancelPayOsPayment(Long orderCode) {
        return markCancelled(orderCode, null);
    }

    private PaymentReconciliationResponse markPaid(
            Long orderCode,
            BigDecimal paidAmount,
            String transactionCode) {

        Donation donation = donationRepository.findByPayosOrderCode(orderCode).orElse(null);
        if (donation != null) {
            validatePaidAmount(donation.getAmount(), paidAmount);
            return markDonationPaid(donation, transactionCode);
        }

        SupportNeedContribution contribution = supportNeedContributionRepository
                .findByPayosOrderCode(orderCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "PayOS payment target not found for order code: " + orderCode));

        validatePaidAmount(contribution.getQuantity(), paidAmount);
        return markContributionPaid(contribution, transactionCode);
    }

    private PaymentReconciliationResponse markDonationPaid(Donation donation, String transactionCode) {
        if (donation.getStatus() == DonationStatus.SUCCESS) {
            return toDonationPaymentResponse(donation);
        }

        if (donation.getStatus() != DonationStatus.PENDING) {
            throw new BadRequestException("Donation payment is not pending");
        }

        donation.setStatus(DonationStatus.SUCCESS);
        donation.setTransactionCode(transactionCode);
        donation.setPaidAt(LocalDateTime.now());

        CommunityFund fund = donation.getFund();
        fund.setTotalBalance(fund.getTotalBalance().add(donation.getAmount()));

        communityFundRepository.save(fund);
        Donation savedDonation = donationRepository.save(donation);

        return toDonationPaymentResponse(savedDonation);
    }

    private PaymentReconciliationResponse markContributionPaid(
            SupportNeedContribution contribution,
            String transactionCode) {

        if (contribution.getStatus() == SupportNeedContributionStatus.SUCCESS) {
            return toContributionPaymentResponse(contribution);
        }

        if (contribution.getStatus() != SupportNeedContributionStatus.PENDING) {
            throw new BadRequestException("Support need contribution payment is not pending");
        }

        SupportNeed supportNeed = contribution.getSupportNeed();
        BigDecimal newReceivedQuantity = supportNeed.getReceivedQuantity().add(contribution.getQuantity());

        if (newReceivedQuantity.compareTo(supportNeed.getRequiredQuantity()) > 0) {
            throw new BadRequestException("Paid contribution exceeds remaining support need amount");
        }

        contribution.setStatus(SupportNeedContributionStatus.SUCCESS);
        contribution.setTransactionCode(transactionCode);
        contribution.setPaidAt(LocalDateTime.now());
        supportNeed.setReceivedQuantity(newReceivedQuantity);

        supportNeedRepository.save(supportNeed);
        updateSupportRequestStatusAfterContribution(supportNeed.getSupportRequest());

        SupportNeedContribution savedContribution = supportNeedContributionRepository.save(contribution);

        return toContributionPaymentResponse(savedContribution);
    }

    private PaymentReconciliationResponse markCancelled(Long orderCode, String transactionCode) {
        Donation donation = donationRepository.findByPayosOrderCode(orderCode).orElse(null);
        if (donation != null) {
            if (donation.getStatus() == DonationStatus.PENDING) {
                donation.setStatus(DonationStatus.CANCELLED);
                donation.setTransactionCode(transactionCode);
                donationRepository.save(donation);
            }
            return toDonationPaymentResponse(donation);
        }

        SupportNeedContribution contribution = supportNeedContributionRepository
                .findByPayosOrderCode(orderCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "PayOS payment target not found for order code: " + orderCode));

        if (contribution.getStatus() == SupportNeedContributionStatus.PENDING) {
            contribution.setStatus(SupportNeedContributionStatus.CANCELLED);
            contribution.setTransactionCode(transactionCode);
            supportNeedContributionRepository.save(contribution);
        }

        return toContributionPaymentResponse(contribution);
    }

    private PaymentReconciliationResponse markFailed(Long orderCode, String transactionCode) {
        Donation donation = donationRepository.findByPayosOrderCode(orderCode).orElse(null);
        if (donation != null) {
            if (donation.getStatus() == DonationStatus.PENDING) {
                donation.setStatus(DonationStatus.FAILED);
                donation.setTransactionCode(transactionCode);
                donationRepository.save(donation);
            }
            return toDonationPaymentResponse(donation);
        }

        SupportNeedContribution contribution = supportNeedContributionRepository
                .findByPayosOrderCode(orderCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "PayOS payment target not found for order code: " + orderCode));

        if (contribution.getStatus() == SupportNeedContributionStatus.PENDING) {
            contribution.setStatus(SupportNeedContributionStatus.FAILED);
            contribution.setTransactionCode(transactionCode);
            supportNeedContributionRepository.save(contribution);
        }

        return toContributionPaymentResponse(contribution);
    }

    private PaymentReconciliationResponse currentStatus(Long orderCode) {
        return donationRepository.findByPayosOrderCode(orderCode)
                .map(this::toDonationPaymentResponse)
                .orElseGet(() -> supportNeedContributionRepository.findByPayosOrderCode(orderCode)
                        .map(this::toContributionPaymentResponse)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "PayOS payment target not found for order code: " + orderCode)));
    }

    private void updateSupportRequestStatusAfterContribution(SupportRequest supportRequest) {
        if (supportRequest.getStatus() == SupportRequestStatus.APPROVED) {
            supportRequest.setStatus(SupportRequestStatus.IN_PROGRESS);
        }

        List<SupportNeed> supportNeeds = supportNeedRepository.findAllBySupportRequestOrderByCreatedAtDesc(
                supportRequest);

        boolean allFulfilled = supportNeeds.stream()
                .allMatch(supportNeed -> supportNeed.getReceivedQuantity()
                        .compareTo(supportNeed.getRequiredQuantity()) >= 0);

        if (allFulfilled && !supportNeeds.isEmpty()) {
            supportRequest.setStatus(SupportRequestStatus.COMPLETED);
        }

        supportRequestRepository.save(supportRequest);
    }

    private void validatePaidAmount(BigDecimal expectedAmount, BigDecimal paidAmount) {
        if (paidAmount == null
                || expectedAmount.setScale(0, RoundingMode.UNNECESSARY)
                        .compareTo(paidAmount.setScale(0, RoundingMode.UNNECESSARY)) != 0) {
            throw new BadRequestException("PayOS paid amount does not match pending payment amount");
        }
    }

    private PaymentReconciliationResponse toDonationPaymentResponse(Donation donation) {
        return PaymentReconciliationResponse.builder()
                .id(donation.getId())
                .paymentType("COMMUNITY_FUND_DONATION")
                .orderCode(donation.getPayosOrderCode())
                .status(donation.getStatus().name())
                .transactionCode(donation.getTransactionCode())
                .build();
    }

    private PaymentReconciliationResponse toContributionPaymentResponse(SupportNeedContribution contribution) {
        return PaymentReconciliationResponse.builder()
                .id(contribution.getId())
                .paymentType("SUPPORT_NEED_CONTRIBUTION")
                .orderCode(contribution.getPayosOrderCode())
                .status(contribution.getStatus().name())
                .transactionCode(contribution.getTransactionCode())
                .build();
    }

    private String normalizeTransactionCode(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }

        String trimmed = value.trim();
        if (trimmed.length() <= 100) {
            return trimmed;
        }

        return trimmed.substring(0, 100);
    }
}
