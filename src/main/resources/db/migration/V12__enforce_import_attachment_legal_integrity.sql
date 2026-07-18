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
