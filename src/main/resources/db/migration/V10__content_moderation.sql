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
