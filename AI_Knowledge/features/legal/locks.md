# Legal Locks

LOCKED | Consent gating is driven by `AuthSessionResponse.pendingLegalDocuments` and active server document versions | The client must not guess acceptance state or hardcode the current version | LegalService, SessionCapabilitiesProcessor, AuthSessionResponse
LOCKED | Each acceptance records the exact active document version, locale, country, channel, and timestamp | Later document revisions must remain distinguishable from prior consent | LegalAcceptance
LOCKED | A failed acceptance keeps the protected-route gate active until session restore proves acceptance | Best-effort writes must self-heal after network errors | LegalConsentGate, legal acceptance endpoints
