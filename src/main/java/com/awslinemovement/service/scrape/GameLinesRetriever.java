package com.awslinemovement.service.scrape;

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

    private List<String> retrieveAllDates(Document doc) {
        List<String> dateList = new ArrayList<>();
        Elements dateElements = doc.select(".date");
        if (dateElements != null) {
            for (Element dateElem : dateElements) {
                dateList.add(dateElem.text());
                System.out.println(dateElem.text());
            }
        }
        return dateList;
    }

    private String retrieveLineInfo(Elements marketElems, int teamIndex) {
        String lineText = marketElems.get(teamIndex).text();
        if (lineText.equals("-")) {
            return "";
        }
        return lineText;
    }

    private void transformGameLineToModel(Element gameLineRowEvent) {
        String awayTeamName = gameLineRowEvent.select("#firstTeamName").text();
        String homeTeamName = gameLineRowEvent.select("#secondTeamName").text();

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
        List<String> dateList = retrieveAllDates(doc);
        Elements rowEvents = doc.select(".row.event");
        Elements gameLineRowEvents = rowEvents.stream().skip(1).collect(Collectors.toCollection(Elements::new));
        for (Element gameLineRowEvent : gameLineRowEvents) {
            transformGameLineToModel(gameLineRowEvent);
        }
    }
}
