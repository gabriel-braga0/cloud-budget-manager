package com.budgetmanager.infrastructure;

import com.budgetmanager.application.TransactionRepository;
import com.budgetmanager.domain.Transaction;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

public class DynamoTransactionRepository implements TransactionRepository {

    private final DynamoDbClient dynamoDbClient;
    private final String tableName;

    public DynamoTransactionRepository(DynamoDbClient dynamoDbClient,
                                       @ConfigProperty(name = "aws.dynamodb.table-name") String tableName) {
        this.dynamoDbClient = dynamoDbClient;
        this.tableName = tableName;
    }

    @Override
    public void save(Transaction transaction) {

    }
}
