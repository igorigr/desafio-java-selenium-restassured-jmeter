package com.desafio.api;

import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

@Epic("Dog API - Testes de API")
@Feature("GET /breeds/list/all")
@Tag("api")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BreedsListTest extends BaseApiTest {

    private static final String ENDPOINT = "/breeds/list/all";

    // =========================================================================
    // CENÁRIOS POSITIVOS
    // =========================================================================

    @Test
    @Order(1)
    @Story("Status code 200")
    @Severity(SeverityLevel.BLOCKER)
    @Description("CT-API-001 - GET /breeds/list/all deve retornar status code 200")
    void CT_API_001_listarTodasRacasDeveRetornar200() {
        log.info("CT-API-001 - Verificando status code 200 em /breeds/list/all");

        given()
            .spec(requestSpec)
        .when()
            .get(ENDPOINT)
        .then()
            .statusCode(200);
    }

    @Test
    @Order(2)
    @Story("Status de sucesso")
    @Severity(SeverityLevel.BLOCKER)
    @Description("CT-API-002 - Response body deve conter status 'success'")
    void CT_API_002_listarTodasRacasDeveRetornarStatusSuccess() {
        log.info("CT-API-002 - Verificando campo 'status' = 'success'");

        given()
            .spec(requestSpec)
        .when()
            .get(ENDPOINT)
        .then()
            .statusCode(200)
            .body("status", equalTo("success"));
    }

    @Test
    @Order(3)
    @Story("Lista de raças não vazia")
    @Severity(SeverityLevel.CRITICAL)
    @Description("CT-API-003 - Campo 'message' deve conter um mapa não vazio de raças de cães")
    void CT_API_003_listarTodasRacasDeveRetornarMapaNaoVazio() {
        log.info("CT-API-003 - Verificando que o mapa de raças não é vazio");

        Response response = given()
            .spec(requestSpec)
        .when()
            .get(ENDPOINT)
        .then()
            .statusCode(200)
            .extract().response();

        Map<String, Object> message = response.jsonPath().getMap("message");

        assertThat(message)
                .as("O campo 'message' deve ser um mapa não vazio")
                .isNotEmpty();

        assertThat(message.size())
                .as("Deve haver ao menos 50 raças cadastradas")
                .isGreaterThan(50);

        log.info("Total de raças encontradas: {}", message.size());
    }

    @Test
    @Order(4)
    @Story("Validação de schema JSON")
    @Severity(SeverityLevel.CRITICAL)
    @Description("CT-API-004 - Response deve estar em conformidade com o JSON Schema definido")
    void CT_API_004_listarTodasRacasDeveConformarComJsonSchema() {
        log.info("CT-API-004 - Validando JSON Schema de /breeds/list/all");

        given()
            .spec(requestSpec)
        .when()
            .get(ENDPOINT)
        .then()
            .statusCode(200)
            .body(matchesJsonSchemaInClasspath(loadSchema("breeds-list-schema.json")));
    }

    @Test
    @Order(5)
    @Story("Tempo de resposta aceitável")
    @Severity(SeverityLevel.NORMAL)
    @Description("CT-API-005 - Tempo de resposta deve ser inferior a 10 segundos")
    void CT_API_005_listarTodasRacasDeveResponderEmTempoAceitavel() {
        log.info("CT-API-005 - Verificando tempo de resposta");

        Response response = given()
            .spec(requestSpec)
        .when()
            .get(ENDPOINT)
        .then()
            .statusCode(200)
            .extract().response();

        long responseTime = response.getTimeIn(TimeUnit.MILLISECONDS);
        log.info("Tempo de resposta: {} ms", responseTime);

        assertThat(responseTime)
                .as("Tempo de resposta deve ser menor que 10.000 ms (10s)")
                .isLessThan(getResponseTimeLimit());
    }

    @Test
    @Order(6)
    @Story("Raças conhecidas presentes")
    @Severity(SeverityLevel.NORMAL)
    @Description("CT-API-006 - Raças conhecidas (hound, bulldog, poodle) devem estar presentes na lista")
    void CT_API_006_listarTodasRacasDeveConterRacasConhecidas() {
        log.info("CT-API-006 - Verificando presença de raças conhecidas");

        Response response = given()
            .spec(requestSpec)
        .when()
            .get(ENDPOINT)
        .then()
            .statusCode(200)
            .extract().response();

        Map<String, Object> breeds = response.jsonPath().getMap("message");

        assertThat(breeds)
                .as("Deve conter a raça 'hound'")
                .containsKey("hound");

        assertThat(breeds)
                .as("Deve conter a raça 'bulldog'")
                .containsKey("bulldog");

        assertThat(breeds)
                .as("Deve conter a raça 'poodle'")
                .containsKey("poodle");
    }

    @Test
    @Order(7)
    @Story("Estrutura de sub-raças")
    @Severity(SeverityLevel.NORMAL)
    @Description("CT-API-007 - Sub-raças devem ser retornadas como arrays (ex: hound possui sub-raças)")
    void CT_API_007_listarTodasRacasDeveRetornarSubRacasComoArrays() {
        log.info("CT-API-007 - Verificando estrutura de sub-raças");

        given()
            .spec(requestSpec)
        .when()
            .get(ENDPOINT)
        .then()
            .statusCode(200)
            .body("message.hound", instanceOf(java.util.List.class))
            .body("message.hound.size()", greaterThan(0));
    }

    // =========================================================================
    // CENÁRIOS NEGATIVOS
    // =========================================================================

    @Test
    @Order(8)
    @Story("Endpoint inexistente")
    @Severity(SeverityLevel.NORMAL)
    @Description("CT-API-008 - Acesso a endpoint inexistente /breeds/list/nonexistent deve retornar 404")
    void CT_API_008_endpointInexistenteDeveRetornar404() {
        log.info("CT-API-008 - Verificando resposta para endpoint inexistente");

        given()
            .spec(requestSpec)
        .when()
            .get("/breeds/list/nonexistent")
        .then()
            .statusCode(404);
    }
}
