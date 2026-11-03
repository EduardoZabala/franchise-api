#!/usr/bin/env bash
# Prints the public URL of the running ECS task (there is no load balancer, so the IP changes per deploy).
# Usage: ./infra/scripts/get-api-url.sh [cluster] [service] [region]
set -euo pipefail

CLUSTER="${1:-franchise-api-cluster}"
SERVICE="${2:-franchise-api-service}"
REGION="${3:-${AWS_REGION:-us-east-1}}"
PORT=8080

TASK_ARN=$(aws ecs list-tasks --cluster "$CLUSTER" --service-name "$SERVICE" \
  --desired-status RUNNING --region "$REGION" --query 'taskArns[0]' --output text)

if [[ -z "$TASK_ARN" || "$TASK_ARN" == "None" ]]; then
  echo "No running task found for service $SERVICE in cluster $CLUSTER" >&2
  exit 1
fi

ENI_ID=$(aws ecs describe-tasks --cluster "$CLUSTER" --tasks "$TASK_ARN" --region "$REGION" \
  --query "tasks[0].attachments[0].details[?name=='networkInterfaceId'].value" --output text)

PUBLIC_IP=$(aws ec2 describe-network-interfaces --network-interface-ids "$ENI_ID" --region "$REGION" \
  --query 'NetworkInterfaces[0].Association.PublicIp' --output text)

echo "http://${PUBLIC_IP}:${PORT}"
