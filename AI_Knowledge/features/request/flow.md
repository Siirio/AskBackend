# Retired request flow

```mermaid
flowchart LR
    S[Customer search] --> C[Published catalog result]
    C -->|explicit contact| M[Business-wide conversation]
    R[Legacy request and supplier-response routes] -. scheduled for removal .-> X[No approved product flow]
```

The approved user flow has no fallback request or supplier broadcast. A customer contacts a matching business through an explicit conversation action.

Do not restore request creation from search, reuse retired request code for the business inbox, or treat legacy endpoint references as product approval.
