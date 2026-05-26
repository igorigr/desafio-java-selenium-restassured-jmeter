package com.desafio.pages;

import org.openqa.selenium.By;

public class ArticlePage extends BasePage {

    private static final By ARTICLE_TITLE = By.cssSelector(
            "h1.entry-title, h1.post-title, article h1, .entry-title, " +
            ".ast-blog-single-element h1"
    );

    private static final By ARTICLE_CONTENT = By.cssSelector(
            ".entry-content, .post-content, article .content, " +
            ".ast-post-format-wrapper, .single-post-content"
    );

    private static final By BREADCRUMB = By.cssSelector(
            ".breadcrumb, .ast-breadcrumbs, nav[aria-label='breadcrumb'], " +
            ".rank-math-breadcrumb"
    );

    public boolean isLoaded() {
        return isElementVisible(ARTICLE_TITLE) || isElementVisible(ARTICLE_CONTENT);
    }

    public String getTitle() {
        if (isElementVisible(ARTICLE_TITLE)) {
            return getText(ARTICLE_TITLE);
        }
        return getPageTitle();
    }

    public boolean isArticlePage() {
        String url = getCurrentUrl();
        String title = getPageTitle();
        return !url.contains("?s=") &&
               !title.contains("Página não encontrada") &&
               !title.contains("404") &&
               isElementPresent(By.tagName("article"));
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }
}
