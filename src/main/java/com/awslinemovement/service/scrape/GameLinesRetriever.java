package com.awslinemovement.service.scrape;

import lombok.NoArgsConstructor;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Date;
import java.util.stream.Collectors;

@NoArgsConstructor
public class GameLinesRetriever {

    private static final int HOME_TEAM_MARKET_IDX = 0;
    private static final int AWAY_TEAM_MARKET_IDX = 1;
    private static final String DASH = "-";
    private static final String YEAR_PREFIX_STRING = DASH + "20";
    private static final int LENGTH_OF_DATE_STR = 6;
    private static final String UPDATE_TICKET_SELECTOR = "a[href*=\"./updateTicket.sbk\"]";
    private static final String VERSUS_STR = "v";

    private String retrieveLineInfo(Elements marketElems, int teamIndex) {
        String lineText = marketElems.get(teamIndex).text();
        if (lineText.equals(DASH)) {
            return "";
        }
        return lineText;
    }

    private String getDateOfEvent(Element gameLineRowEvent) {
        Element updateTicketElement = gameLineRowEvent.select(UPDATE_TICKET_SELECTOR).first();
        if (updateTicketElement != null) {
            String id = updateTicketElement.id();
            String bracketedString = id.substring(id.indexOf("["));
            String[] bracketedStringParts = bracketedString.split(DASH);
            String dateStringPartWithExcessCharacters = bracketedStringParts[3];
            String dateStringPart = dateStringPartWithExcessCharacters.substring(0,LENGTH_OF_DATE_STR);
            return dateStringPart.substring(0, 2) + DASH + dateStringPart.substring(2, 4) + YEAR_PREFIX_STRING + dateStringPart.substring(4,LENGTH_OF_DATE_STR);
        }
        return "";
    }

    private String getUniqueIdentifierForGameEvent(String awayTeamName, String homeTeamName, String dateOfEvent) {
        return (awayTeamName + VERSUS_STR + homeTeamName + dateOfEvent).replaceAll("\\s", DASH);
    }

    private void transformGameLineToModel(Element gameLineRowEvent) {

        long unixTime = System.currentTimeMillis() / 1000L;
        String timestamp = Long.toString(unixTime);
        System.out.println(timestamp);

        String dateOfEvent = getDateOfEvent(gameLineRowEvent);
        if (dateOfEvent.isEmpty()) {
            System.out.println("Could not determine date of event.");
            return;
        }
        System.out.println(dateOfEvent);

        String awayTeamName = gameLineRowEvent.select("#firstTeamName").text();
        String homeTeamName = gameLineRowEvent.select("#secondTeamName").text();
        System.out.println(homeTeamName);
        System.out.println(awayTeamName);

        String uniqueIdentifier = getUniqueIdentifierForGameEvent(awayTeamName, homeTeamName, dateOfEvent);
        System.out.println(uniqueIdentifier);

        // ML class is actually total, total actually ML
        Elements moneyMarketElems = gameLineRowEvent.select(".column.total").select(".market");
        String homeTeamMLOdds = retrieveLineInfo(moneyMarketElems, HOME_TEAM_MARKET_IDX);
        String awayTeamMLOdds = retrieveLineInfo(moneyMarketElems, AWAY_TEAM_MARKET_IDX);
        System.out.println(homeTeamMLOdds);
        System.out.println(awayTeamMLOdds);

        Elements spreadMarketElems = gameLineRowEvent.select(".column.spread").select(".market");
        String homeTeamSpread = retrieveLineInfo(spreadMarketElems, HOME_TEAM_MARKET_IDX);
        String awayTeamSpread = retrieveLineInfo(spreadMarketElems, AWAY_TEAM_MARKET_IDX);

        System.out.println(homeTeamSpread);
        System.out.println(awayTeamSpread);


        Elements totalMarketElems = gameLineRowEvent.select(".column.money").select(".market");
        String homeTeamTotal = retrieveLineInfo(totalMarketElems, HOME_TEAM_MARKET_IDX);
        String awayTeamTotal = retrieveLineInfo(totalMarketElems, AWAY_TEAM_MARKET_IDX);

        System.out.println(homeTeamTotal);
        System.out.println(awayTeamTotal);
    }

    public void retrieveRowEventInformation(Document doc) {
        Elements rowEvents = doc.select(".row.event");
        if (rowEvents.size() <= 1) {
            System.out.println("No lines currently");
            return;
        }

        // SKIP Overarching blank row event
        Elements gameLineRowEvents = rowEvents.stream().skip(1).collect(Collectors.toCollection(Elements::new));

        for (Element gameLineRowEvent : gameLineRowEvents) {
            transformGameLineToModel(gameLineRowEvent);
        }
    }
}
