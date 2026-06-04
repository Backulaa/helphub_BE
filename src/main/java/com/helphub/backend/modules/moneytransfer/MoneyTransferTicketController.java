package com.helphub.backend.modules.moneytransfer;

import com.helphub.backend.common.enums.MoneyTransferTicketStatus;
import com.helphub.backend.common.payload.ApiResponse;
import com.helphub.backend.modules.moneytransfer.dto.request.CreateMoneyTransferTicketRequest;
import com.helphub.backend.modules.moneytransfer.dto.request.RejectMoneyTransferTicketRequest;
import com.helphub.backend.modules.moneytransfer.dto.response.MoneyTransferTicketResponse;
import com.helphub.backend.security.model.CustomUserDetails;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/money-transfer-tickets")
@RequiredArgsConstructor
@Validated
public class MoneyTransferTicketController {

    private final MoneyTransferTicketService moneyTransferTicketService;

    @PostMapping("/community-funds/{fundId}")
    public ResponseEntity<ApiResponse<MoneyTransferTicketResponse>> createCommunityFundTicket(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable @NotNull UUID fundId,
            @Valid @RequestBody CreateMoneyTransferTicketRequest request) {

        MoneyTransferTicketResponse response = moneyTransferTicketService.createCommunityFundTicket(
                currentUser.getUserId(),
                fundId,
                request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<MoneyTransferTicketResponse>builder()
                        .success(true)
                        .message("Money transfer ticket created successfully")
                        .data(response)
                        .build());
    }

    @PostMapping("/support-needs/{supportNeedId}")
    public ResponseEntity<ApiResponse<MoneyTransferTicketResponse>> createSupportNeedTicket(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable @NotNull UUID supportNeedId,
            @Valid @RequestBody CreateMoneyTransferTicketRequest request) {

        MoneyTransferTicketResponse response = moneyTransferTicketService.createSupportNeedTicket(
                currentUser.getUserId(),
                supportNeedId,
                request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<MoneyTransferTicketResponse>builder()
                        .success(true)
                        .message("Money transfer ticket created successfully")
                        .data(response)
                        .build());
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<MoneyTransferTicketResponse>>> getMyTickets(
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        List<MoneyTransferTicketResponse> response = moneyTransferTicketService.getMyTickets(
                currentUser.getUserId());

        return ResponseEntity.ok(ApiResponse.<List<MoneyTransferTicketResponse>>builder()
                .success(true)
                .message("Money transfer tickets fetched successfully")
                .data(response)
                .build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public ResponseEntity<ApiResponse<List<MoneyTransferTicketResponse>>> getAdminTickets(
            @RequestParam(required = false) MoneyTransferTicketStatus status) {

        List<MoneyTransferTicketResponse> response = moneyTransferTicketService.getAdminTickets(status);

        return ResponseEntity.ok(ApiResponse.<List<MoneyTransferTicketResponse>>builder()
                .success(true)
                .message("Money transfer tickets fetched successfully")
                .data(response)
                .build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/pending")
    public ResponseEntity<ApiResponse<List<MoneyTransferTicketResponse>>> getPendingTickets() {

        List<MoneyTransferTicketResponse> response = moneyTransferTicketService.getAdminTickets(
                MoneyTransferTicketStatus.PENDING);

        return ResponseEntity.ok(ApiResponse.<List<MoneyTransferTicketResponse>>builder()
                .success(true)
                .message("Pending money transfer tickets fetched successfully")
                .data(response)
                .build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/admin/{ticketId}/reject")
    public ResponseEntity<ApiResponse<MoneyTransferTicketResponse>> rejectTicket(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable @NotNull UUID ticketId,
            @Valid @RequestBody RejectMoneyTransferTicketRequest request) {

        MoneyTransferTicketResponse response = moneyTransferTicketService.rejectTicket(
                currentUser.getUserId(),
                ticketId,
                request);

        return ResponseEntity.ok(ApiResponse.<MoneyTransferTicketResponse>builder()
                .success(true)
                .message("Money transfer ticket rejected successfully")
                .data(response)
                .build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/admin/{ticketId}/resolve")
    public ResponseEntity<ApiResponse<MoneyTransferTicketResponse>> resolveTicket(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable @NotNull UUID ticketId,
            @RequestPart("proofFile") MultipartFile proofFile,
            @RequestParam(required = false) String adminNote) {

        MoneyTransferTicketResponse response = moneyTransferTicketService.resolveTicket(
                currentUser.getUserId(),
                ticketId,
                proofFile,
                adminNote);

        return ResponseEntity.ok(ApiResponse.<MoneyTransferTicketResponse>builder()
                .success(true)
                .message("Money transfer ticket resolved successfully")
                .data(response)
                .build());
    }
}
