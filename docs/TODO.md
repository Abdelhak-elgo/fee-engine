# Fee Engine — TODO détaillé

> Checklist granulaire dérivée de [PLAN.md](./PLAN.md).
> Chaque tâche = un commit atomique (test rouge → feat vert → refacto si besoin).

## Légende

* ✅ = fait
* ⏳ = en cours
* `[ ]` = à faire
* Format commit : `type(scope): description` (feat/test/refactor/fix/docs/chore/ci)

***

## Phase 0 — Fondations ✅

* [x] Money value object (record immuable, invariants, RoundingMode.HALF\_UP)
* [x] Currency enum (MAD, EUR, USD)
* [x] Transaction record (validation ISO-3166 α-2)
* [x] CustomerType enum (STANDARD, PREMIUM, CORPORATE)
* [x] Channel enum (ONLINE, BRANCH, MOBILE)
* [x] FeeLine record + FeeLineType enum (CHARGE, DISCOUNT)
* [x] FeeBreakdown record (immutable, clamp à zéro, currency check)
* [x] 3 domain exceptions
* [x] 36 tests unitaires verts

***

## Phase 1 — Domaine complet (4h) ✅

### 1.1 FeeRule interface

* [x] feat: `domain/rule/FeeRule.java` avec `appliesTo`, `apply`, `name`
* [x] Commit: `feat(domain): contrat FeeRule (strategy pattern)`

### 1.2 PercentageFeeRule

* [x] test: `applique_le_taux_configuré`
* [x] test: `refuse_null_rate`
* [x] test: `nomme_correctement_la_règle`
* [x] test: `applique_zéro_sur_transaction_zéro`
* [x] feat: `domain/rule/PercentageFeeRule.java`
* [x] Commit: `test(domain): specs PercentageFeeRule` puis `feat(domain): PercentageFeeRule`

### 1.3 FixedFeeRule

* [x] test: `applique_le_montant_fixe_sur_canal_configuré`
* [x] test: `ignore_si_canal_hors_ensemble`
* [x] test: `refuse_ensemble_vide`
* [x] feat: `domain/rule/FixedFeeRule.java`
* [x] Commit: `test:` puis `feat(domain): FixedFeeRule`

### 1.4 ExemptionRule

* [x] test: `annule_les_frais_pour_client_exempté`
* [x] test: `ignore_si_type_non_exempté`
* [x] test: `produit_un_discount_du_montant_total_courant`
* [x] feat: `domain/rule/ExemptionRule.java`
* [x] Commit: `test:` puis `feat(domain): ExemptionRule`

### 1.5 CapRule

* [x] test: `ignore_si_total_sous_plafond`
* [x] test: `plafonne_exactement_au_maximum`
* [x] test: `produit_un_discount_égal_au_dépassement`
* [x] feat: `domain/rule/CapRule.java`
* [x] Commit: `test:` puis `feat(domain): CapRule`

### 1.6 FeeEngine orchestrateur

* [x] test: `moteur_sans_règle_retourne_breakdown_vide`
* [x] test: `moteur_avec_une_règle_applique_cette_règle`
* [x] test: `moteur_respecte_ordre_des_règles`
* [x] test: `moteur_applique_exemption_puis_cap`
* [x] test: `moteur_gère_client_exempté_avec_cap`
* [x] feat: `domain/service/FeeEngine.java` (final class, aucun @Component — D7)
* [x] Commit: `test:` puis `feat(domain): FeeEngine (orchestrateur pur)`

### DoD Phase 1

* [x] ≥ 20 tests supplémentaires verts
* [x] `mvn test` BUILD SUCCESS
* [x] Zéro import Spring dans `domain/**`

***

## Phase 2 — Application + ports IN (2h) ✅

### 2.1 CalculateFeesCommand (D8)

* [x] test: `refuse_amount_null`
* [x] test: `refuse_currency_null`
* [x] test: `accepte_command_valide`
* [x] feat: `application/command/CalculateFeesCommand.java` (non-null seulement)
* [x] Commit: `test:` puis `feat(application): CalculateFeesCommand`

### 2.2 CalculateFeesUseCase interface

* [x] feat: `application/port/in/CalculateFeesUseCase.java`
* [x] Commit: `feat(application): port IN CalculateFeesUseCase`

### 2.3 CalculateFeesService

* [x] test (Mockito): `charge_règles_via_provider`
* [x] test: `instancie_FeeEngine_avec_règles_courantes`
* [x] test: `persiste_calcul_via_repository`
* [x] test: `retourne_le_breakdown_calculé`
* [x] test: `propage_exception_domaine_vers_appelant`
* [x] feat: `application/service/CalculateFeesService.java`
* [x] Commit: `test:` puis `feat(application): CalculateFeesService`

### DoD Phase 2

* [x] Aucun @RestController importé ici
* [x] Clock injecté (testable)

***

## Phase 3 — Port OUT règles (1h) ✅

### 3.1 RuleProvider port

* [x] feat: `application/port/out/RuleProvider.java` avec `load(): List<FeeRule>` (D1)
* [x] Commit: `feat(application): port OUT RuleProvider`

### 3.2 InMemoryRuleProvider

* [x] test: `retourne_les_4_règles_par_défaut_dans_l_ordre`
* [x] test: `règles_avec_valeurs_configurées`
* [x] feat: `adapter/out/rule/InMemoryRuleProvider.java` (@Component)
* [x] Commit: `test:` puis `feat(adapter): InMemoryRuleProvider`

***

## Phase 4 — Adapter IN REST (3h) ✅

### 4.1 DTOs

* [x] feat: `TransactionRequest` (record avec jakarta.validation)
* [x] feat: `FeeBreakdownResponse`, `FeeLineResponse`
* [ ] Commit: `feat(adapter): DTOs REST`

### 4.2 FeeRestMapper (MapStruct)

* [x] test: `normalise_currency_en_majuscules`
* [x] test: `mappe_breakdown_complet_avec_lignes`
* [x] feat: `adapter/in/rest/mapper/FeeRestMapper.java`
* [x] chore: MapStruct 1.6.3 + lombok-mapstruct-binding dans pom.xml
* [ ] Commit: `test:` puis `feat(adapter): FeeRestMapper MapStruct`

### 4.3 FeeController

* [x] feat: `adapter/in/rest/FeeController.java` (dépend de CalculateFeesUseCase interface)
* [x] feat: `config/ApplicationConfig.java` (bean CalculateFeesUseCase + Clock)
* [ ] Commit: `feat(adapter): FeeController`

### 4.4 GlobalExceptionHandler

* [x] test @WebMvcTest: `retourne_400_ProblemDetail_pour_InvalidAmount`
* [x] test: `retourne_400_avec_liste_errors_pour_validation`
* [x] test: `retourne_400_pour_CurrencyMismatch`
* [x] test: `Content-Type_application/problem+json`
* [x] feat: `adapter/in/rest/error/GlobalExceptionHandler.java`
* [ ] Commit: `test:` puis `feat(adapter): GlobalExceptionHandler (RFC 7807)`

### 4.5 Tests @WebMvcTest FeeController

* [x] test: `POST_calculate_cas_nominal_200`
* [x] test: `POST_calculate_validation_échouée_400`
* [x] test: `POST_calculate_devise_inconnue_400`
* [ ] Commit: `test(adapter): FeeController @WebMvcTest`

### DoD Phase 4

* [x] Swagger UI accessible `/swagger-ui.html` (à vérifier manuellement au boot)
* [x] Controller n'importe pas `CalculateFeesService` (dépend de CalculateFeesUseCase)
* [x] 95 tests verts (mvn test) — +9 tests Phase 4

***

## Phase 5 — Persistance (3.5h) ✅

### 5.1 Dépendances Maven

* [x] feat: spring-boot-starter-data-jpa, spring-boot-flyway, flyway-core, flyway-database-postgresql, postgresql
* [x] feat: spring-boot-testcontainers, testcontainers-postgresql, testcontainers-junit-jupiter (test scope) — hypersistence-utils skipped (Hibernate 7 native @JdbcTypeCode)
* [ ] Commit: `chore(deps): persistance Postgres + Flyway + Testcontainers`

### 5.2 Migration Flyway V1 (D4)

* [x] feat: `src/main/resources/db/migration/V1__init.sql` avec payload JSONB versionné
* [ ] Commit: `feat(persistence): migration V1 fee_calculation`

### 5.3 FeeCalculationEntity JPA

* [x] feat: `adapter/out/persistence/jpa/FeeCalculationEntity.java` (immuable-ish, @JdbcTypeCode SqlTypes.JSON)
* [x] feat: `adapter/out/persistence/jpa/FeeCalculationJpaRepository.java`
* [ ] Commit: `feat(persistence): entités JPA`

### 5.4 Port OUT FeeCalculationRepository

* [x] feat: `application/port/out/FeeCalculationRepository.java` (livré en Phase 2)
* [ ] Commit: `feat(application): port OUT FeeCalculationRepository`

### 5.5 FeeCalculationRecord (D6)

* [x] feat: `application/model/FeeCalculationRecord.java` (livré en Phase 2)
* [ ] Commit: `feat(application): FeeCalculationRecord (audit)`

### 5.6 PayloadV1 versioning (D4)

* [x] test: `refuse_version_différente_de_1`
* [x] test: `deserialise_payload_v1_correctement`
* [x] test: `throw_sur_version_inconnue`
* [x] feat: `adapter/out/persistence/payload/PayloadV1.java` + `FeeLineJson.java`
* [x] feat: `adapter/out/persistence/payload/UnknownPayloadVersionException.java`
* [ ] Commit: `test:` puis `feat(persistence): payload JSONB versionné`

### 5.7 FeeCalculationPersistenceAdapter

* [x] feat: `adapter/out/persistence/FeeCalculationPersistenceAdapter.java`
* [x] feat: `adapter/out/persistence/mapper/FeeCalculationEntityMapper.java`
* [x] chore: retire `InMemoryFeeCalculationRepository` (remplacé par l'adapter JPA)
* [ ] Commit: `feat(adapter): FeeCalculationPersistenceAdapter`

### 5.8 Tests Testcontainers (@SpringBootTest, D10)

* [x] test: `AbstractPostgresIntegrationTest` — Testcontainer Postgres 16.2 (`.withReuse(true)`)
* [x] test: `sauvegarde_puis_lecture_payload_json_structuré`
* [x] test: `migration_flyway_appliquée`
* [x] test: payload persisté comme JSONB natif Postgres
* [ ] Commit: `test(persistence): tests Testcontainers`

### DoD Phase 5

* [x] `docker-compose.yml` — Postgres dédié fee-engine sur port 5433
* [x] Flyway V1 appliquée + validation Hibernate OK
* [x] 102 tests verts (`mvn test`) — +7 tests Phase 5
* [x] FeeCalculationRecord persistée en JSONB versionné

***

## Phase 6 — Sécurité Keycloak (2h)

### 6.1 Dépendances

* [ ] feat: spring-boot-starter-oauth2-resource-server, spring-boot-starter-security, spring-security-test
* [ ] Commit: `chore(deps): Spring Security + resource-server`

### 6.2 SecurityConfig

* [ ] feat: `adapter/in/rest/security/SecurityConfig.java`
* [ ] Commit: `feat(security): SecurityConfig resource-server`

### 6.3 KeycloakJwtAuthConverter

* [ ] test: `extrait_realm_roles`
* [ ] test: `extrait_resource_roles`
* [ ] test: `extrait_scope_en_SCOPE_préfixe`
* [ ] feat: `adapter/in/rest/security/KeycloakJwtAuthConverter.java`
* [ ] Commit: `test:` puis `feat(security): KeycloakJwtAuthConverter`

### 6.4 @PreAuthorize sur controllers

* [ ] feat: `@PreAuthorize("hasAuthority('SCOPE_fees:calculate')")` sur FeeController
* [ ] Commit: `feat(security): @PreAuthorize sur endpoints`

### 6.5 Configuration

* [ ] feat: `application.properties` — `spring.security.oauth2.resourceserver.jwt.issuer-uri`
* [ ] feat: `application-test.properties` — issuer mocké
* [ ] Commit: `feat(security): config JWT`

### 6.6 Tests d'authentification

* [ ] test: `401_sans_token`
* [ ] test: `403_avec_token_sans_scope`
* [ ] test: `200_avec_scope_fees_calculate`
* [ ] Commit: `test(security): authent/authz endpoints`

***

## Phase 7 — Observabilité (2h)

### 7.1 Dépendances

* [ ] feat: spring-boot-starter-actuator, micrometer-registry-prometheus, logstash-logback-encoder
* [ ] Commit: `chore(deps): observabilité`

### 7.2 Actuator config

* [ ] feat: `application.properties` expose health/info/metrics/prometheus
* [ ] Commit: `feat(obs): expose Actuator endpoints`

### 7.3 Métrique custom (D9)

* [ ] test: `fee_calculation_duration_est_publiée`
* [ ] test: `percentiles_p50_p95_p99_présents`
* [ ] feat: instrumenter CalculateFeesService avec Timer
* [ ] Commit: `test:` puis `feat(obs): métrique fee_calculation_duration avec p50/p95/p99`

### 7.4 Log JSON

* [ ] feat: `src/main/resources/logback-spring.xml` avec logstash-logback-encoder + MDC
* [ ] feat: injecter calculation\_id en MDC dans CalculateFeesService
* [ ] Commit: `feat(obs): log JSON structuré avec MDC`

### 7.5 Tests

* [ ] test: `/actuator/health_retourne_UP`
* [ ] test: `/actuator/prometheus_expose_fee_calculation_duration`
* [ ] Commit: `test(obs): endpoints Actuator`

***

## Phase 8 — ArchUnit + JaCoCo + PIT (2.5h)

### 8.1 Dépendances

* [ ] feat: archunit-junit5 (test)
* [ ] feat: plugin jacoco-maven-plugin
* [ ] feat: plugin pitest-maven (D3)
* [ ] Commit: `chore(deps): ArchUnit + JaCoCo + PIT`

### 8.2 HexagonalArchitectureTest — 9 règles

* [ ] test: `domain_no_spring`
* [ ] test: `domain_no_jpa`
* [ ] test: `domain_no_jackson`
* [ ] test: `application_no_adapter`
* [ ] test: `adapter_in_controllers_no_service`
* [ ] test: `adapter_out_adapter_classes_implement_port`
* [ ] test: `controllers_in_rest`
* [ ] test: `entities_in_persistence`
* [ ] test: `domain_model_are_records_or_enums`
* [ ] Commit: `test(arch): 9 règles ArchUnit hexagonales`

### 8.3 JaCoCo

* [ ] feat: config JaCoCo (seuil global 80 %, seuil `domain/**` 100 %)
* [ ] Commit: `chore(ci): JaCoCo coverage`

### 8.4 PIT mutation testing (D3)

* [ ] feat: config PIT (targetClasses `domain.*`, threshold 70)
* [ ] test: `mvn org.pitest:pitest-maven:mutationCoverage` ≥ 70 %
* [ ] Commit: `chore(ci): PIT mutation testing`

***

## Phase 9 — DX (2.5h)

### 9.1 Image Keycloak pré-buildée (D5)

* [ ] feat: `docker/keycloak/Dockerfile` (multi-stage, kc.sh build)
* [ ] feat: `docker/keycloak/realm-export.json` (realm fee-engine, client fee-engine-api, scopes fees:calculate + fees:read)
* [ ] Commit: `feat(docker): image Keycloak pré-buildée avec realm baked-in`

### 9.2 Script init Postgres

* [ ] feat: `docker/postgres/init-multiple-dbs.sh` (bases fees + keycloak)
* [ ] Commit: `feat(docker): init multi-db Postgres`

### 9.3 Dockerfile app

* [ ] feat: `Dockerfile` racine (multi-stage Maven → JRE alpine 21)
* [ ] Commit: `feat(docker): Dockerfile app multi-stage`

### 9.4 docker-compose.yml

* [ ] feat: `docker-compose.yml` (Postgres unique 2 bases, Keycloak custom, app)
* [ ] test: `time docker compose up -d --build` < 60s
* [ ] Commit: `feat(docker): compose Postgres + Keycloak + app`

### 9.5 GitHub Actions CI

* [ ] feat: `.github/workflows/ci.yml` (mvn verify, PIT, JaCoCo, PIT report)
* [ ] Commit: `ci: pipeline build + test + coverage + mutation`

### 9.6 Makefile + .env.example

* [ ] feat: `Makefile` (build/test/mutation/run/docker-up/docker-down)
* [ ] feat: `.env.example`
* [ ] Commit: `chore: Makefile + .env.example`

***

## Phase 10 — Angular (4h) — D2

### 10.1 Setup

* [ ] feat: `ng new fees-ui --standalone --style=scss --routing=false`
* [ ] feat: `npm install keycloak-angular keycloak-js`
* [ ] Commit: `feat(ui): init projet Angular 20`

### 10.2 Interfaces TypeScript

* [ ] feat: `src/app/api/models.ts` miroir DTOs
* [ ] Commit: `feat(ui): interfaces TypeScript`

### 10.3 FeeService

* [ ] test: `appel_nominal_200`
* [ ] test: `erreur_400_avec_ProblemDetail`
* [ ] test: `erreur_401`
* [ ] feat: `src/app/api/fee.service.ts`
* [ ] Commit: `test:` puis `feat(ui): FeeService`

### 10.4 Intercepteur JWT

* [ ] feat: `src/app/auth/jwt.interceptor.ts`
* [ ] Commit: `feat(ui): JWT interceptor`

### 10.5 Composant principal

* [ ] test @testing-library: `composant_soumet_le_formulaire`
* [ ] test: `composant_affiche_erreur_ProblemDetail`
* [ ] feat: `src/app/fee-calculator/fee-calculator.component.ts`
* [ ] Commit: `test:` puis `feat(ui): composant fee-calculator`

***

## Phase 11 — Adapter IA (3h)

### 11.1 Dépendance Spring AI

* [ ] feat: spring-ai-openai-spring-boot-starter version 1.0.0 pinné
* [ ] Commit: `chore(deps): Spring AI 1.0.0`

### 11.2 Port ExplanationService

* [ ] feat: `application/port/out/ExplanationService.java`
* [ ] Commit: `feat(application): port OUT ExplanationService`

### 11.3 ExplainFeesUseCase + Service

* [ ] test: `charge_record_par_id`
* [ ] test: `appelle_explanation_service`
* [ ] feat: `application/port/in/ExplainFeesUseCase.java`
* [ ] feat: `application/service/ExplainFeesService.java`
* [ ] Commit: `test:` puis `feat(application): ExplainFees use case`

### 11.4 SpringAiExplanationAdapter (PromptPayload isolé)

* [ ] test: mock ChatClient
* [ ] test: `PromptPayload_mappe_correctement_breakdown`
* [ ] feat: `adapter/out/ai/SpringAiExplanationAdapter.java`
* [ ] feat: `adapter/out/ai/prompt/PromptPayload.java`
* [ ] feat: `adapter/out/ai/prompt/PromptMapper.java`
* [ ] Commit: `test:` puis `feat(adapter): SpringAiExplanationAdapter`

### 11.5 ExplanationController

* [ ] test @WebMvcTest: `POST_explain_avec_SCOPE_fees_read`
* [ ] test: `POST_explain_sans_scope_403`
* [ ] feat: `adapter/in/rest/ExplanationController.java`
* [ ] Commit: `test:` puis `feat(adapter): ExplanationController`

### DoD Phase 11

* [ ] `git diff` phases 1-10 : zéro changement (preuve d'ouverture hexagonale)

***

## Phase 12 — Documentation (2h)

### 12.1 README racine

* [ ] feat: `README.md` (problème, screenshots, stack, démarrage 3 cmd, structure hexagonale, hors scope + roadmap, badges CI/coverage/PIT)
* [ ] Commit: `docs: README racine complet`

### 12.2 ADRs

* [ ] feat: `docs/adr/0001-architecture-hexagonale-apprentissage.md`
* [ ] feat: `docs/adr/0002-strategy-pattern-pour-les-regles.md`
* [ ] feat: `docs/adr/0003-audit-append-only-jsonb-versionne.md`
* [ ] feat: `docs/adr/0004-securite-resource-server-keycloak.md`
* [ ] feat: `docs/adr/0005-adapter-ia-ouverture-hexagonale.md`
* [ ] Commit: `docs: 5 ADRs`

### 12.3 Screenshots

* [ ] feat: `docs/img/swagger.png`
* [ ] feat: `docs/img/ui.png`
* [ ] Commit: `docs: screenshots UI + Swagger`

***

## Tâches transverses (au fil de l'eau)

* [ ] Renommer `MonyTest.java` → `MoneyTest.java` (typo)
* [ ] Supprimer le `.git/` imbriqué dans `src/main/java/com/elgourmat/fee_engine/.git/`
* [ ] Ajouter `.editorconfig`
* [ ] Maintenir `CHANGELOG.md`
* [ ] Chaque commit passe `mvn verify` avant push

***

## Critères de complétion globale

* [ ] 12 phases livrées
* [ ] BUILD SUCCESS local et CI
* [ ] Coverage ≥ 80 % global, 100 % `domain/**`
* [ ] Mutation ≥ 70 % `domain/**`
* [ ] ArchUnit 100 % vert
* [ ] `docker compose up -d --build` < 60s
* [ ] SPA Angular fonctionnelle
* [ ] Endpoint `/explain` répond avec compte OpenAI ou Ollama local
* [ ] 5 ADRs livrés
* [ ] README racine avec screenshots + badges

