# Requests — Frontend UX Expectations

## Customer side
- When catalog results insufficient → auto supplier check created (visible as "Подходящие магазины" tab)
- Customer sees supplier response statuses in search results
- Customer can create manual fallback request for missing items
- Request detail: status, sent businesses, responses received

## Business side (Activity tab)
- Incoming requests appear in Activity feed
- Product requests: respond HAS_ITEM / NO_ITEM / NEED_CLARIFICATION / HAS_ANALOG
- Service requests: respond CAN_PROVIDE / CANNOT_PROVIDE / NEED_CLARIFICATION / SUGGEST_OTHER_TIME
- Response updates same row (no duplicates)
- Confirming service with time → booking created → system event in chat
- Business sees source type: AUTO_REPLY vs real customer inquiry

## Activity display (business cabinet)
- Activity items grouped by status: new, in discussion, confirmed, declined
- Each item shows: customer name, request type, time, current status
- Click opens detail with chat context
