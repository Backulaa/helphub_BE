package com.helphub.backend.modules.moneytransfer.dto.response;

import com.helphub.backend.common.enums.MoneyTransferTicketSourceType;
import com.helphub.backend.common.enums.MoneyTransferTicketStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
public class MoneyTransferTicketResponse {
    private UUID id;
    private UUID requesterId;
    private String requesterName;
    private MoneyTransferTicketSourceType sourceType;
    private UUID sourceId;
    private String sourceName;
    private BigDecimal amount;
    private String reason;
    private MoneyTransferTicketStatus status;
    private String adminNote;
    private String proofImageUrl;
    private UUID resolvedBy;
    private String resolvedByName;
    private LocalDateTime resolvedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
