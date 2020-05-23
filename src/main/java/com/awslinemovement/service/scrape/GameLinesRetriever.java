package com.awslinemovement.service.scrape;

import com.awslinemovement.service.model.dataaccess.GameEvent;
import com.awslinemovement.service.model.dataaccess.LineTuple;
import com.awslinemovement.service.model.dataaccess.Team;
import com.google.common.base.Strings;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.awslinemovement.service.constants.Constants.*;

@Log4j2
@NoArgsConstructor
public class GameLinesRetriever {

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
        if (spreadElem.isEmpty()) {
            return spreadPartList;
        }
        String spreadLine = spreadElem.substring(0, spreadElem.indexOf("(")).replaceAll("\\s", "");
        String spreadOdds = spreadElem.substring(spreadElem.indexOf("(") + 1, spreadElem.length() - 1);
        if (spreadOdds.contains("EV")) {
            spreadOdds = "-100";
        }
        spreadPartList.add(spreadLine);
        spreadPartList.add(spreadOdds);
        return spreadPartList;
    }

    private LineTuple transformRowEventToSpreadOrTotal(Element gameLineRowEvent, boolean spread, int marketIdx) {
        LineTuple lineTuple = new LineTuple();
        String rowEventLineTupleSelector;
        if (spread) {
            rowEventLineTupleSelector = SPREAD_SELECTOR;
        }  else {
            rowEventLineTupleSelector = TOTAL_SELECTOR;
        }

        Elements marketElems = gameLineRowEvent.select(rowEventLineTupleSelector).select(".market");
        String lineTupleElem = retrieveLineInfo(marketElems, marketIdx);
        if (!lineTupleElem.isEmpty()) {
           String line = parseLineParts(lineTupleElem).get(0);
           String odds = parseLineParts(lineTupleElem).get(1);
           lineTuple.setLineAmount(line);
           lineTuple.setLineOdds(odds);
        }
        return lineTuple;
    }

    private Team transformRowEventToTeam(Element gameLineRowEvent, boolean home) {
        Team team = new Team();

        String teamNameSelector;
        int marketIdx;
        if (home) {
            teamNameSelector = HOME_TEAM_NAME_SELECTOR;
            marketIdx = HOME_TEAM_MARKET_IDX;
        } else {
            teamNameSelector = AWAY_TEAM_NAME_SELECTOR;
            marketIdx = AWAY_TEAM_MARKET_IDX;
        }
        team.setName(gameLineRowEvent.select(teamNameSelector).text());

        // ML class is actually total, total actually ML
        Elements moneyMarketElems = gameLineRowEvent.select(ML_SELECTOR).select(".market");
        String teamMLOdds = retrieveLineInfo(moneyMarketElems, marketIdx);
        team.setMl(teamMLOdds);

        team.setSpread(transformRowEventToSpreadOrTotal(gameLineRowEvent, true, marketIdx));
        team.setTotal(transformRowEventToSpreadOrTotal(gameLineRowEvent, false, marketIdx));

        return team;
    }

    private GameEvent transformGameLineToModel(Element gameLineRowEvent) {

        GameEvent gameEvent = new GameEvent();

        long unixTime = System.currentTimeMillis() / 1000L;
        String timestamp = Long.toString(unixTime);
        gameEvent.setTimestamp(timestamp);

        String dateOfEvent = getDateOfEvent(gameLineRowEvent);
        if (dateOfEvent.isEmpty()) {
            log.error("Could not determine date of event.");
            return null;
        } else {
            gameEvent.setEventDate(dateOfEvent);
        }

        Team homeTeam = transformRowEventToTeam(gameLineRowEvent, true);
        Team awayTeam = transformRowEventToTeam(gameLineRowEvent, false);

        String uniqueIdentifier = getUniqueIdentifierForGameEvent(awayTeam.getName(), homeTeam.getName(), dateOfEvent);
        gameEvent.setGameEventIdentifier(uniqueIdentifier);

        gameEvent.setHomeTeam(homeTeam);
        gameEvent.setAwayTeam(awayTeam);

        return gameEvent;
    }

    public Optional<List<GameEvent>> retrieveGameEvents(Document doc) {
        List<GameEvent> gameEventList = new ArrayList<>();
        Elements rowEvents = doc.select(".row.event");
        // 1 Overarching blank row event
        if (rowEvents.size() <= 1) {
            return Optional.empty();
        }

        // SKIP Overarching blank row event
        Elements gameLineRowEvents = rowEvents.stream()
                .skip(1)
                .collect(Collectors.toCollection(Elements::new));

        return Optional.of(gameLineRowEvents.stream()
                .map(this::transformGameLineToModel)
                .filter(g -> doesNotContainNullFields(g))
                .collect(Collectors.toList()));
    }

    private boolean doesNotContainNullFields(GameEvent g) {
        if (g == null) {
            return false;
        }
        if (Strings.isNullOrEmpty(g.getEventDate())
                || Strings.isNullOrEmpty(g.getGameEventIdentifier())
                || Strings.isNullOrEmpty(g.getTimestamp())) {
            return false;
        }
        if (teamHasNullField(g.getAwayTeam()) || teamHasNullField(g.getHomeTeam())) {
            return false;
        }
        return true;
    }

    private boolean teamHasNullField(Team team) {
      if (Strings.isNullOrEmpty(team.getMl()) || Strings.isNullOrEmpty(team.getName())
          || team.getSpread() == null || team.getTotal() == null) {
            return true;
        }
      return false;
    }
}
