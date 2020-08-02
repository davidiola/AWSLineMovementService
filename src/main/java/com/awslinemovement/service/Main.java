package com.awslinemovement.service;

import com.awslinemovement.service.model.api.GameEventRequest;
import com.awslinemovement.service.module.LineMovementServiceModule;

import com.google.inject.Injector;

import static com.google.inject.Guice.createInjector;

public class Main {
    public static void main(String[] args) {
        Injector injector = createInjector(
                new LineMovementServiceModule()
        );

        LineMovementService lineMovementService = injector.getInstance(LineMovementService.class);
        lineMovementService.initScrape();
    }
}
