# Messaging — Feature Locks

LOCKED | Conversation identity is business plus customer | Branches and item/service cards are entry points, never conversation identity; authorized business members share history | ChatConversation, ChatServiceImpl, business inbox
LOCKED | Search never creates chat | Only an explicit customer contact action opens or resumes a business conversation | search/chat integration
LOCKED | Contact privacy: contactActionId pattern, never raw phone/username | HMAC for dedup, encrypted vault for storage | BusinessContact, ContactResolveResponse
LOCKED | Managed-import attachments remain scoped to their conversation | Attachment records must belong to the same conversation as the message that references them | ChatMessage, ChatAttachment
