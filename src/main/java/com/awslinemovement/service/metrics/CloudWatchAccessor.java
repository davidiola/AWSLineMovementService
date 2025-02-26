package com.awslinemovement.service.metrics;

import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.PutMetricDataRequest;
import com.amazonaws.services.cloudwatch.model.StandardUnit;
import com.awslinemovement.service.constants.Constants;
import com.awslinemovement.service.model.dataaccess.GameEvent;
import com.google.inject.Inject;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.awslinemovement.service.constants.Constants.SPORT_STRING_MAP;

@RequiredArgsConstructor(onConstructor=@__(@Inject))
public class CloudWatchAccessor {

    @NonNull
    private final AmazonCloudWatch cloudWatchClient;

    public void transmitMetricForGameLineUrl(boolean success, Constants.Sport sport) {
        Dimension dimension = new Dimension().withName("SPORT").withValue(SPORT_STRING_MAP.get(sport));
        MetricDatum gameLinesUrlMetricDatum = new MetricDatum().withMetricName("GameLineUrlRetrievedSuccessfully").withUnit(StandardUnit.Count).withDimensions(dimension);
        PutMetricDataRequest putMetricDataRequest = new PutMetricDataRequest().withMetricData(gameLinesUrlMetricDatum).withNamespace("LineMovementServiceMetrics");
        if (success) {
            gameLinesUrlMetricDatum.setValue(1.0);
        } else {
            gameLinesUrlMetricDatum.setValue(0.0);
        }
        cloudWatchClient.putMetricData(putMetricDataRequest);
    }

    public void transmitMetricForGameEventsFound(double gameEventListSize, Constants.Sport sport) {
        Dimension gameEventsForSportDimension = new Dimension().withName("SPORT").withValue(SPORT_STRING_MAP.get(sport));
        MetricDatum gameEventsMetricDatum = new MetricDatum().withMetricName("GameLinesFoundForSport").withUnit(StandardUnit.Count).withDimensions(gameEventsForSportDimension);
        PutMetricDataRequest gameEventsMetricRequest = new PutMetricDataRequest().withMetricData(gameEventsMetricDatum).withNamespace("LineMovementServiceMetrics");
        gameEventsMetricDatum.setValue(gameEventListSize);
        cloudWatchClient.putMetricData(gameEventsMetricRequest);
    }

    public void putMetricsToCloudWatchForGameEvent(GameEvent gameEvent) {
        Dimension homeTeamNameDimension = new Dimension().withName("HomeTeamName").withValue(gameEvent.getHomeTeam().getName());
        Dimension awayTeamNameDimension = new Dimension().withName("AwayTeamName").withValue(gameEvent.getAwayTeam().getName());
        Dimension dateOfEventDimension = new Dimension().withName("DateOfEvent").withValue(gameEvent.getEventDate());
        List<Dimension> dynamoGameEventDimensions = Stream.of(homeTeamNameDimension, awayTeamNameDimension, dateOfEventDimension).collect(Collectors.toList());

        MetricDatum dynamoGameEventMetricDatum = new MetricDatum().withMetricName("DynamoGameEventPut").withUnit(StandardUnit.Count).withDimensions(dynamoGameEventDimensions);
        dynamoGameEventMetricDatum.setValue(1.0);
        PutMetricDataRequest dynamoGameEventMetricsRequest = new PutMetricDataRequest().withMetricData(dynamoGameEventMetricDatum).withNamespace("LineMovementServiceMetrics");
        cloudWatchClient.putMetricData(dynamoGameEventMetricsRequest);
    }
}
