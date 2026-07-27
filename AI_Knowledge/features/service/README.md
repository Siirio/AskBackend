# Services

Services owned by a Business, optional branch-specific facts, and chat-first customer contact. Search does not create booking or request records.

## Key decisions
- Services are NOT products. Scheduled service logic must stay explicit.
- ServiceOffering: business-owned service definition (name, description, category, status).
- A Service can be created before any branch exists.
- A Service has one flat `SERVICE` category selected from suggestions or explicitly created by a user.
- A later branch association may hold location-specific price, schedule, or visibility.
- Active Services publish immediately and business lists order new Services first.
- Semantic aliases, controlled concepts, use cases, summaries, and embeddings are derived into `SearchDocument`; they are never stored as canonical Service attributes.
- ON_DEMAND: works without resources/schedules/windows.
- SCHEDULED: may use resources, schedules, windows, and booking.
- ServiceResource: optional abstract capacity — NOT specialist accounts.
- Three-level time model: requestedStartAt → proposedStartAt → confirmedStartAt/EndAt.
- ActivityDisplayStatus derived at runtime (DISCUSSING/CONFIRMED/CONFIRMATION_DECLINED), never stored.
- Every confirmation/change/cancellation creates system event in conversation.
- Chat-first: customer explicitly opens the business-wide conversation from a service offer; search does not create booking or request records.
