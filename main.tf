terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

# 1. Provedor focado no LocalStack
provider "aws" {
  region                      = "us-east-1"
  access_key                  = "test"
  secret_key                  = "test"
  skip_credentials_validation = true
  skip_metadata_api_check     = true
  skip_requesting_account_id  = true

  endpoints {
    dynamodb     = "http://localhost:4566"
    sns          = "http://localhost:4566"
    sqs          = "http://localhost:4566"
    lambda       = "http://localhost:4566"
    iam          = "http://localhost:4566"
    apigateway = "http://localhost:4566" # O motor do HTTP API
  }
}

# 2. Infraestrutura de Dados (Reutilizada)
resource "aws_dynamodb_table" "cloud_budget_table" {
  name           = "CloudBudgetTable"
  billing_mode   = "PAY_PER_REQUEST"
  hash_key       = "PK"
  range_key      = "SK"

  attribute {
    name = "PK"
    type = "S"
  }
  attribute {
    name = "SK"
    type = "S"
  }
  attribute {
    name = "GSI1PK"
    type = "S"
  }
  attribute {
    name = "GSI1SK"
    type = "S"
  }

  global_secondary_index {
    name               = "GSI1"
    hash_key           = "GSI1PK"
    range_key          = "GSI1SK"
    projection_type    = "ALL"
  }
}

resource "aws_sns_topic" "expense_alerts" {
  name = "expense-alerts-topic"
}

resource "aws_sqs_queue" "expense_alerts_queue" {
  name = "expense-alerts-queue"
}

resource "aws_sns_topic_subscription" "expense_alerts_sqs_target" {
  topic_arn = aws_sns_topic.expense_alerts.arn
  protocol  = "sqs"
  endpoint  = aws_sqs_queue.expense_alerts_queue.arn
}

# 3. IAM Role para as Lambdas (API e Worker)
resource "aws_iam_role" "lambda_exec_role" {
  name = "budget_manager_lambda_role"
  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Action = "sts:AssumeRole"
      Effect = "Allow"
      Principal = { Service = "lambda.amazonaws.com" }
    }]
  })
}

# 4. A Lambda da API (O Coração da V2)
resource "aws_lambda_function" "budget_api_lambda" {
  function_name = "budget-api-lambda"
  role          = aws_iam_role.lambda_exec_role.arn
  handler       = "io.quarkus.amazon.lambda.runtime.QuarkusStreamHandler::handleRequest"
  runtime       = "java21"
  timeout       = 30 # APIs podem demorar um pouco mais no Cold Start
  memory_size   = 512

  # Apontando para o ZIP da API que agora tem a extensão lambda-rest
  filename         = "./target/function.zip"
  source_code_hash = filebase64sha256("./target/function.zip")

  environment {
    variables = {
      QUARKUS_LAMBDA_HANDLER = "rest" # Handler padrão da extensão lambda-rest
    }
  }
}

# 5. O API Gateway V1 (REST API - Gratuito no LocalStack)
resource "aws_api_gateway_rest_api" "budget_rest_api" {
  name        = "budget-manager-api"
  description = "API Serverless (V1) para gerenciamento de despesas"
}

# 5.1 Rota Coringa {proxy+}
resource "aws_api_gateway_resource" "proxy_resource" {
  rest_api_id = aws_api_gateway_rest_api.budget_rest_api.id
  parent_id   = aws_api_gateway_rest_api.budget_rest_api.root_resource_id
  path_part   = "{proxy+}"
}

# 5.2 Método ANY para o {proxy+}
resource "aws_api_gateway_method" "proxy_method" {
  rest_api_id   = aws_api_gateway_rest_api.budget_rest_api.id
  resource_id   = aws_api_gateway_resource.proxy_resource.id
  http_method   = "ANY"
  authorization = "NONE"
}

# 5.3 Integração do proxy com a Lambda
resource "aws_api_gateway_integration" "lambda_proxy_integration" {
  rest_api_id             = aws_api_gateway_rest_api.budget_rest_api.id
  resource_id             = aws_api_gateway_resource.proxy_resource.id
  http_method             = aws_api_gateway_method.proxy_method.http_method
  integration_http_method = "POST" # A AWS exige POST na integração com Lambda no V1
  type                    = "AWS_PROXY"
  uri                     = aws_lambda_function.budget_api_lambda.invoke_arn
}

# 5.4 Publicação (Deployment e Stage)
resource "aws_api_gateway_deployment" "api_deployment" {
  depends_on = [aws_api_gateway_integration.lambda_proxy_integration]
  rest_api_id = aws_api_gateway_rest_api.budget_rest_api.id
}

resource "aws_api_gateway_stage" "api_stage" {
  deployment_id = aws_api_gateway_deployment.api_deployment.id
  rest_api_id   = aws_api_gateway_rest_api.budget_rest_api.id
  stage_name    = "dev"
}

# 5.5 Permissão: Deixa o API Gateway invocar a Lambda
resource "aws_lambda_permission" "api_gw_permission" {
  statement_id  = "AllowExecutionFromAPIGateway"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.budget_api_lambda.function_name
  principal     = "apigateway.amazonaws.com"
  source_arn    = "${aws_api_gateway_rest_api.budget_rest_api.execution_arn}/*/*"
}



# 6. O Worker (Consumidor SQS) - Mantemos ele aqui
resource "aws_lambda_function" "expense_alert_lambda" {
  function_name = "expense-alert-lambda"
  role          = aws_iam_role.lambda_exec_role.arn
  handler       = "io.quarkus.amazon.lambda.runtime.QuarkusStreamHandler::handleRequest"
  runtime       = "java21"
  filename      = "../cloud-budget-worker/target/function.zip"
  source_code_hash = filebase64sha256("../cloud-budget-worker/target/function.zip")

  environment {
    variables = {
      QUARKUS_LAMBDA_HANDLER = "expenseAlertLambda"
    }
  }
}

resource "aws_lambda_event_source_mapping" "sqs_to_lambda" {
  event_source_arn = aws_sqs_queue.expense_alerts_queue.arn
  function_name    = aws_lambda_function.expense_alert_lambda.arn
}

# OUTPUT: A URL Mágica
output "api_endpoint" {
  value = aws_api_gateway_stage.api_stage.invoke_url
}