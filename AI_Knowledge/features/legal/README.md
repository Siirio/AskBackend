# Legal

Legal stores versioned active documents and immutable user acceptance records. Authenticated sessions expose `pendingLegalDocuments`; the customer application blocks protected routes while that list is non-empty and refreshes the session after acceptance.

The active Kazakhstan baseline is `USER_TERMS` and `PRIVACY_POLICY`, version `2026-07-18`. Seller onboarding separately accepts `SELLER_TERMS` and `PERSONAL_DATA_CONSENT`.
