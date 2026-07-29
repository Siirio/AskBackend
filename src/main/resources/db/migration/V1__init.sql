-- =============================================================================
-- V1: Fresh-deploy baseline — all tables, indexes, and constraints
-- =============================================================================

CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE SEQUENCE search_projection_version_seq;

-- ---------------------------------------------------------------------------
-- Identity
-- ---------------------------------------------------------------------------

CREATE TABLE app_user (
    id                      UUID        NOT NULL PRIMARY KEY,
    created_at              TIMESTAMPTZ NOT NULL,
    updated_at              TIMESTAMPTZ NOT NULL,
    email                   VARCHAR(255),
    phone                   VARCHAR(32),
    display_name            VARCHAR(255) NOT NULL,
    password_hash           VARCHAR(255) NOT NULL,
    role                    VARCHAR(50)  NOT NULL,
    status                  VARCHAR(50)  NOT NULL,
    is_password_change_required    BOOLEAN     NOT NULL,
    is_two_factor_enabled          BOOLEAN     NOT NULL,
    temp_password_encrypted VARCHAR(255),
    activated_at            TIMESTAMPTZ,
    last_login_at           TIMESTAMPTZ
);

CREATE TABLE auth_challenge (
    id                UUID        NOT NULL PRIMARY KEY,
    created_at        TIMESTAMPTZ NOT NULL,
    updated_at        TIMESTAMPTZ NOT NULL,
    user_id           UUID        REFERENCES app_user(id),
    email             VARCHAR(255),
    channel           VARCHAR(50)  NOT NULL,
    purpose           VARCHAR(50)  NOT NULL,
    code_hash         VARCHAR(255) NOT NULL,
    attempts          INTEGER     NOT NULL,
    max_attempts      INTEGER     NOT NULL,
    expires_at        TIMESTAMPTZ NOT NULL,
    status            VARCHAR(50)  NOT NULL,
    is_remember_me    BOOLEAN,
    registration_data TEXT
);

CREATE TABLE auth_session (
    id                  UUID        NOT NULL PRIMARY KEY,
    created_at          TIMESTAMPTZ NOT NULL,
    updated_at          TIMESTAMPTZ NOT NULL,
    user_id             UUID        NOT NULL REFERENCES app_user(id),
    token_hash          VARCHAR(255) NOT NULL UNIQUE,
    authority           VARCHAR(50)  NOT NULL,
    is_remembered          BOOLEAN     NOT NULL,
    is_activation_required BOOLEAN     NOT NULL,
    expires_at          TIMESTAMPTZ NOT NULL,
    revoked_at          TIMESTAMPTZ
);

CREATE TABLE customer_profile (
    id           UUID        NOT NULL PRIMARY KEY,
    created_at   TIMESTAMPTZ NOT NULL,
    updated_at   TIMESTAMPTZ NOT NULL,
    user_id      UUID        NOT NULL UNIQUE REFERENCES app_user(id),
    icon_file_id VARCHAR(2048)
);

-- ---------------------------------------------------------------------------
-- Business
-- ---------------------------------------------------------------------------

CREATE TABLE city (
    id           UUID        NOT NULL PRIMARY KEY,
    created_at   TIMESTAMPTZ NOT NULL,
    updated_at   TIMESTAMPTZ NOT NULL,
    name         VARCHAR(255) NOT NULL,
    country_code VARCHAR(10)  NOT NULL
);

CREATE TABLE category (
    id         UUID        NOT NULL PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    name       VARCHAR(255) NOT NULL,
    slug       VARCHAR(255),
    type       VARCHAR(16)  NOT NULL,
    source     VARCHAR(16)  NOT NULL
);

CREATE TABLE business (
    id               UUID        NOT NULL PRIMARY KEY,
    created_at       TIMESTAMPTZ NOT NULL,
    updated_at       TIMESTAMPTZ NOT NULL,
    name             VARCHAR(255) NOT NULL,
    legal_name       VARCHAR(255),
    bin              VARCHAR(255),
    iin              VARCHAR(255),
    legal_identifier VARCHAR(32),
    country_code     VARCHAR(8),
    currency         VARCHAR(3),
    legal_form       VARCHAR(32),
    category_id      UUID        NOT NULL,
    scope            VARCHAR(16) NOT NULL,
    online_only      BOOLEAN     NOT NULL
);

CREATE TABLE business_branch (
    id              UUID        NOT NULL PRIMARY KEY,
    created_at      TIMESTAMPTZ NOT NULL,
    updated_at      TIMESTAMPTZ NOT NULL,
    business_id     UUID        NOT NULL REFERENCES business(id),
    city_id         UUID        REFERENCES city(id),
    name            VARCHAR(255) NOT NULL,
    address         VARCHAR(255),
    address_details VARCHAR(512),
    latitude        NUMERIC,
    longitude       NUMERIC,
    time_zone_id    VARCHAR(64),
    pickup_available BOOLEAN NOT NULL
);

CREATE TABLE branch_weekly_hours (
    branch_id   UUID        NOT NULL REFERENCES business_branch(id),
    day_of_week SMALLINT    NOT NULL,
    opens_at    TIME,
    closes_at   TIME
);

CREATE TABLE branch_special_hours (
    branch_id   UUID        NOT NULL REFERENCES business_branch(id),
    date        DATE        NOT NULL,
    closed      BOOLEAN,
    opens_at    TIME,
    closes_at   TIME
);

CREATE TABLE business_member (
    id          UUID        NOT NULL PRIMARY KEY,
    created_at  TIMESTAMPTZ NOT NULL,
    updated_at  TIMESTAMPTZ NOT NULL,
    business_id UUID        NOT NULL REFERENCES business(id),
    user_id     UUID        NOT NULL REFERENCES app_user(id),
    role        VARCHAR(50)  NOT NULL
);

CREATE TABLE branch_member (
    id          UUID        NOT NULL PRIMARY KEY,
    created_at  TIMESTAMPTZ NOT NULL,
    updated_at  TIMESTAMPTZ NOT NULL,
    branch_id   UUID        NOT NULL REFERENCES business_branch(id),
    user_id     UUID        NOT NULL REFERENCES app_user(id),
    role        VARCHAR(50)  NOT NULL,
    UNIQUE (branch_id, user_id)
);

CREATE TABLE business_member_branch (
    id                     UUID        NOT NULL PRIMARY KEY,
    created_at             TIMESTAMPTZ NOT NULL,
    updated_at             TIMESTAMPTZ NOT NULL,
    business_membership_id UUID        NOT NULL REFERENCES business_member(id) ON DELETE CASCADE,
    branch_id              UUID        NOT NULL REFERENCES business_branch(id) ON DELETE CASCADE,
    access_mode            VARCHAR(32) NOT NULL,
    role_override          VARCHAR(32),
    UNIQUE (business_membership_id, branch_id)
);

CREATE TABLE business_invitation (
    id                  UUID         NOT NULL PRIMARY KEY,
    created_at          TIMESTAMPTZ  NOT NULL,
    updated_at          TIMESTAMPTZ  NOT NULL,
    business_id         UUID         NOT NULL REFERENCES business(id),
    invited_email       VARCHAR(255) NOT NULL,
    invited_role        VARCHAR(50)  NOT NULL,
    invited_by_user_id  UUID         NOT NULL REFERENCES app_user(id),
    status              VARCHAR(50)  NOT NULL,
    token_hash          VARCHAR(64)  NOT NULL UNIQUE,
    expires_at          TIMESTAMPTZ  NOT NULL
);

CREATE TABLE business_invitation_branch (
    business_invitation_id UUID NOT NULL REFERENCES business_invitation(id) ON DELETE CASCADE,
    branch_id              UUID NOT NULL REFERENCES business_branch(id) ON DELETE CASCADE,
    PRIMARY KEY (business_invitation_id, branch_id)
);

CREATE TABLE business_invitation_action (
    id              UUID        NOT NULL PRIMARY KEY,
    created_at      TIMESTAMPTZ NOT NULL,
    updated_at      TIMESTAMPTZ NOT NULL,
    invitation_id   UUID        NOT NULL REFERENCES business_invitation(id),
    action_type     VARCHAR(32) NOT NULL,
    acted_by_user_id UUID       REFERENCES app_user(id)
);

CREATE TABLE business_profile (
    id            UUID        NOT NULL PRIMARY KEY,
    created_at    TIMESTAMPTZ NOT NULL,
    updated_at    TIMESTAMPTZ NOT NULL,
    business_id   UUID        NOT NULL UNIQUE REFERENCES business(id),
    brand_color   VARCHAR(255),
    logo_url      VARCHAR(255),
    cover_url     VARCHAR(255),
    description   TEXT,
    number        VARCHAR(255),
    email         VARCHAR(255),
    instagram_url VARCHAR(255),
    telegram_url  VARCHAR(255),
    website_url   VARCHAR(255),
    delivery_coverage VARCHAR(32),
    pickup_available BOOLEAN
);

CREATE TABLE business_profile_delivery_city (
    business_profile_id UUID         NOT NULL REFERENCES business_profile(id) ON DELETE CASCADE,
    city_name           VARCHAR(120) NOT NULL
);

CREATE TABLE business_verification (
    id              UUID        NOT NULL PRIMARY KEY,
    created_at      TIMESTAMPTZ NOT NULL,
    updated_at      TIMESTAMPTZ NOT NULL,
    business_id     UUID        NOT NULL UNIQUE REFERENCES business(id),
    status          VARCHAR(32) NOT NULL,
    bin_iin         VARCHAR(32),
    two_gis_url     VARCHAR(512),
    kaspi_url       VARCHAR(512),
    ozon_url        VARCHAR(512),
    wildberries_url VARCHAR(512),
    website_url     VARCHAR(512),
    instagram_url   VARCHAR(512),
    telegram_url    VARCHAR(512),
    phone           VARCHAR(64),
    corporate_email VARCHAR(255)
);

CREATE TABLE unique_offer (
    id               UUID        NOT NULL PRIMARY KEY,
    created_at       TIMESTAMPTZ NOT NULL,
    updated_at       TIMESTAMPTZ NOT NULL,
    business_id      UUID        NOT NULL REFERENCES business(id),
    name             VARCHAR(255) NOT NULL,
    description      TEXT,
    start_date       TIMESTAMPTZ,
    end_date         TIMESTAMPTZ,
    type             VARCHAR(50)  NOT NULL,
    status           VARCHAR(50)  NOT NULL,
    cover_url        VARCHAR(255),
    discount_percent INTEGER,
    discount_amount  NUMERIC,
    is_active        BOOLEAN     NOT NULL,
    currency         VARCHAR(3),
    tags             JSONB
);

-- ---------------------------------------------------------------------------
-- Items
-- ---------------------------------------------------------------------------

CREATE TABLE item (
    id                UUID        NOT NULL PRIMARY KEY,
    created_at        TIMESTAMPTZ NOT NULL,
    updated_at        TIMESTAMPTZ NOT NULL,
    business_id       UUID        NOT NULL REFERENCES business(id),
    branch_id         UUID        REFERENCES business_branch(id),
    category_id       UUID        NOT NULL,
    name              VARCHAR(255) NOT NULL,
    description       VARCHAR(255),
    deep_link         VARCHAR(2048),
    price             NUMERIC,
    is_active         BOOLEAN     NOT NULL,
    moderation_status VARCHAR(32) NOT NULL,
    moderation_note   VARCHAR(500),
    attributes        JSONB
);

CREATE TABLE product_tag (
    product_id UUID         NOT NULL REFERENCES item(id),
    tag        VARCHAR(255) NOT NULL
);

-- ---------------------------------------------------------------------------
-- Service
-- ---------------------------------------------------------------------------

CREATE TABLE service_offering (
    id             UUID        NOT NULL PRIMARY KEY,
    created_at     TIMESTAMPTZ NOT NULL,
    updated_at     TIMESTAMPTZ NOT NULL,
    business_id    UUID        NOT NULL REFERENCES business(id),
    branch_id      UUID        REFERENCES business_branch(id),
    category_id    UUID        NOT NULL,
    name           VARCHAR(255) NOT NULL,
    description    VARCHAR(255),
    service_mode   VARCHAR(50)  NOT NULL,
    base_price     NUMERIC,
    schedule_text  VARCHAR(255),
    is_active      BOOLEAN     NOT NULL,
    attributes     JSONB
);

-- ---------------------------------------------------------------------------
-- Unique Offer join tables (after item + service_offering)
-- ---------------------------------------------------------------------------

CREATE TABLE unique_offer_product (
    offer_id   UUID NOT NULL REFERENCES unique_offer(id),
    product_id UUID NOT NULL REFERENCES item(id),
    PRIMARY KEY (offer_id, product_id)
);

CREATE TABLE unique_offer_service (
    offer_id   UUID NOT NULL REFERENCES unique_offer(id),
    service_id UUID NOT NULL REFERENCES service_offering(id),
    PRIMARY KEY (offer_id, service_id)
);

CREATE TABLE unique_offer_branch (
    offer_id  UUID NOT NULL REFERENCES unique_offer(id),
    branch_id UUID NOT NULL REFERENCES business_branch(id),
    PRIMARY KEY (offer_id, branch_id)
);

-- ---------------------------------------------------------------------------
-- Chat
-- ---------------------------------------------------------------------------

CREATE TABLE chat_conversation (
    id                         UUID        NOT NULL PRIMARY KEY,
    created_at                 TIMESTAMPTZ NOT NULL,
    updated_at                 TIMESTAMPTZ NOT NULL,
    business_id                UUID        REFERENCES business(id),
    customer_id                UUID,
    conversation_type          VARCHAR(32) NOT NULL,
    conversation_status        VARCHAR(32) NOT NULL,
    managed_import_request_id  UUID,
    subject                    VARCHAR(512) NOT NULL,
    customer_unread_count      INTEGER     NOT NULL,
    business_unread_count      INTEGER     NOT NULL,
    last_message_at            TIMESTAMPTZ
);

CREATE TABLE chat_message (
    id              UUID        NOT NULL PRIMARY KEY,
    created_at      TIMESTAMPTZ NOT NULL,
    updated_at      TIMESTAMPTZ NOT NULL,
    conversation_id UUID        NOT NULL REFERENCES chat_conversation(id),
    sender_type     VARCHAR(16) NOT NULL,
    text            TEXT        NOT NULL,
    attachment_url  VARCHAR(512),
    read_at         TIMESTAMPTZ
);

CREATE TABLE chat_attachment (
    id                   UUID        NOT NULL PRIMARY KEY,
    created_at           TIMESTAMPTZ NOT NULL,
    updated_at           TIMESTAMPTZ NOT NULL,
    conversation_id      UUID        NOT NULL,
    stored_name          VARCHAR(128) NOT NULL UNIQUE,
    original_name        VARCHAR(512) NOT NULL,
    content_type         VARCHAR(128) NOT NULL,
    size_bytes           BIGINT      NOT NULL,
    uploaded_by_user_id  UUID        NOT NULL
);

-- ---------------------------------------------------------------------------
-- Managed Import
-- ---------------------------------------------------------------------------

CREATE TABLE managed_import_request (
    id                           UUID        NOT NULL PRIMARY KEY,
    created_at                   TIMESTAMPTZ NOT NULL,
    updated_at                   TIMESTAMPTZ NOT NULL,
    business_id                  UUID        NOT NULL REFERENCES business(id),
    requested_by_user_id         UUID        NOT NULL REFERENCES app_user(id),
    status                       VARCHAR(32) NOT NULL,
    business_scope               VARCHAR(32) NOT NULL,
    preferred_contact_channel    VARCHAR(32) NOT NULL,
    preferred_contact_value      VARCHAR(512) NOT NULL,
    source_links                 TEXT,
    source_notes                 TEXT,
    conversation_id              UUID,
    responsible_platform_user_id UUID        REFERENCES app_user(id),
    activated_at                 TIMESTAMPTZ,
    expires_at                   TIMESTAMPTZ,
    completed_at                 TIMESTAMPTZ,
    products_published_count     INTEGER
);

CREATE TABLE managed_import_request_source (
    managed_import_request_id UUID        NOT NULL REFERENCES managed_import_request(id) ON DELETE CASCADE,
    source_type               VARCHAR(32) NOT NULL,
    PRIMARY KEY (managed_import_request_id, source_type)
);

-- ---------------------------------------------------------------------------
-- Moderation
-- ---------------------------------------------------------------------------

CREATE TABLE moderation_action (
    id                    UUID        NOT NULL PRIMARY KEY,
    created_at            TIMESTAMPTZ NOT NULL,
    updated_at            TIMESTAMPTZ NOT NULL,
    target_type           VARCHAR(32) NOT NULL,
    target_id             UUID        NOT NULL,
    action                VARCHAR(50) NOT NULL,
    moderation_status     VARCHAR(32) NOT NULL,
    performed_by_user_id  UUID        NOT NULL REFERENCES app_user(id),
    made_by_user_id       UUID        REFERENCES app_user(id),
    reason_code           VARCHAR(64),
    details               TEXT,
    note                  VARCHAR(2000),
    expires_at            TIMESTAMPTZ
);

-- ---------------------------------------------------------------------------
-- Legal
-- ---------------------------------------------------------------------------

CREATE TABLE legal_acceptance (
    id                  UUID        NOT NULL PRIMARY KEY,
    created_at          TIMESTAMPTZ NOT NULL,
    updated_at          TIMESTAMPTZ NOT NULL,
    user_id             UUID        NOT NULL REFERENCES app_user(id),
    document_code       VARCHAR(64) NOT NULL,
    country_code        VARCHAR(8)  NOT NULL,
    locale              VARCHAR(8)  NOT NULL,
    acceptance_channel  VARCHAR(64) NOT NULL,
    accepted_at         TIMESTAMPTZ NOT NULL
);

-- ---------------------------------------------------------------------------
-- Platform
-- ---------------------------------------------------------------------------

CREATE TABLE platform_membership (
    id         UUID        NOT NULL PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    user_id    UUID        NOT NULL UNIQUE REFERENCES app_user(id),
    role       VARCHAR(50) NOT NULL
);

CREATE TABLE platform_membership_permission (
    platform_membership_id UUID        NOT NULL REFERENCES platform_membership(id) ON DELETE CASCADE,
    permission             VARCHAR(80) NOT NULL,
    PRIMARY KEY (platform_membership_id, permission)
);

-- ---------------------------------------------------------------------------
-- Search
-- ---------------------------------------------------------------------------

CREATE TABLE search_document (
    id                           UUID           NOT NULL PRIMARY KEY,
    created_at                   TIMESTAMPTZ    NOT NULL,
    updated_at                   TIMESTAMPTZ    NOT NULL,
    document_type                VARCHAR(50)    NOT NULL,
    aggregate_id                 UUID           NOT NULL,
    title                        VARCHAR(255),
    normalized_title             TEXT,
    summary                      VARCHAR(255),
    category_label               VARCHAR(255),
    brand                        VARCHAR(255),
    category_path                TEXT,
    business_name                VARCHAR(255),
    branch_name                  VARCHAR(255),
    business_id                  UUID           REFERENCES business(id),
    branch_id                    UUID           REFERENCES business_branch(id),
    price                        NUMERIC,
    currency                     VARCHAR(3),
    latitude                     NUMERIC(10,7),
    longitude                    NUMERIC(10,7),
    source                       VARCHAR(50),
    public_note                  VARCHAR(500),
    verified_attributes          JSONB,
    ai_attributes                JSONB,
    aliases                      TEXT,
    concept_ids                  JSONB,
    use_cases                    JSONB,
    semantic_summary             TEXT,
    embedding_text               TEXT,
    semantic_confidence          NUMERIC(4,3),
    semantic_evidence            JSONB,
    semantic_model_version       VARCHAR(255),
    semantic_schema_version      VARCHAR(64),
    semantic_source_hash         VARCHAR(64),
    semantic_metadata_source_hash VARCHAR(64),
    semantic_generated_at        TIMESTAMPTZ,
    ai_search_summary            TEXT,
    availability_status          VARCHAR(32),
    availability_source          VARCHAR(32),
    last_business_updated_at     TIMESTAMPTZ,
    indexed_at                   TIMESTAMPTZ,
    projection_version           BIGINT         NOT NULL,
    indexed_version              BIGINT,
    projection_action            VARCHAR(16)    NOT NULL,
    search_vector                TSVECTOR GENERATED ALWAYS AS (
        to_tsvector('simple',
            coalesce(normalized_title, '') || ' ' ||
            coalesce(title, '') || ' ' ||
            coalesce(brand, '') || ' ' ||
            coalesce(category_path, '') || ' ' ||
            coalesce(category_label, '') || ' ' ||
            coalesce(aliases, '') || ' ' ||
            coalesce(concept_ids::text, '') || ' ' ||
            coalesce(use_cases::text, '') || ' ' ||
            coalesce(semantic_summary, '') || ' ' ||
            coalesce(embedding_text, '') || ' ' ||
            coalesce(ai_search_summary, '') || ' ' ||
            coalesce(summary, '') || ' ' ||
            coalesce(business_name, '') || ' ' ||
            coalesce(branch_name, '')
        )
    ) STORED
);

CREATE TABLE search_document_token (
    search_document_id UUID         NOT NULL REFERENCES search_document(id),
    token              VARCHAR(255) NOT NULL
);

CREATE TABLE search_outbox_event (
    id                    UUID        NOT NULL PRIMARY KEY,
    created_at            TIMESTAMPTZ NOT NULL,
    updated_at            TIMESTAMPTZ NOT NULL,
    aggregate_type        VARCHAR(32) NOT NULL,
    aggregate_id          UUID        NOT NULL,
    event_type            VARCHAR(32) NOT NULL,
    aggregate_version     BIGINT      NOT NULL,
    payload_version       INTEGER     NOT NULL,
    available_at          TIMESTAMPTZ NOT NULL,
    processing_started_at TIMESTAMPTZ,
    processed_at          TIMESTAMPTZ,
    worker_id             VARCHAR(128),
    attempt_count         INTEGER     NOT NULL,
    last_error            VARCHAR(2000),
    status                VARCHAR(20) NOT NULL
);

CREATE TABLE search_query_alias (
    id           UUID         NOT NULL PRIMARY KEY,
    created_at   TIMESTAMPTZ  NOT NULL,
    updated_at   TIMESTAMPTZ  NOT NULL,
    alias_value  VARCHAR(255) NOT NULL,
    target_query VARCHAR(255) NOT NULL
);

-- ---------------------------------------------------------------------------
-- Audit
-- ---------------------------------------------------------------------------

CREATE TABLE significant_event (
    id           UUID        NOT NULL PRIMARY KEY,
    created_at   TIMESTAMPTZ NOT NULL,
    updated_at   TIMESTAMPTZ NOT NULL,
    actor_user_id UUID,
    event_type   VARCHAR(64) NOT NULL,
    business_id  UUID,
    entity_id    UUID,
    metadata     JSONB       NOT NULL
);

-- =============================================================================
-- Indexes
-- =============================================================================

CREATE INDEX idx_app_user_email ON app_user (email);
CREATE INDEX idx_app_user_role ON app_user (role);

CREATE INDEX idx_auth_challenge_user ON auth_challenge (user_id);

CREATE INDEX idx_auth_session_user  ON auth_session (user_id);
CREATE INDEX idx_auth_session_token ON auth_session (token_hash);

CREATE INDEX idx_business_branch_business ON business_branch (business_id);
CREATE INDEX idx_business_branch_city     ON business_branch (city_id);

CREATE INDEX idx_business_member_user     ON business_member (user_id);
CREATE INDEX idx_business_member_business ON business_member (business_id);

CREATE INDEX idx_branch_member_branch ON branch_member (branch_id);
CREATE INDEX idx_branch_member_user   ON branch_member (user_id);

CREATE INDEX idx_business_member_branch_branch ON business_member_branch (branch_id);

CREATE INDEX idx_business_invitation_business_status ON business_invitation (business_id, status);
CREATE INDEX idx_business_invitation_email_status ON business_invitation (lower(invited_email), status);

CREATE UNIQUE INDEX uq_business_member_business_user ON business_member (business_id, user_id);

CREATE UNIQUE INDEX uq_business_invitation_pending
    ON business_invitation (business_id, lower(invited_email), invited_role)
    WHERE status = 'PENDING';

CREATE UNIQUE INDEX uq_category_name_type ON category (name, type);

CREATE INDEX idx_product_business ON item (business_id);
CREATE INDEX idx_product_moderation_status ON item (moderation_status, created_at);
CREATE INDEX idx_product_attributes ON item USING GIN (attributes);

CREATE INDEX idx_service_offering_attributes ON service_offering USING GIN (attributes);

CREATE INDEX idx_chat_conversation_business ON chat_conversation (business_id, last_message_at DESC);
CREATE INDEX idx_chat_conversation_customer ON chat_conversation (customer_id, last_message_at DESC);
CREATE INDEX idx_chat_message_conversation  ON chat_message (conversation_id, created_at);
CREATE INDEX idx_chat_attachment_conversation ON chat_attachment (conversation_id);

CREATE UNIQUE INDEX uq_chat_conversation_business_customer_general
    ON chat_conversation (business_id, customer_id)
    WHERE conversation_type = 'GENERAL_SUPPORT'
      AND business_id IS NOT NULL
      AND customer_id IS NOT NULL;

CREATE INDEX idx_managed_import_request_status ON managed_import_request (status, created_at);

CREATE INDEX idx_moderation_action_status ON moderation_action (moderation_status, created_at);
CREATE INDEX idx_moderation_action_target ON moderation_action (target_type, target_id);

CREATE INDEX idx_business_invitation_action_invitation ON business_invitation_action (invitation_id);

CREATE INDEX idx_legal_acceptance_user ON legal_acceptance (user_id, accepted_at DESC);

CREATE INDEX idx_significant_event_business_created ON significant_event (business_id, created_at);
CREATE INDEX idx_significant_event_type_created ON significant_event (event_type, created_at);

CREATE INDEX idx_search_query_alias_value ON search_query_alias (alias_value);

-- Search document indexes
CREATE UNIQUE INDEX uq_search_document_aggregate ON search_document (document_type, aggregate_id);
CREATE INDEX idx_search_document_search_vector ON search_document USING GIN (search_vector);
CREATE INDEX idx_search_document_title_trgm ON search_document USING GIN (normalized_title gin_trgm_ops);
CREATE INDEX idx_search_document_active_type_price ON search_document (document_type, price, id);
CREATE INDEX idx_search_document_business_active ON search_document (business_id, document_type, id);
CREATE INDEX idx_search_document_verified_attributes ON search_document USING GIN (verified_attributes);
CREATE INDEX idx_search_document_ai_attributes ON search_document USING GIN (ai_attributes);
CREATE INDEX idx_search_document_dirty
    ON search_document (document_type, aggregate_id)
    WHERE projection_action = 'INDEX'
      AND projection_version > coalesce(indexed_version, 0);

-- Search outbox indexes
CREATE UNIQUE INDEX uq_search_outbox_event
    ON search_outbox_event (aggregate_type, aggregate_id, aggregate_version, event_type);
CREATE INDEX idx_search_outbox_claim ON search_outbox_event (status, available_at, created_at);
CREATE INDEX idx_search_outbox_aggregate ON search_outbox_event (aggregate_type, aggregate_id, aggregate_version DESC);
CREATE INDEX idx_search_outbox_processing ON search_outbox_event (processing_started_at)
    WHERE status = 'PROCESSING';

-- =============================================================================
-- Constraints
-- =============================================================================

ALTER TABLE search_outbox_event
    ADD CONSTRAINT chk_search_outbox_status
        CHECK (status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'RETRY', 'DEAD')),
    ADD CONSTRAINT chk_search_outbox_attempt_count
        CHECK (attempt_count >= 0);

-- FK for business.category_id (deferred: business created before category)
ALTER TABLE business
    ADD CONSTRAINT fk_business_category FOREIGN KEY (category_id) REFERENCES category(id);

-- FK for item.category_id (deferred: item created before category unique constraint)
ALTER TABLE item
    ADD CONSTRAINT fk_item_category FOREIGN KEY (category_id) REFERENCES category(id);

-- FK for service_offering.category_id
ALTER TABLE service_offering
    ADD CONSTRAINT fk_service_category FOREIGN KEY (category_id) REFERENCES category(id);

-- FK for chat_attachment (deferred: chat_conversation FK set up above)
ALTER TABLE chat_attachment
    ADD CONSTRAINT fk_chat_attachment_conversation
        FOREIGN KEY (conversation_id) REFERENCES chat_conversation(id) ON DELETE CASCADE;

-- FK for chat_conversation.managed_import_request_id
ALTER TABLE chat_conversation
    ADD CONSTRAINT fk_chat_managed_import_request
        FOREIGN KEY (managed_import_request_id) REFERENCES managed_import_request(id);
