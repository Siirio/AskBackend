# Platform flow

```mermaid
flowchart LR
    SA[Configured super-admin bootstrap] --> PM[Platform membership]
    PM -->|permission check| U[Platform user administration]
    PM -->|assigned active managed-import grant| CA[Business-scoped Item/Service import access]
    PM -->|managed-import permission| MC[Managed-import conversations]
    PM -->|support-chat permission| SC[Customer-business and platform-support conversations]
    PM -->|moderation permission| CR[Generic content reports and item moderation]
```

Platform users see only the cabinet surface enabled by explicit permissions. Item/Service editing is the exception: activation creates a business-scoped, seven-day entitlement for the assigned platform member, and no global edit flag opens other businesses.

Do not infer business catalog access from platform role, extend a managed-import grant to another business, or allow managed-import chat access after grant expiry.
