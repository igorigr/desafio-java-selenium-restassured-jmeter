package com.desafio.api;

import com.desafio.config.ConfigReader;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public abstract class BaseApiTest {

    protected static final Logger log = LoggerFactory.getLogger(BaseApiTest.class);
    protected static RequestSpecification requestSpec;
    protected static ResponseSpecification responseSpec;
    private static final ConfigReader config = ConfigReader.getInstance();

    @BeforeAll
    static void configureRestAssured() {
        RestAssured.baseURI = config.getApiBaseUrl();
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        requestSpec = new RequestSpecBuilder()
                .setBaseUri(config.getApiBaseUrl())
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addFilter(new AllureRestAssured())
                .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                .build();

        responseSpec = new ResponseSpecBuilder()
                .expectContentType(ContentType.JSON)
                .build();

        log.info("RestAssured configured with base URI: {}", config.getApiBaseUrl());
    }

    protected static String loadSchema(String schemaFileName) {
        return "schemas/" + schemaFileName;
    }

    protected static long getResponseTimeLimit() {
        return config.getIntProperty("api.timeout", 10000);
    }
}
