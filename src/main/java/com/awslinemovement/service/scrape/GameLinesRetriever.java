package com.awslinemovement.service.scrape;

import com.awslinemovement.service.model.GameEvent;
import com.awslinemovement.service.model.LineTuple;
import com.awslinemovement.service.model.Team;
import lombok.NoArgsConstructor;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
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

    private List<String> parseLineParts(String spreadElem) {
        List<String> spreadPartList = new ArrayList<>();
        String spreadLine = spreadElem.substring(0, spreadElem.indexOf("(")).replaceAll("\\s", "");
        String spreadOdds = spreadElem.substring(spreadElem.indexOf("(") + 1, spreadElem.length() - 1);
        if (spreadOdds.contains("EV")) {
            spreadOdds = "-100";
        }
        spreadPartList.add(spreadLine);
        spreadPartList.add(spreadOdds);
        return spreadPartList;
    }

    private GameEvent transformGameLineToModel(Element gameLineRowEvent) {

        GameEvent gameEvent = new GameEvent();

        Team homeTeam = new Team();
        LineTuple homeTeamSpread = new LineTuple();
        LineTuple homeTeamTotal = new LineTuple();

        Team awayTeam = new Team();
        LineTuple awayTeamSpread = new LineTuple();
        LineTuple awayTeamTotal = new LineTuple();

        long unixTime = System.currentTimeMillis() / 1000L;
        String timestamp = Long.toString(unixTime);
        gameEvent.setTimestamp(timestamp);

        String dateOfEvent = getDateOfEvent(gameLineRowEvent);
        if (dateOfEvent.isEmpty()) {
            System.out.println("Could not determine date of event.");
            return null;
        } else {
            gameEvent.setEventDate(dateOfEvent);
        }

        String awayTeamName = gameLineRowEvent.select("#firstTeamName").text();
        String homeTeamName = gameLineRowEvent.select("#secondTeamName").text();
        homeTeam.setName(homeTeamName);
        awayTeam.setName(awayTeamName);

        String uniqueIdentifier = getUniqueIdentifierForGameEvent(awayTeamName, homeTeamName, dateOfEvent);
        gameEvent.setGameEventIdentifier(uniqueIdentifier);

        // ML class is actually total, total actually ML
        Elements moneyMarketElems = gameLineRowEvent.select(".column.total").select(".market");
        String homeTeamMLOdds = retrieveLineInfo(moneyMarketElems, HOME_TEAM_MARKET_IDX);
        String awayTeamMLOdds = retrieveLineInfo(moneyMarketElems, AWAY_TEAM_MARKET_IDX);
        homeTeam.setML(homeTeamMLOdds);
        awayTeam.setML(awayTeamMLOdds);

        Elements spreadMarketElems = gameLineRowEvent.select(".column.spread").select(".market");
        String homeTeamSpreadElem = retrieveLineInfo(spreadMarketElems, HOME_TEAM_MARKET_IDX);
        String awayTeamSpreadElem = retrieveLineInfo(spreadMarketElems, AWAY_TEAM_MARKET_IDX);

        String homeTeamSpreadLine = parseLineParts(homeTeamSpreadElem).get(0);
        String homeTeamSpreadOdds = parseLineParts(homeTeamSpreadElem).get(1);

        homeTeamSpread.setLineAmount(homeTeamSpreadLine);
        homeTeamSpread.setLineOdds(homeTeamSpreadOdds);

        String awayTeamSpreadLine = parseLineParts(awayTeamSpreadElem).get(0);
        String awayTeamSpreadOdds = parseLineParts(awayTeamSpreadElem).get(1);

        awayTeamSpread.setLineAmount(awayTeamSpreadLine);
        awayTeamSpread.setLineOdds(awayTeamSpreadOdds);

        Elements totalMarketElems = gameLineRowEvent.select(".column.money").select(".market");
        String homeTeamTotalElem = retrieveLineInfo(totalMarketElems, HOME_TEAM_MARKET_IDX);
        String awayTeamTotalElem = retrieveLineInfo(totalMarketElems, AWAY_TEAM_MARKET_IDX);

        String homeTeamTotalLine = parseLineParts(homeTeamTotalElem).get(0);
        String homeTeamTotalLineWithoutDirection = homeTeamTotalLine.substring(1);
        String homeTeamTotalOdds = parseLineParts(homeTeamTotalElem).get(1);

        homeTeamTotal.setLineAmount(homeTeamTotalLineWithoutDirection);
        homeTeamTotal.setLineOdds(homeTeamTotalOdds);

        String awayTeamTotalLine = parseLineParts(awayTeamTotalElem).get(0);
        String awayTeamTotalLineWithoutDirection = awayTeamTotalLine.substring(1);
        String awayTeamTotalOdds = parseLineParts(awayTeamTotalElem).get(1);

        awayTeamTotal.setLineAmount(awayTeamTotalLineWithoutDirection);
        awayTeamTotal.setLineOdds(awayTeamTotalOdds);

        homeTeam.setSpread(homeTeamSpread);
        homeTeam.setTotal(homeTeamTotal);
        awayTeam.setSpread(awayTeamSpread);
        awayTeam.setTotal(awayTeamTotal);

        gameEvent.setHomeTeam(homeTeam);
        gameEvent.setAwayTeam(awayTeam);
        return gameEvent;
    }

    public List<GameEvent> retrieveGameEvents(Document doc) {
        List<GameEvent> gameEventList = new ArrayList<>();
        Elements rowEvents = doc.select(".row.event");
        if (rowEvents.size() <= 1) {
            System.out.println("No lines currently");
            return gameEventList;
        }

        // SKIP Overarching blank row event
        Elements gameLineRowEvents = rowEvents.stream().skip(1).collect(Collectors.toCollection(Elements::new));

        for (Element gameLineRowEvent : gameLineRowEvents) {
            GameEvent gameEvent = transformGameLineToModel(gameLineRowEvent);
            if (gameEvent == null) {
                System.out.println("Houston, we have a problem");
            } else {
                System.out.println(gameEvent);
                gameEventList.add(gameEvent);
            }
        }
        return gameEventList;
    }
}
