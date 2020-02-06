package com.awslinemovement.service;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClientBuilder;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.PutMetricDataRequest;
import com.amazonaws.services.cloudwatch.model.StandardUnit;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.awslinemovement.service.constants.Constants.Sport;
import com.awslinemovement.service.dynamo.DynamoAccessor;
import com.awslinemovement.service.model.GameEvent;
import com.awslinemovement.service.scrape.GameLinesRetriever;
import com.awslinemovement.service.scrape.SportsBookScrape;

import org.jsoup.nodes.Document;

import java.util.List;

import static com.awslinemovement.service.constants.Constants.*;

public class Main {
    public static void main(String[] args) {
        AWSCredentialsProvider awsCredentialsProvider = new DefaultAWSCredentialsProviderChain();
        AmazonDynamoDB dynamoDBClient = AmazonDynamoDBClientBuilder.standard().withRegion(PROD_AWS_REGION).withCredentials(awsCredentialsProvider).build();
        AmazonCloudWatch cloudWatchClient = AmazonCloudWatchClientBuilder.standard().withRegion(PROD_AWS_REGION).withCredentials(awsCredentialsProvider).build();

        SportsBookScrape sportsBookScrape = new SportsBookScrape();
        Document homeDoc = sportsBookScrape.returnDocumentFromUrl(SPORTSBOOK_BASE_URL);
        String gameLineUrl = sportsBookScrape.retrieveGameLineUrlForSport(Sport.NBA, homeDoc);

        Dimension dimension = new Dimension().withName("SPORT").withValue(SPORT_STRING_MAP.get(Sport.NBA));
        MetricDatum gameLinesUrlMetricDatum = new MetricDatum().withMetricName("GameLineUrlRetrievedCount").withUnit(StandardUnit.Count).withDimensions(dimension);
        PutMetricDataRequest putMetricDataRequest = new PutMetricDataRequest().withMetricData(gameLinesUrlMetricDatum).withNamespace("LineMovementServiceMetrics");

        if (!gameLineUrl.isEmpty()) {
            gameLinesUrlMetricDatum.setValue(1.0);
            cloudWatchClient.putMetricData(putMetricDataRequest);
            Document rowEventsDoc = sportsBookScrape.returnDocumentFromUrl(gameLineUrl);
            GameLinesRetriever gameLinesRetriever = new GameLinesRetriever();
            List<GameEvent> gameEventList = gameLinesRetriever.retrieveGameEvents(rowEventsDoc);
            Dimension noNewGameEventsForSportDimension = new Dimension().withName("SPORT").withValue(SPORT_STRING_MAP.get(Sport.NBA));
            MetricDatum noNewGameEventsMetricDatum = new MetricDatum().withMetricName("NoGameLinesFoundForSport").withUnit(StandardUnit.Count).withDimensions(noNewGameEventsForSportDimension);
            PutMetricDataRequest noNewGameEventsMetricRequest = new PutMetricDataRequest().withMetricData(noNewGameEventsMetricDatum).withNamespace("LineMovementServiceMetrics");
            if (gameEventList.size() == 0) {
                noNewGameEventsMetricDatum.setValue(1.0);
                cloudWatchClient.putMetricData(noNewGameEventsMetricRequest);
                System.out.println("No lines!");
                return;
            }
            noNewGameEventsMetricDatum.setValue(0.0);
            cloudWatchClient.putMetricData(noNewGameEventsMetricRequest);
            DynamoAccessor dynamoAccessor = new DynamoAccessor(dynamoDBClient, cloudWatchClient);
            dynamoAccessor.putGameEventItems(gameEventList);
        } else {
            gameLinesUrlMetricDatum.setValue(0.0);
            cloudWatchClient.putMetricData(putMetricDataRequest);
            System.out.println("Raise exception!");
        }
    }
}
