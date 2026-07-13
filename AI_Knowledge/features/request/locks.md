# Requests — Feature Locks

LOCKED | AUTO_REPLY does NOT count as confirmation | Only real business confirmation advances status | SupplierResponse, CustomerRequest status transitions
LOCKED | Fallback requests exist only when catalog results insufficient | ASK is search-first, not request-first | UnifiedSearchProcessor → CustomerRequest creation
LOCKED | Booking ≠ CustomerRequest | Request asks for confirmation. Booking is confirmed/pending service commitment | Booking entity, CustomerRequest entity
