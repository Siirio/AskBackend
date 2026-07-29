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
    S --> Q[Request password or 2FA change]
    Q --> E[Purpose-bound email challenge]
    E -->|verified owner and purpose| X[Apply security change]
    X --> S
```

The user sees a customer, business-owner, or staff entry path. The frontend receives a Bearer JWT and role/start-route context; it never receives a password hash or verification code.

An authenticated password or two-factor change is never applied by its request call. The request stages the intent and sends a code; confirmation validates the authenticated owner, purpose, code, and staged payload before applying it. Password confirmation revokes other sessions but preserves the session that performed the change.

Do not let staff self-register, treat an OAuth bridge cookie as a permanent session, or accept legal consent before the required verification and role choice.
