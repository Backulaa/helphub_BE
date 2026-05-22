package com.helphub.backend.modules.donation;

import com.helphub.backend.modules.donation.dto.request.CreateDonationRequest;
import com.helphub.backend.modules.donation.dto.response.DonationResponse;

import java.util.List;
import java.util.UUID;

public interface DonationService {

    DonationResponse createDonation(UUID donorId, CreateDonationRequest request);

    List<DonationResponse> getMyDonations(UUID donorId);

    List<DonationResponse> getDonationsByFund(UUID currentUserId, UUID fundId);
}