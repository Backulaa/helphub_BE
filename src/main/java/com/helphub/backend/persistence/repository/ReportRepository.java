package com.helphub.backend.persistence.repository;

import com.helphub.backend.common.enums.ReportStatus;
import com.helphub.backend.common.enums.ReportTargetType;
import com.helphub.backend.persistence.entity.Report;
import com.helphub.backend.persistence.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReportRepository extends JpaRepository<Report, UUID> {

    List<Report> findAllByOrderByCreatedAtDesc();

    List<Report> findAllByStatusOrderByCreatedAtDesc(ReportStatus status);

    List<Report> findAllByReporterOrderByCreatedAtDesc(User reporter);

    Optional<Report> findByReporterAndTargetTypeAndTargetIdAndStatus(
            User reporter,
            ReportTargetType targetType,
            UUID targetId,
            ReportStatus status);
}