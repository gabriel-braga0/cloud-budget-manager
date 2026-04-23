package com.budgetmanager.infrastructure;

import com.budgetmanager.application.EventPublisher;
import com.budgetmanager.domain.Transaction;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

@ApplicationScoped
public class SnsEventPublisher implements EventPublisher {

    private final SnsClient snsClient;
    private final String topicArn;

    public SnsEventPublisher(
            SnsClient snsClient,
            @ConfigProperty(name = "aws.sns.expense-alert-topic-arn") String topicArn) {
        this.snsClient = snsClient;
        this.topicArn = topicArn;
    }

    @Override
    public void publishHighValueExpenseAlert(Transaction transaction) {
        String messagePayload = String.format(
                "{\"transactionId\":\"%s\", \"userId\":\"%s\", \"amount\":%s, \"category\":\"%s\"}",
                transaction.getId(),
                transaction.getUserId(),
                transaction.getAmount().toString(),
                transaction.getCategory().name()
        );

        PublishRequest request = PublishRequest.builder()
                .topicArn(topicArn)
                .message(messagePayload)
                .subject("High Value Expense Alert")
                .build();

        snsClient.publish(request);
        System.out.println("Evento publicado com sucesso no SNS: " + messagePayload);
    }
}