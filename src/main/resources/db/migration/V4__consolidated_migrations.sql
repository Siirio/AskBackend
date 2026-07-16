-- =============================================================================
-- V4: Consolidated migrations from original V4-V10 for staging/prod upgrade
-- Prerequisite: V1, V2, V3 already applied
-- Uses IF NOT EXISTS / IF EXISTS to be safe for fresh DBs too
-- =============================================================================

-- ---------------------------------------------------------------------------
-- 1. Drop duration_minutes (from V4)
-- ---------------------------------------------------------------------------
ALTER TABLE service_branch_offer DROP COLUMN IF EXISTS duration_minutes;

-- ---------------------------------------------------------------------------
-- 2. Add image_url to product and service_offering (from V5)
-- ---------------------------------------------------------------------------
ALTER TABLE product ADD COLUMN IF NOT EXISTS image_url VARCHAR(2048);
ALTER TABLE service_offering ADD COLUMN IF NOT EXISTS image_url VARCHAR(2048);

-- ---------------------------------------------------------------------------
-- 3. Chat tables (from V6 + V8, net final column set)
--    V6 added chat_conversation with status/source/search_query,
--    chat_message with attachment_url. V7 added request_target_id.
--    V8 dropped all four columns and added unread counts.
--    This migration creates the tables in their final form.
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS chat_conversation (
    id UUID PRIMARY KEY,
    business_id UUID NOT NULL REFERENCES business(id),
    customer_id UUID,
    subject VARCHAR(512) NOT NULL,
    last_message_at TIMESTAMPTZ,
    customer_unread_count INTEGER NOT NULL DEFAULT 0,
    business_unread_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS chat_message (
    id UUID PRIMARY KEY,
    conversation_id UUID NOT NULL REFERENCES chat_conversation(id),
    sender_type VARCHAR(16) NOT NULL,
    text TEXT NOT NULL,
    read_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_chat_conversation_business
    ON chat_conversation (business_id, last_message_at DESC);
CREATE INDEX IF NOT EXISTS idx_chat_conversation_customer
    ON chat_conversation (customer_id, last_message_at DESC);
CREATE INDEX IF NOT EXISTS idx_chat_message_conversation
    ON chat_message (conversation_id, created_at);

-- ---------------------------------------------------------------------------
-- 4. Add price to product (from V8)
-- ---------------------------------------------------------------------------
ALTER TABLE product ADD COLUMN IF NOT EXISTS price NUMERIC;

-- ---------------------------------------------------------------------------
-- 5. Many-to-many join tables (from V8)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS product_branch (
    product_id UUID NOT NULL REFERENCES product(id),
    branch_id  UUID NOT NULL REFERENCES business_branch(id),
    PRIMARY KEY (product_id, branch_id)
);

CREATE TABLE IF NOT EXISTS service_branch (
    service_id UUID NOT NULL REFERENCES service_offering(id),
    branch_id  UUID NOT NULL REFERENCES business_branch(id),
    PRIMARY KEY (service_id, branch_id)
);

-- ---------------------------------------------------------------------------
-- 6. BrandDrop -> UniqueOffer rename + new columns (from V8)
-- ---------------------------------------------------------------------------
ALTER TABLE IF EXISTS brand_drop RENAME TO unique_offer;

ALTER TABLE unique_offer ADD COLUMN IF NOT EXISTS discount_percent INTEGER;
ALTER TABLE unique_offer ADD COLUMN IF NOT EXISTS discount_amount NUMERIC;
ALTER TABLE unique_offer ADD COLUMN IF NOT EXISTS enabled BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE unique_offer ADD COLUMN IF NOT EXISTS currency VARCHAR(3) NOT NULL DEFAULT 'KZT';
ALTER TABLE unique_offer ADD COLUMN IF NOT EXISTS tags JSONB DEFAULT '[]';

CREATE TABLE IF NOT EXISTS unique_offer_product (
    offer_id   UUID NOT NULL REFERENCES unique_offer(id),
    product_id UUID NOT NULL REFERENCES product(id),
    PRIMARY KEY (offer_id, product_id)
);

CREATE TABLE IF NOT EXISTS unique_offer_service (
    offer_id   UUID NOT NULL REFERENCES unique_offer(id),
    service_id UUID NOT NULL REFERENCES service_offering(id),
    PRIMARY KEY (offer_id, service_id)
);

CREATE TABLE IF NOT EXISTS unique_offer_branch (
    offer_id  UUID NOT NULL REFERENCES unique_offer(id),
    branch_id UUID NOT NULL REFERENCES business_branch(id),
    PRIMARY KEY (offer_id, branch_id)
);

-- ---------------------------------------------------------------------------
-- 7. Business currency + shipping (from V8)
-- ---------------------------------------------------------------------------
ALTER TABLE business ADD COLUMN IF NOT EXISTS currency VARCHAR(3) NOT NULL DEFAULT 'KZT';
ALTER TABLE business ADD COLUMN IF NOT EXISTS shipping_mode VARCHAR(20);
ALTER TABLE business ADD COLUMN IF NOT EXISTS shipping_city_ids JSONB;

-- ---------------------------------------------------------------------------
-- 8. Auth: drop phone columns, add two_factor_enabled (from V8 + V5)
-- ---------------------------------------------------------------------------
ALTER TABLE app_user DROP COLUMN IF EXISTS phone;
ALTER TABLE app_user ADD COLUMN IF NOT EXISTS two_factor_enabled BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE auth_challenge DROP COLUMN IF EXISTS phone;
DROP INDEX IF EXISTS idx_app_user_phone;

-- ---------------------------------------------------------------------------
-- 9. Customer profile: drop phone, add icon_url (from V8)
-- ---------------------------------------------------------------------------
ALTER TABLE customer_profile DROP COLUMN IF EXISTS phone;
ALTER TABLE customer_profile ADD COLUMN IF NOT EXISTS icon_url VARCHAR(2048);

-- ---------------------------------------------------------------------------
-- 10. Drop legacy tables (from V8)
-- ---------------------------------------------------------------------------
ALTER TABLE booking DROP COLUMN IF EXISTS conversation_id CASCADE;
DROP TABLE IF EXISTS conversation_link CASCADE;
DROP TABLE IF EXISTS conversation_message CASCADE;
DROP TABLE IF EXISTS conversation_participant CASCADE;
DROP TABLE IF EXISTS conversation CASCADE;
DROP TABLE IF EXISTS brand_page_block CASCADE;
DROP TABLE IF EXISTS business_card CASCADE;

-- ---------------------------------------------------------------------------
-- 11. Product and ServiceOffering structured attributes (from V9)
-- ---------------------------------------------------------------------------
ALTER TABLE product ADD COLUMN IF NOT EXISTS attributes JSONB;
CREATE INDEX IF NOT EXISTS idx_product_attributes ON product USING GIN (attributes);
ALTER TABLE product ADD COLUMN IF NOT EXISTS entity_version BIGINT NOT NULL DEFAULT 0;

ALTER TABLE service_offering ADD COLUMN IF NOT EXISTS attributes JSONB;
CREATE INDEX IF NOT EXISTS idx_service_offering_attributes ON service_offering USING GIN (attributes);
ALTER TABLE service_offering ADD COLUMN IF NOT EXISTS entity_version BIGINT NOT NULL DEFAULT 0;

ALTER TABLE product_offer ADD COLUMN IF NOT EXISTS search_version BIGINT NOT NULL DEFAULT 1;
ALTER TABLE service_branch_offer ADD COLUMN IF NOT EXISTS search_version BIGINT NOT NULL DEFAULT 1;

-- ---------------------------------------------------------------------------
-- 12. Transactional search outbox
-- ---------------------------------------------------------------------------
DROP TABLE IF EXISTS search_index_queue;

CREATE TABLE IF NOT EXISTS search_outbox_event (
    id                    UUID PRIMARY KEY,
    aggregate_type        VARCHAR(32)  NOT NULL,
    aggregate_id          UUID         NOT NULL,
    event_type            VARCHAR(32)  NOT NULL,
    aggregate_version     BIGINT       NOT NULL,
    payload_version       INTEGER      NOT NULL DEFAULT 1,
    available_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    processing_started_at TIMESTAMPTZ,
    processed_at          TIMESTAMPTZ,
    worker_id             VARCHAR(128),
    attempt_count         INTEGER      NOT NULL DEFAULT 0,
    last_error            VARCHAR(2000),
    status                VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    created_at            TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_search_outbox_event
        UNIQUE (aggregate_type, aggregate_id, aggregate_version, event_type),
    CONSTRAINT chk_search_outbox_status
        CHECK (status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'RETRY', 'DEAD')),
    CONSTRAINT chk_search_outbox_attempt_count CHECK (attempt_count >= 0)
);

CREATE INDEX IF NOT EXISTS idx_search_outbox_claim
    ON search_outbox_event (status, available_at, created_at);
CREATE INDEX IF NOT EXISTS idx_search_outbox_aggregate
    ON search_outbox_event (aggregate_type, aggregate_id, aggregate_version DESC);
CREATE INDEX IF NOT EXISTS idx_search_outbox_processing
    ON search_outbox_event (processing_started_at)
    WHERE status = 'PROCESSING';

-- ---------------------------------------------------------------------------
-- 13. Separate AI-derived search metadata
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS search_ai_metadata (
    id                 UUID PRIMARY KEY,
    aggregate_type     VARCHAR(32)   NOT NULL,
    aggregate_id       UUID          NOT NULL,
    attribute_key      VARCHAR(128)  NOT NULL,
    attribute_value    JSONB         NOT NULL,
    confidence         NUMERIC(5,4)  NOT NULL,
    evidence           VARCHAR(1000) NOT NULL,
    source             VARCHAR(32)   NOT NULL,
    model_version      VARCHAR(128)  NOT NULL,
    schema_version     VARCHAR(128)  NOT NULL,
    extracted_at       TIMESTAMPTZ   NOT NULL,
    verification_state VARCHAR(32)   NOT NULL,
    created_at         TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at         TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT uq_search_ai_metadata_fact
        UNIQUE (aggregate_type, aggregate_id, attribute_key, schema_version, model_version),
    CONSTRAINT chk_search_ai_metadata_confidence
        CHECK (confidence >= 0 AND confidence <= 1),
    CONSTRAINT chk_search_ai_verification_state
        CHECK (verification_state IN ('AI_DERIVED', 'BUSINESS_CONFIRMED', 'BUSINESS_CORRECTED', 'REJECTED'))
);

CREATE INDEX IF NOT EXISTS idx_search_ai_metadata_aggregate
    ON search_ai_metadata (aggregate_type, aggregate_id);
CREATE INDEX IF NOT EXISTS idx_search_ai_metadata_value
    ON search_ai_metadata USING GIN (attribute_value);

-- ---------------------------------------------------------------------------
-- 14. Versioned rebuildable PostgreSQL search projection and fallback indexes
-- ---------------------------------------------------------------------------
CREATE EXTENSION IF NOT EXISTS pg_trgm;

ALTER TABLE search_document ADD COLUMN IF NOT EXISTS aggregate_id UUID;
ALTER TABLE search_document ADD COLUMN IF NOT EXISTS document_version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE search_document ADD COLUMN IF NOT EXISTS normalized_title TEXT NOT NULL DEFAULT '';
ALTER TABLE search_document ADD COLUMN IF NOT EXISTS brand VARCHAR(255);
ALTER TABLE search_document ADD COLUMN IF NOT EXISTS category_path TEXT;
ALTER TABLE search_document ADD COLUMN IF NOT EXISTS business_name VARCHAR(255);
ALTER TABLE search_document ADD COLUMN IF NOT EXISTS branch_name VARCHAR(255);
ALTER TABLE search_document ADD COLUMN IF NOT EXISTS verified_attributes JSONB NOT NULL DEFAULT '{}';
ALTER TABLE search_document ADD COLUMN IF NOT EXISTS ai_attributes JSONB NOT NULL DEFAULT '{}';
ALTER TABLE search_document ADD COLUMN IF NOT EXISTS aliases TEXT NOT NULL DEFAULT '';
ALTER TABLE search_document ADD COLUMN IF NOT EXISTS ai_search_summary TEXT;
ALTER TABLE search_document ADD COLUMN IF NOT EXISTS currency VARCHAR(3) NOT NULL DEFAULT 'KZT';
ALTER TABLE search_document ADD COLUMN IF NOT EXISTS latitude NUMERIC(10,7);
ALTER TABLE search_document ADD COLUMN IF NOT EXISTS longitude NUMERIC(10,7);
ALTER TABLE search_document ADD COLUMN IF NOT EXISTS availability_status VARCHAR(32) NOT NULL DEFAULT 'UNKNOWN';
ALTER TABLE search_document ADD COLUMN IF NOT EXISTS availability_source VARCHAR(32) NOT NULL DEFAULT 'UNKNOWN';
ALTER TABLE search_document ADD COLUMN IF NOT EXISTS last_business_updated_at TIMESTAMPTZ;
ALTER TABLE search_document ADD COLUMN IF NOT EXISTS indexed_at TIMESTAMPTZ;
ALTER TABLE search_document ADD COLUMN IF NOT EXISTS ai_enrichment_version BIGINT;
ALTER TABLE search_document ADD COLUMN IF NOT EXISTS ai_enrichment_available_at TIMESTAMPTZ NOT NULL DEFAULT now();
ALTER TABLE search_document ADD COLUMN IF NOT EXISTS ai_enrichment_started_at TIMESTAMPTZ;
ALTER TABLE search_document ADD COLUMN IF NOT EXISTS ai_enrichment_worker_id VARCHAR(128);
ALTER TABLE search_document ADD COLUMN IF NOT EXISTS ai_enrichment_attempt_count INTEGER NOT NULL DEFAULT 0;
ALTER TABLE search_document ADD COLUMN IF NOT EXISTS ai_enrichment_error VARCHAR(2000);
ALTER TABLE search_document ADD COLUMN IF NOT EXISTS ai_enrichment_dead BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE search_document ADD COLUMN IF NOT EXISTS search_vector TSVECTOR
    GENERATED ALWAYS AS (
        to_tsvector(
            'simple',
            coalesce(normalized_title, '') || ' ' ||
            coalesce(title, '') || ' ' ||
            coalesce(brand, '') || ' ' ||
            coalesce(category_path, '') || ' ' ||
            coalesce(category_label, '') || ' ' ||
            coalesce(sku, '') || ' ' ||
            coalesce(aliases, '') || ' ' ||
            coalesce(ai_search_summary, '') || ' ' ||
            coalesce(summary, '') || ' ' ||
            coalesce(business_name, '') || ' ' ||
            coalesce(branch_name, '')
        )
    ) STORED;

UPDATE search_document document
SET aggregate_id = COALESCE(document.product_offer_id, document.service_branch_offer_id),
    normalized_title = lower(trim(document.title)),
    business_name = business.name,
    branch_name = branch.name,
    last_business_updated_at = document.updated_at
FROM business, business_branch branch
WHERE document.business_id = business.id
  AND document.branch_id = branch.id;

ALTER TABLE search_document ALTER COLUMN aggregate_id SET NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_search_document_aggregate
    ON search_document (document_type, aggregate_id);
CREATE INDEX IF NOT EXISTS idx_search_document_search_vector
    ON search_document USING GIN (search_vector);
CREATE INDEX IF NOT EXISTS idx_search_document_title_trgm
    ON search_document USING GIN (normalized_title gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_search_document_active_type_price
    ON search_document (document_type, status, price, id);
CREATE INDEX IF NOT EXISTS idx_search_document_business_active
    ON search_document (business_id, document_type, status, id);
CREATE INDEX IF NOT EXISTS idx_search_document_verified_attributes
    ON search_document USING GIN (verified_attributes);
CREATE INDEX IF NOT EXISTS idx_search_document_ai_attributes
    ON search_document USING GIN (ai_attributes);
CREATE INDEX IF NOT EXISTS idx_search_document_ai_enrichment
    ON search_document (ai_enrichment_available_at, id)
    WHERE status = 'ACTIVE';

-- ---------------------------------------------------------------------------
-- 15. Drop orphaned analytics tables (created in V1, never referenced in code)
-- ---------------------------------------------------------------------------
DROP TABLE IF EXISTS search_result_snapshot CASCADE;
DROP TABLE IF EXISTS search_snapshot CASCADE;
DROP TABLE IF EXISTS search_session CASCADE;

-- ---------------------------------------------------------------------------
-- 16. last_login_at tracking (from V6)
-- ---------------------------------------------------------------------------
ALTER TABLE app_user ADD COLUMN IF NOT EXISTS last_login_at TIMESTAMPTZ;
