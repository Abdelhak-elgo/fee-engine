# fee-engine

Moteur de calcul de frais financiers écrit en Java 21 et Spring Boot, structuré en architecture hexagonale stricte et développé en TDD. Projet portfolio destiné à un entretien technique CTO : l'objectif est d'illustrer une base de code propre, testable et évolutive, où le domaine reste totalement indépendant du framework.

## Stack

* Java 21
* Spring Boot (Web MVC + Validation)
* JUnit 5, AssertJ, Mockito
* Maven Wrapper (`./mvnw`)

## Architecture

Architecture hexagonale (Ports & Adapters). Le domaine ne dépend d'aucun framework.

```
adapter/in  ──►  port/in  ──►  application service  ──►  domain
                                        │
                                        ▼
                                    port/out  ──►  adapter/out
```

Arborescence des packages sous `src/main/java/com/elgourmat/fee_engine/` :

```
domain/
  model/       Money, Currency, Transaction, FeeLine, FeeLineType,
               FeeBreakdown, CustomerType, Channel
  exception/
  rule/        FeeRule, PercentageFeeRule, FixedFeeRule,
               ExemptionRule, CapRule
  service/     FeeEngine
application/
  command/     CalculateFeesCommand
  port/in/     CalculateFeesUseCase
  port/out/    RuleProvider, FeeCalculationRepository
  model/       FeeCalculationRecord
  service/     CalculateFeesService
adapter/
  out/rule/            InMemoryRuleProvider
  out/persistence/     InMemoryFeeCalculationRepository
```

## Démarrage

Lancer les tests :

```Shell
./mvnw test
```

Lancer l'application :

```Shell
./mvnw spring-boot:run
```

## État d'avancement

| Phase | Contenu                                         | État     |
| ----- | ----------------------------------------------- | -------- |
| 0     | Fondations (setup Maven, structure)             | Terminée |
| 1     | Domaine complet (règles + FeeEngine)            | En cours |
| 2     | Application + ports IN                          | En cours |
| 3     | Adapters in-memory                              | En cours |
| 4     | REST + ProblemDetail                            | À venir  |
| 5     | Persistance Postgres / Flyway / JSONB versionné | À venir  |
| 6     | Keycloak                                        | À venir  |
| 7     | Observabilité (Micrometer p50/p95/p99)          | À venir  |
| 8     | ArchUnit + JaCoCo + PIT                         | À venir  |
| 9     | Docker Compose + CI                             | À venir  |
| 10    | Frontend Angular 20                             | À venir  |
| 11    | Spring AI                                       | À venir  |
| 12    | ADRs                                            | À venir  |

## Principes

* TDD strict : red → green → refactor
* Immutabilité : records Java, aucune mutation
* `BigDecimal` avec `RoundingMode.HALF_UP` pour tous les calculs monétaires
* Séparation stricte des ports IN et OUT
* `FeeEngine` (domaine) sans aucune annotation Spring

## Documentation

* [docs/PLAN.md](docs/PLAN.md) — plan de développement en 12 phases avec décisions d'architecture verrouillées
* [docs/TODO.md](docs/TODO.md) — checklist granulaire, un commit atomique par tâche

