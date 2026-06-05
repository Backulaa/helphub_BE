package com.helphub.backend.modules.moneytransfer;

import com.helphub.backend.common.enums.MoneyTransferTicketSourceType;
import com.helphub.backend.common.enums.MoneyTransferTicketStatus;
import com.helphub.backend.common.enums.UserRole;
import com.helphub.backend.common.exception.BadRequestException;
import com.helphub.backend.modules.media.MediaService;
import com.helphub.backend.modules.moneytransfer.dto.request.CreateMoneyTransferTicketRequest;
import com.helphub.backend.modules.moneytransfer.dto.response.MoneyTransferTicketResponse;
import com.helphub.backend.persistence.entity.CommunityFund;
import com.helphub.backend.persistence.entity.MoneyTransferTicket;
import com.helphub.backend.persistence.entity.User;
import com.helphub.backend.persistence.repository.CommunityFundMemberRepository;
import com.helphub.backend.persistence.repository.CommunityFundRepository;
import com.helphub.backend.persistence.repository.MoneyTransferTicketRepository;
import com.helphub.backend.persistence.repository.SupportNeedRepository;
import com.helphub.backend.persistence.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MoneyTransferTicketServiceImplTest {

    @Mock
    private MoneyTransferTicketRepository moneyTransferTicketRepository;

    @Mock
    private CommunityFundRepository communityFundRepository;

    @Mock
    private CommunityFundMemberRepository communityFundMemberRepository;

    @Mock
    private SupportNeedRepository supportNeedRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MediaService mediaService;

    @Mock
    private MoneyTransferTicketMapper moneyTransferTicketMapper;

    @Mock
    private MultipartFile proofFile;

    @InjectMocks
    private MoneyTransferTicketServiceImpl moneyTransferTicketService;

    private UUID requesterId;
    private UUID adminId;
    private UUID fundId;
    private User requester;
    private User admin;
    private CommunityFund fund;

    @BeforeEach
    void setUp() {
        requesterId = UUID.randomUUID();
        adminId = UUID.randomUUID();
        fundId = UUID.randomUUID();

        requester = new User();
        requester.setId(requesterId);
        requester.setFullName("Fund Owner");
        requester.setRole(UserRole.REQUESTER);

        admin = new User();
        admin.setId(adminId);
        admin.setFullName("Admin");
        admin.setRole(UserRole.ADMIN);

        fund = new CommunityFund();
        fund.setId(fundId);
        fund.setName("Neighborhood Fund");
        fund.setCreatedBy(requester);
        fund.setIsActive(true);
        fund.setTotalBalance(BigDecimal.valueOf(500));
    }

    @Test
    void createCommunityFundTicket_shouldCountResolvedTransfersAsUnavailableMoney() {
        CreateMoneyTransferTicketRequest request = new CreateMoneyTransferTicketRequest();
        request.setAmount(BigDecimal.valueOf(400));
        request.setReason("Send money");

        when(userRepository.findById(requesterId)).thenReturn(Optional.of(requester));
        when(communityFundRepository.findById(fundId)).thenReturn(Optional.of(fund));
        when(moneyTransferTicketRepository.sumAmountByFundAndStatuses(
                eq(fund),
                eq(List.of(MoneyTransferTicketStatus.PENDING, MoneyTransferTicketStatus.RESOLVED))))
                .thenReturn(BigDecimal.valueOf(400));

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> moneyTransferTicketService.createCommunityFundTicket(requesterId, fundId, request));

        assertEquals("Transfer ticket amount exceeds available community fund balance", exception.getMessage());
        verify(moneyTransferTicketRepository, never()).save(any(MoneyTransferTicket.class));
    }

    @Test
    void createCommunityFundTicket_shouldAllowOnlyRemainingUntransferredFundAmount() {
        CreateMoneyTransferTicketRequest request = new CreateMoneyTransferTicketRequest();
        request.setAmount(BigDecimal.valueOf(100));
        request.setReason("Remaining transfer");

        MoneyTransferTicketResponse expectedResponse = MoneyTransferTicketResponse.builder()
                .id(UUID.randomUUID())
                .amount(BigDecimal.valueOf(100))
                .status(MoneyTransferTicketStatus.PENDING)
                .build();

        when(userRepository.findById(requesterId)).thenReturn(Optional.of(requester));
        when(communityFundRepository.findById(fundId)).thenReturn(Optional.of(fund));
        when(moneyTransferTicketRepository.sumAmountByFundAndStatuses(
                eq(fund),
                eq(List.of(MoneyTransferTicketStatus.PENDING, MoneyTransferTicketStatus.RESOLVED))))
                .thenReturn(BigDecimal.valueOf(400));
        when(moneyTransferTicketRepository.save(any(MoneyTransferTicket.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(moneyTransferTicketMapper.toResponse(any(MoneyTransferTicket.class))).thenReturn(expectedResponse);

        MoneyTransferTicketResponse response =
                moneyTransferTicketService.createCommunityFundTicket(requesterId, fundId, request);

        assertEquals(expectedResponse, response);

        ArgumentCaptor<MoneyTransferTicket> ticketCaptor = ArgumentCaptor.forClass(MoneyTransferTicket.class);
        verify(moneyTransferTicketRepository).save(ticketCaptor.capture());
        assertEquals(BigDecimal.valueOf(100), ticketCaptor.getValue().getAmount());
        assertEquals(MoneyTransferTicketStatus.PENDING, ticketCaptor.getValue().getStatus());
    }

    @Test
    void resolveTicket_shouldNotDeductCommunityFundBalance() {
        UUID ticketId = UUID.randomUUID();
        MoneyTransferTicket ticket = MoneyTransferTicket.builder()
                .requester(requester)
                .sourceType(MoneyTransferTicketSourceType.COMMUNITY_FUND)
                .fund(fund)
                .amount(BigDecimal.valueOf(400))
                .reason("Transfer to owner")
                .status(MoneyTransferTicketStatus.PENDING)
                .build();
        ticket.setId(ticketId);

        MoneyTransferTicketResponse expectedResponse = MoneyTransferTicketResponse.builder()
                .id(ticketId)
                .amount(BigDecimal.valueOf(400))
                .status(MoneyTransferTicketStatus.RESOLVED)
                .build();

        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(moneyTransferTicketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(proofFile.isEmpty()).thenReturn(false);
        when(proofFile.getContentType()).thenReturn("image/png");
        when(mediaService.uploadFile(proofFile, "helphub/transfer-proofs")).thenReturn("https://cdn.example/proof.png");
        when(moneyTransferTicketRepository.sumAmountByFundAndStatuses(
                eq(fund),
                eq(List.of(MoneyTransferTicketStatus.RESOLVED))))
                .thenReturn(BigDecimal.ZERO);
        when(moneyTransferTicketRepository.save(ticket)).thenReturn(ticket);
        when(moneyTransferTicketMapper.toResponse(ticket)).thenReturn(expectedResponse);

        MoneyTransferTicketResponse response =
                moneyTransferTicketService.resolveTicket(adminId, ticketId, proofFile, "Sent");

        assertEquals(expectedResponse, response);
        assertEquals(BigDecimal.valueOf(500), fund.getTotalBalance());
        assertEquals(MoneyTransferTicketStatus.RESOLVED, ticket.getStatus());
        assertEquals("https://cdn.example/proof.png", ticket.getProofImageUrl());

        verify(communityFundRepository, never()).save(fund);
        verify(moneyTransferTicketRepository).sumAmountByFundAndStatuses(
                eq(fund),
                eq(List.of(MoneyTransferTicketStatus.RESOLVED)));
    }
}
