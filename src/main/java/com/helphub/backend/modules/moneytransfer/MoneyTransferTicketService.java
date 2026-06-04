package com.helphub.backend.modules.moneytransfer;

import com.helphub.backend.common.enums.MoneyTransferTicketStatus;
import com.helphub.backend.modules.moneytransfer.dto.request.CreateMoneyTransferTicketRequest;
import com.helphub.backend.modules.moneytransfer.dto.request.RejectMoneyTransferTicketRequest;
import com.helphub.backend.modules.moneytransfer.dto.response.MoneyTransferTicketResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface MoneyTransferTicketService {
    MoneyTransferTicketResponse createCommunityFundTicket(
            UUID currentUserId,
            UUID fundId,
            CreateMoneyTransferTicketRequest request);

    MoneyTransferTicketResponse createSupportNeedTicket(
            UUID currentUserId,
            UUID supportNeedId,
            CreateMoneyTransferTicketRequest request);

    List<MoneyTransferTicketResponse> getMyTickets(UUID currentUserId);

    List<MoneyTransferTicketResponse> getAdminTickets(MoneyTransferTicketStatus status);

    MoneyTransferTicketResponse rejectTicket(
            UUID adminId,
            UUID ticketId,
            RejectMoneyTransferTicketRequest request);

    MoneyTransferTicketResponse resolveTicket(
            UUID adminId,
            UUID ticketId,
            MultipartFile proofFile,
            String adminNote);
}
