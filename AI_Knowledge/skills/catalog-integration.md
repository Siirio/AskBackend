# Catalog Integration Skill

Use for catalog import, normalization, categories, attributes, availability, and catalog-backed search.

Catalog is not simple CRUD. Sellers should not recreate existing catalogs manually when they already have files. Analyze:

- source format, with first-class support for Excel and CSV;
- column mapping;
- product identity;
- duplicate handling;
- category mapping;
- attributes;
- branch/city scope;
- price and availability freshness;
- source of truth;
- search behavior;
- supplier correction workflow.

Likely sources include Excel, CSV, MoySklad, POS, e-commerce exports, CRM, and manual entry.

Catalog import should support upload, preview, column mapping, validation, normalization, correction, import history, and repeated updates.

Catalog-backed search is now a core product path. Manual request routing remains the fallback for missing, stale, or uncertain data.
