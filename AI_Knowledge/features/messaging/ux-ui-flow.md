# Messaging — Frontend UX Expectations

## Chat access
- Always available from: product card, service card, request detail, booking, business page
- Chat opens scoped to the concrete context entity
- If entity is visible and user authenticated → chat can be opened
- WhatsApp, Telegram, phone, email, map action, and Ask chat are separate contact actions

## Chat UI behavior
- Customer sees chat when: they initiated conversation from context OR business messaged them
- "Chats" tab appears in customer UI only after real chat interaction (not auto supplier checks)
- Business sees conversations in Activity tab
- Unread badges per conversation
- System messages (confirmations, time changes) rendered with distinct style from user messages

## Contact actions
- contactActionId pattern: frontend receives opaque ID, backend resolves to redirect/deep-link/display value
- Action types: REDIRECT, DISPLAY, DEEP_LINK, CHAT
- Never raw phone/username exposed to frontend
