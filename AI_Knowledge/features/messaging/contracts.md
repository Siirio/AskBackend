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

## ChatConversation Model
- conversationId, businessId, customerId, customerName, subject
- customerUnreadCount (int, default 0)
- businessUnreadCount (int, default 0)
- lastMessageAt, createdAt

## ChatMessage Model
- messageId, conversationId, senderType (CUSTOMER/BUSINESS)
- text (text only — no attachments)
- readAt, createdAt

## Unread Count Rules
- Customer sends message → increments businessUnreadCount
- Business sends message → increments customerUnreadCount
- markRead(resets appropriate counter to 0 based on readerType)
