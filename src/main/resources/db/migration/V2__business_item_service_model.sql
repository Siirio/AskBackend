CREATE EXTENSION IF NOT EXISTS pgcrypto;

ALTER TABLE business
    ADD COLUMN category_id UUID,
    ADD COLUMN scope VARCHAR(16);

UPDATE business
SET scope = CASE catalog_scope
                WHEN 'PRODUCTS' THEN 'ITEM'
                WHEN 'SERVICES' THEN 'SERVICE'
                ELSE 'BOTH'
            END;

ALTER TABLE category
    ADD COLUMN type VARCHAR(16),
    ADD COLUMN source VARCHAR(16);

UPDATE category
SET type = CASE scope
               WHEN 'SERVICE' THEN 'SERVICE'
               ELSE 'ITEM'
           END,
    source = 'SYSTEM';

INSERT INTO category (id, created_at, updated_at, name, slug, type, source)
SELECT gen_random_uuid(), now(), now(), name, slug, 'SERVICE', 'SYSTEM'
FROM category
WHERE scope = 'BOTH';

ALTER TABLE category
    ALTER COLUMN type SET NOT NULL,
    ALTER COLUMN source SET NOT NULL,
    DROP COLUMN parent_id,
    DROP COLUMN scope;

DELETE FROM category duplicate
USING category original
WHERE duplicate.name = original.name
  AND duplicate.type = original.type
  AND duplicate.id > original.id;

ALTER TABLE category
    ADD CONSTRAINT uq_category_name_type UNIQUE (name, type);

INSERT INTO category (id, created_at, updated_at, name, slug, type, source)
VALUES
    ('00000000-0000-0000-0000-0000000000b1', now(), now(), 'Категория бизнеса', 'business-general', 'BUSINESS', 'SYSTEM'),
    ('00000000-0000-0000-0000-0000000000b2', now(), now(), 'Товары', 'item-general', 'ITEM', 'SYSTEM'),
    ('00000000-0000-0000-0000-0000000000b3', now(), now(), 'Услуги', 'service-general', 'SERVICE', 'SYSTEM')
ON CONFLICT (name, type) DO NOTHING;

UPDATE business
SET category_id = '00000000-0000-0000-0000-0000000000b1'
WHERE category_id IS NULL;

ALTER TABLE business
    ALTER COLUMN category_id SET NOT NULL,
    ALTER COLUMN scope SET NOT NULL,
    ADD CONSTRAINT fk_business_category FOREIGN KEY (category_id) REFERENCES category(id),
    DROP COLUMN catalog_setup_mode,
    DROP COLUMN catalog_scope;

ALTER TABLE item
    ADD COLUMN category_id UUID;

INSERT INTO category (id, created_at, updated_at, name, slug, type, source)
SELECT gen_random_uuid(), now(), now(), category_label,
       lower(regexp_replace(category_label, '[^a-zA-Z0-9А-Яа-я]+', '-', 'g')), 'ITEM', 'USER'
FROM item
WHERE category_label IS NOT NULL AND btrim(category_label) <> ''
ON CONFLICT (name, type) DO NOTHING;

UPDATE item item_row
SET category_id = category.id
FROM category
WHERE category.type = 'ITEM'
  AND category.name = item_row.category_label;

UPDATE item
SET category_id = '00000000-0000-0000-0000-0000000000b2'
WHERE category_id IS NULL;

ALTER TABLE item
    ALTER COLUMN category_id SET NOT NULL,
    ADD CONSTRAINT fk_item_category FOREIGN KEY (category_id) REFERENCES category(id),
    DROP COLUMN category_label;

ALTER TABLE service_offering
    ADD COLUMN category_id UUID;

INSERT INTO category (id, created_at, updated_at, name, slug, type, source)
SELECT gen_random_uuid(), now(), now(), category_label,
       lower(regexp_replace(category_label, '[^a-zA-Z0-9А-Яа-я]+', '-', 'g')), 'SERVICE', 'USER'
FROM service_offering
WHERE category_label IS NOT NULL AND btrim(category_label) <> ''
ON CONFLICT (name, type) DO NOTHING;

UPDATE service_offering service_row
SET category_id = category.id
FROM category
WHERE category.type = 'SERVICE'
  AND category.name = service_row.category_label;

UPDATE service_offering
SET category_id = '00000000-0000-0000-0000-0000000000b3'
WHERE category_id IS NULL;

ALTER TABLE service_offering
    ALTER COLUMN category_id SET NOT NULL,
    ADD CONSTRAINT fk_service_category FOREIGN KEY (category_id) REFERENCES category(id),
    DROP COLUMN category_label;

ALTER TABLE managed_import_request
    RENAME COLUMN catalog_scope TO business_scope;

ALTER TABLE managed_import_request
    ALTER COLUMN expires_at DROP NOT NULL;

UPDATE managed_import_request
SET business_scope = CASE business_scope
                         WHEN 'PRODUCTS' THEN 'ITEM'
                         WHEN 'SERVICES' THEN 'SERVICE'
                         ELSE business_scope
                     END;

DROP TABLE category_alias;

UPDATE platform_membership_permission
SET permission = CASE permission
                     WHEN 'EDIT_CATALOG_DURING_IMPORT' THEN 'EDIT_ITEMS_SERVICES_DURING_IMPORT'
                     WHEN 'USE_AI_CATALOG_TOOLS' THEN 'USE_AI_ITEMS_SERVICES_TOOLS'
                     WHEN 'PUBLISH_CATALOG_DURING_IMPORT' THEN 'PUBLISH_ITEMS_SERVICES_DURING_IMPORT'
                     ELSE permission
                 END;

UPDATE significant_event
SET event_type = 'ITEMS_SERVICES_PUBLISHED'
WHERE event_type = 'CATALOG_PUBLISHED';

UPDATE search_document
SET source = 'ITEMS_SERVICES'
WHERE source = 'CATALOG';
