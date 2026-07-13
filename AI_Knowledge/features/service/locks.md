# Service — Feature Locks

LOCKED | Service MVP is request-to-book, not automatic slot reservation | Customer sends desired time, business confirms/declines | ServiceBranchOffer, CustomerRequest, Booking
LOCKED | ServiceBranchOffer.active is the live-search toggle | No availability scoring or freshness tracking | ServiceBranchOffer, SearchDocument sync
LOCKED | Chat-first, button-for-fixation | Buttons record agreement reached in chat, not replace communication | ChatServiceImpl, SupplierResponse
LOCKED | ActivityDisplayStatus is derived at runtime, never stored | DISCUSSING/CONFIRMED/CONFIRMATION_DECLINED computed from supplierResponseStatus + confirmedStartAt | Activity UI rendering
LOCKED | Every confirmation/time change/cancellation creates system event in conversation | Visible to both customer and business in chat history | ChatServiceImpl, SupplierResponse update
LOCKED | ServiceResource is optional abstract capacity — NOT specialist accounts | No specialist login, payroll, or dedicated UI | ServiceResource entity
