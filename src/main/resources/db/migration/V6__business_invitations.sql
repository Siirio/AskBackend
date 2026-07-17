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
