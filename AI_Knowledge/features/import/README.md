# Product & Service Import

Excel (.xlsx) import via fastexcel streaming parser with auto-mapping engine, plus AI Autodump import for messy data (.txt, .md, .pdf). Backend owns all parsing and normalization.

## Key decisions
- Backend owns Excel parsing (fastexcel-reader). Frontend only handles file upload + preview.
- 5-step pipeline: Upload → Auto-map → User mapping → Preview → Approve → Imported.
- AutoMappingEngine: 30+ Russian/English patterns per TargetField, confidence scoring.
- IGNORE forced for stock/quantity/warehouse/availability columns (not MVP fields).
- RowNormalizer: validates NAME (required → INVALID), PRICE (parse → WARNING).
- AI Autodump: raw dump → import session → AI job → draft cards → business preview → approve → publish.
- AI creates drafts only. Business approval required before products become searchable.
- Both Excel and AI Autodump sync to search_document on publish.

## Access and managed-import lifecycle
- Business cabinets expose ordinary `.xlsx` import only.
- AI Autodump is a platform capability and publishes products only. Ordinary service create/update remains available to an assigned platform importer for service-scoped managed requests.
- Each request declares PRODUCTS, SERVICES, or BOTH. Product and service requests can coexist; overlapping scopes cannot.
- A request stays PENDING until one platform user activates it. Activation creates that user's grant and a seven-day chat; expiry revokes access and deletes the chat and files automatically.
