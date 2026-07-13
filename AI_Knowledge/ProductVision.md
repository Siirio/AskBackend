# ASK Backend — Product Vision

## What we're building
ASK is a local search platform for products and services across city businesses. It is an **intent layer** that routes qualified demand to brands without commoditizing them. Customers search for what they need (in any language, slang, or typos); the platform returns known catalog results ranked by intent match, not by price or rating. When catalog results are insufficient, fallback requests go to businesses for confirmation.

## Core users
- **Customers** — search products/services, contact businesses, submit fallback requests, manage profile with optional preferences (sizes, style, budget, city, favorite brands)
- **Business Owners/Staff** — register businesses/branches, manage product/service catalogs, respond to customer requests, manage chat, view Unique Offers
- **Platform Admins** — moderate businesses, approve public business candidates from external discovery

## Core constraints
- Search sorts by `intent_match` — never price ascending. Price is a filter factor, not the ranking king.
- No buy-box logic. Never collapse different brands into one SKU comparison.
- No public rating scores. Visible signals are badges (data freshness, confirmation speed, card quality). Internal ranking signals are separate.
- Auto-reply does NOT count as confirmation. Only real business confirmation advances status.
- All actions must be traceable to an authenticated user.
- AI (DeepSeek) structures queries — it never selects businesses or invents availability.
- PostgreSQL is the source of truth and search engine. Meilisearch is deferred.
- Never invent stock, delivery, logistics, schedules, or availability without supplier input or trusted integration data.

## What we are NOT building
- A marketplace with price comparison and commodity-style listings
- A general-purpose payment gateway
- A booking/reservation platform (scheduled services are scoped to what businesses explicitly offer)
- A social network or review platform
- An inventory management or ERP system
