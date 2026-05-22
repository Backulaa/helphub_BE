package com.helphub.backend.modules.expense;

import com.helphub.backend.common.enums.CommunityFundMemberRole;
import com.helphub.backend.common.enums.UserRole;
import com.helphub.backend.common.exception.BadRequestException;
import com.helphub.backend.common.exception.ForbiddenException;
import com.helphub.backend.common.exception.ResourceNotFoundException;
import com.helphub.backend.modules.expense.dto.request.CreateExpenseRequest;
import com.helphub.backend.modules.expense.dto.response.ExpenseResponse;
import com.helphub.backend.persistence.entity.CommunityFund;
import com.helphub.backend.persistence.entity.Expense;
import com.helphub.backend.persistence.entity.SupportRequest;
import com.helphub.backend.persistence.entity.User;
import com.helphub.backend.persistence.repository.CommunityFundMemberRepository;
import com.helphub.backend.persistence.repository.CommunityFundRepository;
import com.helphub.backend.persistence.repository.ExpenseRepository;
import com.helphub.backend.persistence.repository.SupportRequestRepository;
import com.helphub.backend.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final CommunityFundRepository communityFundRepository;
    private final CommunityFundMemberRepository communityFundMemberRepository;
    private final SupportRequestRepository supportRequestRepository;
    private final UserRepository userRepository;
    private final ExpenseMapper expenseMapper;

    @Override
    @Transactional
    public ExpenseResponse createExpense(UUID currentUserId, CreateExpenseRequest request) {
        User currentUser = getUserById(currentUserId);
        CommunityFund fund = getCommunityFundByIdOrThrow(request.getFundId());

        validateCanManageFund(currentUser, fund);
        validateActiveFund(fund);

        BigDecimal amount = validatePositiveAmount(request.getAmount());

        if (fund.getTotalBalance().compareTo(amount) < 0) {
            throw new BadRequestException("Expense amount exceeds community fund balance");
        }

        SupportRequest supportRequest = null;

        if (request.getSupportRequestId() != null) {
            supportRequest = supportRequestRepository.findById(Objects.requireNonNull(request.getSupportRequestId()))
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Support request not found with id: " + request.getSupportRequestId()));
        }

        Expense expense = Expense.builder()
                .fund(fund)
                .supportRequest(supportRequest)
                .createdBy(currentUser)
                .amount(amount)
                .description(normalizeRequired(request.getDescription(), "Description is required"))
                .build();

        Expense savedExpense = expenseRepository.save(Objects.requireNonNull(expense));

        fund.setTotalBalance(fund.getTotalBalance().subtract(amount));
        communityFundRepository.save(fund);

        return expenseMapper.toResponse(savedExpense);
    }

    @Override
    public List<ExpenseResponse> getExpensesByFund(UUID currentUserId, UUID fundId) {
        @SuppressWarnings("unused")
        User currentUser = getUserById(currentUserId);
        CommunityFund fund = getCommunityFundByIdOrThrow(fundId);

        return expenseRepository.findAllByFundOrderByCreatedAtDesc(fund)
                .stream()
                .map(expenseMapper::toResponse)
                .toList();
    }

    private User getUserById(UUID userId) {
        return userRepository.findById(Objects.requireNonNull(userId))
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    private CommunityFund getCommunityFundByIdOrThrow(UUID fundId) {
        return communityFundRepository.findById(Objects.requireNonNull(fundId))
                .orElseThrow(() -> new ResourceNotFoundException("Community fund not found with id: " + fundId));
    }

    private void validateActiveFund(CommunityFund fund) {
        if (!Boolean.TRUE.equals(fund.getIsActive())) {
            throw new BadRequestException("Community fund is inactive");
        }
    }

    private void validateCanManageFund(User user, CommunityFund fund) {
        if (user.getRole() == UserRole.ADMIN) {
            return;
        }

        boolean isManager = communityFundMemberRepository.existsByFundAndUserAndRoleAndIsActiveTrue(
                fund,
                user,
                CommunityFundMemberRole.MANAGER);

        if (!isManager) {
            throw new ForbiddenException("Only admin or fund manager can manage fund expenses");
        }
    }

    private BigDecimal validatePositiveAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Amount must be greater than 0");
        }

        return amount;
    }

    private String normalizeRequired(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new BadRequestException(message);
        }

        return value.trim();
    }
}