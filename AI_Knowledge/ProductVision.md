# ASK Backend — Product Vision

## What we're building
ASK is a local search platform for Items and Services across businesses. It is an **intent layer** that routes qualified demand to brands without commoditizing them. Customers select `ITEM` or `SERVICE`, search in natural language, and receive Item or Service rows ranked by intent match rather than price or rating. Each row carries public Business profile context and may explicitly open the shared customer-to-business conversation.

## Core users
- **Customers** — search products/services, contact businesses, manage profile with optional preferences (sizes, style, budget, city, favorite brands)
- **Business Owners/Staff** — register businesses, optionally add branches, manage business-owned Items and Services, manage shared customer conversations, view Unique Offers
- **Platform Admins** — moderate businesses, approve public business candidates from external discovery

## Core constraints
- Search sorts by `intent_match` — never price ascending. Price is a filter factor, not the ranking king.
- No buy-box logic. Never collapse different brands into one SKU comparison.
- No public rating scores. Visible signals are badges (data freshness, confirmation speed, card quality). Internal ranking signals are separate.
- All actions must be traceable to an authenticated user.
- AI (DeepSeek) structures queries — it never selects businesses or invents availability.
- The frontend selects `ITEM` or `SERVICE`; AI cannot override that mode.
- Business and UniqueOffer are context for Item/Service results, never standalone search modes or cards.
- Search is Item/Service retrieval only and never creates requests, supplier outreach, notifications, or chats.
- Meilisearch is the retrieval engine with typo tolerance, Russian stemming, and synonyms. PostgreSQL is source of truth and hydration layer.
- Never invent stock, delivery, logistics, schedules, or availability without supplier input or trusted integration data.

## What we are NOT building
- A marketplace with price comparison and commodity-style listings
- A general-purpose payment gateway
- A booking/reservation platform (scheduled services are scoped to what businesses explicitly offer)
- A social network or review platform
- An inventory management or ERP system
