package com.budgetmanager.domain;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record Category(String id, String name) {
}
