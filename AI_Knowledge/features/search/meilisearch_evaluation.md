# Meilisearch Evaluation

Status: adopted and runtime-verified on 2026-07-16.

- Deployment image: `getmeili/meilisearch:v1.10.3`.
- Java client: `meilisearch-java` configured in `pom.xml`.
- Primary index: configurable through `MEILISEARCH_INDEX_NAME`, default `search_documents_v1`.
- Searchable attributes prioritize title, normalized title, brand, category, aliases, controlled concepts, use cases, semantic/verified/AI summaries, embedding text, business, and branch data.
- The `ask_multilingual` HuggingFace embedder uses `sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2` by default and derives vectors from `embeddingText`.
- Public retrieval runs raw lexical, expanded lexical, and pure semantic lanes and fuses their ranks. Semantic setup or query failure degrades to lexical retrieval.
- Filter and sort fields are allowlisted by the server.
- Index writes, deletes, settings, and swaps wait for task completion and propagate retryable failures.

Meilisearch is not a source of truth. PostgreSQL projections carry the document version, and hydration revalidates canonical active/version state. A missing or unhealthy Meilisearch instance activates bounded PostgreSQL full-text/trigram retrieval.

Runtime evidence on the disposable local schema returned the same exact item through both engines. Engine and fallback evidence is collected from server logs; the anonymous public response deliberately exposes no infrastructure diagnostics or exception reason.

No relevance improvement is claimed without measurement. Dataset v2 covers typo-to-intent, broad activities, Russian, Kazakh, and mixed-language queries. Reports include Recall@10, MRR, Precision@3, semantic miss rate, wrong-category rate, city-filter violation rate, and zero-result correctness. Versioned evaluation data and the CLI runner live under `tools/search-evaluation`; measured reports must be retained with the release evidence.
