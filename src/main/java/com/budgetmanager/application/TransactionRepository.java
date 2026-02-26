package com.budgetmanager.application;

import com.budgetmanager.domain.Transaction;

import java.util.List;

public interface TransactionRepository {
    void save(Transaction transaction);
    List<Transaction> findByUserId(String userId);
}
