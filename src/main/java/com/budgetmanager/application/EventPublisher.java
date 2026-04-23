package com.budgetmanager.application;

import com.budgetmanager.domain.Transaction;

public interface EventPublisher {
    void publishHighValueExpenseAlert(Transaction transaction);
}