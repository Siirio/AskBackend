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
