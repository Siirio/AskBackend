# Messaging — REST API Contracts

## Customer Chat
| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| POST | /api/v1/chat/start | CUSTOMER | Start or resume the customer-to-business conversation |
| POST | /api/v1/chat/conversations/{conversationId}/messages | CUSTOMER | Send message |
| GET | /api/v1/chat/conversations/{conversationId}/messages | CUSTOMER | Get messages |
| POST | /api/v1/chat/conversations/{conversationId}/read | CUSTOMER | Mark as read |

## Business Chat
| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| GET | /api/v1/business-admin/chats | BUSINESS | List conversations |
| POST | /api/v1/business-admin/chats/{conversationId}/messages | BUSINESS | Send message |
| GET | /api/v1/business-admin/chats/{conversationId}/messages | BUSINESS | Get messages |
| POST | /api/v1/business-admin/chats/{conversationId}/read | BUSINESS | Mark as read |

## Platform Chat
| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| GET | /api/v1/platform/chat/conversations | MANAGE_MANAGED_IMPORTS or MANAGE_SUPPORT_CHATS | List conversations allowed by type and grant |
| GET | /api/v1/platform/chat/conversations/{conversationId}/messages | same | Get messages |
| POST | /api/v1/platform/chat/conversations/{conversationId}/messages | same | Send message as PLATFORM |
| POST | /api/v1/platform/chat/conversations/{conversationId}/read | same | Mark read (resets businessUnreadCount) |
| POST | /api/v1/platform/chat/conversations/{conversationId}/close | MANAGE_SUPPORT_CHATS | Close conversation |

`MANAGE_SUPPORT_CHATS` authorizes `GENERAL_SUPPORT` and `PLATFORM_SUPPORT`. `MANAGED_IMPORT` requires `MANAGE_MANAGED_IMPORTS` plus the assigned, unexpired grant for that conversation's Business.

## Chat Files
| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| POST | /api/v1/chat/upload?conversationId={id} | Participant | Upload attachment (multipart `file`) → `{ url }` |
| GET | /api/v1/chat/files/{storedName} | Participant | Download attachment |

- Participant = conversation customer or member of conversation business. Platform attachment access for MANAGED_IMPORT remains limited to the assigned active grant and MANAGE_MANAGED_IMPORTS.
- Upload validation: non-empty, size ≤ `ask.chat.max-file-size`, extension + declared content-type whitelists (`ask.chat.allowed-extensions`, `ask.chat.allowed-content-types`), magic-byte check in storage.
- Stored name is a random UUID + extension; original name only returned via Content-Disposition (UTF-8 encoded builder, no header injection).
- Download responds with `X-Content-Type-Options: nosniff` + `Content-Disposition: attachment`.
- Conversation deletion removes database rows transactionally and deletes physical files only after commit.

## ChatConversation Model
- conversationId, businessId, customerId, customerName, subject
- conversationType: GENERAL_SUPPORT (default) | PLATFORM_SUPPORT | MANAGED_IMPORT
- status: PENDING → IN_CHAT (first PLATFORM message) → CLOSED
- customerUnreadCount, businessUnreadCount (int, default 0)
- lastMessageAt, createdAt

One conversation is unique per `businessId` and `customerId`. A item or service card resolves its business and opens or resumes that conversation. Branches are access and entry points, not conversation identity. Any authorized business member reads the same business-side inbox state.

New conversations are persisted with `PENDING` status and zero unread counters before the first message.

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
