# Fee Engine — Plan de développement (v2, review-intégrée)

> Moteur de calcul de frais financiers en Java 21 / Spring Boot 4, architecture hexagonale, TDD strict.
> Portfolio pour entretien technique CTO Theodo.
> Version 2 : intègre les décisions de la review d'architecture du 2026-09-01.

## Table des matières

1. [Contexte et objectifs](#1-contexte-et-objectifs)
2. [Décisions verrouillées suite à la review](#2-décisions-verrouillées-suite-à-la-review)
3. [Architecture cible](#3-architecture-cible)
4. [Structure des packages](#4-structure-des-packages)
5. [Vue d'ensemble des phases](#5-vue-densemble-des-phases)
6. [Détail des phases](#6-détail-des-phases)
7. [Décisions techniques défendables](#7-décisions-techniques-défendables)
8. [Stratégie de tests](#8-stratégie-de-tests)
9. [Definition of Done par phase](#9-definition-of-done-par-phase)
10. [ADRs à écrire](#10-adrs-à-écrire)
11. [Roadmap post-portfolio](#11-roadmap-post-portfolio)
12. [Sources](#12-sources-et-références)

***

## 1. Contexte et objectifs

### Contexte

Projet portfolio pour entretien technique CTO Theodo. Un CTO découvre le candidat par ce projet.

Objectifs à démontrer sans besoin d'oral :

* Discipline **TDD** stricte
* Maîtrise du **Clean Code** et du **DDD tactique**
* Application d'une **architecture hexagonale** (Ports & Adapters)
* Capacité à produire un **système complet** : persistance, sécurité, observabilité, CI, front-end, IA
* **Apprentissage documenté** dans les ADRs

### Indicateurs de succès

| Indicateur                | Cible                                 |
| ------------------------- | ------------------------------------- |
| Couverture JaCoCo         | ≥ 80 % globale, 100 % sur `domain/**` |
| Mutation score (PIT)      | ≥ 70 % sur `domain/**`                |
| Tests ArchUnit            | 100 % verts                           |
| Temps `docker compose up` | < 60 s (Keycloak pré-buildé)          |
| CI GitHub Actions         | Verte sur `main`                      |
| ADRs                      | ≥ 5 documents                         |
| Endpoints REST            | 2 (`calculate`, `explain`)            |
| Front-end fonctionnel     | 1 SPA Angular avec auth               |

***

## 2. Décisions verrouillées suite à la review

| #       | Décision                                                         | Rationale                                                                         |
| ------- | ---------------------------------------------------------------- | --------------------------------------------------------------------------------- |
| **D1**  | **Pas de multi-tenant**                                          | Hors scope portfolio. `RuleProvider.load()` sans paramètre. Retiré de la roadmap. |
| **D2**  | **Angular gardé intégral** (phase 10, 4h)                        | Full-stack fait partie du signal. À finir proprement.                             |
| **D3**  | **Mutation testing PIT** ajouté                                  | Cible ≥ 70 % sur `domain/**`, badge README.                                       |
| **D4**  | **Versioning JSONB** dès la v1                                   | Payload `{"version": 1, "lines": [...]}` — évite le refactor migration.           |
| **D5**  | **Docker Compose < 60s**                                         | Image Keycloak custom (multi-stage, realm baked in).                              |
| **D6**  | **`FeeCalculationRecord`** **déplacé** vers `application/model/` | L'audit est une préoccupation applicative.                                        |
| **D7**  | **`FeeEngine`** **sans annotation Spring**                       | Instancié par `CalculateFeesService` à chaque appel. Garantit ArchUnit rule #1.   |
| **D8**  | **Validation en deux étages**                                    | DTO REST : `jakarta.validation` ; Command : non-null uniquement.                  |
| **D9**  | **Percentiles Micrometer p50/p95/p99**                           | SLO-ready.                                                                        |
| **D10** | **Testcontainers reuse activé**                                  | `testcontainers.reuse.enable=true`.                                               |

***

## 3. Architecture cible

### Principes hexagonaux inviolables

1. Le domaine ne connaît **aucun** framework — pas de Spring, pas de JPA, pas de Jackson.
2. Les dépendances pointent **vers l'intérieur**.
3. Chaque interaction externe passe par un **port explicite**.
4. Un adapter est **remplaçable** sans toucher au cœur.
5. L'architecture est **testée** par ArchUnit.

### Diagramme des dépendances

```
             ┌─────────────────────────────────────────┐
             │              adapter/in                  │
             │  ┌──────────────┐   ┌──────────────┐    │
             │  │FeeController │   │ExplanationCtrl│   │
             │  └──────┬───────┘   └──────┬───────┘    │
             └─────────┼──────────────────┼────────────┘
                       │                  │
                       ▼                  ▼
             ┌─────────────────────────────────────────┐
             │            application/port/in           │
             │       CalculateFeesUseCase (interface)   │
             │       ExplainFeesUseCase   (interface)   │
             └─────────────┬───────────────────────────┘
                           │
                           ▼
             ┌─────────────────────────────────────────┐
             │           application/service            │
             │      CalculateFeesService                │
             │      ExplainFeesService                  │
             └─────┬────────────────────────┬──────────┘
                   │                        │
                   ▼                        ▼
      ┌────────────────────────┐  ┌────────────────────────┐
      │  application/port/out  │  │        domain          │
      │  FeeCalculationRepo    │  │ Money, FeeRule,        │
      │  RuleProvider          │  │ FeeEngine, ...         │
      │  ExplanationService    │  │  (aucun framework)     │
      └───────┬────────────────┘  └────────────────────────┘
              │
              ▼
      ┌──────────────────────────────────────────────────┐
      │                   adapter/out                     │
      │  JpaFeeCalculationAdapter, InMemoryRuleProvider,  │
      │  SpringAiExplanationAdapter                       │
      └──────────────────────────────────────────────────┘
```

***

## 4. Structure des packages

```
com.elgourmat.fee_engine/
├── domain/                              PUR, testé sans Spring
│   ├── model/           Money, Currency, Transaction, FeeLine, FeeLineType,
│   │                    FeeBreakdown, CustomerType, Channel               ✅
│   ├── exception/       InvalidAmountException, CurrencyMismatchException,
│   │                    InvalidTransactionException                        ✅
│   ├── rule/            FeeRule (interface), PercentageFeeRule,
│   │                    FixedFeeRule, ExemptionRule, CapRule
│   └── service/         FeeEngine (POJO, aucun @Component)
│
├── application/
│   ├── port/
│   │   ├── in/          CalculateFeesUseCase, ExplainFeesUseCase
│   │   └── out/         FeeCalculationRepository, RuleProvider,
│   │                    ExplanationService
│   ├── service/         CalculateFeesService, ExplainFeesService (@Service)
│   ├── command/         CalculateFeesCommand, ExplainFeesCommand
│   └── model/           FeeCalculationRecord            (D6)
│
├── adapter/
│   ├── in/
│   │   └── rest/
│   │       ├── FeeController, ExplanationController
│   │       ├── dto/     TransactionRequest, FeeBreakdownResponse, FeeLineResponse,
│   │       │            ExplanationResponse
│   │       ├── mapper/  FeeRestMapper
│   │       ├── error/   GlobalExceptionHandler (ProblemDetail RFC 7807)
│   │       └── security/ SecurityConfig, KeycloakJwtAuthConverter
│   └── out/
│       ├── persistence/
│       │   ├── FeeCalculationPersistenceAdapter
│       │   ├── jpa/     FeeCalculationEntity, FeeCalculationJpaRepository
│       │   ├── mapper/  FeeCalculationEntityMapper
│       │   └── payload/ PayloadV1, FeeLineJson (D4)
│       ├── rule/        InMemoryRuleProvider
│       └── ai/
│           ├── SpringAiExplanationAdapter
│           └── prompt/  PromptPayload, PromptMapper
│
└── FeeEngineApplication.java
```

***

## 5. Vue d'ensemble des phases

| #       | Nom                                               | Temps | Livrable clé                   |
| ------- | ------------------------------------------------- | ----- | ------------------------------ |
| **0** ✅ | Fondations                                        | Fait  | Domaine testable pur, 36 tests |
| **1**   | Domaine complet (règles + `FeeEngine`)            | 4h    | Moteur fonctionnel             |
| **2**   | Application + ports IN                            | 2h    | `CalculateFeesUseCase` testé   |
| **3**   | Port OUT règles + `InMemoryRuleProvider`          | 1h    | Règles injectables             |
| **4**   | Adapter IN REST + ProblemDetail                   | 3h    | API `/calculate`               |
| **5**   | Persistance Postgres + Flyway + JSONB v1          | 3.5h  | Audit trail versionné          |
| **6**   | Sécurité Keycloak JWT                             | 2h    | Endpoints protégés             |
| **7**   | Observabilité (Actuator + Micrometer p50/p95/p99) | 2h    | Métriques Prometheus           |
| **8**   | ArchUnit + JaCoCo + **PIT**                       | 2.5h  | Architecture garantie          |
| **9**   | DX : Docker Compose + CI GitHub Actions           | 2.5h  | `docker compose up` < 60s      |
| **10**  | Front Angular 20                                  | 4h    | SPA fonctionnelle              |
| **11**  | Adapter IA via Spring AI                          | 3h    | Preuve d'ouverture             |
| **12**  | Documentation (README + 5 ADRs)                   | 2h    | Documentation défendable       |

**Total** : \~31.5h focus, étalable 7-10 jours.

***

## 6. Détail des phases

### Phase 0 — Fondations ✅

Livrables : `Money`, `Currency`, `Transaction`, `CustomerType`, `Channel`, `FeeLine`, `FeeLineType`, `FeeBreakdown`, 3 exceptions domaine, 36 tests verts.

***

### Phase 1 — Domaine complet (4h)

**Objectif** : cœur métier avec règles et orchestrateur, sans framework.

#### 1.1 `FeeRule` interface (10 min)

```Java
package com.elgourmat.fee_engine.domain.rule;

public interface FeeRule {
    boolean appliesTo(Transaction tx, FeeBreakdown current);
    FeeLine apply(Transaction tx, FeeBreakdown current);
    String name();
}
```

#### 1.2 `PercentageFeeRule` (30 min)

Config : `BigDecimal rate`, `String name`. Toujours applicable. Multiplie le montant par le taux.
Tests : taux appliqué, nom respecté, montant zéro produit une ligne zéro.

#### 1.3 `FixedFeeRule` (30 min)

Config : `Money fixedAmount`, `Set<Channel> applicableChannels`. Applicable si canal correspond.
Tests : appliqué au bon canal, ignoré sinon, devise cohérente.

#### 1.4 `ExemptionRule` (30 min)

Config : `Set<CustomerType> exemptedTypes`. `apply()` retourne `FeeLine.discount()` du total courant.
Tests : client exempté → total ramené à zéro ; non exempté → règle ignorée.

#### 1.5 `CapRule` (45 min)

Config : `Money maxFees`. Applicable si `totalFees > maxFees`. `apply()` retourne `discount = totalFees - maxFees`.
Tests : sous plafond → ignorée ; au-dessus → total plafonné exactement.

#### 1.6 `FeeEngine` (1h) — D7

```Java
package com.elgourmat.fee_engine.domain.service;

public final class FeeEngine {
    private final List<FeeRule> rules;
    public FeeEngine(List<FeeRule> rules) { this.rules = List.copyOf(rules); }
    public FeeBreakdown calculate(Transaction tx) { ... }
}
```

**D7** : aucune annotation Spring. Instancié à chaque appel.
Tests : moteur vide, une règle, plusieurs règles, ordre, exemption + cap, cap seul.

#### DoD Phase 1

* [ ] 4 règles + `FeeEngine`
* [ ] ≥ 20 nouveaux tests unitaires verts
* [ ] Aucun import `org.springframework.*` dans `domain/**`

***

### Phase 2 — Application + ports IN (2h)

#### 2.1 `CalculateFeesCommand` — D8 (15 min)

```Java
package com.elgourmat.fee_engine.application.command;

public record CalculateFeesCommand(
    BigDecimal amount, String currency, String customerType,
    String channel, String countryCode
) {
    public CalculateFeesCommand {
        Objects.requireNonNull(amount, "amount required");
        Objects.requireNonNull(currency, "currency required");
    }
}
```

#### 2.2 `CalculateFeesUseCase` (5 min)

```Java
package com.elgourmat.fee_engine.application.port.in;

public interface CalculateFeesUseCase {
    FeeBreakdown handle(CalculateFeesCommand command);
}
```

#### 2.3 `CalculateFeesService` (1h30)

```Java
@Service
public class CalculateFeesService implements CalculateFeesUseCase {
    private final RuleProvider ruleProvider;
    private final FeeCalculationRepository repository;
    private final Clock clock;

    public FeeBreakdown handle(CalculateFeesCommand cmd) {
        Transaction tx = toTransaction(cmd);
        FeeEngine engine = new FeeEngine(ruleProvider.load());
        FeeBreakdown breakdown = engine.calculate(tx);
        repository.save(FeeCalculationRecord.of(tx, breakdown, clock.instant()));
        return breakdown;
    }
}
```

Tests Mockito : ordre des appels, `FeeCalculationRecord` avec les bons champs, propagation d'exception.

#### DoD Phase 2

* [ ] Interface `CalculateFeesUseCase` publique
* [ ] `Clock` injecté

***

### Phase 3 — Port OUT règles (1h)

#### 3.1 `RuleProvider` — D1 (5 min)

```Java
public interface RuleProvider {
    List<FeeRule> load();
}
```

#### 3.2 `InMemoryRuleProvider` (55 min)

`@Component`. Retourne 4 règles : commission 1.5%, frais fixes canal BRANCH 2 MAD, exemption CORPORATE, plafond 100 MAD.

***

### Phase 4 — Adapter IN REST (3h)

#### 4.1 DTOs (30 min)

```Java
public record TransactionRequest(
    @NotNull @Positive BigDecimal amount,
    @NotBlank @Pattern(regexp = "^(MAD|EUR|USD)$") String currency,
    @NotBlank String customerType,
    @NotBlank String channel,
    @NotBlank @Pattern(regexp = "^[A-Za-z]{2}$") String countryCode
) {}
```

#### 4.2 `FeeRestMapper` (30 min)

Normalise `currency.toUpperCase()`, mappe breakdown récursif.

#### 4.3 `FeeController` (30 min)

Dépend de `CalculateFeesUseCase` (interface).

#### 4.4 `GlobalExceptionHandler` (45 min)

`@RestControllerAdvice`. Mappe `InvalidAmountException`, `CurrencyMismatchException`, `MethodArgumentNotValidException` → `ProblemDetail` (`application/problem+json`).

#### 4.5 Tests `@WebMvcTest` (45 min)

Cas nominal 200, validation 400, devise inconnue 400, Content-Type respecté.

***

### Phase 5 — Persistance (3.5h)

#### 5.1 Dépendances (10 min)

`spring-boot-starter-data-jpa`, `flyway-core`, `flyway-database-postgresql`, `postgresql`, `testcontainers-postgresql`, `hypersistence-utils-hibernate-63`.

#### 5.2 Migration V1 (20 min) — D4

```SQL
-- V1__init.sql
CREATE TABLE fee_calculation (
    id UUID PRIMARY KEY,
    calculated_at TIMESTAMPTZ NOT NULL,
    transaction_amount NUMERIC(19,4) NOT NULL,
    transaction_currency VARCHAR(3) NOT NULL,
    customer_type VARCHAR(16) NOT NULL,
    channel VARCHAR(16) NOT NULL,
    country_code CHAR(2) NOT NULL,
    total_fees NUMERIC(19,4) NOT NULL,
    grand_total NUMERIC(19,4) NOT NULL,
    payload JSONB NOT NULL
);
CREATE INDEX idx_fee_calculation_calculated_at ON fee_calculation(calculated_at DESC);
```

#### 5.3 `FeeCalculationEntity` (20 min)

JPA immuable, `payload` mappé via `JsonType` Hibernate 6.3+.

#### 5.4 `FeeCalculationJpaRepository` (5 min)

#### 5.5 Port `FeeCalculationRepository` (5 min)

```Java
public interface FeeCalculationRepository {
    void save(FeeCalculationRecord record);
}
```

#### 5.6 `FeeCalculationRecord` — D6 (10 min)

Placé dans `application/model/`.

#### 5.7 Payload versioning — D4 (15 min)

```Java
public record PayloadV1(int version, List<FeeLineJson> lines) {
    public PayloadV1 { if (version != 1) throw new IllegalArgumentException(); }
}
```

#### 5.8 `FeeCalculationPersistenceAdapter` (30 min)

#### 5.9 Tests `@DataJpaTest` + Testcontainers reuse (1h30) — D10

***

### Phase 6 — Sécurité Keycloak (2h)

#### 6.1 Dépendances (5 min)

`spring-boot-starter-oauth2-resource-server`, `spring-boot-starter-security`, `spring-security-test`.

#### 6.2 `SecurityConfig` (30 min)

Public : `/actuator/health`, `/actuator/info`, `/v3/api-docs/**`, `/swagger-ui/**`.
Authentifié : `/api/**`.

#### 6.3 `KeycloakJwtAuthConverter` (30 min)

Extrait `realm_access.roles`, `resource_access.<client>.roles`, `scope → SCOPE_*`.

#### 6.4 `@PreAuthorize` (10 min)

```Java
@PreAuthorize("hasAuthority('SCOPE_fees:calculate')")
```

#### 6.5 Configuration (5 min)

`spring.security.oauth2.resourceserver.jwt.issuer-uri=${KEYCLOAK_ISSUER_URI}`

#### 6.6 Tests (40 min)

401 sans token, 403 sans scope, 200 avec scope.

***

### Phase 7 — Observabilité (2h)

#### 7.1 Dépendances (5 min)

`spring-boot-starter-actuator`, `micrometer-registry-prometheus`, `logstash-logback-encoder`.

#### 7.2 Actuator (15 min)

```Properties&#x20;files
management.endpoints.web.exposure.include=health,info,metrics,prometheus
management.metrics.distribution.percentiles.fee_calculation_duration=0.5,0.95,0.99
```

#### 7.3 Métrique custom — D9 (30 min)

`Timer.builder("fee_calculation_duration").publishPercentiles(0.5, 0.95, 0.99).tag(...)`.

#### 7.4 Log JSON (30 min)

`logback-spring.xml` + `logstash-logback-encoder` + MDC `calculation_id` et `trace_id`.

#### 7.5 Tests (20 min)

***

### Phase 8 — ArchUnit + JaCoCo + PIT (2.5h)

#### 8.1 Dépendances (5 min)

`archunit-junit5`, plugins `jacoco-maven-plugin` + `pitest-maven`.

#### 8.2 `HexagonalArchitectureTest` (1h) — 9 règles

1. `domain..*` no `org.springframework..*`
2. `domain..*` no `jakarta.persistence..*`
3. `domain..*` no `com.fasterxml.jackson..*`
4. `application..*` no `adapter..*`
5. Controllers dans `adapter.in..*` n'accèdent pas à `application.service..*`
6. `*Adapter` dans `adapter.out..*` implémente `application.port.out..*`
7. `@RestController` dans `adapter.in.rest..*`
8. `@Entity` dans `adapter.out.persistence.jpa..*`
9. `domain.model..*` sont records ou enums

#### 8.3 JaCoCo (30 min)

Seuil global 80 %, seuil `domain/**` 100 %. Exclusions : DTOs, main class.

#### 8.4 PIT — D3 (45 min)

```XML
<plugin>
    <groupId>org.pitest</groupId>
    <artifactId>pitest-maven</artifactId>
    <configuration>
        <targetClasses><param>com.elgourmat.fee_engine.domain.*</param></targetClasses>
        <targetTests><param>com.elgourmat.fee_engine.domain.*Test</param></targetTests>
        <mutationThreshold>70</mutationThreshold>
    </configuration>
</plugin>
```

***

### Phase 9 — DX (2.5h)

#### 9.1 Image Keycloak pré-buildée — D5 (40 min)

`docker/keycloak/Dockerfile` :

```Dockerfile
FROM quay.io/keycloak/keycloak:26.4.1 AS builder
ENV KC_DB=postgres
ENV KC_HEALTH_ENABLED=true
COPY realm-export.json /opt/keycloak/data/import/
RUN /opt/keycloak/bin/kc.sh build

FROM quay.io/keycloak/keycloak:26.4.1
COPY --from=builder /opt/keycloak/ /opt/keycloak/
COPY --from=builder /opt/keycloak/data/import /opt/keycloak/data/import
```

#### 9.2 Script init Postgres

`docker/postgres/init-multiple-dbs.sh` :

```Shell
#!/bin/bash
set -e
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
    CREATE DATABASE fees;
    CREATE DATABASE keycloak;
EOSQL
```

#### 9.3 `Dockerfile` app (20 min)

Multi-stage : Maven `dependency:go-offline` → build → `eclipse-temurin:21-jre-alpine`.

#### 9.4 `docker-compose.yml` (30 min)

```YAML
services:
  postgres:
    image: postgres:16-alpine
    container_name: fee-engine-postgres
    environment:
      POSTGRES_USER: ${POSTGRES_USER:-fees}
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD:-fees}
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./docker/postgres/init-multiple-dbs.sh:/docker-entrypoint-initdb.d/init.sh:ro
    ports: ["5432:5432"]
    networks: [fee_net]
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${POSTGRES_USER:-fees}"]
      interval: 5s
      timeout: 3s
      retries: 10

  keycloak:
    build:
      context: ./docker/keycloak
      dockerfile: Dockerfile
    container_name: fee-engine-keycloak
    command: ["start-dev", "--import-realm", "--optimized"]
    environment:
      KC_HOSTNAME: localhost
      KC_HTTP_ENABLED: "true"
      KC_HOSTNAME_STRICT: "false"
      KC_HEALTH_ENABLED: "true"
      KC_DB: postgres
      KC_DB_URL: jdbc:postgresql://postgres/keycloak
      KC_DB_USERNAME: ${POSTGRES_USER:-fees}
      KC_DB_PASSWORD: ${POSTGRES_PASSWORD:-fees}
      KEYCLOAK_ADMIN: ${KEYCLOAK_ADMIN:-admin}
      KEYCLOAK_ADMIN_PASSWORD: ${KEYCLOAK_ADMIN_PASSWORD:-admin}
    ports: ["8180:8080"]
    depends_on:
      postgres:
        condition: service_healthy
    networks: [fee_net]
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/health/ready"]
      interval: 10s
      timeout: 5s
      retries: 6

  app:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: fee-engine-app
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres/fees
      SPRING_DATASOURCE_USERNAME: ${POSTGRES_USER:-fees}
      SPRING_DATASOURCE_PASSWORD: ${POSTGRES_PASSWORD:-fees}
      KEYCLOAK_ISSUER_URI: http://keycloak:8080/realms/fee-engine
    ports: ["8080:8080"]
    depends_on:
      postgres:
        condition: service_healthy
      keycloak:
        condition: service_healthy
    networks: [fee_net]

volumes:
  postgres_data:

networks:
  fee_net:
    driver: bridge
```

#### 9.5 GitHub Actions CI (30 min)

```YAML
name: CI
on: [push, pull_request]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { distribution: temurin, java-version: 21, cache: maven }
      - run: ./mvnw -B verify
      - run: ./mvnw -B org.pitest:pitest-maven:mutationCoverage
      - uses: codecov/codecov-action@v4
        with: { file: target/site/jacoco/jacoco.xml }
      - uses: actions/upload-artifact@v4
        with: { name: pit-report, path: target/pit-reports/ }
```

#### 9.6 Makefile (10 min)

```makefile
build:       ./mvnw -B verify
test:        ./mvnw -B test
mutation:    ./mvnw -B org.pitest:pitest-maven:mutationCoverage
run:         ./mvnw spring-boot:run
docker-up:   docker compose up -d --build
docker-down: docker compose down
```

***

### Phase 10 — Angular (4h) — D2

#### 10.1 Setup (30 min)

`ng new fees-ui --standalone --style=scss --routing=false`
`npm install keycloak-angular keycloak-js`

#### 10.2 Interfaces TypeScript (15 min)

Miroir des DTOs Java.

#### 10.3 `FeeService` (45 min)

`HttpClient` typé, gestion erreur.

#### 10.4 Intercepteur JWT (30 min)

Ajoute `Authorization: Bearer <token>` sur `/api/**`.

#### 10.5 Composant principal (1h30)

Reactive Form + signals + tableau breakdown + affichage `ProblemDetail`.

#### 10.6 Tests (30 min)

* Service : `HttpTestingController` (200, 400, 401)
* Composant : `@testing-library/angular` (soumission + erreur)

***

### Phase 11 — Adapter IA (3h)

#### 11.1 Dépendance Spring AI (15 min)

```XML
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-openai-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

#### 11.2 Port `ExplanationService` (5 min)

```Java
public interface ExplanationService {
    String explain(FeeBreakdown breakdown);
}
```

#### 11.3 `ExplainFeesUseCase` + Service (20 min)

#### 11.4 `SpringAiExplanationAdapter` (1h)

`PromptPayload` en `adapter/out/ai/prompt/` : mapping isolé, domaine intouché.

#### 11.5 `ExplanationController` (30 min)

`POST /api/v1/fees/{id}/explain`, `@PreAuthorize("hasAuthority('SCOPE_fees:read')")`.

#### 11.6 Tests (50 min)

Mock `ChatClient`, aucun appel réseau.

#### DoD Phase 11

* [ ] Diff des phases 1-10 **inchangé** — prouvé par git

***

### Phase 12 — Documentation (2h)

#### 12.1 `README.md` racine (1h)

Sections : problème, screenshots, stack, démarrage 3 commandes, structure hexagonale, hors scope, roadmap, badges CI/coverage/PIT.

#### 12.2 5 ADRs (1h)

Voir section 10.

***

## 7. Décisions techniques défendables

| Décision                                   | Rationale                     | Défense au CTO                                                        |
| ------------------------------------------ | ----------------------------- | --------------------------------------------------------------------- |
| Records immuables pour VOs                 | Java 21 idiomatique           | « Un `Money` ne peut pas être muté par accident »                     |
| `RoundingMode.HALF_UP` à la construction   | Cohérence de scale            | « `10.0 ≠ 10.00` sur `BigDecimal` ; normaliser tôt »                  |
| `FeeLineType` CHARGE/DISCOUNT              | Money reste positif           | « C'est le type qui exprime le signe »                                |
| `totalFees()` clampé à zéro                | Métier                        | « On ne rembourse pas un client qui n'a pas payé »                    |
| `FeeEngine` sans Spring (D7)               | Domaine pur                   | « ArchUnit garantit qu'il n'y a pas de fuite »                        |
| Ports IN et OUT distincts                  | Direction claire              | « Ce que j'offre vs ce dont je dépends »                              |
| `RuleProvider` externalisé                 | Open-Closed                   | « Demain je branche YAML ou DB sans toucher au moteur »               |
| Testcontainers plutôt que H2 (D10)         | Vrai Postgres                 | « H2 ment sur JSONB, fonctions, casse »                               |
| Audit append-only + payload versionné (D4) | Auditabilité fintech          | « Un calcul est un fait ; version 1 permet la migration »             |
| Keycloak resource-server                   | Séparation authn/authz        | « L'app ne stocke aucun secret utilisateur »                          |
| ArchUnit + PIT (D3)                        | Qualité garantie par le build | « L'architecture et la robustesse des tests ne sont pas décoratives » |
| Angular signals                            | Réactivité fine-grained       | « Moins de re-render que RxJS »                                       |
| Adapter IA via Spring AI                   | Preuve d'ouverture            | « J'ajoute un adapter externe sans changer le cœur »                  |
| Percentiles p50/p95/p99 (D9)               | SLO-ready                     | « Une moyenne cache la queue de distribution »                        |

***

## 8. Stratégie de tests

### Pyramide

```
              /\
             /E2E\           ~3     (Testcontainers Postgres + Keycloak + RestAssured)
            /------\
           /  IT    \        ~12    (@WebMvcTest, @DataJpaTest)
          /----------\
         / Unitaires  \      ~70-80 (JUnit + AssertJ + Mockito)
        /--------------\
       / Property-based\     ~15    (jqwik sur Money — optionnel)
      /------------------\
     /   Architecture    \   ~9     (ArchUnit)
    /----------------------\
   /     Mutation Score     \ 70% domain (PIT)
  /--------------------------\
```

### Règles par couche

| Couche                       | Type de test     | Outils                                         |
| ---------------------------- | ---------------- | ---------------------------------------------- |
| `domain/**`                  | Unitaire pur     | JUnit 5, AssertJ                               |
| `application/service/**`     | Unitaire + mocks | JUnit 5, Mockito                               |
| `adapter/in/rest/**`         | Slice test       | `@WebMvcTest`, MockMvc, spring-security-test   |
| `adapter/out/persistence/**` | Slice test       | `@DataJpaTest`, Testcontainers                 |
| `adapter/out/ai/**`          | Unitaire (mock)  | Mockito                                        |
| E2E                          | Full stack       | `@SpringBootTest`, Testcontainers, RestAssured |
| Architecture                 | Statique         | ArchUnit                                       |
| Mutation                     | Meta-test        | PIT                                            |

### Scenarios E2E

* **E2E-1** : POST /calculate avec token valide → 200, DB row, métrique
* **E2E-2** : POST /calculate sans token → 401 + ProblemDetail
* **E2E-3** : POST /calculate avec code pays invalide → 400 + liste `errors`

### Nommage

Français, décrit le comportement.

***

## 9. Definition of Done par phase

* [ ] Tests **écrits avant** l'implémentation
* [ ] Tous les tests verts (`mvn test`)
* [ ] Aucune régression
* [ ] Commits atomiques (conventional commits)
* [ ] ArchUnit vert (phase 8+)
* [ ] Coverage ≥ 80 % (phase 8+)
* [ ] PIT ≥ 70 % sur `domain/**` (phase 8+)
* [ ] ADR mis à jour si décision structurelle

***

## 10. ADRs à écrire

`docs/adr/`

| #    | Titre                                   | Contenu clé                                                      |
| ---- | --------------------------------------- | ---------------------------------------------------------------- |
| 0001 | Architecture hexagonale — apprentissage | Sources (Vernon, Cockburn, Hombergs), apprentissages en pratique |
| 0002 | Strategy pattern pour les règles        | Pourquoi pas Chain of Responsibility, pas Drools                 |
| 0003 | Audit append-only en JSONB versionné    | Pas d'UPDATE, JSONB vs table normalisée, versioning payload      |
| 0004 | Sécurité en resource-server             | Pas de session, Keycloak, tests sans démarrer Keycloak           |
| 0005 | Adapter IA — port/adapter appliqué      | Comment le LLM ne touche ni domaine ni application               |

**Format** : contexte, décision, conséquences, alternatives écartées, statut, sources.

***

## 11. Roadmap post-portfolio

*(Multi-tenant retiré — D1)*

1. **Règles depuis Postgres + hot-reload** — `DbRuleProvider` + Caffeine + événement Kafka `rules.updated`
2. **Event-sourcing du** **`FeeBreakdown`**
3. **API GraphQL** en plus de REST
4. **Feature flags par pays** — Unleash ou LaunchDarkly
5. **Rate limiting** — Bucket4j
6. **CQRS** — `GetFeeCalculationHistoryUseCase` séparé
7. **Observabilité avancée** — OpenTelemetry, Grafana
8. **Load testing** — k6 avec seuils SLO en CI

***

## 12. Sources et références

**Architecture**

* Alistair Cockburn — *Hexagonal Architecture* (2005)
* Vaughn Vernon — *Implementing Domain-Driven Design*
* Tom Hombergs — *Get Your Hands Dirty on Clean Architecture*
* Reflectoring blog — série hexagonal

**Java / Spring**

* Documentation Spring Boot 4, Spring Security 6, Spring AI 1.0

**Testing**

* Kent Beck — *Test-Driven Development: By Example*
* ArchUnit User Guide
* PIT Mutation Testing docs

**Fintech**

* Martin Fowler — *Analysis Patterns* (Money, Accounting Entry)

