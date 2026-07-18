ALTER TABLE search_document
    ADD COLUMN ai_enrichment_requested BOOLEAN NOT NULL DEFAULT FALSE;

INSERT INTO platform_membership_permission (platform_membership_id, permission)
SELECT id, 'USE_AI_CATALOG_TOOLS'
FROM platform_membership
WHERE status = 'ACTIVE'
ON CONFLICT DO NOTHING;
