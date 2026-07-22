# Platform flow

```mermaid
flowchart LR
    SA[Configured super-admin bootstrap] --> PM[Platform membership]
    PM -->|permission check| U[Platform user administration]
    PM -->|active managed-import grant plus permission| CA[Catalog import access]
    PM -->|managed-import permission| MC[Managed-import conversations]
    PM -->|moderation permission| CR[Catalog review and moderation]
```

Platform users see only the cabinet surface enabled by explicit permissions. Catalog editing additionally needs an active managed-import grant for the target business; role alone is never enough.

Do not infer permission from platform role, give platform staff access to general customer-business chat, or allow AI enrichment without the dedicated platform permission.
