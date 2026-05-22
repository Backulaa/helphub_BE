package com.helphub.backend.modules.expense;

import com.helphub.backend.common.payload.ApiResponse;
import com.helphub.backend.modules.expense.dto.request.CreateExpenseRequest;
import com.helphub.backend.modules.expense.dto.response.ExpenseResponse;
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
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping("/expenses")
    public ResponseEntity<ApiResponse<ExpenseResponse>> createExpense(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody CreateExpenseRequest request) {

        ExpenseResponse response = expenseService.createExpense(
                currentUser.getUserId(),
                request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<ExpenseResponse>builder()
                        .success(true)
                        .message("Expense created successfully")
                        .data(response)
                        .build());
    }

    @GetMapping("/community-funds/{fundId}/expenses")
    public ResponseEntity<ApiResponse<List<ExpenseResponse>>> getExpensesByFund(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable @NotNull UUID fundId) {

        List<ExpenseResponse> response = expenseService.getExpensesByFund(
                currentUser.getUserId(),
                fundId);

        return ResponseEntity.ok(ApiResponse.<List<ExpenseResponse>>builder()
                .success(true)
                .message("Fund expenses fetched successfully")
                .data(response)
                .build());
    }
}