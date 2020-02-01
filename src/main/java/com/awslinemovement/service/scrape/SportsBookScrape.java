package com.awslinemovement.service.scrape;

import com.awslinemovement.service.constants.Constants.Sport;
import static com.awslinemovement.service.constants.Constants.SPORT_STRING_MAP;
import static com.awslinemovement.service.constants.Constants.SPORTSBOOK_BASE_URL;
import lombok.NoArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

@NoArgsConstructor
public class SportsBookScrape {

    public Document returnDocumentFromUrl(String url) {
        try {
            return Jsoup.connect(url).get();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /*
      We want to remove the query string KV pair: fromMenu=true
     */
    private String trimGameLineLink(String link) {
        String queryString = "?fromMenu=true";
        if (link.contains(queryString)) {
            return link.substring(0, link.indexOf(queryString));
        }
        return link;
    }

    private Element getFirstAccordionElement(Element panelPrimary) {
       return panelPrimary.select(".topAccordion").first();
    }

    private boolean accordionTextMatchesSport(String accordionText, Sport sport) {
        return accordionText.equals(SPORT_STRING_MAP.get(sport));
    }

    private Element getFirstCollapsableMenu(Element panelPrimary) {
       return panelPrimary.select(".panel-collapse.collapse").first();
    }

    private Element getFirstEventInCollapsableMenu(Element collapsableMenu) {
       return collapsableMenu.select(".ev").first();
    }

    private String retrieveLinkFromElement(Element gameLineElement) {
        return gameLineElement.attr("href");
    }

    public String retrieveGameLineUrlForSport(Sport sport, Document doc) {
        Elements panelPrimaries = doc.select(".panel.panel-primary");
        for (Element panelPrimary : panelPrimaries) {
            Element accordion = getFirstAccordionElement(panelPrimary);
            if (accordion != null) {
                // System.out.println(accordion.text());
                if (!accordionTextMatchesSport(accordion.text(), sport)) {
                   continue;
                }
                Element collapsableMenu = getFirstCollapsableMenu(panelPrimary);
                if (collapsableMenu != null) {
                    Element gameLine = getFirstEventInCollapsableMenu(collapsableMenu);
                    String gameLineLink = retrieveLinkFromElement(gameLine);
                    System.out.println(SPORTSBOOK_BASE_URL + trimGameLineLink(gameLineLink));
                    return SPORTSBOOK_BASE_URL + trimGameLineLink(gameLineLink);
                }
            }
        }
        return "";
    }
}
