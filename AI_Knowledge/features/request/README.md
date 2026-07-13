# Fallback Requests & Supplier Responses

When catalog results are insufficient, the system auto-creates supplier checks to relevant businesses. Businesses respond via structured statuses. Requests are separate from Bookings.

## Key decisions
- Fallback requests exist only when product/service results are missing or customer wants confirmation.
- CustomerRequest life-cycle: DRAFT → CREATED → DISPATCHING → SENT → PARTIALLY_RESPONDED → COMPLETED (also: EXPIRED, CANCELLED, FAILED).
- Auto supplier check: attached to search session, NOT customer-visible chat.
- Product responses: HAS_ITEM, NO_ITEM, NEED_CLARIFICATION, HAS_ANALOG.
- Service responses: CAN_PROVIDE, CANNOT_PROVIDE, NEED_CLARIFICATION, SUGGEST_OTHER_TIME.
- Supplier response source types: AUTO_REPLY, STAFF_REPLY, BUSINESS_CONFIRMED, DATA_UPDATED, SUPPLIER_CHECK_CONFIRMED.
- AUTO_REPLY does NOT count as confirmation — only real business confirmation advances status.
- Booking is separate from CustomerRequest. Request asks for confirmation. Booking is confirmed commitment.
