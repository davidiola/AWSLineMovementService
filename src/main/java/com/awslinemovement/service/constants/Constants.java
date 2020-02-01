package com.awslinemovement.service.constants;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

public final class Constants {
    public enum Sport { NBA, NFL, NCAAB }
    public static final Map<Sport, String> SPORT_STRING_MAP = ImmutableMap.of(
            Sport.NBA, "NBA",
            Sport.NFL, "NFL",
            Sport.NCAAB, "NCAA Basketball"
    );

    public static final String SPORTSBOOK_BASE_URL = "https://www.sportsbook.ag";
}




