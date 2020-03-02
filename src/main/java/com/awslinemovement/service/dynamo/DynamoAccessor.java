package com.awslinemovement.service.dynamo;

import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.PutMetricDataRequest;
import com.amazonaws.services.cloudwatch.model.StandardUnit;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.awslinemovement.service.LineMovementService;
import com.awslinemovement.service.model.GameEvent;
import com.google.common.collect.ImmutableList;
import lombok.NonNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class DynamoAccessor {
    @NonNull
    private DynamoDBMapper dynamoDBMapper;
    private AmazonCloudWatch cloudWatchClient;

    private static final Logger log = LogManager.getLogger(LineMovementService.class);

    public DynamoAccessor(final AmazonDynamoDB dynamoDBClient, final AmazonCloudWatch cwClient) {
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
            log.info(String.format("Put Game Event to Dynamo: %s", gameEvent));
            putMetricsToCloudWatchForGameEvent(gameEvent);
        }
    }

    public List<GameEvent> retrieveGameEventsForIdentifier(String gameEventIdentifier) {
        Map<String, AttributeValue> eav = new HashMap<String, AttributeValue>();
        eav.put(":val1", new AttributeValue().withS(gameEventIdentifier));

        DynamoDBQueryExpression<GameEvent> queryExpression = new DynamoDBQueryExpression<GameEvent>()
                .withKeyConditionExpression("GameEventIdentifier = :val1").withExpressionAttributeValues(eav);

        List<GameEvent> gameEvents = dynamoDBMapper.query(GameEvent.class, queryExpression);
        return gameEvents;
    }
}
