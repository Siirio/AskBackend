DROP INDEX IF EXISTS idx_search_document_ai_enrichment;

ALTER TABLE search_document
    DROP COLUMN IF EXISTS ai_enrichment_version,
    DROP COLUMN IF EXISTS ai_enrichment_available_at,
    DROP COLUMN IF EXISTS ai_enrichment_started_at,
    DROP COLUMN IF EXISTS ai_enrichment_worker_id,
    DROP COLUMN IF EXISTS ai_enrichment_attempt_count,
    DROP COLUMN IF EXISTS ai_enrichment_error,
    DROP COLUMN IF EXISTS is_ai_enrichment_dead,
    DROP COLUMN IF EXISTS is_ai_enrichment_requested;

DROP TABLE IF EXISTS search_ai_metadata;
