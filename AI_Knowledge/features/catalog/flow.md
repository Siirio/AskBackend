# Catalog flow

```mermaid
flowchart LR
    M[Authorized business member] --> F[Item form]
    F --> I[Item and branch offer]
    I --> O[Search outbox event]
    O --> P[Canonical search projection]
    P --> X[Meilisearch index]
    X --> S[Customer search card]
    S --> D[Item detail or explicit contact]
```

The business user sees item CRUD and enabled/disabled state. The customer sees only an enabled item from an enabled branch, with known catalog facts and price when known.

Do not expose disabled/deleted items in search, add stock or availability claims not present in canonical data, or let AI mutate canonical catalog facts without the approved import workflow.
