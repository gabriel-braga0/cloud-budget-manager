package com.budgetmanager.application;

import com.budgetmanager.domain.Transaction;

public interface TransactionRepository {
    void save(Transaction transaction);
}
