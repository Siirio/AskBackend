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
