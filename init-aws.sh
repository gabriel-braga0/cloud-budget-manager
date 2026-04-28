#!/bin/bash
echo "🚀 Iniciando o Deploy da Infraestrutura com AWS CloudFormation..."

awslocal cloudformation deploy \
    --template-file /tmp/template.yaml \
    --stack-name budget-stack \
    --region us-east-1

echo "✅ Deploy via IaC finalizado com sucesso!"