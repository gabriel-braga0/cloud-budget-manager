package com.budgetmanager.domain;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

// Necessário para o Jackson conseguir serializar o JSON quando compilado em Imagem Nativa (GraalVM)
@RegisterForReflection
public class Transaction {

    private final String id;
    private final String userId;
    private final BigDecimal amount;
    private final TransactionType type;
    private final Category category;
    private final LocalDateTime createdAt;

    public Transaction(String userId, BigDecimal amount, TransactionType type, Category category) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor da transação deve ser maior que zero.");
        }
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("O usuário é obrigatório.");
        }

        this.id = UUID.randomUUID().toString();
        this.userId = userId;
        this.amount = amount;
        this.type = type;
        this.category = category;
        this.createdAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public BigDecimal getAmount() { return amount; }
    public TransactionType getType() { return type; }
    public Category getCategory() { return category; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
