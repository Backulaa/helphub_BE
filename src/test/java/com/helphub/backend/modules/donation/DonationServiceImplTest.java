package com.helphub.backend.modules.donation;

import com.helphub.backend.common.enums.DonationStatus;
import com.helphub.backend.common.enums.PaymentMethod;
import com.helphub.backend.modules.donation.dto.response.DonationResponse;
import com.helphub.backend.modules.payment.PayOsService;
import com.helphub.backend.persistence.entity.CommunityFund;
import com.helphub.backend.persistence.entity.Donation;
import com.helphub.backend.persistence.entity.User;
import com.helphub.backend.persistence.repository.CommunityFundRepository;
import com.helphub.backend.persistence.repository.DonationRepository;
import com.helphub.backend.persistence.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DonationServiceImplTest {

    @Mock
    private DonationRepository donationRepository;

    @Mock
    private CommunityFundRepository communityFundRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DonationMapper donationMapper;

    @Mock
    private PayOsService payOsService;

    @InjectMocks
    private DonationServiceImpl donationService;

    private UUID donorId;
    private UUID fundId;
    private User donor;
    private CommunityFund fund;

    @BeforeEach
    void setUp() {
        donorId = UUID.randomUUID();
        fundId = UUID.randomUUID();

        donor = new User();
        donor.setId(donorId);
        donor.setFullName("Donor");

        fund = new CommunityFund();
        fund.setId(fundId);
        fund.setName("Community Fund");
        fund.setIsActive(true);
        fund.setTotalBalance(BigDecimal.ZERO);
    }

    @Test
    void getMyDonations_shouldReturnOnlySuccessfulDonations() {
        Donation donation = createDonation();
        DonationResponse expectedResponse = createDonationResponse(donation.getId());

        when(userRepository.findById(donorId)).thenReturn(Optional.of(donor));
        when(donationRepository.findAllByDonorAndStatusOrderByCreatedAtDesc(donor, DonationStatus.SUCCESS))
                .thenReturn(List.of(donation));
        when(donationMapper.toResponse(donation)).thenReturn(expectedResponse);

        List<DonationResponse> response = donationService.getMyDonations(donorId);

        assertEquals(1, response.size());
        assertEquals(donation.getId(), response.get(0).getId());

        verify(donationRepository).findAllByDonorAndStatusOrderByCreatedAtDesc(donor, DonationStatus.SUCCESS);
    }

    @Test
    void getDonationsByFund_shouldReturnOnlySuccessfulDonations() {
        Donation donation = createDonation();
        DonationResponse expectedResponse = createDonationResponse(donation.getId());

        when(userRepository.findById(donorId)).thenReturn(Optional.of(donor));
        when(communityFundRepository.findById(fundId)).thenReturn(Optional.of(fund));
        when(donationRepository.findAllByFundAndStatusOrderByCreatedAtDesc(fund, DonationStatus.SUCCESS))
                .thenReturn(List.of(donation));
        when(donationMapper.toResponse(donation)).thenReturn(expectedResponse);

        List<DonationResponse> response = donationService.getDonationsByFund(donorId, fundId);

        assertEquals(1, response.size());
        assertEquals(donation.getId(), response.get(0).getId());

        verify(donationRepository).findAllByFundAndStatusOrderByCreatedAtDesc(fund, DonationStatus.SUCCESS);
    }

    private Donation createDonation() {
        Donation donation = new Donation();
        donation.setId(UUID.randomUUID());
        donation.setFund(fund);
        donation.setDonor(donor);
        donation.setAmount(BigDecimal.valueOf(50000));
        donation.setPaymentMethod(PaymentMethod.PAYOS);
        donation.setStatus(DonationStatus.SUCCESS);
        return donation;
    }

    private DonationResponse createDonationResponse(UUID donationId) {
        return DonationResponse.builder()
                .id(donationId)
                .fundId(fundId)
                .fundName("Community Fund")
                .donorId(donorId)
                .donorName("Donor")
                .amount(BigDecimal.valueOf(50000))
                .paymentMethod(PaymentMethod.PAYOS)
                .status(DonationStatus.SUCCESS)
                .build();
    }
}
