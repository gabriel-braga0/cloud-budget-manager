# ☁️ Cloud Budget Manager (Serverless API)

![GitHub repo size](https://img.shields.io/github/repo-size/gabriel-braga0/cloud-budget-manager)
![GitHub stars](https://img.shields.io/github/stars/gabriel-braga0/cloud-budget-manager?style=social)
![GitHub last commit](https://img.shields.io/github/last-commit/gabriel-braga0/cloud-budget-manager)
![Build](https://img.shields.io/badge/build-passing-brightgreen)
![AWS](https://img.shields.io/badge/AWS-Serverless-orange)
![Java](https://img.shields.io/badge/Java-21-blue)
![Quarkus](https://img.shields.io/badge/Quarkus-Supersonic-red)
![License](https://img.shields.io/badge/license-MIT-green)

---

## 🚀 Destaques Técnicos

- **Zero Cold Start de Código:** Utilização do GraalVM para compilar a aplicação em uma imagem nativa Linux, reduzindo o tempo de inicialização e o tempo de execução da Lambda para **~30ms**
- **Eficiência de Custos:** A API roda perfeitamente no tier mais baixo de memória da AWS Lambda (**128 MB**)
- **Event-Driven:** Desacoplamento total do processamento de background utilizando mensageria (SNS/SQS)
- **Local Cloud Development:** Todo o ecossistema da AWS é emulado localmente via **LocalStack**

---

## 🛠️ Tecnologias Utilizadas

- **Backend:** Java 21, Quarkus (Amazon Lambda REST, DynamoDB, SNS, SQS)
- **Compilação Nativa:** GraalVM
- **Cloud & Serverless:** AWS API Gateway, AWS Lambda, DynamoDB, SNS, SQS
- **Infraestrutura e DevOps:** Terraform, Docker, LocalStack

---

## 📐 Arquitetura

```mermaid
sequenceDiagram
    participant Client as 💻 Cliente
    participant API_GW as 🚪 API Gateway
    participant Lambda_API as ⚡ Lambda (API Nativa)
    participant DynamoDB as 🗄️ DynamoDB
    participant SNS as 📢 Amazon SNS
    participant SQS as 📬 Amazon SQS
    participant Lambda_Worker as ⚙️ Lambda (Worker)

    Client->>API_GW: POST /transactions
    API_GW->>Lambda_API: Proxy Request
    Lambda_API->>DynamoDB: Salva Despesa
    Lambda_API->>SNS: Publica Evento (Nova Despesa)
    Lambda_API-->>API_GW: 201 Created (JSON)
    API_GW-->>Client: Resposta Imediata (~30ms)

    SNS-->>SQS: Faz o fan-out da mensagem
    SQS->>Lambda_Worker: Acorda o Consumidor (Assíncrono)
    Lambda_Worker->>Lambda_Worker: Processa alertas/regras de negócio
```

---

## ⚙️ Como Executar o Projeto (Localmente)

### 📌 Pré-requisitos

- Docker Desktop rodando
- Terraform instalado
- JDK 21+ e Maven  
  *(ou GraalVM para build nativo)*

---

### ▶️ Passo a Passo

#### 1. Subir a Infraestrutura Base (LocalStack)

```bash
docker-compose up -d
```

#### 2. Compilar a Imagem Nativa

```bash
./mvnw clean package -Pnative "-Dquarkus.native.container-build=true"
```

#### 3. Deploy com Terraform

```bash
terraform init
terraform apply -auto-approve
```

#### 4. Testar a API

```bash
curl -X POST http://localhost:4566/_aws/execute-api/{API_ID}/dev/transactions \
     -H "Content-Type: application/json" \
     -d '{"amount": 150.00, "type": "EXPENSE", "categoryId": "cat-01"}'
```

---

## ✅ Resultado Esperado

- API respondendo localmente via API Gateway (LocalStack)
- Persistência no DynamoDB local
- Evento sendo publicado no SNS
- Processamento assíncrono via SQS + Lambda Worker  
