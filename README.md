# Desafio Automação QA

Projeto de automação de testes desenvolvido em **Java 17** com **Selenium WebDriver** (testes Web) e **RestAssured** (testes de API), seguindo boas práticas de arquitetura e escalabilidade.

---

## Índice

- [Tecnologias](#tecnologias)
- [Estrutura do Projeto](#estrutura-do-projeto)
- [Pré-requisitos](#pré-requisitos)
- [Configuração do Ambiente](#configuração-do-ambiente)
- [Executando os Testes](#executando-os-testes)
- [Relatório Allure](#relatório-allure)
- [Cenários de Teste](#cenários-de-teste)
- [CI/CD - GitHub Actions](#cicd---github-actions)

---

## Tecnologias

| Tecnologia             | Versão  | Finalidade                              |
|------------------------|---------|-----------------------------------------|
| Java                   | 17      | Linguagem principal                     |
| Maven                  | 3.9+    | Gerenciador de dependências e build     |
| JUnit 5                | 5.10.2  | Framework de testes                     |
| Selenium WebDriver     | 4.20.0  | Automação de testes Web                 |
| WebDriverManager       | 5.8.0   | Gerenciamento automático de drivers     |
| RestAssured            | 5.4.0   | Testes de API REST                      |
| JSON Schema Validator  | 5.4.0   | Validação de schemas JSON               |
| Allure                 | 2.27.0  | Geração de relatórios                   |
| AssertJ                | 3.25.3  | Assertions fluentes                     |
| Logback                | 1.5.6   | Logging                                 |

---

## Estrutura do Projeto

```
desafio_automacao_java/
├── .github/
│   └── workflows/
│       └── ci.yml                        # Pipeline GitHub Actions
├── src/
│   └── test/
│       ├── java/
│       │   └── com/desafio/
│       │       ├── config/
│       │       │   ├── ConfigReader.java  # Leitura de configurações
│       │       │   └── DriverFactory.java # Gerenciamento do WebDriver
│       │       ├── pages/                 # Page Objects (POM)
│       │       │   ├── BasePage.java
│       │       │   ├── HomePage.java
│       │       │   ├── SearchResultsPage.java
│       │       │   └── ArticlePage.java
│       │       ├── web/
│       │       │   └── BlogSearchTest.java # 12 cenários Web
│       │       └── api/
│       │           ├── BaseApiTest.java
│       │           ├── BreedsListTest.java  # 8 cenários
│       │           ├── BreedImagesTest.java # 10 cenários
│       │           └── RandomImageTest.java # 10 cenários
│       └── resources/
│           ├── config.properties
│           ├── allure.properties
│           ├── logback-test.xml
│           └── schemas/
│               ├── breeds-list-schema.json
│               ├── breed-images-schema.json
│               ├── random-image-schema.json
│               └── error-schema.json
└── pom.xml
```

---

## Pré-requisitos

- **Java 17** ou superior instalado
- **Maven 3.9+** instalado
- **Google Chrome** (para testes Web) — gerenciado automaticamente pelo WebDriverManager
- Conexão com a internet

### Verificar instalações

```bash
java -version
mvn -version
```

---

## Configuração do Ambiente

### 1. Clonar o repositório

```bash
git clone https://github.com/SEU_USUARIO/desafio_automacao_java.git
cd desafio_automacao_java
```

### 2. Configurações (opcional)

As configurações padrão estão em `src/test/resources/config.properties`:

```properties
# Browser: chrome | firefox | edge
browser=chrome

# Modo headless (true para CI/CD, false para desenvolvimento)
headless=false

# URL do blog
base.url=https://blogdoagi.com.br

# URL da Dog API
api.base.url=https://dog.ceo/api
```

Para sobrescrever configurações via linha de comando:

```bash
mvn test -Dbrowser=firefox -Dheadless=true
```

---

## Executando os Testes

### Todos os testes

```bash
mvn test
```

### Somente testes de API

```bash
mvn test -P api
```

### Somente testes Web

```bash
mvn test -P web
```

### Testes Web em modo headless (sem abrir o browser)

```bash
mvn test -P web -Dheadless=true
```

### Testes com browser específico

```bash
# Firefox
mvn test -P web -Dbrowser=firefox

# Edge
mvn test -P web -Dbrowser=edge
```

---

## Relatório Allure

### Gerar e visualizar o relatório após execução

```bash
# Instalar Allure CLI (se não tiver)
# Windows (Scoop):
scoop install allure

# macOS (Homebrew):
brew install allure

# Linux:
sudo apt-add-repository ppa:qameta/allure
sudo apt-get update
sudo apt-get install allure
```

```bash
# Opção 1 — Arquivo HTML único (sem necessidade de servidor)
# Gera um único index.html com tudo embutido — ideal para compartilhar e abrir direto no browser
allure generate --single-file target/allure-results -o target/allure-single --clean
# Abrir:
open target/allure-single/index.html        # macOS
start target/allure-single/index.html       # Windows
xdg-open target/allure-single/index.html   # Linux

# Opção 2 — Relatório completo com servidor local
mvn allure:serve
```

O relatório contém:
- Status de cada cenário (passou/falhou)
- Tempo de execução
- Capturas de tela em caso de falha (testes Web)
- Logs de requisições/respostas (testes de API)

---

## Cenários de Teste

### Testes Web — Blog do Agi (`https://blogdoagi.com.br`)

Foco: **Funcionalidade de pesquisa (lupa no canto superior direito)**

| ID     | Cenário                                             | Tipo     | Severidade |
|--------|-----------------------------------------------------|----------|------------|
| CT001  | Homepage carrega corretamente                       | Positivo | Blocker    |
| CT002  | Pesquisa com termo válido retorna resultados        | Positivo | Blocker    |
| CT003  | Pesquisa com termo parcial retorna múltiplos itens  | Positivo | Critical   |
| CT004  | Resultados exibem títulos de artigos                | Positivo | Critical   |
| CT005  | Clicar no resultado navega para o artigo            | Positivo | Critical   |
| CT006  | Pesquisa com caracteres acentuados funciona         | Positivo | Normal     |
| CT007  | Pesquisa case-insensitive retorna mesmos resultados | Positivo | Normal     |
| CT008  | Ícone de pesquisa abre o campo de busca             | Positivo | Normal     |
| CT009  | Termo inexistente exibe mensagem "sem resultados"   | Negativo | Critical   |
| CT010  | Caracteres especiais não causam erro 500            | Negativo | Normal     |
| CT011  | Busca com apenas números funciona sem erro          | Negativo | Minor      |
| CT012  | Texto muito longo não causa crash                   | Negativo | Minor      |

### Testes de API — Dog API (`https://dog.ceo/api`)

#### GET /breeds/list/all (8 cenários)

| ID          | Cenário                                         | Tipo     |
|-------------|-------------------------------------------------|----------|
| CT-API-001  | Status code 200                                 | Positivo |
| CT-API-002  | Campo status = "success"                        | Positivo |
| CT-API-003  | Mapa de raças não vazio (>50 raças)             | Positivo |
| CT-API-004  | Validação de JSON Schema                        | Positivo |
| CT-API-005  | Tempo de resposta < 10s                         | Positivo |
| CT-API-006  | Raças conhecidas (hound, bulldog, poodle)       | Positivo |
| CT-API-007  | Sub-raças retornadas como arrays                | Positivo |
| CT-API-008  | Endpoint inexistente retorna 404                | Negativo |

#### GET /breed/{breed}/images (10 cenários)

| ID          | Cenário                                         | Tipo     |
|-------------|-------------------------------------------------|----------|
| CT-API-009  | Raça válida retorna status 200                  | Positivo |
| CT-API-010  | Raça válida retorna lista de imagens não vazia  | Positivo |
| CT-API-011  | Validação de JSON Schema para imagens           | Positivo |
| CT-API-012  | URLs de imagens têm extensão válida             | Positivo |
| CT-API-013  | Múltiplas raças válidas retornam imagens (5x)   | Positivo |
| CT-API-014  | Sub-raça válida (hound/afghan) retorna imagens  | Positivo |
| CT-API-015  | Raça inválida retorna 404                       | Negativo |
| CT-API-016  | Raça inválida retorna body de erro              | Negativo |
| CT-API-017  | Schema de erro para raça inválida               | Negativo |
| CT-API-018  | Raça com caracteres especiais não retorna 500   | Negativo |

#### GET /breeds/image/random (10 cenários)

| ID          | Cenário                                         | Tipo     |
|-------------|-------------------------------------------------|----------|
| CT-API-019  | Status code 200                                 | Positivo |
| CT-API-020  | Campo status = "success"                        | Positivo |
| CT-API-021  | Campo message contém URL de imagem válida       | Positivo |
| CT-API-022  | Validação de JSON Schema                        | Positivo |
| CT-API-023  | Tempo de resposta < 10s                         | Positivo |
| CT-API-024  | Múltiplas chamadas retornam imagens diferentes  | Positivo |
| CT-API-025  | URL contém caminho /breeds/ da raça             | Positivo |
| CT-API-026  | Quantidade inválida (0) não retorna 500         | Negativo |
| CT-API-027  | Método POST não suportado retorna 404/405       | Negativo |
| CT-API-028  | Quantidade negativa não retorna 500             | Negativo |

**Total: 12 cenários Web + 28 cenários de API = 40 cenários**

---

## CI/CD - GitHub Actions

O projeto possui pipeline configurado em `.github/workflows/ci.yml` com três jobs:

1. **api-tests** — Executa apenas os testes de API
2. **web-tests** — Executa os testes Web em modo headless com Chrome
3. **full-test-suite** — Job de consolidação que verifica o resultado dos dois anteriores

### Execução manual

Na aba **Actions** do repositório GitHub, selecione o workflow **CI - Automated Tests** e clique em **Run workflow**.

---

## Testes de Performance (JMeter)

Localizados em `performance/`. Consulte o [README dedicado](performance/README.md) para instruções completas.

| Arquivo                          | Tipo         | Threads | Duração  | Throughput Alvo |
|----------------------------------|--------------|---------|----------|-----------------|
| `blazedemo-load-test.jmx`        | Carga        | 300     | 5 min    | 250 req/s       |
| `blazedemo-spike-test.jmx`       | Pico (Spike) | 50→600  | ~3 min   | Máximo possível |

**Execução rápida:**
```bash
# Teste de carga
jmeter -n -t performance/blazedemo-load-test.jmx \
       -l performance/results/load-test/results.jtl \
       -e -o performance/results/load-test/html-report

# Teste de pico
jmeter -n -t performance/blazedemo-spike-test.jmx \
       -l performance/results/spike-test/results.jtl \
       -e -o performance/results/spike-test/html-report
```

---

## Padrões e Boas Práticas Adotadas

- **Page Object Model (POM)**: separação clara entre lógica de teste e interação com a UI
- **ThreadLocal WebDriver**: suporte a execução paralela de testes Web
- **WebDriverManager**: gerenciamento automático de drivers, sem necessidade de instalação manual
- **Configuração externalizada**: todas as configurações em `config.properties`, sobrescrevíveis via `-D`
- **Profiles Maven**: separação por tipo de teste (`api`, `web`)
- **Allure Annotations**: `@Epic`, `@Feature`, `@Story`, `@Severity`, `@Description` para rastreabilidade
- **AssertJ**: assertions descritivas com mensagens de erro claras
- **JSON Schema Validation**: schemas versionados em `src/test/resources/schemas/`
- **Logback**: logging estruturado com níveis apropriados por pacote
