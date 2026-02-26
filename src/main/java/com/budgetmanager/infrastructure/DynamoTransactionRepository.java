package com.budgetmanager.infrastructure;

import com.budgetmanager.application.TransactionRepository;
import com.budgetmanager.domain.Category;
import com.budgetmanager.domain.Transaction;
import com.budgetmanager.domain.TransactionType;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class DynamoTransactionRepository implements TransactionRepository {

    private final DynamoDbClient dynamoDbClient;
    private final String tableName;

    public DynamoTransactionRepository(DynamoDbClient dynamoDbClient, @ConfigProperty(name = "aws.dynamodb.table-name") String tableName) {
        this.dynamoDbClient = dynamoDbClient;
        this.tableName = tableName;
    }

    @Override
    public void save(Transaction transaction) {
        Map<String, AttributeValue> item = new HashMap<>();

        item.put("PK", AttributeValue.builder().s("USER#" + transaction.getUserId()).build());
        item.put("SK", AttributeValue.builder().s("TRANSACTION#" + transaction.getCreatedAt().toString()).build());

        item.put("id", AttributeValue.builder().s(transaction.getId()).build());
        item.put("amount", AttributeValue.builder().n(transaction.getAmount().toString()).build());
        item.put("type", AttributeValue.builder().s(transaction.getType().name()).build());
        item.put("categoryId", AttributeValue.builder().s(transaction.getCategory().id()).build());
        item.put("categoryName", AttributeValue.builder().s(transaction.getCategory().name()).build());

        item.put("entityType", AttributeValue.builder().s("TRANSACTION").build());

        PutItemRequest putRequest = PutItemRequest.builder().tableName(tableName).item(item).build();

        dynamoDbClient.putItem(putRequest);
    }

    @Override
    public List<Transaction> findByUserId(String userId) {
        String pkValue = "USER#" + userId;

        QueryRequest queryRequest = QueryRequest.builder()
                .tableName(tableName)
                .keyConditionExpression("PK = :pk")
                .expressionAttributeValues(Map.of(
                        ":pk", AttributeValue.builder().s(pkValue).build()
                ))
                .build();

        QueryResponse response = dynamoDbClient.query(queryRequest);
        List<Transaction> transactions = new ArrayList<>();

        for (Map<String, AttributeValue> item : response.items()) {
            Category category = new Category(
                    item.get("categoryId").s(),
                    item.get("categoryName").s()
            );

            Transaction transaction = new Transaction(
                    userId,
                    new BigDecimal(item.get("amount").n()),
                    TransactionType.valueOf(item.get("type").s()),
                    category
            );
            transactions.add(transaction);
        }

        return transactions;
    }
}
