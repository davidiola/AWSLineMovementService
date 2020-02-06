package com.awslinemovement.service.dynamo;

import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.PutMetricDataRequest;
import com.amazonaws.services.cloudwatch.model.StandardUnit;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.awslinemovement.service.model.GameEvent;
import lombok.NonNull;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class DynamoAccessor {
    @NonNull
    private AmazonDynamoDB dynamoDBClient;
    private DynamoDBMapper dynamoDBMapper;
    private AmazonCloudWatch cloudWatchClient;

    public DynamoAccessor(final AmazonDynamoDB dynamoDBClient, final AmazonCloudWatch cwClient) {
       this.dynamoDBClient = dynamoDBClient;
       this.cloudWatchClient = cwClient;
       this.dynamoDBMapper = new DynamoDBMapper(dynamoDBClient);
    }

    private void putMetricsToCloudWatchForGameEvent(GameEvent gameEvent) {
        Dimension homeTeamNameDimension = new Dimension().withName("HomeTeamName").withValue(gameEvent.getHomeTeam().getName());
        Dimension awayTeamNameDimension = new Dimension().withName("AwayTeamName").withValue(gameEvent.getAwayTeam().getName());
        Dimension dateOfEventDimension = new Dimension().withName("DateOfEvent").withValue(gameEvent.getEventDate());
        List<Dimension> dynamoGameEventDimensions = Stream.of(homeTeamNameDimension, awayTeamNameDimension, dateOfEventDimension).collect(Collectors.toList());

        MetricDatum dynamoGameEventMetricDatum = new MetricDatum().withMetricName("DynamoGameEventPut").withUnit(StandardUnit.Count).withDimensions(dynamoGameEventDimensions);
        dynamoGameEventMetricDatum.setValue(1.0);
        PutMetricDataRequest dynamoGameEventMetricsRequest = new PutMetricDataRequest().withMetricData(dynamoGameEventMetricDatum).withNamespace("LineMovementServiceMetrics");
        cloudWatchClient.putMetricData(dynamoGameEventMetricsRequest);
    }

    public void putGameEventItems(List<GameEvent> gameEvents) {
        dynamoDBMapper.batchSave(gameEvents);
        for (GameEvent gameEvent : gameEvents) {
            putMetricsToCloudWatchForGameEvent(gameEvent);
        }
    }
}
