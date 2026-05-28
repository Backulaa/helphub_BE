package com.helphub.backend.modules.report;

import com.helphub.backend.common.enums.PostStatus;
import com.helphub.backend.common.enums.ReportStatus;
import com.helphub.backend.common.enums.SupportRequestStatus;
import com.helphub.backend.common.enums.UserRole;
import com.helphub.backend.common.exception.BadRequestException;
import com.helphub.backend.common.exception.ForbiddenException;
import com.helphub.backend.common.exception.ResourceNotFoundException;
import com.helphub.backend.modules.report.dto.request.CreateReportRequest;
import com.helphub.backend.modules.report.dto.request.ResolveReportRequest;
import com.helphub.backend.modules.report.dto.request.ReviewReportRequest;
import com.helphub.backend.modules.report.dto.response.ReportDetailResponse;
import com.helphub.backend.modules.report.dto.response.ReportSummaryResponse;
import com.helphub.backend.persistence.entity.Post;
import com.helphub.backend.persistence.entity.Report;
import com.helphub.backend.persistence.entity.SupportRequest;
import com.helphub.backend.persistence.entity.User;
import com.helphub.backend.persistence.repository.PostRepository;
import com.helphub.backend.persistence.repository.ReportRepository;
import com.helphub.backend.persistence.repository.SupportRequestRepository;
import com.helphub.backend.persistence.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final SupportRequestRepository supportRequestRepository;
    private final ReportMapper reportMapper;

    @Override
    public ReportDetailResponse createReport(UUID currentUserId, CreateReportRequest request) {

        User reporter = getUserById(currentUserId);

        validateReportTarget(reporter, request);

        reportRepository.findByReporterAndTargetTypeAndTargetIdAndStatus(
                reporter,
                request.getTargetType(),
                request.getTargetId(),
                ReportStatus.PENDING)
                .ifPresent(report -> {
                    throw new BadRequestException("You already submitted a pending report for this target");
                });

        Report report = Report.builder()
                .reporter(reporter)
                .targetType(request.getTargetType())
                .targetId(request.getTargetId())
                .reason(normalizeRequired(request.getReason(), "Reason is required"))
                .status(ReportStatus.PENDING)
                .build();

        Report savedReport = reportRepository.save(Objects.requireNonNull(report));

        return reportMapper.toDetailResponse(savedReport);
    }

    @Override
    public List<ReportSummaryResponse> getMyReports(UUID currentUserId) {

        User currentUser = getUserById(currentUserId);

        return reportRepository.findAllByReporterOrderByCreatedAtDesc(currentUser)
                .stream()
                .map(reportMapper::toSummaryResponse)
                .toList();
    }

    @Override
    public List<ReportSummaryResponse> getAllReports() {

        return reportRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(reportMapper::toSummaryResponse)
                .toList();
    }

    @Override
    public List<ReportSummaryResponse> getPendingReports() {

        return reportRepository.findAllByStatusOrderByCreatedAtDesc(ReportStatus.PENDING)
                .stream()
                .map(reportMapper::toSummaryResponse)
                .toList();
    }

    @Override
    public ReportDetailResponse getReportById(UUID reportId) {

        Report report = getReportByIdOrThrow(reportId);

        return reportMapper.toDetailResponse(report);
    }

    @Override
    public ReportDetailResponse reviewReport(
            UUID adminId,
            UUID reportId,
            ReviewReportRequest request) {

        User admin = getAdminById(adminId);

        Report report = getReportByIdOrThrow(reportId);

        if (report.getStatus() != ReportStatus.PENDING) {
            throw new BadRequestException("Only pending reports can be reviewed");
        }

        report.setStatus(ReportStatus.REVIEWED);
        report.setReviewedBy(admin);
        report.setReviewedAt(LocalDateTime.now());
        report.setResolutionNote(
                normalizeRequired(request.getResolutionNote(), "Resolution note is required"));

        Report savedReport = reportRepository.save(report);

        return reportMapper.toDetailResponse(savedReport);
    }

    @Override
    public ReportDetailResponse resolveReport(
            UUID adminId,
            UUID reportId,
            ResolveReportRequest request) {

        User admin = getAdminById(adminId);

        Report report = getReportByIdOrThrow(reportId);

        if (report.getStatus() == ReportStatus.RESOLVED) {
            throw new BadRequestException("Report is already resolved");
        }

        applyModerationAction(report, admin, request);

        report.setStatus(ReportStatus.RESOLVED);
        report.setReviewedBy(admin);
        report.setReviewedAt(LocalDateTime.now());
        report.setResolutionNote(
                normalizeRequired(request.getResolutionNote(), "Resolution note is required"));

        Report savedReport = reportRepository.save(report);

        return reportMapper.toDetailResponse(savedReport);
    }

    private void applyModerationAction(
            Report report,
            User admin,
            ResolveReportRequest request) {

        switch (report.getTargetType()) {

            case POST -> resolvePostReport(report);

            case SUPPORT_REQUEST -> resolveSupportRequestReport(
                    report,
                    admin,
                    request.getSupportRequestRejectionReason());

            case USER -> {
                // currently no automatic moderation action for user
            }

            default -> throw new BadRequestException("Unsupported report target type");
        }
    }

    private void resolvePostReport(Report report) {

        Post post = postRepository.findById(Objects.requireNonNull(report.getTargetId()))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Post not found with id: " + report.getTargetId()));

        post.setIsActive(false);
        post.setStatus(PostStatus.REMOVED);

        postRepository.save(post);
    }

    private void resolveSupportRequestReport(
            Report report,
            User admin,
            String rejectionReason) {

        SupportRequest supportRequest = supportRequestRepository.findById(Objects.requireNonNull(report.getTargetId()))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Support request not found with id: " + report.getTargetId()));

        supportRequest.setStatus(SupportRequestStatus.REJECTED);
        supportRequest.setReviewedBy(admin);
        supportRequest.setReviewedAt(LocalDateTime.now());
        supportRequest.setRejectionReason(
                normalizeRequired(rejectionReason, "Support request rejection reason is required"));

        supportRequestRepository.save(supportRequest);
    }

    private void validateReportTarget(
            User reporter,
            CreateReportRequest request) {

        switch (request.getTargetType()) {

            case POST -> validatePostReport(reporter, request.getTargetId());

            case SUPPORT_REQUEST -> validateSupportRequestReport(
                    reporter,
                    request.getTargetId());

            case USER -> validateUserReport(reporter, request.getTargetId());

            default -> throw new BadRequestException("Unsupported report target type");
        }
    }

    private void validatePostReport(User reporter, UUID postId) {

        Post post = postRepository.findById(Objects.requireNonNull(postId))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Post not found with id: " + postId));

        if (!Boolean.TRUE.equals(post.getIsActive())) {
            throw new BadRequestException("Cannot report inactive post");
        }

        if (post.getAuthor().getId().equals(reporter.getId())) {
            throw new BadRequestException("You cannot report your own post");
        }
    }

    private void validateSupportRequestReport(User reporter, UUID supportRequestId) {

        SupportRequest supportRequest = supportRequestRepository.findById(Objects.requireNonNull(supportRequestId))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Support request not found with id: " + supportRequestId));

        if (supportRequest.getStatus() == SupportRequestStatus.REJECTED
                || supportRequest.getStatus() == SupportRequestStatus.CANCELLED) {
            throw new BadRequestException("Cannot report rejected or cancelled support request");
        }

        if (supportRequest.getRequester().getId().equals(reporter.getId())) {
            throw new BadRequestException("You cannot report your own support request");
        }
    }

    private void validateUserReport(User reporter, UUID targetUserId) {

        User targetUser = getUserById(targetUserId);

        if (!Boolean.TRUE.equals(targetUser.getIsActive())) {
            throw new BadRequestException("Cannot report inactive user");
        }

        if (targetUser.getId().equals(reporter.getId())) {
            throw new BadRequestException("You cannot report yourself");
        }
    }

    private User getAdminById(UUID adminId) {

        User admin = getUserById(adminId);

        if (admin.getRole() != UserRole.ADMIN) {
            throw new ForbiddenException("Only admin can perform this action");
        }

        return admin;
    }

    private Report getReportByIdOrThrow(UUID reportId) {

        return reportRepository.findById(Objects.requireNonNull(reportId))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Report not found with id: " + reportId));
    }

    private User getUserById(UUID userId) {

        return userRepository.findById(Objects.requireNonNull(userId))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + userId));
    }

    private String normalizeRequired(String value, String message) {

        if (!StringUtils.hasText(value)) {
            throw new BadRequestException(message);
        }

        return value.trim();
    }
}