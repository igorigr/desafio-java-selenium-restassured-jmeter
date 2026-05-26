package com.desafio.pages;

import com.desafio.config.ConfigReader;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

public class HomePage extends BasePage {

    private static final String URL = ConfigReader.getInstance().getBaseUrl();

    // Astra theme search icon (primary + fallbacks)
    private static final By SEARCH_ICON = By.cssSelector(
            ".ast-search-icon, .ast-header-custom-item .ast-search-icon, " +
            "button.search-menu-icon, .ast-search-btn, " +
            ".search-toggle, button[aria-label='Search'], button[aria-label='Buscar'], " +
            ".header-search-icon, .icon-search, button.search-icon"
    );

    // Search input (Astra theme overlay + generic WordPress fallbacks)
    private static final By SEARCH_INPUT = By.cssSelector(
            ".ast-search-form input[name='s'], " +
            ".ast-search-form-wrap input[name='s'], " +
            ".search-form input[name='s'], " +
            "input[name='s'], input[type='search'], .search-field, " +
            "#searchform input, form[role='search'] input"
    );

    // Search submit button
    private static final By SEARCH_SUBMIT = By.cssSelector(
            "button[type='submit'], input[type='submit'], .search-submit, " +
            ".ast-search-form button"
    );

    // Navigation / page loaded indicator
    private static final By SITE_LOGO = By.cssSelector(
            ".site-logo, .custom-logo, .site-branding, header .logo, " +
            ".ast-logo-mobile, .header-image, h1.site-title a, .site-title"
    );

    public HomePage open() {
        navigateTo(URL);
        waitForPageLoad();
        log.info("Blog do Agi home page opened");
        return this;
    }

    public boolean isLoaded() {
        String url = getCurrentUrl();
        return isElementVisible(SITE_LOGO) ||
               url.contains("agibank.com.br") ||
               url.contains("blogdoagi.com.br");
    }

    public HomePage clickSearchIcon() {
        log.info("Clicking search icon");
        try {
            click(SEARCH_ICON);
        } catch (Exception e) {
            log.warn("Primary search icon not found, trying alternative approach: {}", e.getMessage());
            List<WebElement> icons = findElements(By.cssSelector("button, a.search, .search"));
            for (WebElement icon : icons) {
                String classAttr = icon.getAttribute("class");
                String ariaLabel = icon.getAttribute("aria-label");
                if ((classAttr != null && classAttr.toLowerCase().contains("search"))
                        || (ariaLabel != null && ariaLabel.toLowerCase().contains("search"))) {
                    scrollToElement(icon);
                    icon.click();
                    log.info("Clicked search element with class: {}", classAttr);
                    break;
                }
            }
        }
        return this;
    }

    public boolean isSearchInputVisible() {
        return isElementVisible(SEARCH_INPUT);
    }

    public SearchResultsPage searchFor(String term) {
        log.info("Searching for: '{}'", term);
        clickSearchIcon();
        typeAndSubmit(SEARCH_INPUT, term);
        waitForPageLoad();
        return new SearchResultsPage();
    }

    public SearchResultsPage searchForDirectUrl(String term) {
        log.info("Navigating directly to search URL for: '{}'", term);
        String searchUrl = URL + "/?s=" + java.net.URLEncoder.encode(term, java.nio.charset.StandardCharsets.UTF_8);
        navigateTo(searchUrl);
        return new SearchResultsPage();
    }
}
