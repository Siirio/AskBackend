# Business flow

```mermaid
flowchart LR
    O[Business owner] --> B[Business]
    B --> BC[Flat BUSINESS category]
    B --> BP[Business profile]
    B --> BR[Optional branches]
    B --> M[Business memberships]
    O -->|creates| I[Invitation or staff member]
    I -->|accepted or activated| M
    M -->|authorized access| C[Business cabinet]
    C --> P[Business-owned Items and Services]
    C --> A[Activity conversations]
```

The customer sees the public business, optional branch, Item, and Service presentation through search cards. Owners and staff see only the cabinet surfaces allowed by membership and branch access.

Do not treat a branch as the legal root, create another business for a staff member, or require a branch before creating an Item or Service.
