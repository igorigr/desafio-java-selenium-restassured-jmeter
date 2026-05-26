package com.desafio.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.stream.Collectors;

public class SearchResultsPage extends BasePage {

    // Article / result items
    private static final By RESULT_ARTICLES = By.cssSelector(
            "h2.entry-title, h1.entry-title, h2.ast-blog-single-element, " +
            ".entry-title, article, .post, .hentry"
    );

    // Title links — most reliable: the <a> inside the heading
    private static final By RESULT_TITLE_LINKS = By.cssSelector(
            "h2.entry-title a, h1.entry-title a, h2.ast-blog-single-element a, " +
            ".entry-title a, .post-title a"
    );

    // No results message
    private static final By NO_RESULTS_MESSAGE = By.cssSelector(
            ".no-results, .not-found, .nothing-found, " +
            ".search-no-results, [class*='no-result'], " +
            ".ast-no-results"
    );

    // Page heading / search results title
    private static final By PAGE_HEADING = By.cssSelector(
            "h1.page-title, h1.search-title, .page-title, " +
            ".ast-search-results-header, .search-results-title, " +
            "h1.entry-title"
    );

    // Pagination
    private static final By PAGINATION = By.cssSelector(
            ".pagination, .nav-links, .page-numbers, .ast-pagination"
    );

    // Article links (same as title links — Astra theme has title = link)
    private static final By ARTICLE_LINKS = By.cssSelector(
            "h2.entry-title a, h1.entry-title a, h2.ast-blog-single-element a, " +
            ".entry-title a, .post-title a"
    );

    // Search form in results page (for re-searching)
    private static final By SEARCH_INPUT_IN_RESULTS = By.cssSelector(
            "input[name='s'], input[type='search'], .search-field"
    );

    public boolean hasResults() {
        List<WebElement> articles = findElements(RESULT_ARTICLES);
        log.debug("Found {} result articles", articles.size());
        return !articles.isEmpty();
    }

    public int getResultCount() {
        return findElements(RESULT_ARTICLES).size();
    }

    public boolean hasNoResultsMessage() {
        if (isElementVisible(NO_RESULTS_MESSAGE)) {
            return true;
        }
        // Check page content for "nenhum resultado" text
        String bodyText = driver.findElement(By.tagName("body")).getText().toLowerCase();
        return bodyText.contains("nenhum resultado") ||
               bodyText.contains("no results") ||
               bodyText.contains("nada encontrado") ||
               bodyText.contains("nothing found") ||
               bodyText.contains("não encontramos");
    }

    public List<String> getResultTitles() {
        List<WebElement> links = findElements(RESULT_TITLE_LINKS);
        return links.stream()
                .map(el -> {
                    String text = el.getText();
                    if (text == null || text.isBlank()) {
                        // getAttribute("textContent") works for off-screen elements
                        text = el.getAttribute("textContent");
                    }
                    return text == null ? "" : text.trim();
                })
                .filter(text -> !text.isBlank())
                .collect(Collectors.toList());
    }

    public boolean anyTitleContains(String term) {
        String lowerTerm = term.toLowerCase();
        return getResultTitles().stream()
                .anyMatch(title -> title.toLowerCase().contains(lowerTerm));
    }

    public boolean isOnSearchResultsPage() {
        String url = getCurrentUrl();
        return url.contains("?s=") || url.contains("/search/") ||
               url.contains("/?s=");
    }

    public boolean isSearchResultsPageWithoutError() {
        int statusIndicator = driver.findElements(By.cssSelector("body.error404, body.search-no-results")).size();
        return !getPageTitle().toLowerCase().contains("404") && statusIndicator == 0;
    }

    public ArticlePage clickFirstResult() {
        List<WebElement> links = findElements(ARTICLE_LINKS);
        if (links.isEmpty()) {
            throw new RuntimeException("No search result links found");
        }
        String href = links.get(0).getAttribute("href");
        log.info("Clicking first result link: {}", href);
        links.get(0).click();
        waitForPageLoad();
        return new ArticlePage();
    }

    public String getFirstResultTitle() {
        List<WebElement> links = findElements(RESULT_TITLE_LINKS);
        if (links.isEmpty()) {
            return "";
        }
        String text = links.get(0).getText();
        if (text == null || text.isBlank()) {
            text = links.get(0).getAttribute("textContent");
        }
        return text == null ? "" : text.trim();
    }

    public boolean hasPagination() {
        return isElementVisible(PAGINATION);
    }

    public String getPageHeading() {
        if (isElementVisible(PAGE_HEADING)) {
            return getText(PAGE_HEADING);
        }
        return "";
    }

    public boolean pageLoadsWithoutCriticalError() {
        String title = getPageTitle();
        String url = getCurrentUrl();
        return !title.contains("500") &&
               !title.contains("Error") &&
               !url.isEmpty() &&
               isElementPresent(By.tagName("body"));
    }
}
