# Meilisearch Query Understanding Evaluation

Status: under investigation. We are evaluating whether Meilisearch meaningfully improves query understanding over current PostgreSQL in-memory keyword scoring.

## Current Pipeline
raw query → DeepSeek (SearchPlan JSON: categories, price range, search terms) → PostgreSQL ILIKE/tsvector keyword matching

## What Meilisearch Adds

### Already available (no AI embedder needed)
- **Typo tolerance**: auto-corrects 1-2 character errors per word. Critical for mixed-language Russian/Kazakh queries with slang
- **Synonyms**: configurable one-way + multi-word synonym maps. Covers product aliases, local brand names, category equivalents
- **Language-aware tokenization**: BCP-47 locale support, Russian stemming included
- **Customizable ranking rules**: words > typo > proximity > attribute > exactness priority chain

### With AI embedder (OpenAI/HuggingFace)
- **Semantic/hybrid search**: understands query meaning beyond exact keywords. "что-то для тренировки" can match sports nutrition products without sharing any words
- **semanticRatio knob**: 0.0 = pure keyword, 0.5 = balanced, 1.0 = pure semantic

## Recommended Architecture (if adopted)

```
raw query → DeepSeek (structured intent: categories, brands, price range, search terms)
         → Meilisearch (typo-tolerant + synonym-aware + optionally semantic retrieval)
         → PostgreSQL (hydration: business profiles, brand profiles, contact actions)
```

DeepSeek remains the intent structurer. Meilisearch replaces PostgreSQL as the document retrieval engine. PostgreSQL stays for relational hydration that search engines cannot do.

## Expected Improvements
1. Fewer unnecessary fallback requests — typos and slang no longer cause empty search results
2. Better match surface — synonyms expand what counts as a match without manual config
3. Future-proof for semantic search — hybrid search is configuration, not architecture change

## Caveats
- AI-powered semantic search requires external embedder API (ongoing cost)
- Synonyms require ongoing curation per business vertical
- Does not replace PostgreSQL — it replaces the scoring layer only
