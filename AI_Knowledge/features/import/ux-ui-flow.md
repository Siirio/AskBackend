# Import — Frontend UX Expectations

## Excel import flow
1. Business selects branch → Import tab → Upload .xlsx file
2. Backend auto-maps columns → preview with confidence scores
3. Business reviews mappings: correct/override column-to-field assignments
4. Preview table: rows color-coded (green=valid, yellow=warning, red=invalid)
5. Business approves → products created as draft → review → publish
6. Published products become searchable

## AI Autodump import
1. Business uploads messy data file (.txt, .md, .pdf)
2. Backend AI processes into draft product/service cards
3. Business previews and edits draft cards
4. Business approves → cards become real product/service records
5. Only approved records become searchable

## File format restrictions
- Excel: .xlsx only (fastexcel, not Apache POI)
- AI Autodump: .txt, .md, .pdf only
- Unsupported formats rejected before upload with clear error message
- Business users see Excel only. Assigned platform importers see Excel plus TXT/MD/PDF AI Autodump.
- The business can request catalog help from onboarding or the product-import area.
