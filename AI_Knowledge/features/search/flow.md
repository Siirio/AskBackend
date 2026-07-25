# Search flow

## Write path

```mermaid
flowchart LR
    P[Processor creates/updates/deletes Item or Service] --> SD[SearchDocumentService upsert/delete SearchDocument in same Tx]
    P --> OE[SearchOutboxService publish event in same Tx]
    SD --> C[COMMIT — aggregate + projection + outbox event atomically]
    OE --> C
    C --> W[Async SKIP LOCKED worker picks up PENDING event]
    W --> PS[SearchProjectionService reads existing SearchDocument, checks projectionVersion]
    PS --> ID[SearchIndexDeliveryService delivers to Meilisearch in REQUIRES_NEW Tx]
    ID --> M[Meilisearch indexed]
```

Key invariant: the SearchDocument is created/updated/deleted synchronously with the canonical aggregate. The worker only reads and delivers — it never creates SearchDocuments.

## Read path

```mermaid
flowchart LR
    U[Customer] --> F[Frontend selects item or service scope]
    F --> Q[POST search with raw query]
    Q --> I[Query interpretation within fixed scope]
    I --> M[Meilisearch bounded candidates by aggregateId]
    M --> H[PostgreSQL hydration via SearchDocument]
    H --> O[Overlay dirty projections — read-your-writes]
    O --> R[Deterministic ranking and sections]
    R --> C[Exact and explicitly relaxed cards]
    M -. unavailable .-> P[PostgreSQL indexed fallback]
    P --> H
```

The customer sees the raw query context, fixed scope, Item/Service cards, and clearly labelled relaxed alternatives. Diagnostics are operational data, not UI content.

Do not let AI switch scope, select a business, invent availability, create requests, chats, notifications, or supplier outreach. Search only returns Item/Service retrieval results.
