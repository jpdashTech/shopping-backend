package com.orderflow.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orderflow.api.config.SqsProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Component
public class SqsPublisher {
    private static final Logger logger = LoggerFactory.getLogger(SqsPublisher.class);

    private final SqsClient sqsClient;
    private final SqsProperties sqsProperties;
    private final ObjectMapper objectMapper;

    public SqsPublisher(SqsClient sqsClient, SqsProperties sqsProperties, ObjectMapper objectMapper) {
        this.sqsClient = sqsClient;
        this.sqsProperties = sqsProperties;
        this.objectMapper = objectMapper;
    }

    public void publish(Object message) {
        try {
            String payload = objectMapper.writeValueAsString(message);
            sqsClient.sendMessage(SendMessageRequest.builder()
                    .queueUrl(sqsProperties.getOrderCreatedQueueUrl())
                    .messageBody(payload)
                    .build());
            logger.info("Published OrderCreated event to SQS");
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to publish SQS message", ex);
        }
    }
}
