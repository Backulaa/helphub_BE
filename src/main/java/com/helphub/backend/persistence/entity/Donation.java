package com.helphub.backend.persistence.entity;

import com.helphub.backend.common.enums.DonationStatus;
import com.helphub.backend.common.enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "donations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Donation extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fund_id", nullable = false)
    private CommunityFund fund;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "donor_id", nullable = false)
    private User donor;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 30)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DonationStatus status;

    @Column(name = "transaction_code", length = 100)
    private String transactionCode;

    @Column(columnDefinition = "TEXT")
    private String note;

    @PrePersist
    public void prePersistDonation() {
        if (this.status == null) {
            this.status = DonationStatus.SUCCESS;
        }
    }
}