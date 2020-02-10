package com.awslinemovement.service.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import lombok.Data;

@DynamoDBTable(tableName = "AWSLineMovementInfrastructure-PROD-GameEventsTable")
@Data
public class GameEvent {
    private String gameEventIdentifier;
    private String timestamp;
    private Team homeTeam;
    private Team awayTeam;
    private String eventDate;

    @DynamoDBHashKey(attributeName = "GameEventIdentifier")
    public String getGameEventIdentifier() { return gameEventIdentifier; }
    public void setGameEventIdentifier(String gameEventIdentifier) { this.gameEventIdentifier = gameEventIdentifier; }

    @DynamoDBRangeKey(attributeName = "Timestamp")
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    @DynamoDBAttribute(attributeName = "HomeTeam")
    public Team getHomeTeam() { return homeTeam; }
    public void setHomeTeam(Team homeTeam) { this.homeTeam = homeTeam; }

    @DynamoDBAttribute(attributeName = "AwayTeam")
    public Team getAwayTeam() { return awayTeam; }
    public void setAwayTeam(Team awayTeam) { this.awayTeam = awayTeam; }

    @DynamoDBAttribute(attributeName = "EventDate")
    public String getEventDate() { return eventDate; }
    public void setEventDate(String eventDate) { this.eventDate = eventDate; }


}
