package com.awslinemovement.service;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.awslinemovement.service.constants.Constants.Sport;
import com.awslinemovement.service.dynamo.DynamoAccessor;
import com.awslinemovement.service.model.GameEvent;
import com.awslinemovement.service.scrape.GameLinesRetriever;
import com.awslinemovement.service.scrape.SportsBookScrape;
import static com.awslinemovement.service.constants.Constants.SPORTSBOOK_BASE_URL;

import org.jsoup.nodes.Document;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        SportsBookScrape sportsBookScrape = new SportsBookScrape();
        Document homeDoc = sportsBookScrape.returnDocumentFromUrl(SPORTSBOOK_BASE_URL);
        String gameLineUrl = sportsBookScrape.retrieveGameLineUrlForSport(Sport.NBA, homeDoc);
        if (!gameLineUrl.isEmpty()) {
            Document rowEventsDoc = sportsBookScrape.returnDocumentFromUrl(gameLineUrl);
            GameLinesRetriever gameLinesRetriever = new GameLinesRetriever();
            List<GameEvent> gameEventList = gameLinesRetriever.retrieveGameEvents(rowEventsDoc);
            if (gameEventList.size() == 0) {
                System.out.println("No lines!");
            }
            AWSCredentialsProvider awsCredentialsProvider = new DefaultAWSCredentialsProviderChain();
            AmazonDynamoDB dynamoDBClient = AmazonDynamoDBClientBuilder.standard().withRegion("us-east-1").withCredentials(awsCredentialsProvider).build();
            DynamoAccessor dynamoAccessor = new DynamoAccessor(dynamoDBClient);
            dynamoAccessor.putGameEventItem(gameEventList.get(0));
        } else {
            System.out.println("Raise exception!");
        }
    }
}
