-- ---------------------------------------------------------------------------
-- Public search: confidence/source/note metadata on search_document
-- ---------------------------------------------------------------------------

ALTER TABLE search_document ADD COLUMN confidence_code VARCHAR(20);
ALTER TABLE search_document ADD COLUMN source          VARCHAR(50);
ALTER TABLE search_document ADD COLUMN public_note      VARCHAR(500);
