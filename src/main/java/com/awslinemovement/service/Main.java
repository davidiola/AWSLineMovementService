package com.awslinemovement.service;

import com.awslinemovement.service.constants.Constants.Sport;
import com.awslinemovement.service.scrape.SportsBookScrape;
import static com.awslinemovement.service.constants.Constants.SPORTSBOOK_BASE_URL;

import org.jsoup.nodes.Document;

public class Main {
    public static void main(String[] args) {
        SportsBookScrape sportsBookScrape = new SportsBookScrape();
        Document doc = sportsBookScrape.returnDocumentFromUrl(SPORTSBOOK_BASE_URL);
        String gameLineUrl = sportsBookScrape.retrieveGameLineUrlForSport(Sport.NBA, doc);
        if (!gameLineUrl.isEmpty()) {
            System.out.println(gameLineUrl);
        }
    }
}
