CREATE EXTENSION IF NOT EXISTS pgcrypto;

INSERT INTO category (id, created_at, updated_at, name, slug, type, source)
SELECT gen_random_uuid(), now(), now(), value, lower(regexp_replace(value, '[^a-zA-Z0-9А-Яа-я]+', '-', 'g')), 'BUSINESS', 'SYSTEM'
FROM unnest(ARRAY[
    'Салон красоты', 'Барбершоп', 'Стоматологическая клиника', 'Медицинский центр',
    'Ветеринарная клиника', 'Учебный центр', 'Компьютерный клуб', 'Клининговая компания',
    'Сервисный центр', 'Магазин электроники', 'Магазин одежды', 'Магазин косметики',
    'Магазин товаров для дома', 'Магазин детских товаров', 'Магазин автотоваров'
]) AS value
ON CONFLICT (name, type) DO NOTHING;

INSERT INTO category (id, created_at, updated_at, name, slug, type, source)
SELECT gen_random_uuid(), now(), now(), value, lower(regexp_replace(value, '[^a-zA-Z0-9А-Яа-я]+', '-', 'g')), 'SERVICE', 'SYSTEM'
FROM unnest(ARRAY[
    'Красота и уход', 'Уборка и химчистка', 'Здоровье', 'Ветеринарные услуги',
    'Образование', 'Игры и развлечения', 'Ремонт техники'
]) AS value
ON CONFLICT (name, type) DO NOTHING;

INSERT INTO category (id, created_at, updated_at, name, slug, type, source)
SELECT gen_random_uuid(), now(), now(), value, lower(regexp_replace(value, '[^a-zA-Z0-9А-Яа-я]+', '-', 'g')), 'ITEM', 'SYSTEM'
FROM unnest(ARRAY[
    'Электроника', 'Бытовая техника', 'Одежда и обувь', 'Красота и уход',
    'Дом и интерьер', 'Детские товары', 'Спорт и отдых', 'Автотовары'
]) AS value
ON CONFLICT (name, type) DO NOTHING;
