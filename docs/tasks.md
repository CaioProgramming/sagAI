# Improvement Tasks Checklist

Date: 2025-09-02 11:01

Note: Each item is actionable and ordered from architectural/high-level to code-level and tooling. Check off items as they are completed.

Related: For user-experience focused feature ideas and roadmap, see docs/feature_ideas.md.

1. [ ] Establish and document the project architecture overview (layers, modules, data flow, AI clients, Room, Remote Config, Hilt). Include a simple C4 or diagram in docs/. 
2. [ ] Define clear module boundaries and package-by-feature conventions (core, features, data/domain/ui). Create/adjust Gradle modules if needed or document rationale for single-module. 
3. [ ] Review dependency injection graph (Hilt): add missing @Module/@Provides or @Binds bindings for interfaces (e.g., UseCases, repositories), and document scopes (Singleton/ViewModel/Activity). 
4. [ ] Introduce a unified error-handling strategy across use cases and AI clients (sealed Error types, mapping Firebase/Network/Parsing errors). 
5. [ ] Standardize coroutine/Flow usage: specify Dispatchers, add result wrappers, and ensure structured concurrency in ViewModels and repositories. 
6. [ ] Implement a repository layer contract for AI prompt generation and saga content persistence, separating Firebase AI, Remote Config, and Room concerns. 
7. [ ] Introduce DTO ↔ Domain mappers for network/AI/DB entities to isolate frameworks from domain models. 
8. [ ] Define prompt generation guidelines and interfaces (PromptBuilder/PromptTemplate) to centralize prompts (HomePrompts, NewSagaPrompts, etc.) with localization support. 
9. [ ] Add localization/internationalization (strings.xml) for user-facing text currently embedded in prompts/UI; keep prompt templates adaptable to Locale. 
10. [ ] Implement robust JSON parsing/validation for AI responses with schema enforcement and fallbacks; log malformed outputs with sample payloads for diagnostics. 
11. [ ] Add network/offline strategy and caching policy for AI-generated artifacts (e.g., memoize prompts per session/day, cache images). 
12. [ ] Introduce feature toggles via Firebase Remote Config with typed accessors and default values; avoid direct key strings scattered in code. 
13. [ ] Create a Notification strategy wrapper (NotificationUtils): define channels, IDs, and a builder API; ensure API 26+ behavior and test coverage. 
14. [ ] Implement analytics/events instrumentation points (saga_created, prompt_generated, chapter_completed) behind an interface with no vendor lock-in. 
15. [ ] Ensure Room database has proper migrations with tests; add schema versioning docs under app/schemas and verify SagaDatabase entities and relations. 
16. [ ] Add DAO/repository unit tests with in-memory Room and coroutine test dispatchers. 
17. [ ] Add ViewModel tests for HomeViewModel and others using Turbine for Flow and MockK/Fake repositories. 
18. [ ] Add integration tests for AI prompt generation path using fakes/stubs (no real network) validating JSON contract. 
19. [ ] Add UI tests for key flows (create saga, chat input interactions) using Compose Testing or Espresso. 
20. [ ] Set up static analysis: ktlint/Detekt with a baseline, and integrate into Gradle with CI checks. 
21. [ ] Enable Android Lint rules; fix or suppress with documented rationale; ensure lint report published. 
22. [ ] Configure CI (GitHub Actions or other) to run build, tests, and static analysis on PRs; cache Gradle. 
23. [ ] Add code coverage reporting (Jacoco/Kover) and minimum thresholds for unit test modules. 
24. [ ] Introduce logging conventions (Timber or Kotlin Logger) and centralize log tags; remove printlns and stray Log calls in production code. 
25. [ ] Replace magic strings/keys (e.g., Remote Config keys like "isDebugger") with strongly-typed constants or a Config object. 
26. [ ] Review nullability and default values across models (e.g., DynamicSagaPrompt.title not empty) and enforce with non-null fields or defaults at boundaries. 
27. [ ] Add Result extensions consistently (onSuccess/onFailure). Consolidate and test extension functions like onSuccessAsync if present. 
28. [ ] Validate thread confinement in ViewModels (emit on correct dispatcher) and ensure use of StateFlow/SharedFlow aligns with UI needs. 
29. [ ] Extract prompt strings to resource templates or constant providers with tests to avoid accidental format regressions. 
30. [ ] Introduce a feature-reliable retry/backoff policy for AI calls (exponential backoff, jitter) with cancellation support. 
31. [ ] Harden JSON adapters (Kotlinx Serialization/Moshi) with explicit serializers for polymorphic types like NarrativeStep. 
32. [ ] Review and optimize database queries, add indices and @Relation where appropriate for Acts/Chapters/Saga entities. 
33. [ ] Add a data backup/export/import mechanism for sagas (JSON) with user consent, tested for schema changes. 
34. [ ] Implement a background work strategy (WorkManager) for long-running AI/image generation tasks with progress notifications. 
35. [ ] Audit permissions and privacy (internet, notifications, media) and add a privacy policy link within the app. 
36. [ ] Create developer documentation: how to run, configure Firebase (Remote Config keys), AI providers setup, and debug flags. 
37. [ ] Add a sample .env or local.properties template documenting required keys and toggles. 
38. [ ] Introduce a simple domain-driven naming convention (UseCase verbs, Repository nouns) and refactor inconsistencies. 
39. [ ] Add sealed UI state models (Loading/Success/Error/Empty) for screens and use in ViewModels. 
40. [ ] Ensure Compose previews (if using Compose) use fake data providers and are buildable without runtime dependencies. 
41. [ ] Add accessibility checks: content descriptions, dynamic font scaling, contrast; add lint rule checks if possible. 
42. [ ] Optimize build: enable configuration cache, Gradle parallelism, and dependency version catalogs (libs.versions.toml). 
43. [ ] Migrate hard-coded delays (e.g., delay(3.seconds)) to a scheduler/clock abstraction for testability. 
44. [ ] Wrap Firebase Remote Config access to avoid direct Firebase references in ViewModels; provide test fakes. 
45. [ ] Introduce error surfaces and user messaging strategy for AI failures with retry actions in UI. 
46. [ ] Create a security review: avoid logging PII or full AI responses; sanitize logs. 
47. [ ] Add proguard/r8 rules for serialization, Hilt, and Room; verify release build shrinks without runtime crashes. 
48. [ ] Validate min/target SDKs, dependency updates, and use Renovate/Dependabot for dependency maintenance. 
49. [ ] Add a changelog and versioning strategy (semantic or calendar) and release notes template. 
50. [ ] Periodically benchmark cold start and heavy operations; add simple benchmarks or tracing for AI calls.
