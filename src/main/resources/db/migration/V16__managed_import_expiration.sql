ALTER TABLE managed_import_request
    ADD COLUMN expires_at TIMESTAMP WITH TIME ZONE;

UPDATE managed_import_request
SET expires_at = COALESCE(activated_at, created_at) + INTERVAL '7 days'
WHERE status = 'ACTIVE';
