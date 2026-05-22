package com.helphub.backend.modules.donation;

import com.helphub.backend.common.payload.ApiResponse;
import com.helphub.backend.modules.donation.dto.request.CreateDonationRequest;
import com.helphub.backend.modules.donation.dto.response.DonationResponse;
import com.helphub.backend.security.model.CustomUserDetails;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Validated
public class DonationController {

    private final DonationService donationService;

    @PostMapping("/donations")
    public ResponseEntity<ApiResponse<DonationResponse>> createDonation(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody CreateDonationRequest request) {

        DonationResponse response = donationService.createDonation(
                currentUser.getUserId(),
                request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<DonationResponse>builder()
                        .success(true)
                        .message("Donation created successfully")
                        .data(response)
                        .build());
    }

    @GetMapping("/donations/my-donations")
    public ResponseEntity<ApiResponse<List<DonationResponse>>> getMyDonations(
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        List<DonationResponse> response = donationService.getMyDonations(
                currentUser.getUserId());

        return ResponseEntity.ok(ApiResponse.<List<DonationResponse>>builder()
                .success(true)
                .message("My donations fetched successfully")
                .data(response)
                .build());
    }

    @GetMapping("/community-funds/{fundId}/donations")
    public ResponseEntity<ApiResponse<List<DonationResponse>>> getDonationsByFund(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable @NotNull UUID fundId) {

        List<DonationResponse> response = donationService.getDonationsByFund(
                currentUser.getUserId(),
                fundId);

        return ResponseEntity.ok(ApiResponse.<List<DonationResponse>>builder()
                .success(true)
                .message("Fund donations fetched successfully")
                .data(response)
                .build());
    }
}