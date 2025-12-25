package com.orderflow.worker.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.net.URI;

@Configuration
@EnableConfigurationProperties({AwsProperties.class, SqsProperties.class})
public class SqsConfig {

    @Bean
    public SqsClient sqsClient(AwsProperties awsProperties) {
        SqsClient.Builder builder = SqsClient.builder()
                .region(Region.of(awsProperties.getRegion()));

        if (awsProperties.getEndpoint() != null && !awsProperties.getEndpoint().isBlank()) {
            builder.endpointOverride(URI.create(awsProperties.getEndpoint()));
        }

        if (awsProperties.getAccessKey() != null && !awsProperties.getAccessKey().isBlank()) {
            builder.credentialsProvider(
                    StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(awsProperties.getAccessKey(), awsProperties.getSecretKey())));
        } else {
            builder.credentialsProvider(DefaultCredentialsProvider.create());
        }

        return builder.build();
    }
}
