package com.helphub.backend.modules.donation;

import com.helphub.backend.modules.donation.dto.request.CreateDonationRequest;
import com.helphub.backend.modules.donation.dto.request.CreatePayOsDonationRequest;
import com.helphub.backend.modules.donation.dto.response.DonationResponse;
import com.helphub.backend.modules.payment.dto.response.PayOsCheckoutResponse;

import java.util.List;
import java.util.UUID;

public interface DonationService {

    DonationResponse createDonation(UUID donorId, CreateDonationRequest request);

    PayOsCheckoutResponse createPayOsDonation(UUID donorId, CreatePayOsDonationRequest request);

    List<DonationResponse> getMyDonations(UUID donorId);

    List<DonationResponse> getDonationsByFund(UUID currentUserId, UUID fundId);
}