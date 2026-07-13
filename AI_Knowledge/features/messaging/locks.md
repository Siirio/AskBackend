# Messaging — Feature Locks

LOCKED | Chat is contextual, not a standalone tab | Always opened from product/service/request/booking/business context | ChatController, frontend navigation
LOCKED | Auto supplier check must NOT create customer-visible chat or unread notification | Customer "Chats" tab appears only after real interaction | SupplierResponse, ChatServiceImpl
LOCKED | Contact privacy: contactActionId pattern, never raw phone/username | HMAC for dedup, encrypted vault for storage | BusinessContact, ContactResolveResponse
LOCKED | Text-only messages in MVP | No attachments/files. Removed attachmentUrl in V8 restructuring | ChatMessage entity
