# Legal REST API Contracts

| Method | Path | Auth | Purpose |
| --- | --- | --- | --- |
| GET | `/api/v1/legal/documents?countryCode=KZ&locale=ru` | No | List active document codes, versions, localized public routes, and effective timestamps |
| POST | `/api/v1/legal/acceptances` | Bearer | Accept active document versions from the account consent gate |
| POST | `/api/v1/legal/registration-acceptances` | Bearer | Accept active document versions during registration/onboarding |

Acceptance request: `documentCodes`, `countryCode`, and `locale`. The server rejects codes without an active document and persists the exact active version. `AuthSessionResponse.pendingLegalDocuments` is the gate source; under the current owner rule it contains the active customer baseline only while the user has no acceptance record at all.
