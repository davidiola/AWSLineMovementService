package com.awslinemovement.service.constants;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

public final class Constants {
    public enum Sport { NBA, NFL, NCAAB }
    public enum LineType { ML, SPREAD, TOTAL }
    public static final Map<Sport, String> SPORT_STRING_MAP = ImmutableMap.of(
            Sport.NBA, "NBA",
            Sport.NFL, "NFL",
            Sport.NCAAB, "NCAA Basketball"
    );

    public static final String SPORTSBOOK_BASE_URL = "https://www.sportsbook.ag";
    public static final String PROD_AWS_REGION = "us-east-1";
    public static final int AWAY_TEAM_MARKET_IDX = 0;
    public static final int HOME_TEAM_MARKET_IDX = 1;
    public static final String DASH = "-";
    public static final String YEAR_PREFIX_STRING = DASH + "20";
    public static final int LENGTH_OF_DATE_STR = 6;
    public static final String UPDATE_TICKET_SELECTOR = "a[href*=\"./updateTicket.sbk\"]";
    public static final String VERSUS_STR = "v";
    public static final String AWAY_TEAM_NAME_SELECTOR = "#firstTeamName";
    public static final String HOME_TEAM_NAME_SELECTOR = "#secondTeamName";
    public static final String TOTAL_SELECTOR = ".column.money";
    public static final String SPREAD_SELECTOR = ".column.spread";
    public static final String ML_SELECTOR = ".column.total";
}




