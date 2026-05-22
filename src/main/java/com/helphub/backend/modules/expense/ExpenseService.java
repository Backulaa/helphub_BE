package com.helphub.backend.modules.expense;

import com.helphub.backend.modules.expense.dto.request.CreateExpenseRequest;
import com.helphub.backend.modules.expense.dto.response.ExpenseResponse;

import java.util.List;
import java.util.UUID;

public interface ExpenseService {

    ExpenseResponse createExpense(UUID currentUserId, CreateExpenseRequest request);

    List<ExpenseResponse> getExpensesByFund(UUID currentUserId, UUID fundId);
}