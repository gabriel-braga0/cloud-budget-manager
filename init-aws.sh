#!/bin/bash
echo "🚀 Iniciando a criação da infraestrutura no LocalStack..."

awslocal dynamodb create-table \
    --region us-east-1 \
    --table-name CloudBudgetTable \
    --attribute-definitions AttributeName=PK,AttributeType=S AttributeName=SK,AttributeType=S AttributeName=GSI1PK,AttributeType=S AttributeName=GSI1SK,AttributeType=S \
    --key-schema AttributeName=PK,KeyType=HASH AttributeName=SK,KeyType=RANGE \
    --global-secondary-indexes '[{"IndexName": "GSI1","KeySchema":[{"AttributeName":"GSI1PK","KeyType":"HASH"},{"AttributeName":"GSI1SK","KeyType":"RANGE"}],"Projection":{"ProjectionType":"ALL"}}]' \
    --billing-mode PAY_PER_REQUEST

awslocal sns create-topic --name expense-alerts-topic

awslocal sqs create-queue --queue-name expense-alerts-queue

awslocal sns subscribe \
    --topic-arn arn:aws:sns:us-east-1:000000000000:expense-alerts-topic \
    --protocol sqs \
    --notification-endpoint arn:aws:sqs:us-east-1:000000000000:expense-alerts-queue

echo "✅ Infraestrutura pronta e vinculada!"