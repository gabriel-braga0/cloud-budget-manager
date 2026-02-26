package com.budgetmanager.application;

import com.budgetmanager.domain.Category;
import com.budgetmanager.domain.Transaction;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CreateTransactionUseCase {

    private final TransactionRepository repository;

    public CreateTransactionUseCase(TransactionRepository repository) {
        this.repository = repository;
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


        return transaction;
    }
}