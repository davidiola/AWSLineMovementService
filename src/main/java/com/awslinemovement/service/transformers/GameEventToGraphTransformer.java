package com.awslinemovement.service.transformers;

import com.awslinemovement.service.constants.Constants;
import com.awslinemovement.service.model.api.GameEventRequest;
import com.awslinemovement.service.model.dataaccess.GameEvent;
import com.awslinemovement.service.model.dataaccess.Team;
import com.awslinemovement.service.model.graph.GraphData;
import com.awslinemovement.service.model.graph.GraphLine;
import com.awslinemovement.service.model.graph.GraphPoint;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

@RequiredArgsConstructor(onConstructor=@__(@Inject))
@Log4j2
public class GameEventToGraphTransformer {

    @NonNull
    private ObjectMapper objectMapper;

    private static final String EVEN = "even";

    private List<GameEvent> sortGameEvents(List<GameEvent> gameEvents) {
        return gameEvents.stream().sorted().collect(Collectors.toList());
    }

    public String transformRequestToIdentifier(GameEventRequest gameEventRequest) {
        return String.format(gameEventRequest.getAwayTeam() + "v" + gameEventRequest.getHomeTeam() + gameEventRequest.getDateOfEvent());
    }

    public String returnGameEventInGraphJSON(List<GameEvent> gameEvents) {
        List<GameEvent> sortedGameEvents = sortGameEvents(gameEvents);
        GraphLine homeTeamSpreadGraphLine = convertTeamLineDataToGraphLine(sortedGameEvents, true, Constants.LineType.ML);
        GraphLine awayTeamSpreadGraphLine = convertTeamLineDataToGraphLine(sortedGameEvents, false, Constants.LineType.ML);
        GraphData graphData = GraphData.builder()
                .lines(ImmutableList.of(homeTeamSpreadGraphLine, awayTeamSpreadGraphLine))
                .build();
        try {
            String json = objectMapper.writeValueAsString(graphData);
            return json;
        } catch (JsonProcessingException e) {
            log.error("Could not process graphData {}", graphData, e);
        }
        return "";
    }

    private GraphLine convertTeamLineDataToGraphLine(List<GameEvent> sortedGameEvents, boolean home, Constants.LineType lineType) {
        GraphLine graphLine = GraphLine.builder()
                .color("hsl(80, 70%, 50%)")
                .id(sortedGameEvents.get(0).getHomeTeam().getName())
                .build();
        if (!home) {
            graphLine.setId(sortedGameEvents.get(0).getAwayTeam().getName());
            graphLine.setColor("hsl(35, 55%, 39%)");
        }

        List<GraphPoint> points = sortedGameEvents.stream()
                .map((gameEvent) -> buildGraphPointForTeamAndLineType(gameEvent, home, lineType))
                .collect(Collectors.toList());

        graphLine.setData(points);
        return graphLine;
    }

    private GraphPoint buildGraphPointForTeamAndLineType(GameEvent event, boolean home, Constants.LineType lineType) {
        GraphPoint graphPoint = GraphPoint.builder().build();
        graphPoint.setX(translateUnixToReadableDate(event.getTimestamp()));
        Team team = event.getAwayTeam();
        if (home) {
            team = event.getHomeTeam();
        }
        switch(lineType) {
          case ML:
            graphPoint.setY(checkLineForEven(team.getMl()));
            break;
          case SPREAD:
            graphPoint.setY(checkLineForEven(team.getSpread().getLineAmount()));
            break;
          case TOTAL:
            graphPoint.setY(checkLineForEven(team.getTotal().getLineAmount()));
            break;
        }
        return graphPoint;
    }

    private String translateUnixToReadableDate(String timestamp) {
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        sf.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = new Date(Long.parseLong(timestamp)*1000);
        System.out.println(sf.format(date));
        return sf.format(date);
    }

    private String checkLineForEven(String lineAmount) {
        if (lineAmount.equals(EVEN)) {
            return "0";
        }
        return lineAmount;
    }
}
