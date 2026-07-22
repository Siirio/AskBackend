# ASK Backend — Product Vision

## What we're building
ASK is a local search platform for products and services across city businesses. It is an **intent layer** that routes qualified demand to brands without commoditizing them. Customers select goods or services, search in natural language, and receive known catalog results ranked by intent match rather than price or rating. A customer may explicitly start a conversation with a matching business.

## Core users
- **Customers** — search products/services, contact businesses, manage profile with optional preferences (sizes, style, budget, city, favorite brands)
- **Business Owners/Staff** — register businesses/branches, manage item/service catalogs, manage shared customer conversations, view Unique Offers
- **Platform Admins** — moderate businesses, approve public business candidates from external discovery

## Core constraints
- Search sorts by `intent_match` — never price ascending. Price is a filter factor, not the ranking king.
- No buy-box logic. Never collapse different brands into one SKU comparison.
- No public rating scores. Visible signals are badges (data freshness, confirmation speed, card quality). Internal ranking signals are separate.
- All actions must be traceable to an authenticated user.
- AI (DeepSeek) structures queries — it never selects businesses or invents availability.
- The frontend selects goods or services; AI cannot override that scope.
- Search is catalog-only and never creates requests, supplier outreach, notifications, or chats.
- Meilisearch is the retrieval engine with typo tolerance, Russian stemming, and synonyms. PostgreSQL is source of truth and hydration layer.
- Never invent stock, delivery, logistics, schedules, or availability without supplier input or trusted integration data.

## What we are NOT building
- A marketplace with price comparison and commodity-style listings
- A general-purpose payment gateway
- A booking/reservation platform (scheduled services are scoped to what businesses explicitly offer)
- A social network or review platform
- An inventory management or ERP system
