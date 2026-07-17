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
        ('MANAGED_IMPORT_TERMS', '/legal/import-service'),
        ('PROHIBITED_PRODUCTS_POLICY', '/legal/prohibited-products'),
        ('CONTENT_POLICY', '/legal/content-policy')
) AS documents(code, public_url)
CROSS JOIN (
    VALUES ('ru'), ('kk'), ('en')
) AS locales(locale);
