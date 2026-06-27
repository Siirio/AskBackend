DO $$
DECLARE
    i integer;
    suffix text;
    business_id uuid;
    branch_id uuid;
    data_source_id uuid;
    product_id uuid;
    product_offer_id uuid;
    product_document_id uuid;
    service_id uuid;
    service_offer_id uuid;
    service_document_id uuid;
    city_ids uuid[] := ARRAY[
        '00000000-0000-0000-0000-0000000000c1'::uuid,
        '00000000-0000-0000-0000-0000000000c2'::uuid,
        '00000000-0000-0000-0000-0000000000c3'::uuid
    ];
    category_ids uuid[] := ARRAY[
        '00000000-0000-0000-0000-0000000000a1'::uuid,
        '00000000-0000-0000-0000-0000000000a2'::uuid,
        '00000000-0000-0000-0000-0000000000a3'::uuid,
        '00000000-0000-0000-0000-0000000000a4'::uuid,
        '00000000-0000-0000-0000-0000000000a5'::uuid
    ];
    city_id uuid;
    category_id uuid;
    business_name text;
    branch_name text;
    category_label text;
BEGIN
    FOR i IN 1..40 LOOP
        suffix := lpad(i::text, 12, '0');
        business_id := ('10000000-0000-0000-0000-' || suffix)::uuid;
        branch_id := ('10000001-0000-0000-0000-' || suffix)::uuid;
        data_source_id := ('10000002-0000-0000-0000-' || suffix)::uuid;
        product_id := ('10000003-0000-0000-0000-' || suffix)::uuid;
        product_offer_id := ('10000004-0000-0000-0000-' || suffix)::uuid;
        product_document_id := ('10000005-0000-0000-0000-' || suffix)::uuid;
        service_id := ('10000006-0000-0000-0000-' || suffix)::uuid;
        service_offer_id := ('10000007-0000-0000-0000-' || suffix)::uuid;
        service_document_id := ('10000008-0000-0000-0000-' || suffix)::uuid;
        city_id := city_ids[((i - 1) % array_length(city_ids, 1)) + 1];
        category_id := category_ids[((i - 1) % array_length(category_ids, 1)) + 1];
        category_label := CASE category_id
            WHEN '00000000-0000-0000-0000-0000000000a1'::uuid THEN 'Автозапчасти'
            WHEN '00000000-0000-0000-0000-0000000000a2'::uuid THEN 'Бытовая техника'
            WHEN '00000000-0000-0000-0000-0000000000a3'::uuid THEN 'Услуги красоты'
            WHEN '00000000-0000-0000-0000-0000000000a4'::uuid THEN 'Ремонт и сервис'
            ELSE 'Строительство'
        END;
        business_name := CASE
            WHEN i <= 15 THEN 'Тест Товары ' || lpad(i::text, 2, '0')
            WHEN i <= 30 THEN 'Тест Услуги ' || lpad((i - 15)::text, 2, '0')
            ELSE 'Тест Товары и Услуги ' || lpad((i - 30)::text, 2, '0')
        END;
        branch_name := business_name || ' - филиал';

        INSERT INTO business (id, created_at, updated_at, name, legal_name, bin, status)
        VALUES (business_id, now(), now(), business_name, business_name || ' ТОО', '99' || lpad(i::text, 10, '0'), 'ACTIVE')
        ON CONFLICT (id) DO NOTHING;

        INSERT INTO business_branch (id, created_at, updated_at, business_id, city_id, name, address, latitude, longitude, online_only, status)
        VALUES (branch_id, now(), now(), business_id, city_id, branch_name, 'Тестовая улица, ' || i, 44.8000 + i / 10000.0, 65.4000 + i / 10000.0, false, 'ACTIVE')
        ON CONFLICT (id) DO NOTHING;

        INSERT INTO data_source (id, created_at, updated_at, business_id, source_type, name, status)
        VALUES (data_source_id, now(), now(), business_id, 'MANUAL', 'Тестовый ввод ' || business_name, 'ACTIVE')
        ON CONFLICT (id) DO NOTHING;

        IF i <= 15 OR i > 30 THEN
            INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status)
            VALUES (product_id, now(), now(), business_id, category_id, category_label, 'Тестовый товар ' || lpad(i::text, 2, '0'), 'Демо-товар для тестового стенда', 'TEST-P-' || lpad(i::text, 3, '0'), '{"source":"test-stand"}', 'ACTIVE')
            ON CONFLICT (id) DO NOTHING;

            INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status)
            VALUES (product_offer_id, now(), now(), product_id, branch_id, data_source_id, 1000 + i * 250, true, 'ACTIVE')
            ON CONFLICT (id) DO NOTHING;

            INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status, source, public_note)
            VALUES (product_document_id, now(), now(), 'PRODUCT', product_offer_id, null, 'Тестовый товар ' || lpad(i::text, 2, '0'), business_name, category_label, 'TEST-P-' || lpad(i::text, 3, '0'), '{"source":"test-stand"}', business_id, branch_id, 1000 + i * 250, 'ACTIVE', 'MANUAL', 'Тестовые данные')
            ON CONFLICT (id) DO NOTHING;

            INSERT INTO search_document_token (search_document_id, token)
            VALUES (product_document_id, 'тестовый'), (product_document_id, 'товар'), (product_document_id, lower('TEST-P-' || lpad(i::text, 3, '0')))
            ON CONFLICT DO NOTHING;
        END IF;

        IF i > 15 THEN
            INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status)
            VALUES (service_id, now(), now(), business_id, category_id, 'Тестовая услуга ' || lpad(i::text, 2, '0'), 'Демо-услуга для тестового стенда', 'ACTIVE')
            ON CONFLICT (id) DO NOTHING;

            INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status)
            VALUES (service_offer_id, now(), now(), service_id, branch_id, 'ON_DEMAND', 2000 + i * 300, 30 + (i % 5) * 15, 'Пн-Пт 09:00-18:00', true, 'ACTIVE')
            ON CONFLICT (id) DO NOTHING;

            INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status, source, public_note)
            VALUES (service_document_id, now(), now(), 'SERVICE', null, service_offer_id, 'Тестовая услуга ' || lpad(i::text, 2, '0'), business_name, category_label, null, '{"source":"test-stand"}', business_id, branch_id, 2000 + i * 300, 'ACTIVE', 'MANUAL', 'Тестовые данные')
            ON CONFLICT (id) DO NOTHING;

            INSERT INTO search_document_token (search_document_id, token)
            VALUES (service_document_id, 'тестовая'), (service_document_id, 'услуга'), (service_document_id, category_label)
            ON CONFLICT DO NOTHING;
        END IF;
    END LOOP;
END $$;
