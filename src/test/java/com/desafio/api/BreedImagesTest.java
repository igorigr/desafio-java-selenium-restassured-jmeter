package com.desafio.api;

import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

@Epic("Dog API - Testes de API")
@Feature("GET /breed/{breed}/images")
@Tag("api")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BreedImagesTest extends BaseApiTest {

    private static final String ENDPOINT = "/breed/{breed}/images";

    // =========================================================================
    // CENÁRIOS POSITIVOS
    // =========================================================================

    @Test
    @Order(1)
    @Story("Status code 200 com raça válida")
    @Severity(SeverityLevel.BLOCKER)
    @Description("CT-API-009 - GET /breed/hound/images com raça válida deve retornar status code 200")
    void CT_API_009_racaValidaDeveRetornar200() {
        log.info("CT-API-009 - Verificando status 200 para raça 'hound'");

        given()
            .spec(requestSpec)
            .pathParam("breed", "hound")
        .when()
            .get(ENDPOINT)
        .then()
            .statusCode(200)
            .body("status", equalTo("success"));
    }

    @Test
    @Order(2)
    @Story("Lista de imagens não vazia")
    @Severity(SeverityLevel.CRITICAL)
    @Description("CT-API-010 - Raça válida deve retornar lista de imagens não vazia")
    void CT_API_010_racaValidaDeveRetornarListaDeImagensNaoVazia() {
        log.info("CT-API-010 - Verificando lista de imagens para 'labrador'");

        Response response = given()
            .spec(requestSpec)
            .pathParam("breed", "labrador")
        .when()
            .get(ENDPOINT)
        .then()
            .statusCode(200)
            .body("message", not(empty()))
            .extract().response();

        List<String> images = response.jsonPath().getList("message");

        assertThat(images)
                .as("Lista de imagens não deve ser vazia")
                .isNotEmpty();

        assertThat(images.get(0))
                .as("URL da imagem deve começar com 'https://'")
                .startsWith("https://");

        log.info("Total de imagens encontradas para labrador: {}", images.size());
    }

    @Test
    @Order(3)
    @Story("Validação de schema JSON")
    @Severity(SeverityLevel.CRITICAL)
    @Description("CT-API-011 - Response de imagens deve estar em conformidade com o JSON Schema")
    void CT_API_011_imagensDaRacaDeveConformarComJsonSchema() {
        log.info("CT-API-011 - Validando JSON Schema de /breed/hound/images");

        given()
            .spec(requestSpec)
            .pathParam("breed", "hound")
        .when()
            .get(ENDPOINT)
        .then()
            .statusCode(200)
            .body(matchesJsonSchemaInClasspath(loadSchema("breed-images-schema.json")));
    }

    @Test
    @Order(4)
    @Story("URLs de imagens válidas")
    @Severity(SeverityLevel.NORMAL)
    @Description("CT-API-012 - Todas as URLs de imagens devem ser URLs válidas com extensão de imagem")
    void CT_API_012_urlsDasImagensDevemSerUrlsValidas() {
        log.info("CT-API-012 - Verificando formato das URLs de imagens para 'poodle'");

        Response response = given()
            .spec(requestSpec)
            .pathParam("breed", "poodle")
        .when()
            .get(ENDPOINT)
        .then()
            .statusCode(200)
            .extract().response();

        List<String> images = response.jsonPath().getList("message");

        assertThat(images)
                .as("Todas as imagens devem ter URL válida")
                .allMatch(url -> url.startsWith("https://images.dog.ceo/"));

        assertThat(images)
                .as("Todas as URLs devem terminar com extensão de imagem")
                .allMatch(url -> url.endsWith(".jpg") || url.endsWith(".jpeg") ||
                                 url.endsWith(".png") || url.endsWith(".gif"));
    }

    @ParameterizedTest(name = "Raça: {0}")
    @ValueSource(strings = {"beagle", "boxer", "bulldog", "chihuahua", "dalmatian"})
    @Order(5)
    @Story("Múltiplas raças válidas")
    @Severity(SeverityLevel.NORMAL)
    @Description("CT-API-013 - Múltiplas raças válidas devem retornar imagens")
    void CT_API_013_multiplas_racas_validas_devem_retornar_imagens(String breed) {
        log.info("CT-API-013 - Testando raça: '{}'", breed);

        given()
            .spec(requestSpec)
            .pathParam("breed", breed)
        .when()
            .get(ENDPOINT)
        .then()
            .statusCode(200)
            .body("status", equalTo("success"))
            .body("message", not(empty()));
    }

    @Test
    @Order(6)
    @Story("Sub-raça válida")
    @Severity(SeverityLevel.NORMAL)
    @Description("CT-API-014 - Raça com sub-raça (hound/afghan) deve retornar imagens específicas")
    void CT_API_014_subRacaValidaDeveRetornarImagens() {
        log.info("CT-API-014 - Testando sub-raça 'hound/afghan'");

        given()
            .spec(requestSpec)
        .when()
            .get("/breed/hound/afghan/images")
        .then()
            .statusCode(200)
            .body("status", equalTo("success"))
            .body("message", not(empty()));
    }

    // =========================================================================
    // CENÁRIOS NEGATIVOS
    // =========================================================================

    @Test
    @Order(7)
    @Story("Raça inválida retorna 404")
    @Severity(SeverityLevel.CRITICAL)
    @Description("CT-API-015 - GET /breed/{breed}/images com raça inválida deve retornar status code 404")
    void CT_API_015_racaInvalidaDeveRetornar404() {
        log.info("CT-API-015 - Testando raça inexistente 'invalidbreedxyz'");

        given()
            .spec(requestSpec)
            .pathParam("breed", "invalidbreedxyz")
        .when()
            .get(ENDPOINT)
        .then()
            .statusCode(404);
    }

    @Test
    @Order(8)
    @Story("Raça inválida retorna status error")
    @Severity(SeverityLevel.CRITICAL)
    @Description("CT-API-016 - Raça inválida deve retornar body com status 'error' e mensagem de erro")
    void CT_API_016_racaInvalidaDeveRetornarBodyDeErro() {
        log.info("CT-API-016 - Verificando body de erro para raça inválida");

        given()
            .spec(requestSpec)
            .pathParam("breed", "cachorronaoexiste")
        .when()
            .get(ENDPOINT)
        .then()
            .statusCode(404)
            .body("status", equalTo("error"))
            .body("message", not(emptyString()));
    }

    @Test
    @Order(9)
    @Story("Validação de schema de erro")
    @Severity(SeverityLevel.NORMAL)
    @Description("CT-API-017 - Response de erro para raça inválida deve estar conforme o schema de erro")
    void CT_API_017_racaInvalidaDeveConformarComSchemaDeErro() {
        log.info("CT-API-017 - Validando JSON Schema de erro para raça inválida");

        given()
            .spec(requestSpec)
            .pathParam("breed", "racainvalida")
        .when()
            .get(ENDPOINT)
        .then()
            .statusCode(404)
            .body(matchesJsonSchemaInClasspath(loadSchema("error-schema.json")));
    }

    @Test
    @Order(10)
    @Story("Raça com caracteres especiais")
    @Severity(SeverityLevel.MINOR)
    @Description("CT-API-018 - Raça com caracteres especiais deve retornar 404 sem erro interno do servidor")
    void CT_API_018_racaComCaracteresEspeciaisNaoDeveRetornar500() {
        log.info("CT-API-018 - Testando raça com caracteres especiais");

        Response response = given()
            .spec(requestSpec)
            .pathParam("breed", "invalid@breed")
        .when()
            .get(ENDPOINT)
        .then()
            .extract().response();

        assertThat(response.statusCode())
                .as("Status code não deve ser 500 (erro interno do servidor)")
                .isNotEqualTo(500);
    }
}
