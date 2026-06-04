package com.helphub.backend.persistence.repository;

import com.helphub.backend.common.enums.MoneyTransferTicketStatus;
import com.helphub.backend.persistence.entity.CommunityFund;
import com.helphub.backend.persistence.entity.MoneyTransferTicket;
import com.helphub.backend.persistence.entity.SupportNeed;
import com.helphub.backend.persistence.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface MoneyTransferTicketRepository extends JpaRepository<MoneyTransferTicket, UUID> {

    List<MoneyTransferTicket> findAllByOrderByCreatedAtDesc();

    List<MoneyTransferTicket> findAllByStatusOrderByCreatedAtDesc(MoneyTransferTicketStatus status);

    List<MoneyTransferTicket> findAllByRequesterOrderByCreatedAtDesc(User requester);

    @Query("""
            SELECT COALESCE(SUM(ticket.amount), 0)
            FROM MoneyTransferTicket ticket
            WHERE ticket.fund = :fund
              AND ticket.status IN :statuses
            """)
    BigDecimal sumAmountByFundAndStatuses(CommunityFund fund, Collection<MoneyTransferTicketStatus> statuses);

    @Query("""
            SELECT COALESCE(SUM(ticket.amount), 0)
            FROM MoneyTransferTicket ticket
            WHERE ticket.supportNeed = :supportNeed
              AND ticket.status IN :statuses
            """)
    BigDecimal sumAmountBySupportNeedAndStatuses(
            SupportNeed supportNeed,
            Collection<MoneyTransferTicketStatus> statuses);
}
