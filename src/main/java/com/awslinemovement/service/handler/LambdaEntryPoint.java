package com.awslinemovement.service.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.awslinemovement.service.LineMovementService;
import com.awslinemovement.service.module.LineMovementServiceModule;
import com.google.inject.Injector;

import java.util.Map;

import static com.google.inject.Guice.createInjector;

public class LambdaEntryPoint implements RequestHandler<Map<String,Object>, String> {
    @Override
    public String handleRequest(Map<String,Object> event, Context context)
    {
        Injector injector = createInjector(
                new LineMovementServiceModule()
        );
        LineMovementService lineMovementService = injector.getInstance(LineMovementService.class);
        lineMovementService.initScrape();
        return "success";
    }
}
