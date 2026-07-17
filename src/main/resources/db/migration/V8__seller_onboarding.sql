ALTER TABLE business
    ADD COLUMN country_code VARCHAR(8),
    ADD COLUMN legal_form VARCHAR(32),
    ADD COLUMN legal_identifier VARCHAR(32),
    ADD COLUMN preferred_contact_channel VARCHAR(32),
    ADD COLUMN preferred_contact_value VARCHAR(512),
    ADD COLUMN catalog_setup_mode VARCHAR(32),
    ADD COLUMN catalog_source_links TEXT,
    ADD COLUMN catalog_source_notes TEXT,
    ADD COLUMN catalog_deadline_at TIMESTAMPTZ,
    ADD COLUMN catalog_status VARCHAR(32);

UPDATE business
SET country_code = COALESCE(country_code, 'KZ'),
    catalog_setup_mode = COALESCE(catalog_setup_mode, 'MANUAL'),
    catalog_status = COALESCE(catalog_status, 'COMPLETED');

CREATE TABLE business_delivery_profile (
    id UUID NOT NULL PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    business_id UUID NOT NULL UNIQUE REFERENCES business(id),
    pickup_available BOOLEAN NOT NULL,
    delivery_scope VARCHAR(32) NOT NULL,
    terms_ru TEXT,
    terms_kk TEXT,
    terms_en TEXT
);

CREATE TABLE business_catalog_source (
    business_id UUID NOT NULL REFERENCES business(id) ON DELETE CASCADE,
    source_type VARCHAR(32) NOT NULL,
    PRIMARY KEY (business_id, source_type)
);

CREATE TABLE business_delivery_city (
    delivery_profile_id UUID NOT NULL REFERENCES business_delivery_profile(id) ON DELETE CASCADE,
    city_id UUID NOT NULL REFERENCES city(id),
    PRIMARY KEY (delivery_profile_id, city_id)
);
