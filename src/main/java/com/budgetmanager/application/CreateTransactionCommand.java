package com.budgetmanager.application;

import com.budgetmanager.domain.TransactionType;

import java.math.BigDecimal;

public record CreateTransactionCommand(
        String userId,
        BigDecimal amount,
        TransactionType type,
        String categoryId,
        String categoryName
) {}
