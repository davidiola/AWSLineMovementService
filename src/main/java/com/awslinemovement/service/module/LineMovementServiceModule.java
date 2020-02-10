package com.awslinemovement.service.module;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.awslinemovement.service.LineMovementService;
import com.awslinemovement.service.dynamo.DynamoAccessor;
import com.awslinemovement.service.scrape.SportsBookScrape;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

import static com.awslinemovement.service.constants.Constants.PROD_AWS_REGION;

public class LineMovementServiceModule extends AbstractModule {
    @Override
    protected void configure() {
    }

    @Provides
    @Singleton
    AWSCredentialsProvider getAWSCredentials() {
        return new DefaultAWSCredentialsProviderChain();
    }

    @Provides
    @Singleton
    AmazonDynamoDB getDynamoDBClient(final AWSCredentialsProvider awsCredentialsProvider) {
        return AmazonDynamoDBClientBuilder.standard().withRegion(PROD_AWS_REGION)
                .withCredentials(awsCredentialsProvider).build();
    }

    @Provides
    @Singleton
    AmazonCloudWatch getCloudWatchClient(final AWSCredentialsProvider awsCredentialsProvider) {
        return AmazonCloudWatchClientBuilder.standard().withRegion(PROD_AWS_REGION)
                .withCredentials(awsCredentialsProvider).build();
    }

    @Provides
    @Singleton
    DynamoAccessor getDynamoAccessor(final AmazonDynamoDB dynamoDBClient, final AmazonCloudWatch cwClient) {
        return new DynamoAccessor(dynamoDBClient, cwClient);
    }

    @Provides
    @Singleton
    SportsBookScrape getSportsbookScrape() {
        return new SportsBookScrape();
    }

    @Provides
    @Singleton
    LineMovementService getLineMovementService(final DynamoAccessor dynamoAccessor, final AmazonCloudWatch cwClient, final SportsBookScrape sportsBookScrape) {
        return new LineMovementService(cwClient, dynamoAccessor, sportsBookScrape);
    }



}
