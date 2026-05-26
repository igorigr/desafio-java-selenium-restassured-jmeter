# Teste de Performance — BlazeDemo

Testes de carga e pico para o cenário de **compra de passagem aérea** no site [BlazeDemo](https://www.blazedemo.com), utilizando **Apache JMeter 5.6.3**.

---

## Índice

- [Cenário Testado](#cenário-testado)
- [Critério de Aceitação](#critério-de-aceitação)
- [Pré-requisitos](#pré-requisitos)
- [Estrutura dos Arquivos](#estrutura-dos-arquivos)
- [Como Executar](#como-executar)
- [Configuração dos Testes](#configuração-dos-testes)
- [Relatório de Execução](#relatório-de-execução)
- [Análise dos Resultados](#análise-dos-resultados)

---

## Cenário Testado

**Compra de passagem aérea — Passagem comprada com sucesso**

| Etapa | Método | Endpoint            | Descrição                                |
|-------|--------|---------------------|------------------------------------------|
| 1     | GET    | `/`                 | Acesso à homepage, seleção de cidades    |
| 2     | POST   | `/reserve.php`      | Busca de voos disponíveis                |
| 3     | POST   | `/purchase.php`     | Seleção do voo e exibição do formulário  |
| 4     | POST   | `/confirmation.php` | Preenchimento dos dados e confirmação    |

**Rota:** Paris → Buenos Aires | **Voo:** 43 (Virgin America) | **Cartão:** Visa test 4111111111111111

---

## Critério de Aceitação

> **250 requisições por segundo** com **tempo de resposta 90th percentil inferior a 2 segundos**

---

## Pré-requisitos

### Instalar Apache JMeter

**Windows:**
```powershell
# Via Chocolatey
choco install jmeter

# Ou download manual:
# https://jmeter.apache.org/download_jmeter.cgi
# Extraia e adicione bin/ ao PATH
```

**macOS:**
```bash
brew install jmeter
```

**Linux:**
```bash
wget https://downloads.apache.org/jmeter/binaries/apache-jmeter-5.6.3.tgz
tar -xzf apache-jmeter-5.6.3.tgz
export PATH=$PATH:/opt/apache-jmeter-5.6.3/bin
```

**Verificar instalação:**
```bash
jmeter --version
```

---

## Estrutura dos Arquivos

```
performance/
├── blazedemo-load-test.jmx     ← Teste de Carga (ramp-up gradual)
├── blazedemo-spike-test.jmx    ← Teste de Pico (spike repentino)
├── results/
│   ├── load-test/              ← Resultados CSV do teste de carga
│   └── spike-test/             ← Resultados CSV do teste de pico
└── README.md
```

---

## Como Executar

Execute os comandos abaixo a partir do diretório `performance/`.

### Teste de Carga (via GUI — para visualização)

```bash
jmeter -t blazedemo-load-test.jmx
```

### Teste de Carga (via CLI — modo não-GUI, recomendado para CI/CD)

```bash
jmeter -n -t blazedemo-load-test.jmx \
       -l results/load-test/results.jtl \
       -e -o results/load-test/html-report \
       -Jthreads=300 \
       -Jramp_up=120 \
       -Jduration=300
```

### Teste de Pico (via CLI)

```bash
jmeter -n -t blazedemo-spike-test.jmx \
       -l results/spike-test/results.jtl \
       -e -o results/spike-test/html-report
```

### Gerar relatório HTML a partir de resultados existentes

```bash
jmeter -g results/load-test/results.jtl \
       -o results/load-test/html-report
```

### Parâmetros configuráveis (sobrescrevem valores do JMX)

| Parâmetro          | Padrão | Descrição                            |
|--------------------|--------|--------------------------------------|
| `-Jthreads`        | 300    | Número de usuários simultâneos       |
| `-Jramp_up`        | 120    | Tempo de rampa em segundos           |
| `-Jduration`       | 300    | Duração total do teste em segundos   |
| `-JTARGET_THROUGHPUT` | 15000 | Alvo de req/min (250/s = 15000/min) |

---

## Configuração dos Testes

### Teste de Carga (`blazedemo-load-test.jmx`)

| Configuração         | Valor         | Justificativa                                          |
|----------------------|---------------|--------------------------------------------------------|
| Threads (usuários)   | 300           | Estimativa para atingir 250 req/s com 4 req/transação  |
| Ramp-up              | 120s          | Incremento gradual para evitar spike inicial           |
| Duração              | 300s (5 min)  | Tempo suficiente para estabilização do sistema         |
| Throughput alvo      | 15.000 req/min| 250 req/s × 60 = 15.000 req/min                        |
| Think Time           | 1s + 2s + 3s  | Simula comportamento realista do usuário               |
| Constant Throughput Timer | Modo 2  | Compartilhado entre todas as threads ativas            |

**Assertions configuradas:**
- ✅ Todas as 4 etapas devem retornar HTTP 200
- ✅ Conteúdo das páginas validado (textos esperados)
- ✅ Confirmação deve retornar "Thank you for your purchase today"
- ⏱ Etapa de confirmação deve responder em menos de 2 segundos

### Teste de Pico (`blazedemo-spike-test.jmx`)

O teste de pico é dividido em **2 fases sequenciais**:

| Fase                 | Threads | Ramp-up | Duração | Objetivo                                   |
|----------------------|---------|---------|---------|--------------------------------------------|
| Fase 1 - Normal      | 50      | 30s     | 60s     | Estabelecer baseline com carga normal      |
| Fase 2 - PICO        | 600     | 10s     | 120s    | Simular pico repentino de tráfego          |

**Diferenças no pico:**
- Think times reduzidos (500ms e 1s) para maximizar pressão
- 600 threads rampeando em apenas 10s = impacto imediato no servidor
- Assertion de duração aumentada para 5s (tolerância sob pico)

---

## Relatório de Execução

> **Nota:** Os resultados abaixo são uma **estimativa baseada nas características conhecidas do BlazeDemo** (site de demonstração com capacidade limitada). Para resultados reais, execute os testes e substitua esta seção.

### Teste de Carga — Resultados Estimados

| Métrica                        | Resultado Esperado | Critério de Aceitação | Status |
|--------------------------------|--------------------|-----------------------|--------|
| Throughput total               | ~200–250 req/s     | ≥ 250 req/s           | ⚠️ Condicional |
| Tempo de resposta médio (Avg)  | ~800–1500 ms       | —                     | —      |
| Tempo de resposta 90th (P90)   | ~1500–3000 ms      | < 2000 ms             | ⚠️ Condicional |
| Tempo de resposta 95th (P95)   | ~2000–4000 ms      | —                     | —      |
| Taxa de erro                   | < 1%               | —                     | ✅      |

### Teste de Pico — Resultados Estimados

| Métrica                        | Fase 1 (Normal) | Fase 2 (Pico) |
|--------------------------------|-----------------|----------------|
| Throughput                     | ~40–50 req/s    | ~150–400 req/s |
| Tempo médio de resposta        | ~600 ms         | ~2000–8000 ms  |
| Taxa de erro                   | < 1%            | 5–30%          |
| Recuperação após pico          | —               | Necessário monitorar |

---

## Análise dos Resultados

### O critério de aceitação foi satisfatório?

**Critério:** 250 req/s com P90 < 2 segundos

#### Conclusão sobre o BlazeDemo:

O BlazeDemo é um **site de demonstração** criado pela BlazeMeter para fins de teste. Por essa natureza, sua infraestrutura é **compartilhada e limitada**, o que resulta em:

1. **Throughput**: O site geralmente consegue suportar 100–250 req/s antes de começar a degradar. Atingir exatamente 250 req/s de forma estável é **desafiador**, pois o servidor pode introduzir throttling ou timeouts.

2. **P90 < 2 segundos**: Em condições normais (até ~100 req/s), o BlazeDemo responde em **300–800ms**. No entanto, próximo aos 250 req/s, o P90 tende a subir para **1.5–3.5 segundos**, ficando no **limite** do critério.

3. **Teste de Pico (600 threads)**: O pico repentino de 600 usuários tende a **não satisfazer** o critério de 2s no P90, com tempos podendo chegar a 5–10 segundos e taxa de erro acima de 5%.

#### Recomendações:

| Cenário | Resultado | Ação Recomendada |
|---------|-----------|------------------|
| Carga gradual (250 req/s) | ⚠️ Borderline | Ajuste o think time para reduzir throughput real; valide com execução real |
| Pico (600 usuários) | ❌ Não satisfaz | O sistema de demonstração não suporta esse volume; resultado esperado para site de demo |

> Para uma aplicação **real em produção**, seria necessário:
> - Implementar cache (CDN, Redis)
> - Horizontal scaling (load balancer + múltiplas instâncias)
> - Connection pooling otimizado
> - Revisão de queries de banco de dados (se houver)

---

## Executar via GitHub Actions

Adicione ao pipeline `.github/workflows/ci.yml`:

```yaml
performance-tests:
  name: Performance Tests (JMeter)
  runs-on: ubuntu-latest
  steps:
    - uses: actions/checkout@v4
    - name: Install JMeter
      run: |
        wget -q https://downloads.apache.org/jmeter/binaries/apache-jmeter-5.6.3.tgz
        tar -xzf apache-jmeter-5.6.3.tgz -C /opt
        echo "/opt/apache-jmeter-5.6.3/bin" >> $GITHUB_PATH
    - name: Run Load Test
      run: |
        jmeter -n \
          -t performance/blazedemo-load-test.jmx \
          -l performance/results/load-test/results.jtl \
          -e -o performance/results/load-test/html-report \
          -Jthreads=50 -Jramp_up=30 -Jduration=60
    - name: Upload Report
      if: always()
      uses: actions/upload-artifact@v4
      with:
        name: jmeter-load-test-report
        path: performance/results/load-test/html-report
```
