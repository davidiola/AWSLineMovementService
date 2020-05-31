package com.awslinemovement.service.model.api;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GameEventRequest {
    private String homeTeam;
    private String awayTeam;
    private String dateOfEvent;
}
