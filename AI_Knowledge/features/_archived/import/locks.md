# Import — Feature Locks

LOCKED | Backend owns all Excel parsing and normalization | Frontend only handles upload + preview display | ExcelParser, AutoMappingEngine, RowNormalizer
LOCKED | AI Autodump creates drafts only — business approval required before publish | AI must not publish live records or invent availability truth | AutodumpImportSession
LOCKED | IGNORE forced for stock/quantity/warehouse/availability columns | These fields don't exist in MVP | AutoMappingEngine, TargetField
LOCKED | Managed-import catalog access is business-scoped and activation-driven | The assigned platform member receives the requested ITEM/SERVICE/BOTH access immediately for seven days; no global catalog-edit permission substitutes for that grant | managed import activation and catalog processors
