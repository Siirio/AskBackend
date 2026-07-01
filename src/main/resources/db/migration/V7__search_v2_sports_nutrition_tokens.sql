WITH sport_documents AS (
    SELECT id
    FROM search_document
    WHERE status = 'ACTIVE'
      AND document_type = 'PRODUCT'
      AND (
        LOWER(COALESCE(title, '')) LIKE ANY (ARRAY[
            '%спортпит%', '%спортивное питание%', '%креатин%', '%протеин%', '%гейнер%',
            '%предтреник%', '%предтренировочный%', '%батончик%', '%аминокислот%', '%bcaa%',
            '%eaa%', '%витамин%', '%creatine%', '%protein%', '%gainer%', '%preworkout%'
        ])
        OR LOWER(COALESCE(summary, '')) LIKE ANY (ARRAY[
            '%спортпит%', '%спортивное питание%', '%креатин%', '%протеин%', '%гейнер%',
            '%предтреник%', '%предтренировочный%', '%батончик%', '%аминокислот%', '%bcaa%',
            '%eaa%', '%витамин%', '%creatine%', '%protein%', '%gainer%', '%preworkout%'
        ])
        OR LOWER(COALESCE(category_label, '')) LIKE ANY (ARRAY[
            '%спортпит%', '%спортивное питание%', '%креатин%', '%протеин%', '%гейнер%',
            '%предтреник%', '%предтренировочный%', '%батончик%', '%аминокислот%', '%bcaa%',
            '%eaa%', '%витамин%', '%creatine%', '%protein%', '%gainer%', '%preworkout%'
        ])
      )
),
sport_tokens AS (
    SELECT id AS search_document_id, token
    FROM sport_documents
    CROSS JOIN (VALUES
        ('спортпит'), ('спортивное питание'), ('sports nutrition'), ('добавки'), ('бад')
    ) AS tokens(token)
),
specific_tokens AS (
    SELECT document.id AS search_document_id, tokens.token
    FROM search_document document
    CROSS JOIN (VALUES
        ('креатин', ARRAY['%креатин%', '%creatine%', '%моногидрат%']),
        ('creatine', ARRAY['%креатин%', '%creatine%', '%моногидрат%']),
        ('моногидрат', ARRAY['%креатин%', '%creatine%', '%моногидрат%']),
        ('протеин', ARRAY['%протеин%', '%protein%', '%whey%']),
        ('protein', ARRAY['%протеин%', '%protein%', '%whey%']),
        ('сывороточный протеин', ARRAY['%протеин%', '%protein%', '%whey%']),
        ('гейнер', ARRAY['%гейнер%', '%gainer%']),
        ('gainer', ARRAY['%гейнер%', '%gainer%']),
        ('предтреник', ARRAY['%предтреник%', '%предтренировочный%', '%preworkout%']),
        ('предтренировочный комплекс', ARRAY['%предтреник%', '%предтренировочный%', '%preworkout%']),
        ('preworkout', ARRAY['%предтреник%', '%предтренировочный%', '%preworkout%']),
        ('батончик', ARRAY['%батончик%', '%protein bar%']),
        ('батончики', ARRAY['%батончик%', '%protein bar%']),
        ('protein bar', ARRAY['%батончик%', '%protein bar%']),
        ('аминокислоты', ARRAY['%аминокислот%', '%bcaa%', '%eaa%']),
        ('bcaa', ARRAY['%аминокислот%', '%bcaa%', '%eaa%']),
        ('eaa', ARRAY['%аминокислот%', '%bcaa%', '%eaa%']),
        ('витамины', ARRAY['%витамин%', '%vitamins%']),
        ('витамин', ARRAY['%витамин%', '%vitamins%']),
        ('vitamins', ARRAY['%витамин%', '%vitamins%'])
    ) AS tokens(token, patterns)
    WHERE document.status = 'ACTIVE'
      AND document.document_type = 'PRODUCT'
      AND (
        LOWER(COALESCE(document.title, '')) LIKE ANY (tokens.patterns)
        OR LOWER(COALESCE(document.summary, '')) LIKE ANY (tokens.patterns)
        OR LOWER(COALESCE(document.category_label, '')) LIKE ANY (tokens.patterns)
      )
)
INSERT INTO search_document_token (search_document_id, token)
SELECT token_data.search_document_id, token_data.token
FROM (
    SELECT search_document_id, token FROM sport_tokens
    UNION
    SELECT search_document_id, token FROM specific_tokens
) token_data
WHERE NOT EXISTS (
    SELECT 1
    FROM search_document_token existing_token
    WHERE existing_token.search_document_id = token_data.search_document_id
      AND existing_token.token = token_data.token
);

INSERT INTO search_query_alias (id, created_at, updated_at, alias_value, target_query, status)
SELECT alias_data.id, now(), now(), alias_data.alias_value, alias_data.target_query, 'ACTIVE'
FROM (
    VALUES
        ('00000000-0000-0000-0000-00000000a601'::uuid, 'спортпит', 'спортивное питание'),
        ('00000000-0000-0000-0000-00000000a602'::uuid, 'спортпит', 'креатин'),
        ('00000000-0000-0000-0000-00000000a603'::uuid, 'спортпит', 'протеин'),
        ('00000000-0000-0000-0000-00000000a604'::uuid, 'спортивное питание', 'спортпит'),
        ('00000000-0000-0000-0000-00000000a605'::uuid, 'креатин', 'creatine'),
        ('00000000-0000-0000-0000-00000000a606'::uuid, 'creatine', 'креатин')
) AS alias_data(id, alias_value, target_query)
WHERE NOT EXISTS (
    SELECT 1
    FROM search_query_alias existing_alias
    WHERE existing_alias.alias_value = alias_data.alias_value
      AND existing_alias.target_query = alias_data.target_query
);
