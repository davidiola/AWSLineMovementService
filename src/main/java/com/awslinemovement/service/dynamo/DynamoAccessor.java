package com.awslinemovement.service.dynamo;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.awslinemovement.service.model.GameEvent;
import lombok.NonNull;

public class DynamoAccessor {
    @NonNull
    private AmazonDynamoDB dynamoDBClient;
    private DynamoDBMapper dynamoDBMapper;

    public DynamoAccessor(final AmazonDynamoDB dynamoDBClient) {
       this.dynamoDBClient = dynamoDBClient;
       this.dynamoDBMapper = new DynamoDBMapper(dynamoDBClient);
    }


    public void putGameEventItem(GameEvent gameEvent) {
        dynamoDBMapper.save(gameEvent);
    }
}
