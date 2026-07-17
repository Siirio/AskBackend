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
