package com.helphub.backend.modules.communityfund;

import com.helphub.backend.common.enums.MoneyTransferTicketStatus;
import com.helphub.backend.modules.communityfund.dto.response.CommunityFundDetailResponse;
import com.helphub.backend.modules.communityfund.dto.response.CommunityFundMemberResponse;
import com.helphub.backend.modules.communityfund.dto.response.CommunityFundSummaryResponse;
import com.helphub.backend.persistence.entity.CommunityFund;
import com.helphub.backend.persistence.entity.CommunityFundMember;
import com.helphub.backend.persistence.entity.User;
import com.helphub.backend.persistence.repository.MoneyTransferTicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CommunityFundMapper {

    private final MoneyTransferTicketRepository moneyTransferTicketRepository;

    public CommunityFundSummaryResponse toSummaryResponse(CommunityFund fund) {
        User creator = fund.getCreatedBy();
        BigDecimal availableTransferAmount = getAvailableTransferAmount(fund);

        return CommunityFundSummaryResponse.builder()
                .id(fund.getId())
                .name(fund.getName())
                .totalBalance(fund.getTotalBalance())
                .availableTransferAmount(availableTransferAmount)
                .isActive(fund.getIsActive())
                .createdBy(creator.getId())
                .createdByName(creator.getFullName())
                .createdAt(fund.getCreatedAt())
                .build();
    }

    public CommunityFundDetailResponse toDetailResponse(CommunityFund fund) {
        User creator = fund.getCreatedBy();
        BigDecimal availableTransferAmount = getAvailableTransferAmount(fund);

        return CommunityFundDetailResponse.builder()
                .id(fund.getId())
                .name(fund.getName())
                .description(fund.getDescription())
                .totalBalance(fund.getTotalBalance())
                .availableTransferAmount(availableTransferAmount)
                .isActive(fund.getIsActive())
                .createdBy(creator.getId())
                .createdByName(creator.getFullName())
                .createdAt(fund.getCreatedAt())
                .updatedAt(fund.getUpdatedAt())
                .build();
    }

    public CommunityFundMemberResponse toMemberResponse(CommunityFundMember member) {
        User user = member.getUser();

        return CommunityFundMemberResponse.builder()
                .fundId(member.getFund().getId())
                .userId(user.getId())
                .userName(user.getFullName())
                .userEmail(user.getEmail())
                .role(member.getRole())
                .isActive(member.getIsActive())
                .joinedAt(member.getJoinedAt())
                .build();
    }

    private BigDecimal getAvailableTransferAmount(CommunityFund fund) {
        BigDecimal reservedAmount = moneyTransferTicketRepository.sumAmountByFundAndStatuses(
                fund,
                List.of(MoneyTransferTicketStatus.PENDING, MoneyTransferTicketStatus.RESOLVED));
        BigDecimal availableAmount = fund.getTotalBalance().subtract(reservedAmount);
        return availableAmount.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : availableAmount;
    }
}
