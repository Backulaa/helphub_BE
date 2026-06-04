package com.helphub.backend.persistence.repository;

import com.helphub.backend.common.enums.DonationStatus;
import com.helphub.backend.persistence.entity.CommunityFund;
import com.helphub.backend.persistence.entity.Donation;
import com.helphub.backend.persistence.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DonationRepository extends JpaRepository<Donation, UUID> {

    List<Donation> findAllByFundAndStatusOrderByCreatedAtDesc(CommunityFund fund, DonationStatus status);

    List<Donation> findAllByDonorAndStatusOrderByCreatedAtDesc(User donor, DonationStatus status);

    Optional<Donation> findByPayosOrderCode(Long payosOrderCode);
}