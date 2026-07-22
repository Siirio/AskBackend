# Unique offers flow

```mermaid
flowchart LR
    O[Business owner] --> F[Create or update unique offer]
    F --> L[Link to items, services, or branches]
    L --> A[Active offer]
    A --> R[Search ranking and card decoration]
    R --> C[Customer result card]
```

The business user sees offer lifecycle and linked item/service objects. The customer sees a discount label/effective price or a non-discount brand label; the internal score boost is never rendered.

Do not turn a unique offer into a standalone search result, a buy-box comparison, or a public trust score.
