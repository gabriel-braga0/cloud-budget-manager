package com.budgetmanager.application;

import com.budgetmanager.domain.Category;
import com.budgetmanager.domain.Transaction;
import com.budgetmanager.domain.TransactionType;
import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;

@ApplicationScoped
public class CreateTransactionUseCase {

    private final TransactionRepository repository;
    private final EventPublisher eventPublisher;

    public CreateTransactionUseCase(TransactionRepository repository, EventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    public Transaction execute(CreateTransactionCommand command) {
        Category category = new Category(command.categoryId(), command.categoryName());

        Transaction transaction = new Transaction(
                command.userId(),
                command.amount(),
                command.type(),
                category
        );

        repository.save(transaction);

        BigDecimal threshold = new BigDecimal("1000.00");
        if (transaction.getType() == TransactionType.EXPENSE && transaction.getAmount().compareTo(threshold) > 0) {
            System.out.println("Alerta: Despesa de alto valor detectada! Publicando evento...");
            eventPublisher.publishHighValueExpenseAlert(transaction);
        }

        return transaction;
    }
}