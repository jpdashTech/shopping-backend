#!/usr/bin/env bash
set -e

awslocal sqs create-queue --queue-name order-created-dlq
DLQ_URL=$(awslocal sqs get-queue-url --queue-name order-created-dlq --query 'QueueUrl' --output text)
DLQ_ARN=$(awslocal sqs get-queue-attributes --queue-url "$DLQ_URL" --attribute-names QueueArn --query 'Attributes.QueueArn' --output text)

REDRIVE_POLICY=$(cat <<POLICY
{"deadLetterTargetArn":"$DLQ_ARN","maxReceiveCount":"3"}
POLICY
)

awslocal sqs create-queue --queue-name order-created --attributes RedrivePolicy="$REDRIVE_POLICY"
