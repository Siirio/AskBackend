ALTER TABLE search_document
    ADD COLUMN projection_version BIGINT;

UPDATE search_document
SET projection_version = (EXTRACT(EPOCH FROM updated_at) * 1000)::BIGINT
WHERE projection_version IS NULL;

ALTER TABLE search_document
    ALTER COLUMN projection_version SET NOT NULL;
