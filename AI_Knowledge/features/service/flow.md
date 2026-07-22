# Service flow

```mermaid
flowchart LR
    M[Authorized business member] --> F[Service form]
    F --> SO[Service offering]
    SO --> BO[Service branch offer]
    BO --> O[Search outbox event]
    O --> S[Customer service search card]
    S -->|explicit action| C[Business-wide conversation]
    C --> D[Customer and business discuss details]
```

The customer sees service name, business/branch, known price or price-from, and known duration. Contact is explicit and opens/resumes the business-wide chat.

Do not model services as items, create a booking/calendar record from search, or present schedule text as a reservable slot system.
