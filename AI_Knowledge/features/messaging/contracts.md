# Messaging — REST API Contracts

## Customer Chat
| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| POST | /api/v1/chat/start | CUSTOMER | Start conversation from context |
| POST | /api/v1/chat/{conversationId}/messages | CUSTOMER | Send message |
| GET | /api/v1/chat/{conversationId}/messages | CUSTOMER | Get messages |
| POST | /api/v1/chat/{conversationId}/read | CUSTOMER | Mark as read |

## Business Chat
| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| GET | /api/v1/business-admin/chats | BUSINESS | List conversations |
| POST | /api/v1/business-admin/chats/{conversationId}/messages | BUSINESS | Send message |
| GET | /api/v1/business-admin/chats/{conversationId}/messages | BUSINESS | Get messages |
| POST | /api/v1/business-admin/chats/{conversationId}/read | BUSINESS | Mark as read |

## Platform Chat (MANAGED_IMPORT conversations only)
| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| GET | /api/v1/platform/chat/conversations | MANAGE_MANAGED_IMPORTS or MANAGE_SUPPORT_CHATS | List MANAGED_IMPORT conversations |
| GET | /api/v1/platform/chat/conversations/{conversationId}/messages | same | Get messages |
| POST | /api/v1/platform/chat/conversations/{conversationId}/messages | same | Send message as PLATFORM |
| POST | /api/v1/platform/chat/conversations/{conversationId}/read | same | Mark read (resets businessUnreadCount) |
| POST | /api/v1/platform/chat/conversations/{conversationId}/close | MANAGE_SUPPORT_CHATS | Close conversation |

Platform endpoints reject GENERAL_SUPPORT conversations (403 ACCESS_DENIED) — platform staff must not read private customer↔business chats.

## Chat Files
| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| POST | /api/v1/chat/upload?conversationId={id} | Participant | Upload attachment (multipart `file`) → `{ url }` |
| GET | /api/v1/chat/files/{storedName} | Participant | Download attachment |

- Participant = conversation customer or member of conversation business. Platform access is limited to MANAGED_IMPORT conversations, an active grant, and MANAGE_MANAGED_IMPORTS or MANAGE_SUPPORT_CHATS.
- Upload validation: non-empty, size ≤ `ask.chat.max-file-size`, extension + declared content-type whitelists (`ask.chat.allowed-extensions`, `ask.chat.allowed-content-types`), magic-byte check in storage.
- Stored name is a random UUID + extension; original name only returned via Content-Disposition (UTF-8 encoded builder, no header injection).
- Download responds with `X-Content-Type-Options: nosniff` + `Content-Disposition: attachment`.
- Conversation deletion removes database rows transactionally and deletes physical files only after commit.

## ChatConversation Model
- conversationId, businessId, customerId, customerName, subject
- conversationType: GENERAL_SUPPORT (default) | MANAGED_IMPORT
- status: PENDING → IN_CHAT (first PLATFORM message) → CLOSED
- customerUnreadCount, businessUnreadCount (int, default 0)
- lastMessageAt, createdAt

## ChatMessage Model
- messageId, conversationId, senderType (CUSTOMER/BUSINESS/PLATFORM)
- text, optional attachmentUrl; uploaded attachments are registered separately and must belong to the same conversation
- readAt, createdAt

## Unread Count Rules
- CUSTOMER message → increments businessUnreadCount
- BUSINESS/PLATFORM message → increments customerUnreadCount
- markRead resets the reader-side counter; PLATFORM reads as the "business side" of MANAGED_IMPORT conversations
## Customer Support

| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| POST | /api/v1/chat/support | Bearer | Return the existing permanent `PLATFORM_SUPPORT` conversation or create it. Optional `businessId` opens business-to-platform context after membership validation |
