package com.budgetmanager.domain;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public enum TransactionType {
    INCOME,
    EXPENSE,
}
