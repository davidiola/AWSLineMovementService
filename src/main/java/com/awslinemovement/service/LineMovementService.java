package com.awslinemovement.service;


import com.awslinemovement.service.constants.Constants;
import com.awslinemovement.service.dynamo.DynamoAccessor;
import com.awslinemovement.service.metrics.CloudWatchAccessor;
import com.awslinemovement.service.model.api.GameEventRequest;
import com.awslinemovement.service.model.dataaccess.GameEvent;
import com.awslinemovement.service.scrape.GameLinesRetriever;
import com.awslinemovement.service.scrape.SportsBookScrape;
import com.awslinemovement.service.transformers.GameEventToGraphTransformer;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;

import java.util.List;
import java.util.Optional;

import static com.awslinemovement.service.constants.Constants.SPORTSBOOK_BASE_URL;
import static com.awslinemovement.service.constants.Constants.SPORT_STRING_MAP;

@RequiredArgsConstructor(onConstructor=@__(@Inject))
public class LineMovementService {

    @NonNull
    private final CloudWatchAccessor cloudWatchAccessor;
    @NonNull
    private final DynamoAccessor dynamoAccessor;
    @NonNull
    private final SportsBookScrape sportsBookScrape;
    @NonNull
    private final GameEventToGraphTransformer gameEventToGraphTransformer;
    @NonNull
    private final GameLinesRetriever gameLinesRetriever;

    private static final Logger log = LogManager.getLogger(LineMovementService.class);



    public void initScrape() {
        putGameEventItems();
    }

    public String initGetAPI(GameEventRequest gameEventRequest) {
        String json = retrieveGameEventGraph(gameEventRequest);
        return json;
    }

    private void putGameEventItems() {
        List<Constants.Sport> supportedSportsList = ImmutableList.of(Constants.Sport.NBA,
                Constants.Sport.NFL,
                Constants.Sport.NHL,
                Constants.Sport.MLB);
        Document homeDoc = sportsBookScrape.returnDocumentFromUrl(SPORTSBOOK_BASE_URL);
        supportedSportsList.stream().forEach((sport) -> retrieveLinesForSport(sport, homeDoc));
    }

    private void retrieveLinesForSport(final Constants.Sport sport, final Document homeDoc) {
        sportsBookScrape.retrieveGameLineUrlForSport(sport, homeDoc).ifPresentOrElse(url -> processGameLineUrl(url, sport),
                () -> {
                    cloudWatchAccessor.transmitMetricForGameLineUrl(false, sport);
                    log.error("Error: GameLineUrl is empty.");
                    throw new RuntimeException("Error: GameLineUrl is empty.");
                });
    }

    private void processGameLineUrl(String gameLineUrl, Constants.Sport sport) {
        log.debug("Fetched GameLineUrl of {} for Sport {}", gameLineUrl, sport);
        cloudWatchAccessor.transmitMetricForGameLineUrl(true, sport);
        Document rowEventsDoc = sportsBookScrape.returnDocumentFromUrl(gameLineUrl);
        Optional<List<GameEvent>> optGameEventList = gameLinesRetriever.retrieveGameEvents(rowEventsDoc);

        if (optGameEventList.isEmpty()) {
          cloudWatchAccessor.transmitMetricForGameEventsFound(0.0, sport);
          log.debug("No lines for sport: {}", SPORT_STRING_MAP.get(sport));
          System.out.println("No lines!");
          return;
        }

        List<GameEvent> gameEventList = optGameEventList.get();

        gameEventList.stream()
                .forEach(System.out::println);

        log.info("Retrieved {} lines for sport: {}", gameEventList.size(), SPORT_STRING_MAP.get(sport));
        cloudWatchAccessor.transmitMetricForGameEventsFound(gameEventList.size(), sport);
        dynamoAccessor.putGameEventItems(gameEventList);
    }

    private String retrieveGameEventGraph(GameEventRequest gameEventRequest) {
        // transform input to gameEventIdentifier
        String gameEventIdentifier = gameEventToGraphTransformer.transformRequestToIdentifier(gameEventRequest);
        System.out.println(gameEventIdentifier);
        List<GameEvent> events = dynamoAccessor.retrieveGameEventsForIdentifier(gameEventIdentifier);
        if (events.isEmpty()) {
            log.error("No GameEvents found for identifier: {}", gameEventIdentifier);
            return "none";
        } else {
            return gameEventToGraphTransformer.returnGameEventInGraphJSON(events);
        }
    }
}
