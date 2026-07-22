# Chat / Messaging

Business-wide customer chat with unread counts. Product and service cards may open chat, but the conversation is shared across all branches of the same business.

## Key decisions
- Simplified text-only: ChatConversation + ChatMessage. No attachments, no statuses, no source/searchQuery.
- Unread tracking: customerUnreadCount + businessUnreadCount counters per conversation.
- One customer and business share one durable conversation. Branches and offers are entry points, not conversation identity.
- Every authorized business member can see the shared customer history.
- Chat is NOT a standalone tab. Customer "Chats" tab appears only after real chat interaction.
- Search never creates a chat; only an explicit customer contact action does.
- System events (confirmations, time changes, cancellations) appear as system messages in conversation.
- Open for brand extension: links, quick replies, FAQs, AI assistant allowed. Cannot break user flow, spam, or change system statuses.
- Managed-import chat is a separate platform workflow and may use validated attachments during the grant window.
