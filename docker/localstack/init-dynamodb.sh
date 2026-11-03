#!/bin/bash
# Runs automatically when LocalStack is ready (mounted in /etc/localstack/init/ready.d).
# Creates the single DynamoDB table used by the API. Idempotent: skips it if it already exists.
set -euo pipefail

TABLE_NAME="${DYNAMODB_TABLE_NAME:-franchises}"

if awslocal dynamodb describe-table --table-name "$TABLE_NAME" > /dev/null 2>&1; then
  echo "DynamoDB table '$TABLE_NAME' already exists"
  exit 0
fi

awslocal dynamodb create-table \
  --table-name "$TABLE_NAME" \
  --attribute-definitions AttributeName=pk,AttributeType=S AttributeName=sk,AttributeType=S \
  --key-schema AttributeName=pk,KeyType=HASH AttributeName=sk,KeyType=RANGE \
  --billing-mode PAY_PER_REQUEST

echo "DynamoDB table '$TABLE_NAME' created"
