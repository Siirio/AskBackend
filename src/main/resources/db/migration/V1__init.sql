-- =============================================================================
-- V1: Initial schema - all core entities
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
    id          UUID        NOT NULL PRIMARY KEY,
    created_at  TIMESTAMPTZ NOT NULL,
    updated_at  TIMESTAMPTZ NOT NULL,
    business_id UUID        NOT NULL REFERENCES business(id),
    category_id UUID        NOT NULL REFERENCES category(id),
    name        VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    sku         VARCHAR(255),
    status      VARCHAR(50)  NOT NULL
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
    original_file_name VARCHAR(255) NOT NULL,
    status            VARCHAR(50)  NOT NULL,
    imported_at       TIMESTAMPTZ
);

CREATE TABLE catalog_import_column_mapping (
    id                UUID        NOT NULL PRIMARY KEY,
    created_at        TIMESTAMPTZ NOT NULL,
    updated_at        TIMESTAMPTZ NOT NULL,
    catalog_import_id UUID        NOT NULL REFERENCES catalog_import(id),
    source_column     VARCHAR(255) NOT NULL,
    target_field      VARCHAR(255) NOT NULL
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
    id                UUID        NOT NULL PRIMARY KEY,
    created_at        TIMESTAMPTZ NOT NULL,
    updated_at        TIMESTAMPTZ NOT NULL,
    catalog_import_id UUID        NOT NULL REFERENCES catalog_import(id),
    product_id        UUID        REFERENCES product(id),
    product_offer_id  UUID        REFERENCES product_offer(id),
    row_number        INTEGER     NOT NULL,
    row_payload       VARCHAR(255) NOT NULL
);

-- ---------------------------------------------------------------------------
-- Service
-- ---------------------------------------------------------------------------

CREATE TABLE service_offering (
    id          UUID        NOT NULL PRIMARY KEY,
    created_at  TIMESTAMPTZ NOT NULL,
    updated_at  TIMESTAMPTZ NOT NULL,
    business_id UUID        NOT NULL REFERENCES business(id),
    category_id UUID        NOT NULL REFERENCES category(id),
    name        VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    status      VARCHAR(50)  NOT NULL
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

-- Add FK for booking_id on conversation_link (deferred for dependency order)
ALTER TABLE conversation_link ADD CONSTRAINT fk_conversation_link_booking
    FOREIGN KEY (booking_id) REFERENCES booking(id);

-- Add FK for customer_request_id on conversation_link
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
    status                  VARCHAR(50)  NOT NULL
);

CREATE TABLE search_document_token (
    search_document_id UUID         NOT NULL REFERENCES search_document(id),
    token              VARCHAR(255) NOT NULL
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

CREATE INDEX idx_booking_customer ON booking (customer_id);
