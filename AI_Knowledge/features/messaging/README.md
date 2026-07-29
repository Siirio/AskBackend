# Chat / Messaging

Business-wide customer chat with unread counts. Product and service cards may open chat, but the conversation is shared across all branches of the same business.

## Key decisions
- Platform chat moderation is organized by `PLATFORM_SUPPORT`, `MANAGED_IMPORT`, and `GENERAL_SUPPORT`. Only unresolved message violations create yellow `REVIEW` or red `CRITICAL` moderation counters; ordinary unread messages do not.
- ChatConversation + ChatMessage support text, conversation-scoped attachments, and per-message `readAt` receipts. They do not expose a separate mutable delivery-status enum or searchQuery.
- Unread tracking: customerUnreadCount + businessUnreadCount counters per conversation.
- One customer and business share one durable conversation. Branches and offers are entry points, not conversation identity.
- Every authorized business member can see the shared customer history.
- Chat is NOT a standalone tab. Customer "Chats" tab appears only after real chat interaction.
- Search never creates a chat; only an explicit customer contact action does.
- System events (confirmations, time changes, cancellations) appear as system messages in conversation.
- Open for brand extension: links, quick replies, FAQs, AI assistant allowed. Cannot break user flow, spam, or change system statuses.
- Managed-import chat is a separate platform workflow and may use validated attachments during the grant window.
- Before a managed-import conversation is assigned, opening its chat shows a durable request-sent confirmation instead of an empty conversation.
