package com.helphub.backend.persistence.repository;

import com.helphub.backend.persistence.entity.CommunityFund;
import com.helphub.backend.persistence.entity.Donation;
import com.helphub.backend.persistence.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DonationRepository extends JpaRepository<Donation, UUID> {

    List<Donation> findAllByFundOrderByCreatedAtDesc(CommunityFund fund);

    List<Donation> findAllByDonorOrderByCreatedAtDesc(User donor);
}