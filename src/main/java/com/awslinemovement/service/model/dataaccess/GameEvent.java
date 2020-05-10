package com.awslinemovement.service.model.dataaccess;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import lombok.Data;

@DynamoDBTable(tableName = "AWSLineMovementInfrastructure-PROD-GameEventsTable")
@Data
public class GameEvent implements Comparable<GameEvent> {
    @DynamoDBHashKey(attributeName = "GameEventIdentifier")
    private String gameEventIdentifier;
    @DynamoDBRangeKey(attributeName = "Timestamp")
    private String timestamp;
    @DynamoDBAttribute(attributeName = "HomeTeam")
    private Team homeTeam;
    @DynamoDBAttribute(attributeName = "AwayTeam")
    private Team awayTeam;
    @DynamoDBAttribute(attributeName = "EventDate")
    private String eventDate;

    @Override
    public int compareTo(GameEvent e) {
        if (this.timestamp.isEmpty() || e.timestamp.isEmpty()) {
            return 0;
        }
        return this.timestamp.compareTo(e.timestamp);
    }

}
