package com.helphub.backend.modules.donation;

import com.helphub.backend.modules.donation.dto.response.DonationResponse;
import com.helphub.backend.persistence.entity.Donation;
import org.springframework.stereotype.Component;

@Component
public class DonationMapper {

    public DonationResponse toResponse(Donation donation) {
        return DonationResponse.builder()
                .id(donation.getId())
                .fundId(donation.getFund().getId())
                .fundName(donation.getFund().getName())
                .donorId(donation.getDonor().getId())
                .donorName(donation.getDonor().getFullName())
                .amount(donation.getAmount())
                .paymentMethod(donation.getPaymentMethod())
                .status(donation.getStatus())
                .transactionCode(donation.getTransactionCode())
                .note(donation.getNote())
                .createdAt(donation.getCreatedAt())
                .build();
    }
}