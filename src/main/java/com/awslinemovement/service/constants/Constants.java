package com.awslinemovement.service.constants;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

public final class Constants {
    public enum Sport { NBA, NFL }
    public static final Map<Sport, String> SPORT_STRING_MAP = ImmutableMap.of(
            Sport.NBA, "NBA",
            Sport.NFL, "NFL"
    );

    public static final String SPORTSBOOK_BASE_URL = "https://www.sportsbook.ag";
}




