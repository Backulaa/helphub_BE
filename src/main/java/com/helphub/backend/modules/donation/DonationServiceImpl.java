package com.helphub.backend.modules.donation;

import com.helphub.backend.common.enums.DonationStatus;
import com.helphub.backend.common.exception.BadRequestException;
import com.helphub.backend.common.exception.ResourceNotFoundException;
import com.helphub.backend.modules.donation.dto.request.CreateDonationRequest;
import com.helphub.backend.modules.donation.dto.response.DonationResponse;
import com.helphub.backend.persistence.entity.CommunityFund;
import com.helphub.backend.persistence.entity.Donation;
import com.helphub.backend.persistence.entity.User;
import com.helphub.backend.persistence.repository.CommunityFundRepository;
import com.helphub.backend.persistence.repository.DonationRepository;
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
public class DonationServiceImpl implements DonationService {

    private final DonationRepository donationRepository;
    private final CommunityFundRepository communityFundRepository;
    private final UserRepository userRepository;
    private final DonationMapper donationMapper;

    @Override
    @Transactional
    public DonationResponse createDonation(UUID donorId, CreateDonationRequest request) {
        User donor = getUserById(donorId);
        CommunityFund fund = getCommunityFundByIdOrThrow(request.getFundId());

        validateActiveFund(fund);

        BigDecimal amount = validatePositiveAmount(request.getAmount());

        Donation donation = Donation.builder()
                .fund(fund)
                .donor(donor)
                .amount(amount)
                .paymentMethod(Objects.requireNonNull(request.getPaymentMethod()))
                .status(DonationStatus.SUCCESS)
                .transactionCode(normalizeNullable(request.getTransactionCode()))
                .note(normalizeNullable(request.getNote()))
                .build();

        Donation savedDonation = donationRepository.save(Objects.requireNonNull(donation));

        fund.setTotalBalance(fund.getTotalBalance().add(amount));
        communityFundRepository.save(fund);

        return donationMapper.toResponse(savedDonation);
    }

    @Override
    public List<DonationResponse> getMyDonations(UUID donorId) {
        User donor = getUserById(donorId);

        return donationRepository.findAllByDonorOrderByCreatedAtDesc(donor)
                .stream()
                .map(donationMapper::toResponse)
                .toList();
    }

    @Override
    public List<DonationResponse> getDonationsByFund(UUID currentUserId, UUID fundId) {
        @SuppressWarnings("unused")
        User currentUser = getUserById(currentUserId);
        CommunityFund fund = getCommunityFundByIdOrThrow(fundId);

        return donationRepository.findAllByFundOrderByCreatedAtDesc(fund)
                .stream()
                .map(donationMapper::toResponse)
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

    private BigDecimal validatePositiveAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Amount must be greater than 0");
        }

        return amount;
    }

    private String normalizeNullable(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }

        return value.trim();
    }
}