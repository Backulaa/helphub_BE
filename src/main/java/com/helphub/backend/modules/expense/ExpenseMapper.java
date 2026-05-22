package com.helphub.backend.modules.expense;

import com.helphub.backend.modules.expense.dto.response.ExpenseResponse;
import com.helphub.backend.persistence.entity.Expense;
import com.helphub.backend.persistence.entity.SupportRequest;
import org.springframework.stereotype.Component;

@Component
public class ExpenseMapper {

    public ExpenseResponse toResponse(Expense expense) {
        SupportRequest supportRequest = expense.getSupportRequest();

        return ExpenseResponse.builder()
                .id(expense.getId())
                .fundId(expense.getFund().getId())
                .fundName(expense.getFund().getName())
                .supportRequestId(supportRequest != null ? supportRequest.getId() : null)
                .supportRequestTitle(supportRequest != null ? supportRequest.getTitle() : null)
                .createdBy(expense.getCreatedBy().getId())
                .createdByName(expense.getCreatedBy().getFullName())
                .amount(expense.getAmount())
                .description(expense.getDescription())
                .createdAt(expense.getCreatedAt())
                .build();
    }
}