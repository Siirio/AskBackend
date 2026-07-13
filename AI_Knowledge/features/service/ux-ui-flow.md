# Service — Frontend UX Expectations

## Service result cards (customer-facing)
- Service image or business image
- Service name + business name + branch
- Approximate price or "price from"
- Approximate duration when known
- District or distance
- Desired-time request action
- Actions: request booking, write

## Service booking flow
1. Customer finds service → chooses desired time → sends request
2. Request appears in business Activity tab
3. Business responds in chat: confirm, decline, suggest other time, ask clarifying question
4. When agreement reached in chat → business clicks "CAN_PROVIDE" + sets confirmedStartAt/EndAt
5. System event in conversation visible to both sides
6. Booking record created for confirmed appointments

## Three-tier maturity model
- Level 1 (MVP current): Request-to-book with desired time. Chat-driven confirmation.
- Level 2 (current): Minimal confirmed appointment tracking with overlap check.
- Level 3 (deferred): Full calendar system with masters, resources, schedules.

## Service management (business cabinet)
- Services tab: list with name, category, price, duration, status (active/inactive)
- Actions: edit, activate, deactivate, delete
- Schedule text: free-form display text (e.g., "Пн-Пт 9:00-18:00"), not slot blocking
