-- =============================================================================
-- V4: Monotonic search projection versioning
-- =============================================================================

CREATE SEQUENCE IF NOT EXISTS search_projection_version_seq;

ALTER TABLE search_document ADD COLUMN IF NOT EXISTS projection_version BIGINT;
ALTER TABLE search_document ADD COLUMN IF NOT EXISTS indexed_version BIGINT;

UPDATE search_document
SET projection_version = nextval('search_projection_version_seq')
WHERE projection_version IS NULL;

ALTER TABLE search_document ALTER COLUMN projection_version SET NOT NULL;

ALTER TABLE search_document ADD CONSTRAINT uq_search_document_aggregate
    UNIQUE USING INDEX uq_search_document_aggregate;

CREATE INDEX IF NOT EXISTS idx_search_document_dirty
    ON search_document (document_type, aggregate_id)
    WHERE projection_version > coalesce(indexed_version, 0);
