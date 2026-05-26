package com.desafio.api;

import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

@Epic("Dog API - Testes de API")
@Feature("GET /breeds/image/random")
@Tag("api")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RandomImageTest extends BaseApiTest {

    private static final String ENDPOINT = "/breeds/image/random";

    // =========================================================================
    // CENÁRIOS POSITIVOS
    // =========================================================================

    @Test
    @Order(1)
    @Story("Status code 200")
    @Severity(SeverityLevel.BLOCKER)
    @Description("CT-API-019 - GET /breeds/image/random deve retornar status code 200")
    void CT_API_019_imagemRandomDeveRetornar200() {
        log.info("CT-API-019 - Verificando status code 200 em /breeds/image/random");

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
    @Description("CT-API-020 - Response body deve conter status 'success'")
    void CT_API_020_imagemRandomDeveRetornarStatusSuccess() {
        log.info("CT-API-020 - Verificando campo 'status' = 'success'");

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
    @Story("URL de imagem válida")
    @Severity(SeverityLevel.CRITICAL)
    @Description("CT-API-021 - Campo 'message' deve conter uma URL de imagem válida")
    void CT_API_021_imagemRandomDeveConterUrlDeImagemValida() {
        log.info("CT-API-021 - Verificando URL da imagem aleatória");

        Response response = given()
            .spec(requestSpec)
        .when()
            .get(ENDPOINT)
        .then()
            .statusCode(200)
            .body("message", not(emptyString()))
            .extract().response();

        String imageUrl = response.jsonPath().getString("message");
        log.info("URL da imagem retornada: {}", imageUrl);

        assertThat(imageUrl)
                .as("URL deve começar com 'https://'")
                .startsWith("https://");

        assertThat(imageUrl)
                .as("URL deve ser do domínio dog.ceo")
                .contains("dog.ceo");

        boolean hasImageExtension = imageUrl.endsWith(".jpg") || imageUrl.endsWith(".jpeg") ||
                                    imageUrl.endsWith(".png") || imageUrl.endsWith(".gif");
        assertThat(hasImageExtension)
                .as("URL deve ter extensão de imagem válida (.jpg, .jpeg, .png, .gif)")
                .isTrue();
    }

    @Test
    @Order(4)
    @Story("Validação de schema JSON")
    @Severity(SeverityLevel.CRITICAL)
    @Description("CT-API-022 - Response deve estar em conformidade com o JSON Schema definido")
    void CT_API_022_imagemRandomDeveConformarComJsonSchema() {
        log.info("CT-API-022 - Validando JSON Schema de /breeds/image/random");

        given()
            .spec(requestSpec)
        .when()
            .get(ENDPOINT)
        .then()
            .statusCode(200)
            .body(matchesJsonSchemaInClasspath(loadSchema("random-image-schema.json")));
    }

    @Test
    @Order(5)
    @Story("Tempo de resposta aceitável")
    @Severity(SeverityLevel.NORMAL)
    @Description("CT-API-023 - Tempo de resposta deve ser inferior a 10 segundos")
    void CT_API_023_imagemRandomDeveResponderEmTempoAceitavel() {
        log.info("CT-API-023 - Verificando tempo de resposta");

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
                .as("Tempo de resposta deve ser menor que 10.000 ms")
                .isLessThan(getResponseTimeLimit());
    }

    @Test
    @Order(6)
    @Story("Aleatoriedade das imagens")
    @Severity(SeverityLevel.NORMAL)
    @Description("CT-API-024 - Múltiplas chamadas devem retornar imagens diferentes (aleatoriedade)")
    void CT_API_024_multiplas_chamadas_devem_retornar_imagens_diferentes() {
        log.info("CT-API-024 - Verificando aleatoriedade em 5 chamadas consecutivas");
        int numberOfCalls = 5;
        Set<String> uniqueImages = new HashSet<>();

        for (int i = 0; i < numberOfCalls; i++) {
            Response response = given()
                .spec(requestSpec)
            .when()
                .get(ENDPOINT)
            .then()
                .statusCode(200)
                .extract().response();

            String imageUrl = response.jsonPath().getString("message");
            uniqueImages.add(imageUrl);
            log.info("Chamada {}: {}", i + 1, imageUrl);
        }

        assertThat(uniqueImages.size())
                .as("Em %d chamadas, deve haver ao menos 2 imagens diferentes (verificando aleatoriedade)", numberOfCalls)
                .isGreaterThan(1);
    }

    @Test
    @Order(7)
    @Story("URL de imagem acessível")
    @Severity(SeverityLevel.NORMAL)
    @Description("CT-API-025 - A URL retornada deve conter o caminho de uma raça de cão conhecida")
    void CT_API_025_urlDeveConterCaminhoDaRaca() {
        log.info("CT-API-025 - Verificando estrutura do caminho da URL");

        Response response = given()
            .spec(requestSpec)
        .when()
            .get(ENDPOINT)
        .then()
            .statusCode(200)
            .extract().response();

        String imageUrl = response.jsonPath().getString("message");

        assertThat(imageUrl)
                .as("URL deve conter '/breeds/' no caminho")
                .contains("/breeds/");
    }

    // =========================================================================
    // CENÁRIOS NEGATIVOS
    // =========================================================================

    @Test
    @Order(8)
    @Story("Múltiplas imagens aleatórias (parâmetro inválido)")
    @Severity(SeverityLevel.NORMAL)
    @Description("CT-API-026 - Requisitar quantidade inválida de imagens deve retornar 404 ou ignorar o parâmetro")
    void CT_API_026_quantidadeInvalidaDeImagesDeveSerTratada() {
        log.info("CT-API-026 - Verificando comportamento com quantidade inválida");

        Response response = given()
            .spec(requestSpec)
        .when()
            .get("/breeds/image/random/0")
        .then()
            .extract().response();

        assertThat(response.statusCode())
                .as("Status code deve ser 200 ou 404, mas não 500")
                .isIn(200, 404);
    }

    @Test
    @Order(9)
    @Story("Método HTTP inválido")
    @Severity(SeverityLevel.MINOR)
    @Description("CT-API-027 - Chamada com método POST ao endpoint deve retornar 404 ou 405")
    void CT_API_027_metodoPostNaoDeveSerSuportado() {
        log.info("CT-API-027 - Testando método POST no endpoint de imagem aleatória");

        Response response = given()
            .spec(requestSpec)
        .when()
            .post(ENDPOINT)
        .then()
            .extract().response();

        assertThat(response.statusCode())
                .as("POST não deve ser suportado - esperado 404 ou 405")
                .isIn(404, 405);
    }

    @Test
    @Order(10)
    @Story("Quantidade de imagens com valor negativo")
    @Severity(SeverityLevel.MINOR)
    @Description("CT-API-028 - Solicitar número negativo de imagens deve retornar 404 sem erro interno")
    void CT_API_028_quantidadeNegativaDeImagesNaoDeveRetornar500() {
        log.info("CT-API-028 - Testando quantidade negativa de imagens aleatórias");

        Response response = given()
            .spec(requestSpec)
        .when()
            .get("/breeds/image/random/-1")
        .then()
            .extract().response();

        assertThat(response.statusCode())
                .as("Número negativo não deve causar erro interno (500)")
                .isNotEqualTo(500);
    }
}
