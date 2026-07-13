# Chat / Messaging

Universal text-only chat with unread counts. Chat is always available from concrete context (product, service, request, booking, business page) but never a standalone bottom navigation tab.

## Key decisions
- Simplified text-only: ChatConversation + ChatMessage. No attachments, no statuses, no source/searchQuery.
- Unread tracking: customerUnreadCount + businessUnreadCount counters per conversation.
- Chat is contextual: opened from product/service/request/booking/business screens via contact actions.
- Chat is NOT a standalone tab. Customer "Chats" tab appears only after real chat interaction.
- Auto supplier check must NOT create customer-visible chat or unread notification.
- System events (confirmations, time changes, cancellations) appear as system messages in conversation.
- Open for brand extension: links, quick replies, FAQs, AI assistant allowed. Cannot break user flow, spam, or change system statuses.
