# Unified Search

ASK searches either `ITEM` or `SERVICE`; the customer selects the mode and AI cannot change it. PostgreSQL is canonical. Each searchable mutation writes the complete `SearchDocument` and outbox state transactionally, and the delivery worker converges Meilisearch to that version without holding a database transaction during network calls.

Meilisearch performs hybrid keyword/semantic retrieval. Explicit filters and requested sorting are applied in Meilisearch across the full eligible catalogue before pagination. The API returns one bounded page at a time; clients request page 0, 1, 2 and so on until `hasNext=false`. There is no fixed 200-result candidate window and Meilisearch pagination is configured to permit the entire indexed result set.

Supported global sorts are relevance, distance, ascending price, descending price, and active Unique Offers first. Unique Offers decorate linked Item/Service results and never become standalone cards.

Each card contains the Item/Service identity, business presentation, optional branch context, up to three ordered images, and ordered purchase destinations. Match reasons remain response metadata and are not displayed in the customer UI. Search-card `openingSummary` was removed because it was never populated; branch-management responses retain their computed opening summary.

After deploying a change to indexed fields or index settings, run the existing full rebuild with `ask.search.reindex.on-startup=true` for one startup so older Meilisearch documents receive the current business, branch, and `_geo` fields.

Desktop hover preview and mobile detail presentation are frontend behavior. Search itself creates no chat, request, notification, or supplier outreach.
