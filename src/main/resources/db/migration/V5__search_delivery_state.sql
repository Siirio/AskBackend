ALTER TABLE search_document
    ADD COLUMN IF NOT EXISTS projection_action VARCHAR(16);

UPDATE search_document
SET projection_action = 'INDEX'
WHERE projection_action IS NULL;

UPDATE search_outbox_event
SET status = 'COMPLETED',
    processed_at = coalesce(processed_at, now()),
    worker_id = NULL,
    processing_started_at = NULL,
    last_error = 'Retired unsupported search aggregate type',
    updated_at = now()
WHERE aggregate_type NOT IN ('ITEM', 'SERVICE');

DELETE FROM search_document_token
WHERE search_document_id IN (
    SELECT id
    FROM search_document
    WHERE document_type NOT IN ('ITEM', 'SERVICE')
);

DELETE FROM search_document
WHERE document_type NOT IN ('ITEM', 'SERVICE');

ALTER TABLE search_document
    ALTER COLUMN projection_action SET NOT NULL,
    ALTER COLUMN title DROP NOT NULL,
    ALTER COLUMN normalized_title DROP NOT NULL,
    ALTER COLUMN currency DROP NOT NULL,
    ALTER COLUMN verified_attributes DROP NOT NULL,
    ALTER COLUMN ai_attributes DROP NOT NULL,
    ALTER COLUMN aliases DROP NOT NULL,
    ALTER COLUMN availability_status DROP NOT NULL,
    ALTER COLUMN availability_source DROP NOT NULL;

SELECT setval(
    'search_projection_version_seq',
    greatest(
        coalesce((SELECT max(projection_version) FROM search_document), 1),
        coalesce((SELECT max(aggregate_version) FROM search_outbox_event), 1),
        1
    ),
    true
);

DROP INDEX IF EXISTS idx_search_document_dirty;

CREATE INDEX idx_search_document_dirty
    ON search_document (document_type, aggregate_id)
    WHERE projection_action = 'INDEX'
      AND projection_version > coalesce(indexed_version, 0);
