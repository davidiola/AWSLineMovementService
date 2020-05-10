package com.awslinemovement.service;

import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.PutMetricDataRequest;
import com.amazonaws.services.cloudwatch.model.StandardUnit;
import com.awslinemovement.service.constants.Constants;
import com.awslinemovement.service.dynamo.DynamoAccessor;
import com.awslinemovement.service.model.dataaccess.GameEvent;
import com.awslinemovement.service.scrape.SportsBookScrape;
import com.awslinemovement.service.transformers.GameEventToGraphTransformer;
import com.google.inject.Inject;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;

import java.util.List;

import static com.awslinemovement.service.constants.Constants.SPORTSBOOK_BASE_URL;
import static com.awslinemovement.service.constants.Constants.SPORT_STRING_MAP;

@RequiredArgsConstructor(onConstructor=@__(@Inject))
public class LineMovementService {

    @NonNull
    private final AmazonCloudWatch cloudWatchClient;
    @NonNull
    private final DynamoAccessor dynamoAccessor;
    @NonNull
    private final SportsBookScrape sportsBookScrape;
    @NonNull
    private final GameEventToGraphTransformer gameEventToGraphTransformer;

    private static final Logger log = LogManager.getLogger(LineMovementService.class);

    private void transmitCloudWatchMetricForGameLineUrl(boolean success, Constants.Sport sport) {
        Dimension dimension = new Dimension().withName("SPORT").withValue(SPORT_STRING_MAP.get(sport));
        MetricDatum gameLinesUrlMetricDatum = new MetricDatum().withMetricName("GameLineUrlRetrievedCount").withUnit(StandardUnit.Count).withDimensions(dimension);
        PutMetricDataRequest putMetricDataRequest = new PutMetricDataRequest().withMetricData(gameLinesUrlMetricDatum).withNamespace("LineMovementServiceMetrics");
        if (success) {
            gameLinesUrlMetricDatum.setValue(1.0);
        } else {
            gameLinesUrlMetricDatum.setValue(0.0);
        }
        cloudWatchClient.putMetricData(putMetricDataRequest);
    }

    private void transmitCloudWatchMetricForGameEventsFound(double gameEventListSize, Constants.Sport sport) {
        Dimension noNewGameEventsForSportDimension = new Dimension().withName("SPORT").withValue(SPORT_STRING_MAP.get(sport));
        MetricDatum noNewGameEventsMetricDatum = new MetricDatum().withMetricName("NoGameLinesFoundForSport").withUnit(StandardUnit.Count).withDimensions(noNewGameEventsForSportDimension);
        PutMetricDataRequest noNewGameEventsMetricRequest = new PutMetricDataRequest().withMetricData(noNewGameEventsMetricDatum).withNamespace("LineMovementServiceMetrics");
        noNewGameEventsMetricDatum.setValue(gameEventListSize);
        cloudWatchClient.putMetricData(noNewGameEventsMetricRequest);
    }

    public void init() {
        Constants.Sport sport = Constants.Sport.NBA;
        Document homeDoc = sportsBookScrape.returnDocumentFromUrl(SPORTSBOOK_BASE_URL);
        String gameLineUrl = sportsBookScrape.retrieveGameLineUrlForSport(sport, homeDoc);

        /*
        if (!gameLineUrl.isEmpty()) {
            transmitCloudWatchMetricForGameLineUrl(true, sport);
            Document rowEventsDoc = sportsBookScrape.returnDocumentFromUrl(gameLineUrl);
            GameLinesRetriever gameLinesRetriever = new GameLinesRetriever();
            List<GameEvent> gameEventList = gameLinesRetriever.retrieveGameEvents(rowEventsDoc);
            List<GameEvent> filteredGameEventList = gameLinesRetriever.filterGameEventsWithNullFields(gameEventList);

            filteredGameEventList.stream().forEach(System.out::println);

            if (filteredGameEventList.size() == 0) {
                transmitCloudWatchMetricForGameEventsFound(0.0, sport);
                log.debug(String.format("No lines for sport: %s", SPORT_STRING_MAP.get(sport)));
                System.out.println("No lines!");
                return;
            }

            log.info(String.format("Retrieved %d lines for sport: %s", filteredGameEventList.size(), SPORT_STRING_MAP.get(sport)));
            transmitCloudWatchMetricForGameEventsFound(filteredGameEventList.size(), sport);
            dynamoAccessor.putGameEventItems(filteredGameEventList);
        } else {
            transmitCloudWatchMetricForGameLineUrl(false, sport);
            System.out.println("Raise exception!");
        }
         */
        List<GameEvent> events = dynamoAccessor.retrieveGameEventsForIdentifier("Los-Angeles-LakersvNew-Orleans-Pelicans03-01-2020");
        gameEventToGraphTransformer.writeGameEventToGraphDataFile(events);
    }
}
