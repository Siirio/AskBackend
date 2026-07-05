ALTER TABLE brand_page_block
    ADD COLUMN page_status VARCHAR(50) NOT NULL DEFAULT 'PUBLISHED';

ALTER TABLE business_contact
    ADD COLUMN contact_hash VARCHAR(64),
    ADD COLUMN encrypted_value TEXT,
    ADD COLUMN display_value VARCHAR(255),
    ADD COLUMN visibility VARCHAR(50) NOT NULL DEFAULT 'AFTER_CONTACT';

ALTER TABLE search_document
    ADD COLUMN brand_drop_id UUID REFERENCES brand_drop(id);

CREATE TABLE brand_drop_tag (
    brand_drop_id UUID         NOT NULL REFERENCES brand_drop(id),
    tag           VARCHAR(255) NOT NULL
);

CREATE TABLE brand_drop_product (
    brand_drop_id UUID NOT NULL REFERENCES brand_drop(id),
    product_id    UUID NOT NULL
);

CREATE INDEX idx_brand_page_block_business_status_order
    ON brand_page_block (business_id, page_status, display_order);

CREATE INDEX idx_brand_drop_tag_drop
    ON brand_drop_tag (brand_drop_id);

CREATE INDEX idx_brand_drop_product_drop
    ON brand_drop_product (brand_drop_id);

CREATE INDEX idx_search_document_brand_drop
    ON search_document (brand_drop_id);

CREATE INDEX idx_business_contact_hash
    ON business_contact (contact_hash);
