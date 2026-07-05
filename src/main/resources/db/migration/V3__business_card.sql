CREATE TABLE business_card (
    id           UUID        NOT NULL PRIMARY KEY,
    created_at   TIMESTAMPTZ NOT NULL,
    updated_at   TIMESTAMPTZ NOT NULL,
    business_id  UUID        NOT NULL UNIQUE REFERENCES business(id),
    blocks       TEXT        NOT NULL DEFAULT '[]',
    published_at TIMESTAMPTZ,
    status       VARCHAR(50) NOT NULL DEFAULT 'DRAFT'
);
