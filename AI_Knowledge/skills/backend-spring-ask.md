# Backend Spring Ask Skill

Use for Ask backend work in Java, Spring Boot, APIs, persistence, stores, requests, catalog, services, and integrations.

Read `AGENTS.md`, `ARCHITECTURE_NARRATIVE.md`, and `CODEX_PLAYBOOK.md` first.

Rules:

- Java 21, Maven, Spring Boot, Spring MVC, PostgreSQL, Flyway, OpenAPI, JUnit 5.
- Do not copy frontend FSD literally into Spring Boot. Use backend feature/domain-based packaging with clean/hexagonal boundaries.
- Keep related API contracts, use cases, domain rules, repositories, mappers, tests, and feature config near the product capability when it improves local reasoning.
- Keep core provider-agnostic.
- Treat external systems as adapters.
- Use DTOs for API boundaries.
- Keep controllers thin and returning `ResponseEntity`.
- Move non-trivial flows toward `Controller -> Processor or UseCase -> DomainService -> Repository`.
- Use Flyway for schema changes.
- Do not implement real provider calls without scope, credentials, docs, and tests.
- Do not invent stock, delivery, schedule, or availability facts.

Before edits, search local patterns and map dependent DTOs, mappers, repositories, services, tests, and config.
