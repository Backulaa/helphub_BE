package com.helphub.backend.modules.moneytransfer;

import com.helphub.backend.common.enums.CommunityFundMemberRole;
import com.helphub.backend.common.enums.MoneyTransferTicketSourceType;
import com.helphub.backend.common.enums.MoneyTransferTicketStatus;
import com.helphub.backend.common.enums.SupportType;
import com.helphub.backend.common.enums.UserRole;
import com.helphub.backend.common.exception.BadRequestException;
import com.helphub.backend.common.exception.ForbiddenException;
import com.helphub.backend.common.exception.ResourceNotFoundException;
import com.helphub.backend.modules.media.MediaService;
import com.helphub.backend.modules.moneytransfer.dto.request.CreateMoneyTransferTicketRequest;
import com.helphub.backend.modules.moneytransfer.dto.request.RejectMoneyTransferTicketRequest;
import com.helphub.backend.modules.moneytransfer.dto.response.MoneyTransferTicketResponse;
import com.helphub.backend.persistence.entity.CommunityFund;
import com.helphub.backend.persistence.entity.Expense;
import com.helphub.backend.persistence.entity.MoneyTransferTicket;
import com.helphub.backend.persistence.entity.SupportNeed;
import com.helphub.backend.persistence.entity.SupportRequest;
import com.helphub.backend.persistence.entity.User;
import com.helphub.backend.persistence.repository.CommunityFundMemberRepository;
import com.helphub.backend.persistence.repository.CommunityFundRepository;
import com.helphub.backend.persistence.repository.ExpenseRepository;
import com.helphub.backend.persistence.repository.MoneyTransferTicketRepository;
import com.helphub.backend.persistence.repository.SupportNeedRepository;
import com.helphub.backend.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MoneyTransferTicketServiceImpl implements MoneyTransferTicketService {

    private static final String TRANSFER_PROOF_FOLDER = "helphub/transfer-proofs";

    private final MoneyTransferTicketRepository moneyTransferTicketRepository;
    private final CommunityFundRepository communityFundRepository;
    private final CommunityFundMemberRepository communityFundMemberRepository;
    private final SupportNeedRepository supportNeedRepository;
    private final UserRepository userRepository;
    private final ExpenseRepository expenseRepository;
    private final MediaService mediaService;
    private final MoneyTransferTicketMapper moneyTransferTicketMapper;

    @Override
    @Transactional
    public MoneyTransferTicketResponse createCommunityFundTicket(
            UUID currentUserId,
            UUID fundId,
            CreateMoneyTransferTicketRequest request) {

        User currentUser = getUserById(currentUserId);
        CommunityFund fund = getCommunityFundByIdOrThrow(fundId);

        validateCanRequestFundTransfer(currentUser, fund);
        validateActiveFund(fund);

        BigDecimal amount = validatePositiveAmount(request.getAmount());
        BigDecimal availableAmount = getAvailableFundAmount(fund);

        if (availableAmount.compareTo(amount) < 0) {
            throw new BadRequestException("Transfer ticket amount exceeds available community fund balance");
        }

        MoneyTransferTicket ticket = MoneyTransferTicket.builder()
                .requester(currentUser)
                .sourceType(MoneyTransferTicketSourceType.COMMUNITY_FUND)
                .fund(fund)
                .amount(amount)
                .reason(normalizeRequired(request.getReason(), "Reason is required"))
                .status(MoneyTransferTicketStatus.PENDING)
                .build();

        MoneyTransferTicket savedTicket = moneyTransferTicketRepository.save(Objects.requireNonNull(ticket));
        return moneyTransferTicketMapper.toResponse(savedTicket);
    }

    @Override
    @Transactional
    public MoneyTransferTicketResponse createSupportNeedTicket(
            UUID currentUserId,
            UUID supportNeedId,
            CreateMoneyTransferTicketRequest request) {

        User currentUser = getUserById(currentUserId);
        SupportNeed supportNeed = getSupportNeedByIdOrThrow(supportNeedId);
        SupportRequest supportRequest = supportNeed.getSupportRequest();

        validateCanRequestSupportNeedTransfer(currentUser, supportNeed);

        BigDecimal amount = validatePositiveAmount(request.getAmount());
        BigDecimal availableAmount = getAvailableSupportNeedAmount(supportNeed);

        if (availableAmount.compareTo(amount) < 0) {
            throw new BadRequestException("Transfer ticket amount exceeds available support need money");
        }

        MoneyTransferTicket ticket = MoneyTransferTicket.builder()
                .requester(currentUser)
                .sourceType(MoneyTransferTicketSourceType.SUPPORT_NEED)
                .supportNeed(supportNeed)
                .amount(amount)
                .reason(normalizeRequired(request.getReason(), "Reason is required"))
                .status(MoneyTransferTicketStatus.PENDING)
                .build();

        MoneyTransferTicket savedTicket = moneyTransferTicketRepository.save(Objects.requireNonNull(ticket));
        return moneyTransferTicketMapper.toResponse(savedTicket);
    }

    @Override
    public List<MoneyTransferTicketResponse> getMyTickets(UUID currentUserId) {
        User currentUser = getUserById(currentUserId);

        return moneyTransferTicketRepository.findAllByRequesterOrderByCreatedAtDesc(currentUser)
                .stream()
                .map(moneyTransferTicketMapper::toResponse)
                .toList();
    }

    @Override
    public List<MoneyTransferTicketResponse> getAdminTickets(MoneyTransferTicketStatus status) {
        List<MoneyTransferTicket> tickets = status != null
                ? moneyTransferTicketRepository.findAllByStatusOrderByCreatedAtDesc(status)
                : moneyTransferTicketRepository.findAllByOrderByCreatedAtDesc();

        return tickets.stream()
                .map(moneyTransferTicketMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public MoneyTransferTicketResponse rejectTicket(
            UUID adminId,
            UUID ticketId,
            RejectMoneyTransferTicketRequest request) {

        User admin = getAdminById(adminId);
        MoneyTransferTicket ticket = getTicketByIdOrThrow(ticketId);
        validatePendingTicket(ticket);

        ticket.setStatus(MoneyTransferTicketStatus.REJECTED);
        ticket.setAdminNote(normalizeRequired(request.getRejectionReason(), "Rejection reason is required"));
        ticket.setResolvedBy(admin);
        ticket.setResolvedAt(LocalDateTime.now());

        MoneyTransferTicket savedTicket = moneyTransferTicketRepository.save(ticket);
        return moneyTransferTicketMapper.toResponse(savedTicket);
    }

    @Override
    @Transactional
    public MoneyTransferTicketResponse resolveTicket(
            UUID adminId,
            UUID ticketId,
            MultipartFile proofFile,
            String adminNote) {

        User admin = getAdminById(adminId);
        MoneyTransferTicket ticket = getTicketByIdOrThrow(ticketId);
        validatePendingTicket(ticket);
        validateProofImage(proofFile);

        String proofImageUrl = mediaService.uploadFile(proofFile, TRANSFER_PROOF_FOLDER);

        if (ticket.getSourceType() == MoneyTransferTicketSourceType.COMMUNITY_FUND) {
            resolveCommunityFundTicket(ticket, admin);
        }

        ticket.setStatus(MoneyTransferTicketStatus.RESOLVED);
        ticket.setProofImageUrl(proofImageUrl);
        ticket.setAdminNote(normalizeNullable(adminNote));
        ticket.setResolvedBy(admin);
        ticket.setResolvedAt(LocalDateTime.now());

        MoneyTransferTicket savedTicket = moneyTransferTicketRepository.save(ticket);
        return moneyTransferTicketMapper.toResponse(savedTicket);
    }

    private void resolveCommunityFundTicket(MoneyTransferTicket ticket, User admin) {
        CommunityFund fund = ticket.getFund();
        if (fund == null) {
            throw new BadRequestException("Community fund transfer ticket is missing fund");
        }

        if (fund.getTotalBalance().compareTo(ticket.getAmount()) < 0) {
            throw new BadRequestException("Community fund balance is not enough to resolve this ticket");
        }

        Expense expense = Expense.builder()
                .fund(fund)
                .createdBy(admin)
                .amount(ticket.getAmount())
                .description("External money transfer resolved for ticket " + ticket.getId())
                .build();

        expenseRepository.save(Objects.requireNonNull(expense));
        fund.setTotalBalance(fund.getTotalBalance().subtract(ticket.getAmount()));
        communityFundRepository.save(fund);
    }

    private User getUserById(UUID userId) {
        return userRepository.findById(Objects.requireNonNull(userId))
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    private User getAdminById(UUID adminId) {
        User admin = getUserById(adminId);

        if (admin.getRole() != UserRole.ADMIN) {
            throw new ForbiddenException("Only admin can perform this action");
        }

        return admin;
    }

    private CommunityFund getCommunityFundByIdOrThrow(UUID fundId) {
        return communityFundRepository.findById(Objects.requireNonNull(fundId))
                .orElseThrow(() -> new ResourceNotFoundException("Community fund not found with id: " + fundId));
    }

    private SupportNeed getSupportNeedByIdOrThrow(UUID supportNeedId) {
        return supportNeedRepository.findById(Objects.requireNonNull(supportNeedId))
                .orElseThrow(() -> new ResourceNotFoundException("Support need not found with id: " + supportNeedId));
    }

    private MoneyTransferTicket getTicketByIdOrThrow(UUID ticketId) {
        return moneyTransferTicketRepository.findById(Objects.requireNonNull(ticketId))
                .orElseThrow(() -> new ResourceNotFoundException("Money transfer ticket not found with id: " + ticketId));
    }

    private void validateCanRequestFundTransfer(User currentUser, CommunityFund fund) {
        if (currentUser.getRole() == UserRole.ADMIN || fund.getCreatedBy().getId().equals(currentUser.getId())) {
            return;
        }

        boolean isManager = communityFundMemberRepository.existsByFundAndUserAndRoleAndIsActiveTrue(
                fund,
                currentUser,
                CommunityFundMemberRole.MANAGER);

        if (!isManager) {
            throw new ForbiddenException("Only admin, fund creator, or fund manager can request fund transfer");
        }
    }

    private void validateCanRequestSupportNeedTransfer(User currentUser, SupportNeed supportNeed) {
        SupportRequest supportRequest = supportNeed.getSupportRequest();

        if (currentUser.getRole() != UserRole.ADMIN
                && !supportRequest.getRequester().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Only the requester can request transfer for this support need");
        }

        if (supportNeed.getSupportType() != SupportType.MONEY) {
            throw new BadRequestException("Only money support needs can create transfer tickets");
        }
    }

    private void validatePendingTicket(MoneyTransferTicket ticket) {
        if (ticket.getStatus() != MoneyTransferTicketStatus.PENDING) {
            throw new BadRequestException("Only pending tickets can be updated");
        }
    }

    private void validateActiveFund(CommunityFund fund) {
        if (!Boolean.TRUE.equals(fund.getIsActive())) {
            throw new BadRequestException("Community fund is inactive");
        }
    }

    private void validateProofImage(MultipartFile proofFile) {
        if (proofFile == null || proofFile.isEmpty()) {
            throw new BadRequestException("Proof image is required");
        }

        String mimeType = proofFile.getContentType();
        if (!StringUtils.hasText(mimeType) || !mimeType.startsWith("image/")) {
            throw new BadRequestException("Proof file must be an image");
        }
    }

    private BigDecimal getAvailableFundAmount(CommunityFund fund) {
        BigDecimal pendingAmount = moneyTransferTicketRepository.sumAmountByFundAndStatuses(
                fund,
                List.of(MoneyTransferTicketStatus.PENDING));

        return fund.getTotalBalance().subtract(pendingAmount);
    }

    private BigDecimal getAvailableSupportNeedAmount(SupportNeed supportNeed) {
        BigDecimal reservedAmount = moneyTransferTicketRepository.sumAmountBySupportNeedAndStatuses(
                supportNeed,
                List.of(MoneyTransferTicketStatus.PENDING, MoneyTransferTicketStatus.RESOLVED));

        return supportNeed.getReceivedQuantity().subtract(reservedAmount);
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

    private String normalizeNullable(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }

        return value.trim();
    }
}
