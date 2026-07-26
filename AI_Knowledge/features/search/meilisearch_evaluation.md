# Meilisearch Evaluation

Status: adopted and runtime-verified on 2026-07-16.

- Deployment image: `getmeili/meilisearch:v1.10.3`.
- Java client: `meilisearch-java` configured in `pom.xml`.
- Primary index: configurable through `MEILISEARCH_INDEX_NAME`, default `search_documents_v1`.
- Searchable attributes prioritize title, normalized title, brand, category, SKU, aliases, verified/AI summaries, business, and branch data.
- Filter and sort fields are allowlisted by the server.
- Index writes, deletes, settings, and swaps wait for task completion and propagate retryable failures.

Meilisearch is not a source of truth. PostgreSQL projections carry the document version, and hydration revalidates canonical active/version state. A missing or unhealthy Meilisearch instance activates bounded PostgreSQL full-text/trigram retrieval.

Runtime evidence on the disposable local schema returned the same exact item through both engines. Engine and fallback evidence is collected from server logs; the anonymous public response deliberately exposes no infrastructure diagnostics or exception reason.

No relevance improvement is claimed solely from adopting Meilisearch. Versioned evaluation data and the CLI runner live under `tools/search-evaluation`; measured reports must be retained with the release evidence.
