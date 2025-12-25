package com.orderflow.worker.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orderflow.worker.config.SqsProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.util.List;

@Component
public class SqsOrderConsumer {
    private static final Logger logger = LoggerFactory.getLogger(SqsOrderConsumer.class);
    private static final int MAX_RETRIES = 3;

    private final SqsClient sqsClient;
    private final SqsProperties sqsProperties;
    private final ObjectMapper objectMapper;
    private final OrderProcessingService processingService;

    public SqsOrderConsumer(SqsClient sqsClient,
                            SqsProperties sqsProperties,
                            ObjectMapper objectMapper,
                            OrderProcessingService processingService) {
        this.sqsClient = sqsClient;
        this.sqsProperties = sqsProperties;
        this.objectMapper = objectMapper;
        this.processingService = processingService;
    }

    @Scheduled(fixedDelayString = "${orderflow.worker.poll-delay-ms:5000}")
    public void poll() {
        ReceiveMessageRequest request = ReceiveMessageRequest.builder()
                .queueUrl(sqsProperties.getOrderCreatedQueueUrl())
                .maxNumberOfMessages(5)
                .waitTimeSeconds(10)
                .attributeNamesWithStrings("ApproximateReceiveCount")
                .build();
        List<Message> messages = sqsClient.receiveMessage(request).messages();
        for (Message message : messages) {
            handleMessage(message);
        }
    }

    private void handleMessage(Message message) {
        int receiveCount = Integer.parseInt(message.attributes().getOrDefault("ApproximateReceiveCount", "1"));
        if (receiveCount > 1) {
            backoff(receiveCount);
        }

        try {
            OrderEventEnvelope envelope = objectMapper.readValue(message.body(), OrderEventEnvelope.class);
            MDC.put("correlationId", envelope.correlationId());

            if (!"OrderCreated".equals(envelope.eventType())) {
                deleteMessage(message);
                return;
            }

            processingService.process(envelope);
            deleteMessage(message);
        } catch (Exception ex) {
            logger.error("Failed to process message {}", message.messageId(), ex);
            if (receiveCount >= MAX_RETRIES) {
                sendToDlq(message);
                deleteMessage(message);
            }
        } finally {
            MDC.remove("correlationId");
        }
    }

    private void sendToDlq(Message message) {
        sqsClient.sendMessage(SendMessageRequest.builder()
                .queueUrl(sqsProperties.getOrderCreatedDlqUrl())
                .messageBody(message.body())
                .build());
        logger.error("Message {} sent to DLQ", message.messageId());
    }

    private void deleteMessage(Message message) {
        sqsClient.deleteMessage(DeleteMessageRequest.builder()
                .queueUrl(sqsProperties.getOrderCreatedQueueUrl())
                .receiptHandle(message.receiptHandle())
                .build());
    }

    private void backoff(int receiveCount) {
        long delayMillis = (long) Math.min(8000, Math.pow(2, receiveCount - 1) * 1000);
        try {
            Thread.sleep(delayMillis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}
