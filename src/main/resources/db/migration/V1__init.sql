-- =============================================================================
-- V1: Unified base schema — all tables, indexes, constraints, and reference data
-- =============================================================================

-- ---------------------------------------------------------------------------
-- Identity
-- ---------------------------------------------------------------------------

CREATE TABLE app_user (
    id            UUID        NOT NULL PRIMARY KEY,
    created_at    TIMESTAMPTZ NOT NULL,
    updated_at    TIMESTAMPTZ NOT NULL,
    email         VARCHAR(255),
    phone         VARCHAR(255),
    display_name  VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(50)  NOT NULL,
    status        VARCHAR(50)  NOT NULL,
    must_change_password       BOOLEAN     NOT NULL DEFAULT FALSE,
    temp_password_encrypted    VARCHAR(255),
    activated_at               TIMESTAMPTZ
);

CREATE TABLE auth_challenge (
    id                UUID        NOT NULL PRIMARY KEY,
    created_at        TIMESTAMPTZ NOT NULL,
    updated_at        TIMESTAMPTZ NOT NULL,
    user_id           UUID        REFERENCES app_user(id),
    email             VARCHAR(255),
    phone             VARCHAR(255),
    channel           VARCHAR(50)  NOT NULL,
    purpose           VARCHAR(50)  NOT NULL,
    code_hash         VARCHAR(255) NOT NULL,
    attempts          INTEGER     NOT NULL DEFAULT 0,
    max_attempts      INTEGER     NOT NULL,
    expires_at        TIMESTAMPTZ NOT NULL,
    status            VARCHAR(50)  NOT NULL,
    remember_me       BOOLEAN     NOT NULL DEFAULT FALSE,
    registration_data TEXT
);

CREATE TABLE auth_session (
    id          UUID        NOT NULL PRIMARY KEY,
    created_at  TIMESTAMPTZ NOT NULL,
    updated_at  TIMESTAMPTZ NOT NULL,
    user_id     UUID        NOT NULL REFERENCES app_user(id),
    token_hash  VARCHAR(255) NOT NULL UNIQUE,
    authority   VARCHAR(50)  NOT NULL,
    remembered  BOOLEAN     NOT NULL DEFAULT FALSE,
    activation_required BOOLEAN NOT NULL DEFAULT FALSE,
    expires_at  TIMESTAMPTZ NOT NULL,
    revoked_at  TIMESTAMPTZ
);

CREATE TABLE customer_profile (
    id           UUID        NOT NULL PRIMARY KEY,
    created_at   TIMESTAMPTZ NOT NULL,
    updated_at   TIMESTAMPTZ NOT NULL,
    user_id      UUID        NOT NULL UNIQUE REFERENCES app_user(id),
    display_name VARCHAR(255),
    phone        VARCHAR(255)
);

-- ---------------------------------------------------------------------------
-- Business
-- ---------------------------------------------------------------------------

CREATE TABLE category (
    id         UUID        NOT NULL PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    parent_id  UUID        REFERENCES category(id),
    name       VARCHAR(255) NOT NULL,
    slug       VARCHAR(255),
    status     VARCHAR(50)  NOT NULL
);

CREATE TABLE city (
    id           UUID        NOT NULL PRIMARY KEY,
    created_at   TIMESTAMPTZ NOT NULL,
    updated_at   TIMESTAMPTZ NOT NULL,
    name         VARCHAR(255) NOT NULL,
    country_code VARCHAR(10)  NOT NULL,
    status       VARCHAR(50)  NOT NULL
);

CREATE TABLE business (
    id          UUID        NOT NULL PRIMARY KEY,
    created_at  TIMESTAMPTZ NOT NULL,
    updated_at  TIMESTAMPTZ NOT NULL,
    name        VARCHAR(255) NOT NULL,
    legal_name  VARCHAR(255),
    bin         VARCHAR(255),
    status      VARCHAR(50)  NOT NULL
);

CREATE TABLE brand_profile (
    id             UUID        NOT NULL PRIMARY KEY,
    created_at     TIMESTAMPTZ NOT NULL,
    updated_at     TIMESTAMPTZ NOT NULL,
    business_id    UUID        NOT NULL UNIQUE REFERENCES business(id),
    brand_color    VARCHAR(255),
    logo_url       VARCHAR(255),
    cover_url      VARCHAR(255),
    tone_of_voice  VARCHAR(255),
    description    TEXT,
    instagram_url  VARCHAR(255),
    telegram_url   VARCHAR(255),
    website_url    VARCHAR(255)
);

CREATE TABLE brand_page_block (
    id             UUID        NOT NULL PRIMARY KEY,
    created_at     TIMESTAMPTZ NOT NULL,
    updated_at     TIMESTAMPTZ NOT NULL,
    business_id    UUID        NOT NULL REFERENCES business(id),
    block_type     VARCHAR(50) NOT NULL,
    display_order  INTEGER     NOT NULL,
    config_json    TEXT,
    enabled        BOOLEAN     NOT NULL DEFAULT TRUE
);

CREATE TABLE brand_drop (
    id             UUID         NOT NULL PRIMARY KEY,
    created_at     TIMESTAMPTZ  NOT NULL,
    updated_at     TIMESTAMPTZ  NOT NULL,
    business_id    UUID         NOT NULL REFERENCES business(id),
    name           VARCHAR(255) NOT NULL,
    description    TEXT,
    start_date     TIMESTAMPTZ,
    end_date       TIMESTAMPTZ,
    type           VARCHAR(50)  NOT NULL,
    status         VARCHAR(50)  NOT NULL,
    cover_url      VARCHAR(255)
);

CREATE TABLE business_branch (
    id          UUID        NOT NULL PRIMARY KEY,
    created_at  TIMESTAMPTZ NOT NULL,
    updated_at  TIMESTAMPTZ NOT NULL,
    business_id UUID        NOT NULL REFERENCES business(id),
    city_id     UUID        REFERENCES city(id),
    name        VARCHAR(255) NOT NULL,
    address     VARCHAR(255),
    latitude    NUMERIC,
    longitude   NUMERIC,
    online_only BOOLEAN     NOT NULL DEFAULT FALSE,
    status      VARCHAR(50)  NOT NULL
);

CREATE TABLE business_member (
    id          UUID        NOT NULL PRIMARY KEY,
    created_at  TIMESTAMPTZ NOT NULL,
    updated_at  TIMESTAMPTZ NOT NULL,
    business_id UUID        NOT NULL REFERENCES business(id),
    user_id     UUID        NOT NULL REFERENCES app_user(id),
    role        VARCHAR(50)  NOT NULL,
    status      VARCHAR(50)  NOT NULL
);

CREATE TABLE branch_member (
    id          UUID        NOT NULL PRIMARY KEY,
    created_at  TIMESTAMPTZ NOT NULL,
    updated_at  TIMESTAMPTZ NOT NULL,
    branch_id   UUID        NOT NULL REFERENCES business_branch(id),
    user_id     UUID        NOT NULL REFERENCES app_user(id),
    role        VARCHAR(50)  NOT NULL,
    status      VARCHAR(50)  NOT NULL,
    UNIQUE (branch_id, user_id)
);

CREATE TABLE branch_invite (
    id          UUID        NOT NULL PRIMARY KEY,
    created_at  TIMESTAMPTZ NOT NULL,
    updated_at  TIMESTAMPTZ NOT NULL,
    branch_id   UUID        NOT NULL REFERENCES business_branch(id),
    code        VARCHAR(255) NOT NULL UNIQUE,
    role        VARCHAR(50)  NOT NULL,
    max_uses    INTEGER     NOT NULL DEFAULT 1,
    use_count   INTEGER     NOT NULL DEFAULT 0,
    expires_at  TIMESTAMPTZ NOT NULL,
    created_by  UUID        NOT NULL REFERENCES app_user(id),
    revoked_at  TIMESTAMPTZ
);

CREATE TABLE business_contact (
    id           UUID        NOT NULL PRIMARY KEY,
    created_at   TIMESTAMPTZ NOT NULL,
    updated_at   TIMESTAMPTZ NOT NULL,
    business_id  UUID        NOT NULL REFERENCES business(id),
    branch_id    UUID        REFERENCES business_branch(id),
    contact_type    VARCHAR(50)  NOT NULL,
    contact_value   VARCHAR(255) NOT NULL,
    is_primary      BOOLEAN      NOT NULL DEFAULT FALSE,
    status          VARCHAR(50)  NOT NULL
);

CREATE TABLE data_source (
    id          UUID        NOT NULL PRIMARY KEY,
    created_at  TIMESTAMPTZ NOT NULL,
    updated_at  TIMESTAMPTZ NOT NULL,
    business_id UUID        NOT NULL REFERENCES business(id),
    source_type VARCHAR(50)  NOT NULL,
    name        VARCHAR(255) NOT NULL,
    status      VARCHAR(50)  NOT NULL
);

-- ---------------------------------------------------------------------------
-- Catalog
-- ---------------------------------------------------------------------------

CREATE TABLE product (
    id                  UUID        NOT NULL PRIMARY KEY,
    created_at          TIMESTAMPTZ NOT NULL,
    updated_at          TIMESTAMPTZ NOT NULL,
    business_id         UUID        NOT NULL REFERENCES business(id),
    category_id         UUID        REFERENCES category(id),
    category_label      VARCHAR(255),
    name                VARCHAR(255) NOT NULL,
    description         VARCHAR(255),
    sku                 VARCHAR(255),
    characteristics_json TEXT,
    moderation_status   VARCHAR(32)  NOT NULL DEFAULT 'PENDING'
        CHECK (moderation_status IN ('PENDING', 'APPROVED', 'REJECTED')),
    moderation_note     VARCHAR(500),
    status              VARCHAR(50)  NOT NULL
);

CREATE TABLE product_tag (
    product_id UUID         NOT NULL REFERENCES product(id),
    tag        VARCHAR(255) NOT NULL
);

CREATE TABLE catalog_import (
    id                UUID        NOT NULL PRIMARY KEY,
    created_at        TIMESTAMPTZ NOT NULL,
    updated_at        TIMESTAMPTZ NOT NULL,
    data_source_id    UUID        NOT NULL REFERENCES data_source(id),
    business_id       UUID        REFERENCES business(id),
    branch_id         UUID        REFERENCES business_branch(id),
    created_by        UUID        REFERENCES app_user(id),
    original_file_name VARCHAR(255) NOT NULL,
    status            VARCHAR(50)  NOT NULL,
    total_rows        INTEGER,
    valid_rows        INTEGER,
    invalid_rows      INTEGER,
    warning_rows      INTEGER,
    imported_at       TIMESTAMPTZ
);

CREATE TABLE catalog_import_column_mapping (
    id                  UUID        NOT NULL PRIMARY KEY,
    created_at          TIMESTAMPTZ NOT NULL,
    updated_at          TIMESTAMPTZ NOT NULL,
    catalog_import_id   UUID        NOT NULL REFERENCES catalog_import(id),
    source_column       VARCHAR(255) NOT NULL,
    target_field        VARCHAR(255) NOT NULL,
    characteristic_name VARCHAR(255),
    approved            BOOLEAN     NOT NULL DEFAULT FALSE,
    confidence          DOUBLE PRECISION
);

CREATE TABLE product_offer (
    id                      UUID        NOT NULL PRIMARY KEY,
    created_at              TIMESTAMPTZ NOT NULL,
    updated_at              TIMESTAMPTZ NOT NULL,
    product_id              UUID        NOT NULL REFERENCES product(id),
    branch_id               UUID        NOT NULL REFERENCES business_branch(id),
    data_source_id          UUID        REFERENCES data_source(id),
    price                   NUMERIC,
    enabled                 BOOLEAN     NOT NULL DEFAULT TRUE,
    status                  VARCHAR(50)  NOT NULL
);

CREATE TABLE raw_catalog_row (
    id                      UUID        NOT NULL PRIMARY KEY,
    created_at              TIMESTAMPTZ NOT NULL,
    updated_at              TIMESTAMPTZ NOT NULL,
    catalog_import_id       UUID        NOT NULL REFERENCES catalog_import(id),
    product_id              UUID        REFERENCES product(id),
    product_offer_id        UUID        REFERENCES product_offer(id),
    row_number              INTEGER     NOT NULL,
    row_payload             TEXT        NOT NULL,
    normalized_data_json    TEXT,
    validation_errors_json  TEXT,
    validation_warnings_json TEXT,
    status                  VARCHAR(50)
);

-- ---------------------------------------------------------------------------
-- Service
-- ---------------------------------------------------------------------------

CREATE TABLE service_offering (
    id             UUID        NOT NULL PRIMARY KEY,
    created_at     TIMESTAMPTZ NOT NULL,
    updated_at     TIMESTAMPTZ NOT NULL,
    business_id    UUID        NOT NULL REFERENCES business(id),
    category_id    UUID        REFERENCES category(id),
    category_label VARCHAR(255),
    name           VARCHAR(255) NOT NULL,
    description    VARCHAR(255),
    status         VARCHAR(50)  NOT NULL
);

CREATE TABLE service_branch_offer (
    id                      UUID        NOT NULL PRIMARY KEY,
    created_at              TIMESTAMPTZ NOT NULL,
    updated_at              TIMESTAMPTZ NOT NULL,
    service_offering_id     UUID        NOT NULL REFERENCES service_offering(id),
    branch_id               UUID        NOT NULL REFERENCES business_branch(id),
    service_mode            VARCHAR(50)  NOT NULL,
    base_price              NUMERIC,
    duration_minutes        INTEGER,
    schedule_text           VARCHAR(255),
    category_label          VARCHAR(255),
    active                  BOOLEAN     NOT NULL DEFAULT TRUE,
    status                  VARCHAR(50)  NOT NULL
);

CREATE TABLE service_resource (
    id            UUID        NOT NULL PRIMARY KEY,
    created_at    TIMESTAMPTZ NOT NULL,
    updated_at    TIMESTAMPTZ NOT NULL,
    branch_id     UUID        NOT NULL REFERENCES business_branch(id),
    name          VARCHAR(255) NOT NULL,
    resource_type VARCHAR(50)  NOT NULL,
    status        VARCHAR(50)  NOT NULL
);

CREATE TABLE resource_service_assignment (
    id                      UUID NOT NULL PRIMARY KEY,
    created_at              TIMESTAMPTZ NOT NULL,
    updated_at              TIMESTAMPTZ NOT NULL,
    resource_id             UUID NOT NULL REFERENCES service_resource(id),
    service_branch_offer_id UUID NOT NULL REFERENCES service_branch_offer(id)
);

CREATE TABLE service_schedule (
    id          UUID        NOT NULL PRIMARY KEY,
    created_at  TIMESTAMPTZ NOT NULL,
    updated_at  TIMESTAMPTZ NOT NULL,
    resource_id UUID        NOT NULL REFERENCES service_resource(id),
    timezone    VARCHAR(255) NOT NULL,
    status      VARCHAR(50)  NOT NULL
);

CREATE TABLE service_window (
    id          UUID        NOT NULL PRIMARY KEY,
    created_at  TIMESTAMPTZ NOT NULL,
    updated_at  TIMESTAMPTZ NOT NULL,
    schedule_id UUID        NOT NULL REFERENCES service_schedule(id),
    starts_at   TIMESTAMPTZ NOT NULL,
    ends_at     TIMESTAMPTZ NOT NULL,
    window_type VARCHAR(50)  NOT NULL
);

-- ---------------------------------------------------------------------------
-- Messaging
-- ---------------------------------------------------------------------------

CREATE TABLE conversation (
    id         UUID        NOT NULL PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    status     VARCHAR(50)  NOT NULL,
    subject    VARCHAR(255)
);

CREATE TABLE conversation_participant (
    id               UUID        NOT NULL PRIMARY KEY,
    created_at       TIMESTAMPTZ NOT NULL,
    updated_at       TIMESTAMPTZ NOT NULL,
    conversation_id  UUID        NOT NULL REFERENCES conversation(id),
    user_id          UUID        REFERENCES app_user(id),
    business_id      UUID        REFERENCES business(id),
    branch_id        UUID        REFERENCES business_branch(id),
    participant_type VARCHAR(50)  NOT NULL
);

CREATE TABLE conversation_message (
    id              UUID        NOT NULL PRIMARY KEY,
    created_at      TIMESTAMPTZ NOT NULL,
    updated_at      TIMESTAMPTZ NOT NULL,
    conversation_id UUID        NOT NULL REFERENCES conversation(id),
    sender_user_id  UUID        NOT NULL REFERENCES app_user(id),
    body            TEXT        NOT NULL,
    status          VARCHAR(50)  NOT NULL
);

CREATE TABLE conversation_link (
    id                      UUID NOT NULL PRIMARY KEY,
    created_at              TIMESTAMPTZ NOT NULL,
    updated_at              TIMESTAMPTZ NOT NULL,
    conversation_id         UUID NOT NULL REFERENCES conversation(id),
    customer_request_id     UUID,
    booking_id              UUID,
    product_offer_id        UUID REFERENCES product_offer(id),
    service_branch_offer_id UUID REFERENCES service_branch_offer(id)
);

-- ---------------------------------------------------------------------------
-- Request
-- ---------------------------------------------------------------------------

CREATE TABLE customer_request (
    id                      UUID        NOT NULL PRIMARY KEY,
    created_at              TIMESTAMPTZ NOT NULL,
    updated_at              TIMESTAMPTZ NOT NULL,
    user_id                 UUID        NOT NULL REFERENCES app_user(id),
    city_id                 UUID        NOT NULL REFERENCES city(id),
    category_id             UUID        REFERENCES category(id),
    product_offer_id        UUID        REFERENCES product_offer(id),
    service_branch_offer_id UUID        REFERENCES service_branch_offer(id),
    query_text              VARCHAR(255) NOT NULL,
    status                  VARCHAR(50)  NOT NULL,
    expires_at              TIMESTAMPTZ
);

CREATE TABLE request_target (
    id                  UUID        NOT NULL PRIMARY KEY,
    created_at          TIMESTAMPTZ NOT NULL,
    updated_at          TIMESTAMPTZ NOT NULL,
    customer_request_id UUID        NOT NULL REFERENCES customer_request(id),
    branch_id           UUID        NOT NULL REFERENCES business_branch(id),
    status              VARCHAR(50)  NOT NULL
);

CREATE TABLE supplier_response (
    id                UUID        NOT NULL PRIMARY KEY,
    created_at        TIMESTAMPTZ NOT NULL,
    updated_at        TIMESTAMPTZ NOT NULL,
    request_target_id UUID        NOT NULL REFERENCES request_target(id),
    status            VARCHAR(50)  NOT NULL,
    response_source   VARCHAR(50)  NOT NULL DEFAULT 'BUSINESS_CONFIRMED',
    price             NUMERIC,
    product_hint      VARCHAR(255),
    comment           VARCHAR(255)
);

-- ---------------------------------------------------------------------------
-- Booking
-- ---------------------------------------------------------------------------

CREATE TABLE booking (
    id                      UUID        NOT NULL PRIMARY KEY,
    created_at              TIMESTAMPTZ NOT NULL,
    updated_at              TIMESTAMPTZ NOT NULL,
    customer_id             UUID        NOT NULL REFERENCES app_user(id),
    service_branch_offer_id UUID        NOT NULL REFERENCES service_branch_offer(id),
    branch_id               UUID        NOT NULL REFERENCES business_branch(id),
    conversation_id         UUID        REFERENCES conversation(id),
    status                  VARCHAR(50)  NOT NULL,
    requested_start_at      TIMESTAMPTZ,
    confirmed_start_at      TIMESTAMPTZ,
    confirmed_end_at        TIMESTAMPTZ,
    source                  VARCHAR(50)  NOT NULL,
    customer_note           VARCHAR(255),
    provider_note           VARCHAR(255)
);

ALTER TABLE conversation_link ADD CONSTRAINT fk_conversation_link_booking
    FOREIGN KEY (booking_id) REFERENCES booking(id);

ALTER TABLE conversation_link ADD CONSTRAINT fk_conversation_link_customer_request
    FOREIGN KEY (customer_request_id) REFERENCES customer_request(id);

-- ---------------------------------------------------------------------------
-- Search
-- ---------------------------------------------------------------------------

CREATE TABLE search_session (
    id                   UUID        NOT NULL PRIMARY KEY,
    created_at           TIMESTAMPTZ NOT NULL,
    updated_at           TIMESTAMPTZ NOT NULL,
    user_id              UUID        NOT NULL REFERENCES app_user(id),
    city_id              UUID        NOT NULL REFERENCES city(id),
    category_id          UUID        REFERENCES category(id),
    raw_query            VARCHAR(255) NOT NULL,
    scope                VARCHAR(50)  NOT NULL,
    status               VARCHAR(50)  NOT NULL,
    started_at           TIMESTAMPTZ NOT NULL,
    last_active_at       TIMESTAMPTZ NOT NULL,
    snapshot_expires_at  TIMESTAMPTZ
);

CREATE TABLE search_snapshot (
    id                    UUID        NOT NULL PRIMARY KEY,
    created_at            TIMESTAMPTZ NOT NULL,
    updated_at            TIMESTAMPTZ NOT NULL,
    search_session_id     UUID        NOT NULL REFERENCES search_session(id),
    user_id               UUID        NOT NULL REFERENCES app_user(id),
    raw_query             VARCHAR(255) NOT NULL,
    scope                 VARCHAR(50)  NOT NULL,
    city_id               UUID        NOT NULL REFERENCES city(id),
    category_id           UUID        REFERENCES category(id),
    status                VARCHAR(50)  NOT NULL,
    result_count          INTEGER     NOT NULL DEFAULT 0,
    product_result_count  INTEGER     NOT NULL DEFAULT 0,
    service_result_count  INTEGER     NOT NULL DEFAULT 0,
    request_id            UUID        REFERENCES customer_request(id),
    booking_id            UUID        REFERENCES booking(id),
    expires_at            TIMESTAMPTZ NOT NULL
);

CREATE TABLE search_result_snapshot (
    id                      UUID        NOT NULL PRIMARY KEY,
    created_at              TIMESTAMPTZ NOT NULL,
    updated_at              TIMESTAMPTZ NOT NULL,
    search_snapshot_id      UUID        NOT NULL REFERENCES search_snapshot(id),
    result_type             VARCHAR(50)  NOT NULL,
    result_rank             INTEGER     NOT NULL,
    product_offer_id        UUID        REFERENCES product_offer(id),
    service_branch_offer_id UUID        REFERENCES service_branch_offer(id),
    business_id             UUID        REFERENCES business(id),
    branch_id               UUID        REFERENCES business_branch(id),
    title                   VARCHAR(255) NOT NULL,
    summary                 VARCHAR(255),
    price                   NUMERIC,
    status_label_key        VARCHAR(255),
    source_type             VARCHAR(50),
    distance_meters         INTEGER
);

CREATE TABLE search_document (
    id                      UUID        NOT NULL PRIMARY KEY,
    created_at              TIMESTAMPTZ NOT NULL,
    updated_at              TIMESTAMPTZ NOT NULL,
    document_type           VARCHAR(50)  NOT NULL,
    product_offer_id        UUID        REFERENCES product_offer(id),
    service_branch_offer_id UUID        REFERENCES service_branch_offer(id),
    title                   VARCHAR(255) NOT NULL,
    summary                 VARCHAR(255),
    category_label          VARCHAR(255),
    sku                     VARCHAR(255),
    characteristics_json    TEXT,
    business_id             UUID        REFERENCES business(id),
    branch_id               UUID        REFERENCES business_branch(id),
    price                   NUMERIC,
    status                  VARCHAR(50)  NOT NULL,
    source                  VARCHAR(50),
    public_note             VARCHAR(500)
);

CREATE TABLE search_document_token (
    search_document_id UUID         NOT NULL REFERENCES search_document(id),
    token              VARCHAR(255) NOT NULL
);

CREATE TABLE search_query_alias (
    id           UUID         PRIMARY KEY,
    created_at   TIMESTAMPTZ  NOT NULL,
    updated_at   TIMESTAMPTZ  NOT NULL,
    alias_value  VARCHAR(255) NOT NULL,
    target_query VARCHAR(255) NOT NULL,
    status       VARCHAR(50)  NOT NULL
);

-- ---------------------------------------------------------------------------
-- Indexes
-- ---------------------------------------------------------------------------

CREATE INDEX idx_app_user_email   ON app_user (email);
CREATE INDEX idx_app_user_phone   ON app_user (phone);
CREATE INDEX idx_app_user_status ON app_user (status);
CREATE INDEX idx_app_user_role   ON app_user (role);

CREATE INDEX idx_auth_challenge_user   ON auth_challenge (user_id);
CREATE INDEX idx_auth_challenge_status ON auth_challenge (status);

CREATE INDEX idx_auth_session_user      ON auth_session (user_id);
CREATE INDEX idx_auth_session_token     ON auth_session (token_hash);

CREATE INDEX idx_business_branch_business ON business_branch (business_id);
CREATE INDEX idx_business_branch_city     ON business_branch (city_id);

CREATE INDEX idx_brand_profile_business ON brand_profile (business_id);
CREATE INDEX idx_brand_page_block_business_order ON brand_page_block (business_id, display_order);
CREATE INDEX idx_brand_drop_business_status ON brand_drop (business_id, status);

CREATE INDEX idx_business_member_user     ON business_member (user_id);
CREATE INDEX idx_business_member_business ON business_member (business_id);

CREATE INDEX idx_branch_member_branch ON branch_member (branch_id);
CREATE INDEX idx_branch_member_user   ON branch_member (user_id);

CREATE INDEX idx_branch_invite_branch ON branch_invite (branch_id);
CREATE INDEX idx_branch_invite_code   ON branch_invite (code);

CREATE INDEX idx_business_contact_business ON business_contact (business_id);
CREATE INDEX idx_business_contact_branch   ON business_contact (branch_id);

CREATE INDEX idx_product_business  ON product (business_id);
CREATE INDEX idx_product_category  ON product (category_id);
CREATE INDEX idx_product_moderation_status ON product (moderation_status, created_at);

CREATE INDEX idx_product_offer_product ON product_offer (product_id);
CREATE INDEX idx_product_offer_branch  ON product_offer (branch_id);
CREATE INDEX idx_product_offer_enabled ON product_offer (enabled);

CREATE INDEX idx_service_branch_offer_service ON service_branch_offer (service_offering_id);
CREATE INDEX idx_service_branch_offer_branch  ON service_branch_offer (branch_id);
CREATE INDEX idx_service_branch_offer_active  ON service_branch_offer (active);

CREATE INDEX idx_conversation_message_conv ON conversation_message (conversation_id);

CREATE INDEX idx_customer_request_user ON customer_request (user_id);

CREATE INDEX idx_search_session_user ON search_session (user_id);

CREATE INDEX idx_search_result_snapshot_parent ON search_result_snapshot (search_snapshot_id);

CREATE INDEX idx_search_query_alias_value ON search_query_alias (alias_value, status);

CREATE INDEX idx_booking_customer ON booking (customer_id);

CREATE INDEX idx_catalog_import_business ON catalog_import (business_id);
CREATE INDEX idx_catalog_import_branch ON catalog_import (branch_id);
CREATE INDEX idx_raw_catalog_row_import ON raw_catalog_row (catalog_import_id);
CREATE INDEX idx_catalog_import_column_mapping_import ON catalog_import_column_mapping (catalog_import_id);
CREATE INDEX idx_data_source_business_type ON data_source (business_id, source_type);

-- ---------------------------------------------------------------------------
-- Reference data: cities
-- ---------------------------------------------------------------------------
INSERT INTO city (id, created_at, updated_at, name, country_code, status) VALUES
  ('00000000-0000-0000-0000-0000000000c1', now(), now(), 'Кызылорда',         'KZ', 'ACTIVE'),
  ('00000000-0000-0000-0000-0000000000c2', now(), now(), 'Алматы',             'KZ', 'ACTIVE'),
  ('00000000-0000-0000-0000-0000000000c3', now(), now(), 'Астана',             'KZ', 'ACTIVE'),
  ('00000000-0000-0000-0000-0000000000c4', now(), now(), 'Шымкент',            'KZ', 'ACTIVE'),
  ('00000000-0000-0000-0000-0000000000c5', now(), now(), 'Караганда',          'KZ', 'ACTIVE'),
  ('00000000-0000-0000-0000-0000000000c6', now(), now(), 'Актобе',             'KZ', 'ACTIVE'),
  ('00000000-0000-0000-0000-0000000000c7', now(), now(), 'Тараз',              'KZ', 'ACTIVE'),
  ('00000000-0000-0000-0000-0000000000c8', now(), now(), 'Павлодар',           'KZ', 'ACTIVE'),
  ('00000000-0000-0000-0000-0000000000c9', now(), now(), 'Усть-Каменогорск',   'KZ', 'ACTIVE'),
  ('00000000-0000-0000-0000-0000000000ca', now(), now(), 'Семей',              'KZ', 'ACTIVE'),
  ('00000000-0000-0000-0000-0000000000cb', now(), now(), 'Атырау',             'KZ', 'ACTIVE'),
  ('00000000-0000-0000-0000-0000000000cc', now(), now(), 'Костанай',           'KZ', 'ACTIVE'),
  ('00000000-0000-0000-0000-0000000000cd', now(), now(), 'Уральск',            'KZ', 'ACTIVE'),
  ('00000000-0000-0000-0000-0000000000ce', now(), now(), 'Петропавловск',      'KZ', 'ACTIVE'),
  ('00000000-0000-0000-0000-0000000000cf', now(), now(), 'Актау',              'KZ', 'ACTIVE'),
  ('00000000-0000-0000-0000-0000000000d0', now(), now(), 'Темиртау',           'KZ', 'ACTIVE'),
  ('00000000-0000-0000-0000-0000000000d1', now(), now(), 'Туркестан',          'KZ', 'ACTIVE'),
  ('00000000-0000-0000-0000-0000000000d2', now(), now(), 'Кокшетау',           'KZ', 'ACTIVE'),
  ('00000000-0000-0000-0000-0000000000d3', now(), now(), 'Талдыкорган',        'KZ', 'ACTIVE'),
  ('00000000-0000-0000-0000-0000000000d4', now(), now(), 'Экибастуз',          'KZ', 'ACTIVE'),
  ('00000000-0000-0000-0000-0000000000d5', now(), now(), 'Рудный',             'KZ', 'ACTIVE'),
  ('00000000-0000-0000-0000-0000000000d6', now(), now(), 'Жезказган',          'KZ', 'ACTIVE'),
  ('00000000-0000-0000-0000-0000000000d7', now(), now(), 'Конаев',             'KZ', 'ACTIVE');

-- ---------------------------------------------------------------------------
-- Reference data: categories
-- ---------------------------------------------------------------------------
INSERT INTO category (id, created_at, updated_at, parent_id, name, slug, status) VALUES
  ('00000000-0000-0000-0000-0000000000a1', now(), now(), null, 'Автозапчасти',    'autoparts',    'ACTIVE'),
  ('00000000-0000-0000-0000-0000000000a2', now(), now(), null, 'Бытовая техника',  'appliances',   'ACTIVE'),
  ('00000000-0000-0000-0000-0000000000a3', now(), now(), null, 'Услуги красоты',   'beauty',       'ACTIVE'),
  ('00000000-0000-0000-0000-0000000000a4', now(), now(), null, 'Ремонт и сервис',  'repair',       'ACTIVE'),
  ('00000000-0000-0000-0000-0000000000a5', now(), now(), null, 'Строительство',    'construction', 'ACTIVE'),
  ('00000000-0000-0000-0000-0000000000af', now(), now(), null, 'Общее',           'general',      'ACTIVE');
-- =============================================================================

CREATE TABLE autodump_import_session (
    id                UUID         NOT NULL PRIMARY KEY,
    created_at        TIMESTAMPTZ  NOT NULL,
    updated_at        TIMESTAMPTZ  NOT NULL,
    business_id       UUID         NOT NULL REFERENCES business(id),
    branch_id         UUID         NOT NULL REFERENCES business_branch(id),
    created_by        UUID         NOT NULL REFERENCES app_user(id),
    source_type       VARCHAR(50)  NOT NULL,
    status            VARCHAR(50)  NOT NULL,
    input_summary     VARCHAR(500),
    total_draft_count INTEGER      NOT NULL DEFAULT 0,
    approved_count    INTEGER      NOT NULL DEFAULT 0,
    rejected_count    INTEGER      NOT NULL DEFAULT 0,
    error_count       INTEGER      NOT NULL DEFAULT 0,
    completed_at      TIMESTAMPTZ
);

CREATE TABLE autodump_raw_input (
    id                 UUID         NOT NULL PRIMARY KEY,
    created_at         TIMESTAMPTZ  NOT NULL,
    updated_at         TIMESTAMPTZ  NOT NULL,
    import_session_id  UUID         NOT NULL REFERENCES autodump_import_session(id),
    original_file_name VARCHAR(500),
    content_type       VARCHAR(255),
    storage_kind       VARCHAR(50)  NOT NULL,
    storage_ref        VARCHAR(1000),
    raw_text           TEXT,
    sha256             VARCHAR(64),
    size_bytes         BIGINT
);

CREATE TABLE autodump_ai_job (
    id                    UUID         NOT NULL PRIMARY KEY,
    created_at            TIMESTAMPTZ  NOT NULL,
    updated_at            TIMESTAMPTZ  NOT NULL,
    import_session_id     UUID         NOT NULL REFERENCES autodump_import_session(id),
    raw_input_id          UUID         REFERENCES autodump_raw_input(id),
    status                VARCHAR(50)  NOT NULL,
    provider              VARCHAR(50),
    model                 VARCHAR(100),
    prompt_version        VARCHAR(50),
    input_token_estimate  INTEGER,
    output_token_estimate INTEGER,
    raw_response_json     TEXT,
    error_message         TEXT,
    attempt_count         INTEGER      NOT NULL DEFAULT 0,
    started_at            TIMESTAMPTZ,
    finished_at           TIMESTAMPTZ
);

CREATE TABLE autodump_draft_item (
    id                              UUID         NOT NULL PRIMARY KEY,
    created_at                      TIMESTAMPTZ  NOT NULL,
    updated_at                      TIMESTAMPTZ  NOT NULL,
    import_session_id               UUID         NOT NULL REFERENCES autodump_import_session(id),
    ai_job_id                       UUID         REFERENCES autodump_ai_job(id),
    item_type                       VARCHAR(20)  NOT NULL,
    status                          VARCHAR(50)  NOT NULL,
    title                           VARCHAR(500),
    normalized_title                VARCHAR(500),
    category_label                  VARCHAR(255),
    subcategory_label               VARCHAR(255),
    description                     TEXT,
    price                           NUMERIC,
    price_text                      VARCHAR(255),
    currency                        VARCHAR(10)  DEFAULT 'KZT',
    brand                           VARCHAR(255),
    tags_json                       TEXT,
    custom_attributes_json          TEXT,
    source_reference                TEXT,
    confidence_notes                TEXT,
    needs_review                    BOOLEAN      NOT NULL DEFAULT FALSE,
    duplicate_group_key             VARCHAR(255),
    published_product_offer_id      UUID         REFERENCES product_offer(id),
    published_service_branch_offer_id UUID       REFERENCES service_branch_offer(id)
);

CREATE TABLE autodump_draft_attribute (
    id              UUID         NOT NULL PRIMARY KEY,
    created_at      TIMESTAMPTZ  NOT NULL,
    updated_at      TIMESTAMPTZ  NOT NULL,
    draft_item_id   UUID         NOT NULL REFERENCES autodump_draft_item(id),
    attribute_key   VARCHAR(255) NOT NULL,
    attribute_value TEXT         NOT NULL,
    source          VARCHAR(50)  NOT NULL
);

CREATE TABLE autodump_audit_event (
    id                 UUID         NOT NULL PRIMARY KEY,
    created_at         TIMESTAMPTZ  NOT NULL,
    updated_at         TIMESTAMPTZ  NOT NULL,
    import_session_id  UUID         NOT NULL REFERENCES autodump_import_session(id),
    draft_item_id      UUID         REFERENCES autodump_draft_item(id),
    actor_user_id      UUID         REFERENCES app_user(id),
    event_type         VARCHAR(50)  NOT NULL,
    payload_json       TEXT
);

CREATE TABLE autodump_import_error (
    id                 UUID         NOT NULL PRIMARY KEY,
    created_at         TIMESTAMPTZ  NOT NULL,
    updated_at         TIMESTAMPTZ  NOT NULL,
    import_session_id  UUID         NOT NULL REFERENCES autodump_import_session(id),
    draft_item_id      UUID         REFERENCES autodump_draft_item(id),
    severity           VARCHAR(20)  NOT NULL,
    code               VARCHAR(100) NOT NULL,
    message            TEXT         NOT NULL,
    payload_json       TEXT
);

CREATE INDEX idx_autodump_session_branch   ON autodump_import_session(branch_id);
CREATE INDEX idx_autodump_session_status   ON autodump_import_session(business_id, status);
CREATE INDEX idx_autodump_raw_input_session ON autodump_raw_input(import_session_id);
CREATE INDEX idx_autodump_ai_job_session   ON autodump_ai_job(import_session_id);
CREATE INDEX idx_autodump_ai_job_status    ON autodump_ai_job(status);
CREATE INDEX idx_autodump_draft_session    ON autodump_draft_item(import_session_id);
CREATE INDEX idx_autodump_draft_status     ON autodump_draft_item(import_session_id, status);
CREATE INDEX idx_autodump_audit_session    ON autodump_audit_event(import_session_id);
CREATE INDEX idx_autodump_error_session    ON autodump_import_error(import_session_id);

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

CREATE TABLE business_card (
    id           UUID        NOT NULL PRIMARY KEY,
    created_at   TIMESTAMPTZ NOT NULL,
    updated_at   TIMESTAMPTZ NOT NULL,
    business_id  UUID        NOT NULL UNIQUE REFERENCES business(id),
    blocks       TEXT        NOT NULL DEFAULT '[]',
    published_at TIMESTAMPTZ,
    status       VARCHAR(50) NOT NULL DEFAULT 'DRAFT'
);

-- =============================================================================
-- Consolidated legacy schema additions
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
    business_id UUID REFERENCES business(id),
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

WITH ranked_users AS (
    SELECT id,
           lower(email) AS normalized_email,
           first_value(id) OVER (
               PARTITION BY lower(email)
               ORDER BY CASE WHEN role = 'CUSTOMER' THEN 0 ELSE 1 END, created_at, id
           ) AS canonical_user_id
    FROM app_user
    WHERE email IS NOT NULL
)
DELETE FROM business_member duplicate
USING ranked_users duplicate_user, ranked_users canonical_user, business_member canonical_member
WHERE duplicate.user_id = duplicate_user.id
  AND canonical_user.id = duplicate_user.canonical_user_id
  AND canonical_member.user_id = canonical_user.id
  AND canonical_member.business_id = duplicate.business_id
  AND duplicate.id <> canonical_member.id;

WITH ranked_users AS (
    SELECT id,
           first_value(id) OVER (
               PARTITION BY lower(email)
               ORDER BY CASE WHEN role = 'CUSTOMER' THEN 0 ELSE 1 END, created_at, id
           ) AS canonical_user_id
    FROM app_user
    WHERE email IS NOT NULL
)
UPDATE business_member member
SET user_id = ranked_users.canonical_user_id
FROM ranked_users
WHERE member.user_id = ranked_users.id
  AND member.user_id <> ranked_users.canonical_user_id;

WITH ranked_users AS (
    SELECT id,
           first_value(id) OVER (
               PARTITION BY lower(email)
               ORDER BY CASE WHEN role = 'CUSTOMER' THEN 0 ELSE 1 END, created_at, id
           ) AS canonical_user_id
    FROM app_user
    WHERE email IS NOT NULL
)
DELETE FROM branch_member duplicate
USING ranked_users duplicate_user, branch_member canonical_member
WHERE duplicate.user_id = duplicate_user.id
  AND canonical_member.user_id = duplicate_user.canonical_user_id
  AND canonical_member.branch_id = duplicate.branch_id
  AND duplicate.id <> canonical_member.id;

WITH ranked_users AS (
    SELECT id,
           first_value(id) OVER (
               PARTITION BY lower(email)
               ORDER BY CASE WHEN role = 'CUSTOMER' THEN 0 ELSE 1 END, created_at, id
           ) AS canonical_user_id
    FROM app_user
    WHERE email IS NOT NULL
)
UPDATE branch_member member
SET user_id = ranked_users.canonical_user_id
FROM ranked_users
WHERE member.user_id = ranked_users.id
  AND member.user_id <> ranked_users.canonical_user_id;

UPDATE business_member
SET role = 'WORKER'
WHERE role = 'MEMBER';

CREATE UNIQUE INDEX IF NOT EXISTS uq_business_member_business_user
    ON business_member (business_id, user_id);

INSERT INTO customer_profile (id, created_at, updated_at, user_id, display_name)
SELECT md5('customer-profile:' || user_account.id::text)::uuid,
       now(),
       now(),
       user_account.id,
       user_account.display_name
FROM app_user user_account
WHERE NOT EXISTS (
    SELECT 1
    FROM customer_profile profile
    WHERE profile.user_id = user_account.id
);

CREATE TABLE platform_membership (
    id         UUID        NOT NULL PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    user_id    UUID        NOT NULL UNIQUE REFERENCES app_user(id),
    role       VARCHAR(50) NOT NULL,
    status     VARCHAR(50) NOT NULL
);

CREATE TABLE platform_membership_permission (
    platform_membership_id UUID        NOT NULL REFERENCES platform_membership(id) ON DELETE CASCADE,
    permission             VARCHAR(80) NOT NULL,
    PRIMARY KEY (platform_membership_id, permission)
);

WITH platform_users AS (
    SELECT user_account.id,
           first_value(user_account.id) OVER (
               PARTITION BY lower(user_account.email)
               ORDER BY CASE WHEN user_account.role = 'CUSTOMER' THEN 0 ELSE 1 END,
                        user_account.created_at,
                        user_account.id
           ) AS canonical_user_id,
           user_account.role,
           user_account.status
    FROM app_user user_account
    WHERE user_account.email IS NOT NULL
)
INSERT INTO platform_membership (id, created_at, updated_at, user_id, role, status)
SELECT md5('platform-membership:' || canonical_user_id::text)::uuid,
       now(),
       now(),
       canonical_user_id,
       CASE role
           WHEN 'PLATFORM_SUPER_ADMIN' THEN 'SUPER_ADMIN'
           WHEN 'PLATFORM_ADMIN' THEN 'ADMIN'
           ELSE 'MODERATOR'
       END,
       CASE WHEN status = 'ACTIVE' THEN 'ACTIVE' ELSE 'INACTIVE' END
FROM platform_users
WHERE role IN ('PLATFORM_SUPER_ADMIN', 'PLATFORM_ADMIN', 'PLATFORM_MODERATOR')
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO platform_membership_permission (platform_membership_id, permission)
SELECT membership.id, permission.permission
FROM platform_membership membership
CROSS JOIN LATERAL (
    SELECT unnest(
        CASE membership.role
            WHEN 'SUPER_ADMIN' THEN ARRAY[
                'MANAGE_PLATFORM_USERS',
                'MANAGE_MANAGED_IMPORTS',
                'EDIT_CATALOG_DURING_IMPORT',
                'PUBLISH_CATALOG_DURING_IMPORT',
                'MANAGE_SUPPORT_CHATS',
                'MODERATE_CONTENT',
                'SUSPEND_BUSINESS',
                'BAN_BUSINESS'
            ]::VARCHAR[]
            WHEN 'ADMIN' THEN ARRAY[
                'MANAGE_MANAGED_IMPORTS',
                'EDIT_CATALOG_DURING_IMPORT',
                'PUBLISH_CATALOG_DURING_IMPORT',
                'MANAGE_SUPPORT_CHATS'
            ]::VARCHAR[]
            ELSE ARRAY[
                'MODERATE_CONTENT',
                'SUSPEND_BUSINESS'
            ]::VARCHAR[]
        END
    ) AS permission
) permission
ON CONFLICT DO NOTHING;

CREATE TABLE business_member_branch (
    id                     UUID        NOT NULL PRIMARY KEY,
    created_at             TIMESTAMPTZ NOT NULL,
    updated_at             TIMESTAMPTZ NOT NULL,
    business_membership_id UUID        NOT NULL REFERENCES business_member(id) ON DELETE CASCADE,
    branch_id              UUID        NOT NULL REFERENCES business_branch(id) ON DELETE CASCADE,
    UNIQUE (business_membership_id, branch_id)
);

INSERT INTO business_member_branch (id, created_at, updated_at, business_membership_id, branch_id)
SELECT md5('business-member-branch:' || membership.id::text || ':' || branch_member.branch_id::text)::uuid,
       now(),
       now(),
       membership.id,
       branch_member.branch_id
FROM branch_member
JOIN business_branch branch ON branch.id = branch_member.branch_id
JOIN business_member membership
  ON membership.business_id = branch.business_id
 AND membership.user_id = branch_member.user_id
ON CONFLICT (business_membership_id, branch_id) DO NOTHING;

CREATE INDEX idx_platform_membership_status
    ON platform_membership (status);

CREATE INDEX idx_business_member_branch_branch
    ON business_member_branch (branch_id);

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
    expires_at          TIMESTAMPTZ  NOT NULL,
    accepted_by_user_id UUID         REFERENCES app_user(id),
    accepted_at         TIMESTAMPTZ,
    declined_at         TIMESTAMPTZ,
    revoked_at          TIMESTAMPTZ
);

CREATE TABLE business_invitation_branch (
    business_invitation_id UUID NOT NULL REFERENCES business_invitation(id) ON DELETE CASCADE,
    branch_id              UUID NOT NULL REFERENCES business_branch(id) ON DELETE CASCADE,
    PRIMARY KEY (business_invitation_id, branch_id)
);

CREATE INDEX idx_business_invitation_business_status
    ON business_invitation (business_id, status);

CREATE INDEX idx_business_invitation_email_status
    ON business_invitation (lower(invited_email), status);

CREATE UNIQUE INDEX uq_business_invitation_pending
    ON business_invitation (business_id, lower(invited_email), invited_role)
    WHERE status = 'PENDING';

CREATE TABLE legal_document (
    id UUID NOT NULL PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    code VARCHAR(64) NOT NULL,
    version VARCHAR(32) NOT NULL,
    country_code VARCHAR(8) NOT NULL,
    locale VARCHAR(8) NOT NULL,
    public_url VARCHAR(512) NOT NULL,
    effective_at TIMESTAMPTZ NOT NULL,
    active BOOLEAN NOT NULL,
    CONSTRAINT uq_legal_document_version UNIQUE (code, version, country_code, locale)
);

CREATE TABLE legal_acceptance (
    id UUID NOT NULL PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    user_id UUID NOT NULL REFERENCES app_user(id),
    document_code VARCHAR(64) NOT NULL,
    document_version VARCHAR(32) NOT NULL,
    country_code VARCHAR(8) NOT NULL,
    locale VARCHAR(8) NOT NULL,
    acceptance_channel VARCHAR(64) NOT NULL,
    accepted_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_legal_acceptance UNIQUE (
        user_id,
        document_code,
        document_version,
        country_code,
        locale
    )
);

CREATE INDEX idx_legal_document_active
    ON legal_document (country_code, locale, active);

CREATE INDEX idx_legal_acceptance_user
    ON legal_acceptance (user_id, accepted_at DESC);

INSERT INTO legal_document (
    id,
    created_at,
    updated_at,
    code,
    version,
    country_code,
    locale,
    public_url,
    effective_at,
    active
)
SELECT
    md5(code || ':' || locale || ':1.0')::uuid,
    NOW(),
    NOW(),
    code,
    '1.0',
    'KZ',
    locale,
    public_url,
    NOW(),
    TRUE
FROM (
    VALUES
        ('USER_TERMS', '/legal/user-terms'),
        ('PRIVACY_POLICY', '/legal/privacy'),
        ('SELLER_TERMS', '/legal/seller-terms'),
        ('PERSONAL_DATA_CONSENT', '/legal/personal-data-consent'),
        ('MANAGED_IMPORT_TERMS', '/legal/import-service'),
        ('PROHIBITED_PRODUCTS_POLICY', '/legal/prohibited-products'),
        ('CONTENT_POLICY', '/legal/content-policy')
) AS documents(code, public_url)
CROSS JOIN (
    VALUES ('ru'), ('kk'), ('en')
) AS locales(locale);

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

ALTER TABLE chat_conversation
    ADD COLUMN conversation_type VARCHAR(32) NOT NULL DEFAULT 'GENERAL_SUPPORT',
    ADD COLUMN conversation_status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    ADD COLUMN managed_import_request_id UUID;

CREATE TABLE managed_import_request (
    id UUID NOT NULL PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    business_id UUID NOT NULL REFERENCES business(id),
    requested_by_user_id UUID NOT NULL REFERENCES app_user(id),
    status VARCHAR(32) NOT NULL,
    preferred_contact_channel VARCHAR(32) NOT NULL,
    preferred_contact_value VARCHAR(512) NOT NULL,
    source_links TEXT,
    source_notes TEXT,
    conversation_id UUID,
    responsible_platform_user_id UUID REFERENCES app_user(id),
    activated_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    products_published_count INTEGER
);

CREATE TABLE managed_import_request_source (
    managed_import_request_id UUID NOT NULL REFERENCES managed_import_request(id) ON DELETE CASCADE,
    source_type VARCHAR(32) NOT NULL,
    PRIMARY KEY (managed_import_request_id, source_type)
);

CREATE TABLE managed_import_grant (
    id UUID NOT NULL PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    business_id UUID NOT NULL REFERENCES business(id),
    managed_import_request_id UUID NOT NULL UNIQUE REFERENCES managed_import_request(id),
    granted_by_user_id UUID NOT NULL REFERENCES app_user(id),
    status VARCHAR(32) NOT NULL,
    granted_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ
);

CREATE INDEX idx_managed_import_request_status
    ON managed_import_request (status, created_at);

CREATE INDEX idx_managed_import_grant_business_status
    ON managed_import_grant (business_id, status);

ALTER TABLE chat_conversation
    ADD CONSTRAINT fk_chat_managed_import_request
    FOREIGN KEY (managed_import_request_id)
    REFERENCES managed_import_request(id);

ALTER TABLE business
    ADD COLUMN moderation_status VARCHAR(32) NOT NULL DEFAULT 'VISIBLE';

CREATE TABLE content_report (
    id UUID NOT NULL PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    reporter_user_id UUID NOT NULL REFERENCES app_user(id),
    target_type VARCHAR(32) NOT NULL,
    target_id UUID NOT NULL,
    reason_code VARCHAR(64) NOT NULL,
    details TEXT,
    status VARCHAR(32) NOT NULL,
    resolved_by_user_id UUID REFERENCES app_user(id),
    resolved_at TIMESTAMPTZ
);

CREATE INDEX idx_content_report_status_created
    ON content_report (status, created_at);

CREATE INDEX idx_content_report_target
    ON content_report (target_type, target_id);

CREATE TABLE significant_event (
    id UUID NOT NULL PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    actor_user_id UUID,
    event_type VARCHAR(64) NOT NULL,
    business_id UUID,
    entity_id UUID,
    metadata JSONB NOT NULL DEFAULT '{}'::jsonb
);

CREATE INDEX idx_significant_event_business_created
    ON significant_event (business_id, created_at);

CREATE INDEX idx_significant_event_type_created
    ON significant_event (event_type, created_at);

ALTER TABLE product
    ADD COLUMN hidden_by_moderator BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE chat_attachment (
    id UUID NOT NULL PRIMARY KEY,
    conversation_id UUID NOT NULL,
    stored_name VARCHAR(128) NOT NULL UNIQUE,
    original_name VARCHAR(512) NOT NULL,
    content_type VARCHAR(128) NOT NULL,
    size_bytes BIGINT NOT NULL,
    uploaded_by_user_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_chat_attachment_conversation
    ON chat_attachment (conversation_id);

DROP TABLE IF EXISTS branch_invite;

WITH ranked_open_grants AS (
    SELECT
        grant_row.id,
        ROW_NUMBER() OVER (
            PARTITION BY grant_row.business_id
            ORDER BY
                request_row.created_at ASC,
                grant_row.granted_at ASC,
                grant_row.created_at ASC,
                grant_row.id ASC
        ) AS rank_in_business
    FROM managed_import_grant grant_row
    JOIN managed_import_request request_row
      ON request_row.id = grant_row.managed_import_request_id
    WHERE grant_row.status = 'ACTIVE'
      AND request_row.status IN ('PENDING', 'ACTIVE')
),
retained_grants AS (
    SELECT id
    FROM ranked_open_grants
    WHERE rank_in_business = 1
)
UPDATE managed_import_grant grant_row
SET status = 'REVOKED',
    revoked_at = COALESCE(grant_row.revoked_at, NOW()),
    updated_at = NOW()
WHERE grant_row.status = 'ACTIVE'
  AND NOT EXISTS (
      SELECT 1
      FROM retained_grants retained
      WHERE retained.id = grant_row.id
  );

CREATE UNIQUE INDEX IF NOT EXISTS uq_managed_import_grant_active_business
    ON managed_import_grant (business_id)
    WHERE status = 'ACTIVE';

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_chat_attachment_conversation'
          AND conrelid = 'chat_attachment'::regclass
    ) THEN
        ALTER TABLE chat_attachment
            ADD CONSTRAINT fk_chat_attachment_conversation
            FOREIGN KEY (conversation_id)
            REFERENCES chat_conversation(id)
            ON DELETE CASCADE
            NOT VALID;
    END IF;
END
$$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_chat_attachment_uploader'
          AND conrelid = 'chat_attachment'::regclass
    ) THEN
        ALTER TABLE chat_attachment
            ADD CONSTRAINT fk_chat_attachment_uploader
            FOREIGN KEY (uploaded_by_user_id)
            REFERENCES app_user(id)
            NOT VALID;
    END IF;
END
$$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM chat_attachment attachment
        LEFT JOIN chat_conversation conversation
          ON conversation.id = attachment.conversation_id
        WHERE conversation.id IS NULL
    ) THEN
        ALTER TABLE chat_attachment
            VALIDATE CONSTRAINT fk_chat_attachment_conversation;
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM chat_attachment attachment
        LEFT JOIN app_user uploader
          ON uploader.id = attachment.uploaded_by_user_id
        WHERE uploader.id IS NULL
    ) THEN
        ALTER TABLE chat_attachment
            VALIDATE CONSTRAINT fk_chat_attachment_uploader;
    END IF;
END
$$;

WITH ranked_active_documents AS (
    SELECT
        id,
        ROW_NUMBER() OVER (
            PARTITION BY code, country_code, locale
            ORDER BY
                effective_at DESC,
                created_at DESC,
                version DESC,
                id DESC
        ) AS active_rank
    FROM legal_document
    WHERE active = TRUE
)
UPDATE legal_document document
SET active = FALSE,
    updated_at = NOW()
FROM ranked_active_documents ranked
WHERE document.id = ranked.id
  AND ranked.active_rank > 1;

CREATE UNIQUE INDEX IF NOT EXISTS uq_legal_document_active_locale
    ON legal_document (code, country_code, locale)
    WHERE active = TRUE;

CREATE TEMP TABLE user_merge_map ON COMMIT DROP AS
SELECT
    id AS source_user_id,
    FIRST_VALUE(id) OVER (
        PARTITION BY LOWER(email)
        ORDER BY
            CASE WHEN status = 'ACTIVE' THEN 0 ELSE 1 END,
            CASE WHEN role = 'CUSTOMER' THEN 0 ELSE 1 END,
            last_login_at DESC NULLS LAST,
            created_at ASC,
            id ASC
    ) AS canonical_user_id
FROM app_user
WHERE email IS NOT NULL;

CREATE UNIQUE INDEX ON user_merge_map (source_user_id);
CREATE INDEX ON user_merge_map (canonical_user_id);

WITH merged_user_state AS (
    SELECT
        mapping.canonical_user_id,
        MAX(user_account.last_login_at) AS last_login_at,
        MIN(user_account.activated_at) FILTER (
            WHERE user_account.activated_at IS NOT NULL
        ) AS activated_at
    FROM user_merge_map mapping
    JOIN app_user user_account
      ON user_account.id = mapping.source_user_id
    GROUP BY mapping.canonical_user_id
)
UPDATE app_user canonical
SET last_login_at = merged.last_login_at,
    activated_at = COALESCE(canonical.activated_at, merged.activated_at),
    updated_at = GREATEST(canonical.updated_at, NOW())
FROM merged_user_state merged
WHERE canonical.id = merged.canonical_user_id;

CREATE TEMP TABLE customer_profile_merge ON COMMIT DROP AS
SELECT
    profile.id AS source_profile_id,
    mapping.canonical_user_id,
    FIRST_VALUE(profile.id) OVER (
        PARTITION BY mapping.canonical_user_id
        ORDER BY
            CASE WHEN profile.user_id = mapping.canonical_user_id THEN 0 ELSE 1 END,
            profile.created_at ASC,
            profile.id ASC
    ) AS retained_profile_id
FROM customer_profile profile
JOIN user_merge_map mapping
  ON mapping.source_user_id = profile.user_id;

WITH merged_profile_state AS (
    SELECT
        profile_mapping.retained_profile_id,
        (ARRAY_AGG(profile.display_name ORDER BY
            CASE WHEN profile.user_id = profile_mapping.canonical_user_id THEN 0 ELSE 1 END,
            profile.updated_at DESC,
            profile.id
        ) FILTER (WHERE profile.display_name IS NOT NULL))[1] AS display_name,
        (ARRAY_AGG(profile.icon_url ORDER BY
            CASE WHEN profile.user_id = profile_mapping.canonical_user_id THEN 0 ELSE 1 END,
            profile.updated_at DESC,
            profile.id
        ) FILTER (WHERE profile.icon_url IS NOT NULL))[1] AS icon_url
    FROM customer_profile_merge profile_mapping
    JOIN customer_profile profile
      ON profile.id = profile_mapping.source_profile_id
    GROUP BY profile_mapping.retained_profile_id
)
UPDATE customer_profile retained
SET display_name = COALESCE(retained.display_name, merged.display_name),
    icon_url = COALESCE(retained.icon_url, merged.icon_url),
    updated_at = GREATEST(retained.updated_at, NOW())
FROM merged_profile_state merged
WHERE retained.id = merged.retained_profile_id;

DELETE FROM customer_profile profile
USING customer_profile_merge mapping
WHERE profile.id = mapping.source_profile_id
  AND mapping.source_profile_id <> mapping.retained_profile_id;

UPDATE customer_profile profile
SET user_id = mapping.canonical_user_id,
    updated_at = GREATEST(profile.updated_at, NOW())
FROM customer_profile_merge mapping
WHERE profile.id = mapping.retained_profile_id
  AND profile.user_id <> mapping.canonical_user_id;

CREATE TEMP TABLE business_membership_merge ON COMMIT DROP AS
SELECT
    membership.id AS source_membership_id,
    mapping.canonical_user_id,
    membership.business_id,
    FIRST_VALUE(membership.id) OVER (
        PARTITION BY mapping.canonical_user_id, membership.business_id
        ORDER BY
            CASE membership.role
                WHEN 'OWNER' THEN 0
                WHEN 'MANAGER' THEN 1
                ELSE 2
            END,
            CASE WHEN membership.status = 'ACTIVE' THEN 0 ELSE 1 END,
            membership.created_at ASC,
            membership.id ASC
    ) AS retained_membership_id
FROM business_member membership
JOIN user_merge_map mapping
  ON mapping.source_user_id = membership.user_id;

INSERT INTO business_member_branch (
    id,
    created_at,
    updated_at,
    business_membership_id,
    branch_id
)
SELECT
    MD5(
        'business-member-branch:'
        || membership_mapping.retained_membership_id::text
        || ':'
        || assignment.branch_id::text
    )::uuid,
    MIN(assignment.created_at),
    MAX(assignment.updated_at),
    membership_mapping.retained_membership_id,
    assignment.branch_id
FROM business_membership_merge membership_mapping
JOIN business_member_branch assignment
  ON assignment.business_membership_id = membership_mapping.source_membership_id
GROUP BY
    membership_mapping.retained_membership_id,
    assignment.branch_id
ON CONFLICT (business_membership_id, branch_id) DO NOTHING;

WITH membership_candidates AS (
    SELECT
        membership_mapping.retained_membership_id,
        membership.status,
        CASE membership.role
            WHEN 'OWNER' THEN 0
            WHEN 'MANAGER' THEN 1
            ELSE 2
        END AS role_rank
    FROM business_membership_merge membership_mapping
    JOIN business_member membership
      ON membership.id = membership_mapping.source_membership_id
),
strongest_membership_role AS (
    SELECT
        retained_membership_id,
        MIN(role_rank) AS role_rank
    FROM membership_candidates
    GROUP BY retained_membership_id
),
merged_membership_state AS (
    SELECT
        strongest.retained_membership_id,
        CASE strongest.role_rank
            WHEN 0 THEN 'OWNER'
            WHEN 1 THEN 'MANAGER'
            ELSE 'WORKER'
        END AS role,
        CASE
            WHEN BOOL_OR(candidate.status = 'ACTIVE') THEN 'ACTIVE'
            WHEN BOOL_OR(candidate.status = 'SUSPENDED') THEN 'SUSPENDED'
            WHEN BOOL_OR(candidate.status = 'INACTIVE') THEN 'INACTIVE'
            ELSE MIN(candidate.status)
        END AS status
    FROM strongest_membership_role strongest
    JOIN membership_candidates candidate
      ON candidate.retained_membership_id = strongest.retained_membership_id
     AND candidate.role_rank = strongest.role_rank
    GROUP BY strongest.retained_membership_id, strongest.role_rank
)
UPDATE business_member retained
SET role = merged.role,
    status = merged.status,
    updated_at = GREATEST(retained.updated_at, NOW())
FROM merged_membership_state merged
WHERE retained.id = merged.retained_membership_id;

DELETE FROM business_member membership
USING business_membership_merge mapping
WHERE membership.id = mapping.source_membership_id
  AND mapping.source_membership_id <> mapping.retained_membership_id;

UPDATE business_member membership
SET user_id = mapping.canonical_user_id,
    updated_at = GREATEST(membership.updated_at, NOW())
FROM business_membership_merge mapping
WHERE membership.id = mapping.retained_membership_id
  AND membership.user_id <> mapping.canonical_user_id;

CREATE TEMP TABLE branch_membership_merge ON COMMIT DROP AS
SELECT
    membership.id AS source_membership_id,
    mapping.canonical_user_id,
    membership.branch_id,
    FIRST_VALUE(membership.id) OVER (
        PARTITION BY mapping.canonical_user_id, membership.branch_id
        ORDER BY
            CASE membership.role
                WHEN 'OWNER' THEN 0
                WHEN 'MANAGER' THEN 1
                ELSE 2
            END,
            CASE WHEN membership.status = 'ACTIVE' THEN 0 ELSE 1 END,
            membership.created_at ASC,
            membership.id ASC
    ) AS retained_membership_id
FROM branch_member membership
JOIN user_merge_map mapping
  ON mapping.source_user_id = membership.user_id;

WITH branch_candidates AS (
    SELECT
        membership_mapping.retained_membership_id,
        membership.status,
        CASE membership.role
            WHEN 'OWNER' THEN 0
            WHEN 'MANAGER' THEN 1
            ELSE 2
        END AS role_rank
    FROM branch_membership_merge membership_mapping
    JOIN branch_member membership
      ON membership.id = membership_mapping.source_membership_id
),
strongest_branch_role AS (
    SELECT
        retained_membership_id,
        MIN(role_rank) AS role_rank
    FROM branch_candidates
    GROUP BY retained_membership_id
),
merged_branch_state AS (
    SELECT
        strongest.retained_membership_id,
        CASE strongest.role_rank
            WHEN 0 THEN 'OWNER'
            WHEN 1 THEN 'MANAGER'
            ELSE 'WORKER'
        END AS role,
        CASE
            WHEN BOOL_OR(candidate.status = 'ACTIVE') THEN 'ACTIVE'
            WHEN BOOL_OR(candidate.status = 'SUSPENDED') THEN 'SUSPENDED'
            WHEN BOOL_OR(candidate.status = 'INACTIVE') THEN 'INACTIVE'
            ELSE MIN(candidate.status)
        END AS status
    FROM strongest_branch_role strongest
    JOIN branch_candidates candidate
      ON candidate.retained_membership_id = strongest.retained_membership_id
     AND candidate.role_rank = strongest.role_rank
    GROUP BY strongest.retained_membership_id, strongest.role_rank
)
UPDATE branch_member retained
SET role = merged.role,
    status = merged.status,
    updated_at = GREATEST(retained.updated_at, NOW())
FROM merged_branch_state merged
WHERE retained.id = merged.retained_membership_id;

DELETE FROM branch_member membership
USING branch_membership_merge mapping
WHERE membership.id = mapping.source_membership_id
  AND mapping.source_membership_id <> mapping.retained_membership_id;

UPDATE branch_member membership
SET user_id = mapping.canonical_user_id,
    updated_at = GREATEST(membership.updated_at, NOW())
FROM branch_membership_merge mapping
WHERE membership.id = mapping.retained_membership_id
  AND membership.user_id <> mapping.canonical_user_id;

CREATE TEMP TABLE platform_membership_merge ON COMMIT DROP AS
SELECT
    membership.id AS source_membership_id,
    mapping.canonical_user_id,
    FIRST_VALUE(membership.id) OVER (
        PARTITION BY mapping.canonical_user_id
        ORDER BY
            CASE membership.role
                WHEN 'SUPER_ADMIN' THEN 0
                WHEN 'ADMIN' THEN 1
                ELSE 2
            END,
            CASE WHEN membership.status = 'ACTIVE' THEN 0 ELSE 1 END,
            membership.created_at ASC,
            membership.id ASC
    ) AS retained_membership_id
FROM platform_membership membership
JOIN user_merge_map mapping
  ON mapping.source_user_id = membership.user_id;

INSERT INTO platform_membership_permission (
    platform_membership_id,
    permission
)
SELECT DISTINCT
    membership_mapping.retained_membership_id,
    permission.permission
FROM platform_membership_merge membership_mapping
JOIN platform_membership_permission permission
  ON permission.platform_membership_id = membership_mapping.source_membership_id
ON CONFLICT (platform_membership_id, permission) DO NOTHING;

WITH platform_role_candidates AS (
    SELECT
        membership_mapping.retained_membership_id,
        membership.role,
        membership.status
    FROM platform_membership_merge membership_mapping
    JOIN platform_membership membership
      ON membership.id = membership_mapping.source_membership_id
    UNION ALL
    SELECT
        membership_mapping.retained_membership_id,
        CASE user_account.role
            WHEN 'PLATFORM_SUPER_ADMIN' THEN 'SUPER_ADMIN'
            WHEN 'PLATFORM_ADMIN' THEN 'ADMIN'
            ELSE 'MODERATOR'
        END,
        CASE WHEN user_account.status = 'ACTIVE' THEN 'ACTIVE' ELSE 'INACTIVE' END
    FROM platform_membership_merge membership_mapping
    JOIN user_merge_map user_mapping
      ON user_mapping.canonical_user_id = membership_mapping.canonical_user_id
    JOIN app_user user_account
      ON user_account.id = user_mapping.source_user_id
    WHERE user_account.role IN (
        'PLATFORM_SUPER_ADMIN',
        'PLATFORM_ADMIN',
        'PLATFORM_MODERATOR'
    )
),
merged_platform_state AS (
    SELECT
        strongest.retained_membership_id,
        CASE strongest.role_rank
            WHEN 0 THEN 'SUPER_ADMIN'
            WHEN 1 THEN 'ADMIN'
            ELSE 'MODERATOR'
        END AS role,
        CASE
            WHEN BOOL_OR(candidate.status = 'ACTIVE') THEN 'ACTIVE'
            ELSE 'INACTIVE'
        END AS status
    FROM (
        SELECT
            retained_membership_id,
            MIN(
                CASE role
                    WHEN 'SUPER_ADMIN' THEN 0
                    WHEN 'ADMIN' THEN 1
                    ELSE 2
                END
            ) AS role_rank
        FROM platform_role_candidates
        GROUP BY retained_membership_id
    ) strongest
    JOIN platform_role_candidates candidate
      ON candidate.retained_membership_id = strongest.retained_membership_id
     AND CASE candidate.role
             WHEN 'SUPER_ADMIN' THEN 0
             WHEN 'ADMIN' THEN 1
             ELSE 2
         END = strongest.role_rank
    GROUP BY strongest.retained_membership_id, strongest.role_rank
)
UPDATE platform_membership retained
SET role = merged.role,
    status = merged.status,
    updated_at = GREATEST(retained.updated_at, NOW())
FROM merged_platform_state merged
WHERE retained.id = merged.retained_membership_id;

INSERT INTO platform_membership_permission (
    platform_membership_id,
    permission
)
SELECT
    membership.id,
    permission.permission
FROM platform_membership membership
CROSS JOIN LATERAL (
    SELECT UNNEST(
        CASE membership.role
            WHEN 'SUPER_ADMIN' THEN ARRAY[
                'MANAGE_PLATFORM_USERS',
                'MANAGE_MANAGED_IMPORTS',
                'EDIT_CATALOG_DURING_IMPORT',
                'PUBLISH_CATALOG_DURING_IMPORT',
                'MANAGE_SUPPORT_CHATS',
                'MODERATE_CONTENT',
                'SUSPEND_BUSINESS',
                'BAN_BUSINESS'
            ]::VARCHAR[]
            WHEN 'ADMIN' THEN ARRAY[
                'MANAGE_MANAGED_IMPORTS',
                'EDIT_CATALOG_DURING_IMPORT',
                'PUBLISH_CATALOG_DURING_IMPORT',
                'MANAGE_SUPPORT_CHATS'
            ]::VARCHAR[]
            ELSE ARRAY[
                'MODERATE_CONTENT',
                'SUSPEND_BUSINESS'
            ]::VARCHAR[]
        END
    ) AS permission
) permission
WHERE membership.id IN (
    SELECT DISTINCT retained_membership_id
    FROM platform_membership_merge
)
ON CONFLICT (platform_membership_id, permission) DO NOTHING;

DELETE FROM platform_membership membership
USING platform_membership_merge mapping
WHERE membership.id = mapping.source_membership_id
  AND mapping.source_membership_id <> mapping.retained_membership_id;

UPDATE platform_membership membership
SET user_id = mapping.canonical_user_id,
    updated_at = GREATEST(membership.updated_at, NOW())
FROM platform_membership_merge mapping
WHERE membership.id = mapping.retained_membership_id
  AND membership.user_id <> mapping.canonical_user_id;

CREATE TEMP TABLE legal_acceptance_merge ON COMMIT DROP AS
SELECT
    acceptance.id AS source_acceptance_id,
    mapping.canonical_user_id,
    FIRST_VALUE(acceptance.id) OVER (
        PARTITION BY
            mapping.canonical_user_id,
            acceptance.document_code,
            acceptance.document_version,
            acceptance.country_code,
            acceptance.locale
        ORDER BY
            acceptance.accepted_at ASC,
            acceptance.created_at ASC,
            acceptance.id ASC
    ) AS retained_acceptance_id
FROM legal_acceptance acceptance
JOIN user_merge_map mapping
  ON mapping.source_user_id = acceptance.user_id;

DELETE FROM legal_acceptance acceptance
USING legal_acceptance_merge mapping
WHERE acceptance.id = mapping.source_acceptance_id
  AND mapping.source_acceptance_id <> mapping.retained_acceptance_id;

UPDATE legal_acceptance acceptance
SET user_id = mapping.canonical_user_id
FROM legal_acceptance_merge mapping
WHERE acceptance.id = mapping.retained_acceptance_id
  AND acceptance.user_id <> mapping.canonical_user_id;

DO $$
DECLARE
    reference_record RECORD;
BEGIN
    FOR reference_record IN
        SELECT
            namespace.nspname AS schema_name,
            relation.relname AS table_name,
            attribute.attname AS column_name
        FROM pg_constraint constraint_record
        JOIN pg_class relation
          ON relation.oid = constraint_record.conrelid
        JOIN pg_namespace namespace
          ON namespace.oid = relation.relnamespace
        JOIN pg_attribute attribute
          ON attribute.attrelid = constraint_record.conrelid
         AND attribute.attnum = constraint_record.conkey[1]
        WHERE constraint_record.contype = 'f'
          AND constraint_record.confrelid = 'app_user'::regclass
          AND CARDINALITY(constraint_record.conkey) = 1
    LOOP
        EXECUTE FORMAT(
            'UPDATE %I.%I target
             SET %I = mapping.canonical_user_id
             FROM user_merge_map mapping
             WHERE target.%I = mapping.source_user_id
               AND target.%I <> mapping.canonical_user_id',
            reference_record.schema_name,
            reference_record.table_name,
            reference_record.column_name,
            reference_record.column_name,
            reference_record.column_name
        );
    END LOOP;
END
$$;

DO $$
BEGIN
    IF TO_REGCLASS('chat_conversation') IS NOT NULL
       AND EXISTS (
           SELECT 1
           FROM information_schema.columns
           WHERE table_schema = CURRENT_SCHEMA()
             AND table_name = 'chat_conversation'
             AND column_name = 'customer_id'
       ) THEN
        UPDATE chat_conversation conversation
        SET customer_id = mapping.canonical_user_id
        FROM user_merge_map mapping
        WHERE conversation.customer_id = mapping.source_user_id
          AND conversation.customer_id <> mapping.canonical_user_id;
    END IF;

    IF TO_REGCLASS('significant_event') IS NOT NULL
       AND EXISTS (
           SELECT 1
           FROM information_schema.columns
           WHERE table_schema = CURRENT_SCHEMA()
             AND table_name = 'significant_event'
             AND column_name = 'actor_user_id'
       ) THEN
        UPDATE significant_event event_record
        SET actor_user_id = mapping.canonical_user_id
        FROM user_merge_map mapping
        WHERE event_record.actor_user_id = mapping.source_user_id
          AND event_record.actor_user_id <> mapping.canonical_user_id;
    END IF;
END
$$;

DELETE FROM app_user duplicate
USING user_merge_map mapping
WHERE duplicate.id = mapping.source_user_id
  AND mapping.source_user_id <> mapping.canonical_user_id;

CREATE UNIQUE INDEX IF NOT EXISTS uq_app_user_normalized_email
    ON app_user (LOWER(email))
    WHERE email IS NOT NULL;

ALTER TABLE content_report
    ADD COLUMN resolution TEXT;

ALTER TABLE business_branch
    ADD COLUMN address_details VARCHAR(512);

ALTER TABLE managed_import_request
    ADD COLUMN expires_at TIMESTAMP WITH TIME ZONE;

UPDATE managed_import_request
SET expires_at = COALESCE(activated_at, created_at) + INTERVAL '7 days'
WHERE status = 'ACTIVE';

ALTER TABLE search_document
    ADD COLUMN ai_enrichment_requested BOOLEAN NOT NULL DEFAULT FALSE;

INSERT INTO platform_membership_permission (platform_membership_id, permission)
SELECT id, 'USE_AI_CATALOG_TOOLS'
FROM platform_membership
WHERE status = 'ACTIVE'
ON CONFLICT DO NOTHING;

ALTER TABLE chat_message
    ADD COLUMN attachment_url VARCHAR(512);

ALTER TABLE business
    ADD COLUMN catalog_scope VARCHAR(32) NOT NULL DEFAULT 'BOTH';

ALTER TABLE managed_import_request
    ADD COLUMN catalog_scope VARCHAR(32) NOT NULL DEFAULT 'PRODUCTS';
