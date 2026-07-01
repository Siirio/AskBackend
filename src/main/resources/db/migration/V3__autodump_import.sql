-- =============================================================================
-- V3: AI Autodump Import
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
