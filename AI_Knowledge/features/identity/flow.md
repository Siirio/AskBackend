# Identity flow

```mermaid
flowchart LR
    U[User] --> F[Frontend entry]
    F -->|customer or business registration| A[Auth API]
    A --> C[Challenge and verification code]
    C -->|verified| R[Role and legal acceptance]
    R --> S[Revocable server session]
    S --> J[JWT response]
    J --> V[Customer search or business cabinet]
    O[Google callback] --> S
    W[Owner creates staff] --> T[Temporary-password session]
    T --> P[Forced password change]
    P --> S
```

The user sees a customer, business-owner, or staff entry path. The frontend receives a Bearer JWT and role/start-route context; it never receives a password hash or verification code.

Do not let staff self-register, treat an OAuth bridge cookie as a permanent session, or accept legal consent before the required verification and role choice.
