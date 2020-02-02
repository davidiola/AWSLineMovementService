package com.awslinemovement.service.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName = "GameEventsTable")
public class GameEvent {
    private String uniqueIdentifier;
    private String timestamp;
    private Team homeTeam;
    private Team awayTeam;
    private String dateOfEvent;

    @DynamoDBHashKey(attributeName = "Id")
    public String getId() { return uniqueIdentifier; }
    public void setId(String uniqueIdentifier) { this.uniqueIdentifier = uniqueIdentifier; }

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
    public String getEventDate() { return dateOfEvent; }
    public void setEventDate(String dateOfEvent) { this.dateOfEvent = dateOfEvent; }


}
