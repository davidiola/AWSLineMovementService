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
import java.util.Optional;

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

    private Optional<Element> getFirstAccordionElement(Element panelPrimary) {
       return Optional.ofNullable(panelPrimary.select(".topAccordion").first());
    }

    private boolean accordionTextMatchesSport(String accordionText, Sport sport) {
        return accordionText.equals(SPORT_STRING_MAP.get(sport));
    }

    private Optional<Element> getFirstCollapsableMenu(Element panelPrimary) {
       return Optional.ofNullable(panelPrimary.select(".panel-collapse.collapse").first());
    }

    private Optional<Element> getFirstEventInCollapsableMenu(Element collapsableMenu) {
       return Optional.ofNullable(collapsableMenu.select(".ev").first());
    }

    private String retrieveLinkFromElement(Element gameLineElement) {
        return gameLineElement.attr("href");
    }

    public Optional<String> retrieveGameLineUrlForSport(Sport sport, Document doc) {
        Elements panelPrimaries = doc.select(".panel.panel-primary");
        if (!panelPrimaries.isEmpty()) {
            for (Element panelPrimary : panelPrimaries) {
                Optional<Element> optAccordion = getFirstAccordionElement(panelPrimary);
                if (optAccordion.isPresent()) {
                    if (!accordionTextMatchesSport(optAccordion.get().text(), sport)) {
                        continue;
                    }
                    Optional<Element> collapsableMenu = getFirstCollapsableMenu(panelPrimary);
                    if (collapsableMenu.isPresent()) {
                        Optional<Element> gameLine = getFirstEventInCollapsableMenu(collapsableMenu.get());
                        if (gameLine.isPresent()) {
                            String gameLineLink = retrieveLinkFromElement(gameLine.get());
                            return Optional.of(SPORTSBOOK_BASE_URL + trimGameLineLink(gameLineLink));
                        }
                    }
                }
            }
        }

        return Optional.empty();
    }
}
