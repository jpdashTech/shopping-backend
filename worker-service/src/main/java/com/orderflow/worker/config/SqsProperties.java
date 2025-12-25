package com.orderflow.worker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "orderflow.sqs")
public class SqsProperties {
    private String orderCreatedQueueUrl;
    private String orderCreatedDlqUrl;

    public String getOrderCreatedQueueUrl() {
        return orderCreatedQueueUrl;
    }

    public void setOrderCreatedQueueUrl(String orderCreatedQueueUrl) {
        this.orderCreatedQueueUrl = orderCreatedQueueUrl;
    }

    public String getOrderCreatedDlqUrl() {
        return orderCreatedDlqUrl;
    }

    public void setOrderCreatedDlqUrl(String orderCreatedDlqUrl) {
        this.orderCreatedDlqUrl = orderCreatedDlqUrl;
    }
}
