INSERT INTO category (id, created_at, updated_at, parent_id, name, slug, status)
SELECT '00000000-0000-0000-0000-0000000000af', now(), now(), null, 'Общее', 'general', 'ACTIVE'
WHERE NOT EXISTS (
    SELECT 1
    FROM category
    WHERE slug = 'general'
);

WITH general_category AS (
    SELECT id
    FROM category
    WHERE slug = 'general'
    LIMIT 1
)
UPDATE service_offering service
SET category_id = (SELECT id FROM general_category)
WHERE service.category_id IN (
    SELECT id
    FROM category
    WHERE slug = 'beauty'
)
AND (
    lower(coalesce(service.name, '')) LIKE '%велик%'
    OR lower(coalesce(service.name, '')) LIKE '%велосипед%'
    OR lower(coalesce(service.description, '')) LIKE '%велик%'
    OR lower(coalesce(service.description, '')) LIKE '%велосипед%'
);

UPDATE search_document document
SET category_label = 'Общее'
WHERE document.document_type = 'SERVICE'
AND (
    lower(coalesce(document.title, '')) LIKE '%велик%'
    OR lower(coalesce(document.title, '')) LIKE '%велосипед%'
    OR lower(coalesce(document.summary, '')) LIKE '%велик%'
    OR lower(coalesce(document.summary, '')) LIKE '%велосипед%'
);

INSERT INTO search_document_token (search_document_id, token)
SELECT token_data.search_document_id, token_data.token
FROM (
    SELECT document.id AS search_document_id, tokens.token
    FROM search_document document
    CROSS JOIN (
        VALUES
            ('велики'),
            ('велик'),
            ('велосипед'),
            ('велосипеды'),
            ('прокат велосипедов'),
            ('аренда велосипедов'),
            ('прокат великов'),
            ('bike rental'),
            ('bicycle rental')
    ) AS tokens(token)
    WHERE document.document_type = 'SERVICE'
    AND (
        lower(coalesce(document.title, '')) LIKE '%велик%'
        OR lower(coalesce(document.title, '')) LIKE '%велосипед%'
        OR lower(coalesce(document.summary, '')) LIKE '%велик%'
        OR lower(coalesce(document.summary, '')) LIKE '%велосипед%'
    )
) AS token_data
WHERE NOT EXISTS (
    SELECT 1
    FROM search_document_token existing_token
    WHERE existing_token.search_document_id = token_data.search_document_id
    AND existing_token.token = token_data.token
);
