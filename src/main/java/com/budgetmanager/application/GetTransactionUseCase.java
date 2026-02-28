package com.budgetmanager.application;

import com.budgetmanager.domain.Transaction;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class GetTransactionUseCase {

    private final TransactionRepository transactionRepository;

    public GetTransactionUseCase(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public List<Transaction> execute(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("ID do usuário não pode ser nulo");
        }
        return transactionRepository.findByUserId(userId);
    }

    public List<Transaction> executeByMonth(String userId, String month) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("ID do usuário não pode ser nulo");
        } else if (month == null || month.isBlank()) {
            throw new IllegalArgumentException("Mes não pode ser nulo");
        }
        return transactionRepository.findByUserIdAndMonth(userId, month);
    }
}
