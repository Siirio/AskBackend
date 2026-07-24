# Messaging — Feature Locks

LOCKED | Conversation identity is business plus customer | Branches and item/service cards are entry points, never conversation identity; authorized business members share history | ChatConversation, ChatServiceImpl, business inbox
LOCKED | Search never creates chat | Only an explicit customer contact action opens or resumes a business conversation | search/chat integration
LOCKED | Contact privacy: contactActionId pattern, never raw phone/username | HMAC for dedup, encrypted vault for storage | BusinessContact, ContactResolveResponse
LOCKED | Managed-import attachments remain scoped to their conversation | Attachment records must belong to the same conversation as the message that references them | ChatMessage, ChatAttachment
LOCKED | Platform support chat access is explicit | MANAGE_SUPPORT_CHATS permits GENERAL_SUPPORT and PLATFORM_SUPPORT inspection; MANAGED_IMPORT requires MANAGE_MANAGED_IMPORTS plus the assigned active grant | PlatformChatProcessor
