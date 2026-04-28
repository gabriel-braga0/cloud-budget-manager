package com.budgetmanager.infrastructure;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.util.List;

@ApplicationScoped
public class SqsExpenseAlertConsumer {

    private final SqsClient sqsClient;
    private final String queueUrl;

    public SqsExpenseAlertConsumer(
            SqsClient sqsClient,
            @ConfigProperty(name = "aws.sqs.expense-alert-queue-url") String queueUrl) {
        this.sqsClient = sqsClient;
        this.queueUrl = queueUrl;
    }

    @Scheduled(every = "5s")
    public void consumeMessages() {

        ReceiveMessageRequest receiveRequest = ReceiveMessageRequest.builder()
                .queueUrl(queueUrl)
                .maxNumberOfMessages(5)
                .waitTimeSeconds(2)
                .build();

        List<Message> messages = sqsClient.receiveMessage(receiveRequest).messages();

        for (Message message : messages) {
            System.out.println("\n=================================================");
            System.out.println("📩 [CONSUMIDOR SQS] Nova mensagem recebida da fila!");
            System.out.println("Conteúdo do Alerta: " + message.body());

            System.out.println("📧 Simulando envio de e-mail de alerta de gastos...");

            sqsClient.deleteMessage(DeleteMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .receiptHandle(message.receiptHandle())
                    .build());

            System.out.println("✅ Mensagem apagada da fila com sucesso.");
            System.out.println("=================================================\n");
        }
    }
}