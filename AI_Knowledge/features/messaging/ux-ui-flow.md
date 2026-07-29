# Messaging — Frontend UX Expectations

## Chat access
- Available from: item card, service card, and business page
- A card opens the existing customer-to-business conversation or creates it after explicit customer action
- The conversation is shared across all branches of the same business
- If entity is visible and user authenticated → chat can be opened
- WhatsApp, Telegram, phone, email, map action, and Ask chat are separate contact actions

## Chat UI behavior
- Customer sees chat when: they initiated a business conversation OR business messaged them
- "Chats" tab appears in customer UI only after real chat interaction
- Business sees conversations in Activity tab
- Unread badges per conversation
- Own messages show sent and read states derived from `readAt`; customer, business, support, and managed-import drawers use the same receipt semantics.
- System messages (confirmations, time changes) rendered with distinct style from user messages

## Contact actions
- contactActionId pattern: frontend receives opaque ID, backend resolves to redirect/deep-link/display value
- Action types: REDIRECT, DISPLAY, DEEP_LINK, CHAT
- Never raw phone/username exposed to frontend
- Managed-import participants can exchange text, external contact links, and uploaded files during the seven-day access window.
- A pending managed-import request without a conversation ID renders a request-sent confirmation when its chat is opened.
