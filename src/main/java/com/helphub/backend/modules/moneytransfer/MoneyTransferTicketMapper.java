package com.helphub.backend.modules.moneytransfer;

import com.helphub.backend.persistence.entity.MoneyTransferTicket;
import com.helphub.backend.persistence.entity.User;
import com.helphub.backend.modules.moneytransfer.dto.response.MoneyTransferTicketResponse;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MoneyTransferTicketMapper {

    public MoneyTransferTicketResponse toResponse(MoneyTransferTicket ticket) {
        User requester = ticket.getRequester();
        User resolvedBy = ticket.getResolvedBy();

        return MoneyTransferTicketResponse.builder()
                .id(ticket.getId())
                .requesterId(requester.getId())
                .requesterName(requester.getFullName())
                .sourceType(ticket.getSourceType())
                .sourceId(resolveSourceId(ticket))
                .sourceName(resolveSourceName(ticket))
                .amount(ticket.getAmount())
                .reason(ticket.getReason())
                .status(ticket.getStatus())
                .adminNote(ticket.getAdminNote())
                .proofImageUrl(ticket.getProofImageUrl())
                .resolvedBy(resolvedBy != null ? resolvedBy.getId() : null)
                .resolvedByName(resolvedBy != null ? resolvedBy.getFullName() : null)
                .resolvedAt(ticket.getResolvedAt())
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .build();
    }

    private UUID resolveSourceId(MoneyTransferTicket ticket) {
        if (ticket.getFund() != null) {
            return ticket.getFund().getId();
        }

        return ticket.getSupportNeed() != null ? ticket.getSupportNeed().getId() : null;
    }

    private String resolveSourceName(MoneyTransferTicket ticket) {
        if (ticket.getFund() != null) {
            return ticket.getFund().getName();
        }

        return ticket.getSupportNeed() != null ? ticket.getSupportNeed().getNeedName() : null;
    }
}
