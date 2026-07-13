# Import — Feature Locks

LOCKED | Backend owns all Excel parsing and normalization | Frontend only handles upload + preview display | ExcelParser, AutoMappingEngine, RowNormalizer
LOCKED | AI Autodump creates drafts only — business approval required before publish | AI must not publish live records or invent availability truth | AutodumpImportSession
LOCKED | IGNORE forced for stock/quantity/warehouse/availability columns | These fields don't exist in MVP | AutoMappingEngine, TargetField
