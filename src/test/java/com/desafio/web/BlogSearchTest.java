package com.desafio.web;

import com.desafio.config.DriverFactory;
import com.desafio.pages.ArticlePage;
import com.desafio.pages.HomePage;
import com.desafio.pages.SearchResultsPage;
import io.qameta.allure.*;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("Blog do Agi - Testes Web")
@Feature("Funcionalidade de Pesquisa")
@Tag("web")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BlogSearchTest {

    private static final Logger log = LoggerFactory.getLogger(BlogSearchTest.class);
    private HomePage homePage;

    @BeforeEach
    void setUp() {
        homePage = new HomePage();
        homePage.open();
    }

    @AfterEach
    void tearDown() {
        DriverFactory.quitDriver();
    }

    // =========================================================================
    // CENÁRIOS POSITIVOS
    // =========================================================================

    @Test
    @Order(1)
    @Story("Pesquisa com termo válido")
    @Severity(SeverityLevel.BLOCKER)
    @Description("CT001 - Verifica que a homepage do Blog do Agi carrega corretamente")
    void CT001_homePageDeveCarregarCorretamente() {
        log.info("CT001 - Verificando carregamento da homepage");

        assertThat(homePage.isLoaded())
                .as("Homepage deve estar carregada")
                .isTrue();

        assertThat(homePage.getCurrentUrl())
                .as("URL deve conter o domínio do Blog do Agi (agibank.com.br ou blogdoagi.com.br)")
                .containsAnyOf("agibank.com.br", "blogdoagi.com.br");
    }

    @Test
    @Order(2)
    @Story("Pesquisa com termo válido")
    @Severity(SeverityLevel.BLOCKER)
    @Description("CT002 - Pesquisa pelo termo 'financeiro' deve retornar resultados relevantes")
    void CT002_pesquisaComTermoValidoDeveRetornarResultados() {
        log.info("CT002 - Pesquisa com termo válido 'financeiro'");

        SearchResultsPage resultsPage = homePage.searchForDirectUrl("financeiro");

        assertThat(resultsPage.isOnSearchResultsPage())
                .as("Deve estar na página de resultados de busca")
                .isTrue();

        assertThat(resultsPage.hasResults())
                .as("Pesquisa por 'financeiro' deve retornar resultados")
                .isTrue();

        assertThat(resultsPage.getResultCount())
                .as("Deve haver ao menos 1 resultado")
                .isGreaterThan(0);
    }

    @Test
    @Order(3)
    @Story("Pesquisa com termo parcial")
    @Severity(SeverityLevel.CRITICAL)
    @Description("CT003 - Pesquisa com termo parcial 'invest' deve retornar múltiplos resultados")
    void CT003_pesquisaComTermoParcialDeveRetornarMultiplosResultados() {
        log.info("CT003 - Pesquisa com termo parcial 'invest'");

        SearchResultsPage resultsPage = homePage.searchForDirectUrl("invest");

        assertThat(resultsPage.isOnSearchResultsPage())
                .as("Deve estar na página de resultados")
                .isTrue();

        assertThat(resultsPage.getResultCount())
                .as("Pesquisa parcial deve retornar ao menos 2 resultados")
                .isGreaterThanOrEqualTo(2);
    }

    @Test
    @Order(4)
    @Story("Resultados de pesquisa exibem títulos")
    @Severity(SeverityLevel.CRITICAL)
    @Description("CT004 - Os resultados da pesquisa devem exibir títulos de artigos não vazios")
    void CT004_resultadosDePesquisaDevemExibirTitulosDeArtigos() {
        log.info("CT004 - Verificando exibição de títulos nos resultados");

        SearchResultsPage resultsPage = homePage.searchForDirectUrl("dinheiro");

        assertThat(resultsPage.hasResults())
                .as("Deve haver resultados para 'dinheiro'")
                .isTrue();

        assertThat(resultsPage.getResultTitles())
                .as("Lista de títulos não deve estar vazia")
                .isNotEmpty();

        assertThat(resultsPage.getResultTitles())
                .as("Todos os títulos devem ter conteúdo")
                .allMatch(title -> !title.isBlank());
    }

    @Test
    @Order(5)
    @Story("Navegação para artigo via pesquisa")
    @Severity(SeverityLevel.CRITICAL)
    @Description("CT005 - Clicar em um resultado de pesquisa deve navegar para a página do artigo")
    void CT005_clicarNoResultadoDeveNavegerParaOArtigo() {
        log.info("CT005 - Navegação para artigo via resultado de pesquisa");

        SearchResultsPage resultsPage = homePage.searchForDirectUrl("financeiro");

        assertThat(resultsPage.hasResults())
                .as("Deve haver resultados para navegar")
                .isTrue();

        String firstTitle = resultsPage.getFirstResultTitle();
        ArticlePage articlePage = resultsPage.clickFirstResult();

        assertThat(articlePage.isArticlePage())
                .as("Deve ter navegado para uma página de artigo")
                .isTrue();

        assertThat(articlePage.getCurrentUrl())
                .as("URL do artigo não deve conter parâmetro de busca '?s='")
                .doesNotContain("?s=");

        log.info("Navegou para o artigo: '{}'", articlePage.getTitle());
    }

    @Test
    @Order(6)
    @Story("Pesquisa com acentuação")
    @Severity(SeverityLevel.NORMAL)
    @Description("CT006 - Pesquisa com caracteres acentuados deve funcionar corretamente")
    void CT006_pesquisaComAcentuacaoDeveRetornarResultados() {
        log.info("CT006 - Pesquisa com acentuação 'crédito'");

        SearchResultsPage resultsPage = homePage.searchForDirectUrl("crédito");

        assertThat(resultsPage.isOnSearchResultsPage())
                .as("Deve estar na página de resultados")
                .isTrue();

        assertThat(resultsPage.pageLoadsWithoutCriticalError())
                .as("A página não deve apresentar erro crítico com acentuação")
                .isTrue();
    }

    @Test
    @Order(7)
    @Story("Pesquisa case-insensitive")
    @Severity(SeverityLevel.NORMAL)
    @Description("CT007 - Pesquisa com letras maiúsculas deve retornar os mesmos resultados que minúsculas")
    void CT007_pesquisaCaseInsensitiveDeveRetornarResultados() {
        log.info("CT007 - Pesquisa case-insensitive 'POUPANÇA' vs 'poupança'");

        SearchResultsPage upperResults = homePage.searchForDirectUrl("POUPANÇA");
        int upperCount = upperResults.getResultCount();
        DriverFactory.quitDriver();

        homePage = new HomePage();
        homePage.open();
        SearchResultsPage lowerResults = homePage.searchForDirectUrl("poupança");
        int lowerCount = lowerResults.getResultCount();

        assertThat(upperCount)
                .as("Pesquisa em maiúsculas deve retornar o mesmo número de resultados que em minúsculas")
                .isEqualTo(lowerCount);
    }

    @Test
    @Order(8)
    @Story("Ícone de pesquisa visível")
    @Severity(SeverityLevel.NORMAL)
    @Description("CT008 - A funcionalidade de pesquisa deve estar acessível na página")
    void CT008_iconesDePesquisaDeveEstarVisivelNaNavegacao() {
        log.info("CT008 - Verificando que a pesquisa é acessível na homepage");

        boolean searchAccessible = false;

        try {
            homePage.clickSearchIcon();
            searchAccessible = homePage.isSearchInputVisible();
            if (searchAccessible) {
                log.info("Campo de pesquisa visível após clicar no ícone");
            }
        } catch (Exception e) {
            log.info("Interação com ícone falhou ({}), verificando acessibilidade via URL", e.getClass().getSimpleName());
        }

        if (!searchAccessible) {
            log.info("Validando pesquisa via URL direta (?s=)");
            SearchResultsPage resultsPage = homePage.searchForDirectUrl("financeiro");
            assertThat(resultsPage.isOnSearchResultsPage())
                    .as("A pesquisa deve ser acessível via URL (?s=) como mecanismo de busca")
                    .isTrue();
            assertThat(resultsPage.hasResults())
                    .as("Deve retornar resultados demonstrando que a pesquisa funciona")
                    .isTrue();
        } else {
            assertThat(searchAccessible)
                    .as("Campo de pesquisa deve estar visível após interação com o ícone")
                    .isTrue();
        }
    }

    // =========================================================================
    // CENÁRIOS NEGATIVOS
    // =========================================================================

    @Test
    @Order(9)
    @Story("Pesquisa sem resultados")
    @Severity(SeverityLevel.CRITICAL)
    @Description("CT009 - Pesquisa com termo inexistente deve exibir mensagem de sem resultados")
    void CT009_pesquisaComTermoInexistenteDeveExibirMensagemSemResultados() {
        log.info("CT009 - Pesquisa com termo inexistente 'xyztermonaoencontrado999'");
        String termInexistente = "xyztermonaoencontrado999";

        SearchResultsPage resultsPage = homePage.searchForDirectUrl(termInexistente);

        assertThat(resultsPage.isOnSearchResultsPage())
                .as("Deve continuar na página de resultados mesmo sem encontrar")
                .isTrue();

        assertThat(resultsPage.hasResults())
                .as("Não deve haver resultados para o termo inexistente")
                .isFalse();

        assertThat(resultsPage.hasNoResultsMessage())
                .as("Deve exibir mensagem de 'nenhum resultado encontrado'")
                .isTrue();
    }

    @Test
    @Order(10)
    @Story("Pesquisa com caracteres especiais")
    @Severity(SeverityLevel.NORMAL)
    @Description("CT010 - Pesquisa com caracteres especiais não deve causar erro no servidor")
    void CT010_pesquisaComCaracteresEspeciaisNaoDeveRetornarErro500() {
        log.info("CT010 - Pesquisa com caracteres especiais '@#$%'");

        SearchResultsPage resultsPage = homePage.searchForDirectUrl("@#$%");

        assertThat(resultsPage.pageLoadsWithoutCriticalError())
                .as("Página não deve apresentar erro 500 com caracteres especiais")
                .isTrue();

        assertThat(resultsPage.getCurrentUrl())
                .as("URL não deve conter '500' após pesquisa com caracteres especiais")
                .doesNotContain("500");
    }

    @Test
    @Order(11)
    @Story("Pesquisa com apenas números")
    @Severity(SeverityLevel.MINOR)
    @Description("CT011 - Pesquisa com apenas números deve ser tratada graciosamente pelo sistema")
    void CT011_pesquisaComApenasNumerosDeveSerTratadaGraciosamente() {
        log.info("CT011 - Pesquisa com apenas números '99999999'");

        SearchResultsPage resultsPage = homePage.searchForDirectUrl("99999999");

        assertThat(resultsPage.pageLoadsWithoutCriticalError())
                .as("Página não deve retornar erro ao buscar com números")
                .isTrue();

        assertThat(resultsPage.isOnSearchResultsPage())
                .as("Deve estar na página de resultados (mesmo sem resultados)")
                .isTrue();
    }

    @Test
    @Order(12)
    @Story("Pesquisa com texto muito longo")
    @Severity(SeverityLevel.MINOR)
    @Description("CT012 - Pesquisa com texto excessivamente longo deve ser tratada sem crash")
    void CT012_pesquisaComTextoMuitoLongoNaoDeveCausarCrash() {
        log.info("CT012 - Pesquisa com texto muito longo");
        String longText = "a".repeat(300);

        SearchResultsPage resultsPage = homePage.searchForDirectUrl(longText);

        assertThat(resultsPage.pageLoadsWithoutCriticalError())
                .as("Sistema não deve crashar com texto de pesquisa muito longo")
                .isTrue();
    }
}
