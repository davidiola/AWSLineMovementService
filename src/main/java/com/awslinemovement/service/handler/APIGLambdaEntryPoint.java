package com.awslinemovement.service.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.awslinemovement.service.LineMovementService;
import com.awslinemovement.service.model.api.GameEventRequest;
import com.awslinemovement.service.module.LineMovementServiceModule;
import com.google.inject.Injector;
import lombok.extern.log4j.Log4j2;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Optional;

import static com.google.inject.Guice.createInjector;

@Log4j2
public class APIGLambdaEntryPoint implements RequestStreamHandler {

    private final static String HOME_QUERY_PARAM = "homeTeam";
    private final static String AWAY_QUERY_PARAM = "awayTeam";
    private final static String DATE_QUERY_PARAM = "date";

    private final static String QUERY_STRING_PARAMS = "queryStringParameters";

    private final static String GRAPH_DATA_FIELD = "graphData";
    private final static String STATUS_CODE_FIELD = "statusCode";

    private final static int STATUS_CODE_SUCCESS = 200;
    private final static int STATUS_CODE_INTERNAL_FAILURE = 400;
    private final static int STATUS_CODE_NOT_FOUND = 404;

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
        JSONParser parser = new JSONParser();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        JSONObject responseJson = new JSONObject();
        String graphDataJson = "";

        try {
            JSONObject event = (JSONObject) parser.parse(reader);
            JSONObject responseBody = new JSONObject();

            if (event.get(QUERY_STRING_PARAMS) != null) {
                JSONObject queryParams = (JSONObject) event.get(QUERY_STRING_PARAMS);
                Optional<GameEventRequest> gameEventRequest = buildGameEventRequestFromQueryParams(queryParams);
                if (gameEventRequest.isPresent()) {
                    Injector injector = createInjector(
                            new LineMovementServiceModule()
                    );
                    LineMovementService lineMovementService = injector.getInstance(LineMovementService.class);
                    graphDataJson = lineMovementService.initGetAPI(gameEventRequest.get());
                }
            }
            if (graphDataJson.isEmpty()) {
                log.error("Empty graphDataJson");
                responseBody.put(STATUS_CODE_FIELD, STATUS_CODE_INTERNAL_FAILURE);
            } else if (graphDataJson == "none") {
                responseJson.put(STATUS_CODE_FIELD, STATUS_CODE_NOT_FOUND);
            }
            else {
                responseBody.put(GRAPH_DATA_FIELD, graphDataJson);
                responseJson.put(STATUS_CODE_FIELD, STATUS_CODE_SUCCESS);
            }
            responseJson.put("body", responseBody.toString());

        } catch (ParseException exception) {
            responseJson.put(STATUS_CODE_FIELD, STATUS_CODE_INTERNAL_FAILURE);
            responseJson.put("exception", exception);
        }

        OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
        writer.write(responseJson.toString());
        writer.close();
    }

    private Optional<GameEventRequest> buildGameEventRequestFromQueryParams(JSONObject queryParams) {
       if ( (queryParams.get(HOME_QUERY_PARAM) == null) || (queryParams.get(AWAY_QUERY_PARAM) == null) || (queryParams.get(DATE_QUERY_PARAM) == null)) {
           log.error("Query parameters of {} are invalid", queryParams);
           return Optional.empty();
       }
       return Optional.of(
               GameEventRequest.builder()
                       .homeTeam((String)queryParams.get(HOME_QUERY_PARAM))
                       .awayTeam((String)queryParams.get(AWAY_QUERY_PARAM))
                       .dateOfEvent((String)queryParams.get(DATE_QUERY_PARAM))
                       .build()
       );
    }
}
