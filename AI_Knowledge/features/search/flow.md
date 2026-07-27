# Search flow

## Mutation and delivery

```mermaid
flowchart LR
    A[Item or Service mutation] --> P[Complete PostgreSQL projection]
    P --> V[Sequence version]
    V --> O[Matching outbox event]
    O --> C[Atomic commit]
    C --> R[Prepare exact desired state]
    R --> M[Meilisearch call outside transaction]
    M --> F[Confirm current desired state]
    F -->|unchanged| I[Set indexedVersion]
    F -->|changed| Q[Requeue newest action and version]
```

## Retrieval and presentation

```mermaid
flowchart LR
    U[Customer selects ITEM or SERVICE] --> Q[Raw query plus explicit filters]
    Q --> D[Deterministic interpretation]
    D --> A[Optional AI hints]
    A --> E[Exact full-query lane]
    A --> X[Combined expanded lane]
    A --> S[Semantic vector lane]
    E --> R[Reciprocal Rank Fusion]
    X --> R
    S --> R
    R --> H[PostgreSQL hydration and dirty overlay]
    H --> B[Public Business profile hydration]
    B --> C[Compact Item or Service row]
    C -->|open row| M[Full Item or Service and Business-profile modal]
    C -->|chat| T[Explicit business conversation action]
```

Semantic failure preserves both lexical lanes. Full Meilisearch failure switches to bounded PostgreSQL lexical retrieval. AI cannot change mode, apply hard inferred constraints, select businesses, or invent availability.
