# Product & Service Import

Excel (.xlsx) import via fastexcel streaming parser with auto-mapping engine, plus AI Autodump import for messy data (.txt, .md, .pdf). Backend owns all parsing and normalization.

## Key decisions
- Backend owns Excel parsing (fastexcel-reader). Frontend only handles file upload + preview.
- 5-step pipeline: Upload → Auto-map → User mapping → Preview → Approve → Imported.
- AutoMappingEngine: 30+ Russian/English patterns per TargetField, confidence scoring.
- IGNORE forced for stock/quantity/warehouse/availability columns (not MVP fields).
- RowNormalizer: validates NAME (required → INVALID), PRICE (parse → WARNING).
- AI Autodump: raw dump → import session → AI job → item or service draft cards → platform/business review → approve → publish.
- AI creates drafts only. Business approval required before products become searchable.
- Both Excel and AI Autodump sync to search_document on publish.

## Access and managed-import lifecycle
- Business cabinets expose ordinary `.xlsx` import only.
- AI Autodump is a platform capability for both products and services. It is available only to authorized platform members during the assigned managed-import window.
- Each request uses the entity-backed `BusinessScope`: `ITEM`, `SERVICE`, or `BOTH`. Item and service requests can coexist; overlapping scopes cannot.
- A request stays PENDING until one platform user activates it. Activation creates that user's grant and a seven-day chat; expiry revokes access and deletes the chat and files automatically.
