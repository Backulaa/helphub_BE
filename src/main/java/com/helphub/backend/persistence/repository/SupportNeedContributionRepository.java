package com.helphub.backend.persistence.repository;

import com.helphub.backend.common.enums.SupportNeedContributionStatus;
import com.helphub.backend.persistence.entity.SupportNeed;
import com.helphub.backend.persistence.entity.SupportNeedContribution;
import com.helphub.backend.persistence.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SupportNeedContributionRepository extends JpaRepository<SupportNeedContribution, UUID> {

    List<SupportNeedContribution> findAllBySupportNeedOrderByCreatedAtDesc(SupportNeed supportNeed);

    List<SupportNeedContribution> findAllBySupportNeedAndStatusOrderByCreatedAtDesc(
            SupportNeed supportNeed,
            SupportNeedContributionStatus status);

    List<SupportNeedContribution> findAllByContributorAndStatusOrderByCreatedAtDesc(
            User contributor,
            SupportNeedContributionStatus status);

    Optional<SupportNeedContribution> findByPayosOrderCode(Long payosOrderCode);
}