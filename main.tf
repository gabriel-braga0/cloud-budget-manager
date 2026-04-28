terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region                      = "us-east-1"
  access_key                  = "test"
  secret_key                  = "test"
  skip_credentials_validation = true
  skip_metadata_api_check     = true
  skip_requesting_account_id  = true

  endpoints {
    dynamodb = "http://localhost:4566"
    sns      = "http://localhost:4566"
    sqs      = "http://localhost:4566"
    lambda   = "http://localhost:4566"
    iam      = "http://localhost:4566"
  }
}

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

resource "aws_sqs_queue_policy" "expense_alerts_queue_policy" {
  queue_url = aws_sqs_queue.expense_alerts_queue.id
  policy    = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect    = "Allow"
        Principal = "*"
        Action    = "sqs:SendMessage"
        Resource  = aws_sqs_queue.expense_alerts_queue.arn
        Condition = {
          ArnEquals = { "aws:SourceArn" = aws_sns_topic.expense_alerts.arn }
        }
      }
    ]
  })
}

resource "aws_iam_role" "lambda_exec_role" {
  name = "expense_alert_lambda_role"
  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Action = "sts:AssumeRole"
      Effect = "Allow"
      Principal = { Service = "lambda.amazonaws.com" }
    }]
  })
}

resource "aws_lambda_function" "expense_alert_lambda" {
  function_name = "expense-alert-lambda"
  role          = aws_iam_role.lambda_exec_role.arn
  handler       = "io.quarkus.amazon.lambda.runtime.QuarkusStreamHandler::handleRequest"
  runtime       = "java21"
  timeout       = 15
  memory_size   = 512

  filename         = "../cloud-budget-worker/target/function.zip"
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
  batch_size       = 5
}