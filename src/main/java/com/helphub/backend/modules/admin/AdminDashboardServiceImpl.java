package com.helphub.backend.modules.admin;

import com.helphub.backend.common.enums.PostStatus;
import com.helphub.backend.common.enums.ReportStatus;
import com.helphub.backend.common.enums.ReportTargetType;
import com.helphub.backend.common.enums.SupportRequestStatus;
import com.helphub.backend.common.enums.UserRole;
import com.helphub.backend.modules.admin.dto.response.CategoryStatisticsItemResponse;
import com.helphub.backend.modules.admin.dto.response.CategoryStatisticsResponse;
import com.helphub.backend.modules.admin.dto.response.PostStatisticsResponse;
import com.helphub.backend.modules.admin.dto.response.ReportStatisticsResponse;
import com.helphub.backend.modules.admin.dto.response.SupportRequestStatisticsResponse;
import com.helphub.backend.modules.admin.dto.response.UserStatisticsResponse;
import com.helphub.backend.persistence.repository.CategoryRepository;
import com.helphub.backend.persistence.repository.PostRepository;
import com.helphub.backend.persistence.repository.ReportRepository;
import com.helphub.backend.persistence.repository.SupportRequestRepository;
import com.helphub.backend.persistence.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final UserRepository userRepository;
    private final SupportRequestRepository supportRequestRepository;
    private final PostRepository postRepository;
    private final ReportRepository reportRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public UserStatisticsResponse getUserStatistics() {
        return UserStatisticsResponse.builder()
                .totalUsers(userRepository.count())
                .activeUsers(userRepository.countByIsActiveTrue())
                .inactiveUsers(userRepository.countByIsActiveFalse())
                .requesters(userRepository.countByRole(UserRole.REQUESTER))
                .volunteers(userRepository.countByRole(UserRole.VOLUNTEER))
                .collaborators(userRepository.countByRole(UserRole.COLLABORATOR))
                .admins(userRepository.countByRole(UserRole.ADMIN))
                .build();
    }

    @Override
    public SupportRequestStatisticsResponse getSupportRequestStatistics() {
        return SupportRequestStatisticsResponse.builder()
                .totalSupportRequests(supportRequestRepository.count())
                .pending(supportRequestRepository.countByStatus(SupportRequestStatus.PENDING))
                .approved(supportRequestRepository.countByStatus(SupportRequestStatus.APPROVED))
                .inProgress(supportRequestRepository.countByStatus(SupportRequestStatus.IN_PROGRESS))
                .rejected(supportRequestRepository.countByStatus(SupportRequestStatus.REJECTED))
                .completed(supportRequestRepository.countByStatus(SupportRequestStatus.COMPLETED))
                .cancelled(supportRequestRepository.countByStatus(SupportRequestStatus.CANCELLED))
                .build();
    }

    @Override
    public PostStatisticsResponse getPostStatistics() {
        return PostStatisticsResponse.builder()
                .totalPosts(postRepository.count())
                .active(postRepository.countByStatus(PostStatus.ACTIVE))
                .underReview(postRepository.countByStatus(PostStatus.UNDER_REVIEW))
                .hidden(postRepository.countByStatus(PostStatus.HIDDEN))
                .removed(postRepository.countByStatus(PostStatus.REMOVED))
                .build();
    }

    @Override
    public ReportStatisticsResponse getReportStatistics() {
        return ReportStatisticsResponse.builder()
                .totalReports(reportRepository.count())
                .supportRequestReports(reportRepository.countByTargetType(ReportTargetType.SUPPORT_REQUEST))
                .postReports(reportRepository.countByTargetType(ReportTargetType.POST))
                .userReports(reportRepository.countByTargetType(ReportTargetType.USER))
                .pending(reportRepository.countByStatus(ReportStatus.PENDING))
                .reviewed(reportRepository.countByStatus(ReportStatus.REVIEWED))
                .resolved(reportRepository.countByStatus(ReportStatus.RESOLVED))
                .build();
    }

    @Override
    public CategoryStatisticsResponse getCategoryStatistics() {
        List<CategoryStatisticsItemResponse> categories = categoryRepository.countSupportRequestsByCategory()
                .stream()
                .map(item -> CategoryStatisticsItemResponse.builder()
                        .categoryId(item.getCategoryId())
                        .categoryName(item.getCategoryName())
                        .supportRequestCount(item.getSupportRequestCount() != null
                                ? item.getSupportRequestCount()
                                : 0L)
                        .build())
                .toList();

        return CategoryStatisticsResponse.builder()
                .totalCategories(categoryRepository.count())
                .activeCategories(categoryRepository.countByIsActiveTrue())
                .categories(categories)
                .build();
    }
}