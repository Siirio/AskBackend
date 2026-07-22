# Search flow

```mermaid
flowchart LR
    U[Customer] --> F[Frontend selects item or service scope]
    F --> Q[POST search with raw query]
    Q --> I[Query interpretation within fixed scope]
    I --> M[Meilisearch bounded candidates]
    M --> H[PostgreSQL hydration and version check]
    H --> R[Deterministic ranking and sections]
    R --> C[Exact and explicitly relaxed cards]
    M -. unavailable .-> P[PostgreSQL indexed fallback]
    P --> H
```

The customer sees the raw query context, fixed scope, catalog cards, and clearly labelled relaxed alternatives. Diagnostics are operational data, not UI content.

Do not let AI switch scope, select a business, invent availability, create requests, chats, notifications, or supplier outreach. Search only returns catalog retrieval results.
