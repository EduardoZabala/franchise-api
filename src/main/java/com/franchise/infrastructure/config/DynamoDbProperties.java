package com.franchise.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * DynamoDB connection settings.
 *
 * @param region    AWS region
 * @param endpoint  optional endpoint override (LocalStack / DynamoDB Local); blank means real AWS
 * @param tableName single table that stores franchises, branches and products
 */
@ConfigurationProperties(prefix = "aws.dynamodb")
public record DynamoDbProperties(String region, String endpoint, String tableName) {
}
