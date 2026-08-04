# Search — Frontend UX Expectations

1. Customer selects `ITEM` or `SERVICE` and enters a natural-language query.
2. Results render as compact rows, never standalone Business or UniqueOffer cards.
3. The left side shows the Business logo and the most relevant short Item/Service information.
4. The right side shows price when known; the far-right chat button opens/resumes the Business conversation.
5. Clicking the row outside the chat action opens a modal with the full Item/Service description and public Business profile.

The Business profile area may show business name, logo, cover, public description, phone, email, website, Instagram, and Telegram. It never shows legal identifiers, private account credentials, two-factor-authentication state, moderation facts, or other private account data.

The personal login account and Business profile are distinct. Email/password/2FA belong to the person’s account; brand and public contact links belong to the Business profile.

Branch address, city, and distance appear only when canonical branch/location data exists. Distance changes ranking only when the user explicitly selects distance sorting.

Filter & Sort operates on the full eligible catalogue before pagination. Every applied sort or filter issues a page-zero search, then bounded pages append through infinite scroll until `hasNext=false`. Price, company, city, radius, and visible map-area filters are server-side hard filters. The company multi-select uses `companyFacets` from the search response; these choices and counts cover the full current query with all active filters except `businessIds` and are never derived from loaded cards.
