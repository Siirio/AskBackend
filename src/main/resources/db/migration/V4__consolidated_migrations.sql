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

ALTER TABLE service_offering ADD COLUMN IF NOT EXISTS attributes JSONB;
CREATE INDEX IF NOT EXISTS idx_service_offering_attributes ON service_offering USING GIN (attributes);

-- ---------------------------------------------------------------------------
-- 12. Search index queue for debounced AI attribute extraction (from V9)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS search_index_queue (
    id              UUID PRIMARY KEY,
    entity_type     VARCHAR(20)  NOT NULL,
    entity_id       UUID         NOT NULL,
    scheduled_at    TIMESTAMPTZ  NOT NULL,
    retry_count     INTEGER      NOT NULL DEFAULT 0,
    last_error      TEXT,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_search_index_queue_entry UNIQUE (entity_type, entity_id)
);

CREATE INDEX IF NOT EXISTS idx_search_index_queue_scheduled
    ON search_index_queue (scheduled_at);

-- ---------------------------------------------------------------------------
-- 13. Search document attributes (from V10)
-- ---------------------------------------------------------------------------
ALTER TABLE search_document ADD COLUMN IF NOT EXISTS attributes JSONB;
CREATE INDEX IF NOT EXISTS idx_search_document_attributes ON search_document USING GIN (attributes);

-- ---------------------------------------------------------------------------
-- 14. Drop orphaned analytics tables (created in V1, never referenced in code)
-- ---------------------------------------------------------------------------
DROP TABLE IF EXISTS search_result_snapshot CASCADE;
DROP TABLE IF EXISTS search_snapshot CASCADE;
DROP TABLE IF EXISTS search_session CASCADE;
