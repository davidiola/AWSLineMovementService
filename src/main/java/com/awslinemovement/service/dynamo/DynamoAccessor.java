package com.awslinemovement.service.dynamo;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.awslinemovement.service.LineMovementService;
import com.awslinemovement.service.metrics.CloudWatchAccessor;
import com.awslinemovement.service.model.dataaccess.GameEvent;
import com.google.inject.Inject;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RequiredArgsConstructor(onConstructor=@__(@Inject))
public class DynamoAccessor {
    @NonNull
    private DynamoDBMapper dynamoDBMapper;
    @NonNull
    private CloudWatchAccessor cloudWatchAccessor;

    private static final Logger log = LogManager.getLogger(LineMovementService.class);

    public void putGameEventItems(List<GameEvent> gameEvents) {
        dynamoDBMapper.batchSave(gameEvents);
        for (GameEvent gameEvent : gameEvents) {
            log.info(String.format("Put Game Event to Dynamo: %s", gameEvent));
            cloudWatchAccessor.putMetricsToCloudWatchForGameEvent(gameEvent);
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
