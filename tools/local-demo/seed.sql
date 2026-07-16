BEGIN;

UPDATE city
SET name = CASE id
    WHEN '00000000-0000-0000-0000-0000000000c2'::uuid THEN 'Almaty'
    WHEN '00000000-0000-0000-0000-0000000000c3'::uuid THEN 'Astana'
    ELSE name
END,
updated_at = now()
WHERE id IN (
    '00000000-0000-0000-0000-0000000000c2'::uuid,
    '00000000-0000-0000-0000-0000000000c3'::uuid
);

UPDATE category
SET name = CASE slug
    WHEN 'appliances' THEN 'Electronics'
    WHEN 'beauty' THEN 'Beauty services'
    WHEN 'repair' THEN 'Repair services'
    WHEN 'general' THEN 'Sport and leisure'
    ELSE name
END,
updated_at = now()
WHERE slug IN ('appliances', 'beauty', 'repair', 'general');

INSERT INTO business (id, created_at, updated_at, name, legal_name, bin, status) VALUES
    ('10000000-0000-0000-0000-000000000001', now(), now(), 'Trail Lab', 'Trail Lab Demo', null, 'ACTIVE'),
    ('10000000-0000-0000-0000-000000000002', now(), now(), 'Nomad Tech', 'Nomad Tech Demo', null, 'ACTIVE'),
    ('10000000-0000-0000-0000-000000000003', now(), now(), 'Beauty Point', 'Beauty Point Demo', null, 'ACTIVE'),
    ('10000000-0000-0000-0000-000000000004', now(), now(), 'Fix Hub', 'Fix Hub Demo', null, 'ACTIVE');

INSERT INTO brand_profile (
    id, created_at, updated_at, business_id, brand_color, tone_of_voice, description
) VALUES
    ('11000000-0000-0000-0000-000000000001', now(), now(), '10000000-0000-0000-0000-000000000001', '#1F6B4F', 'Practical', 'Running and outdoor gear selected for local conditions.'),
    ('11000000-0000-0000-0000-000000000002', now(), now(), '10000000-0000-0000-0000-000000000002', '#3155A6', 'Clear', 'Personal electronics and accessories with straightforward specifications.'),
    ('11000000-0000-0000-0000-000000000003', now(), now(), '10000000-0000-0000-0000-000000000003', '#A54B78', 'Warm', 'Everyday hair and nail services in central Almaty.'),
    ('11000000-0000-0000-0000-000000000004', now(), now(), '10000000-0000-0000-0000-000000000004', '#B46626', 'Direct', 'Device diagnostics and repair services in Almaty and Astana.');

INSERT INTO business_branch (
    id, created_at, updated_at, business_id, city_id, name, address, latitude, longitude, online_only, status
) VALUES
    ('12000000-0000-0000-0000-000000000001', now(), now(), '10000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-0000000000c2', 'Trail Lab Dostyk', 'Dostyk Avenue 48', 43.2392000, 76.9567000, false, 'ACTIVE'),
    ('12000000-0000-0000-0000-000000000002', now(), now(), '10000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-0000000000c2', 'Nomad Tech Abay', 'Abay Avenue 44', 43.2389000, 76.9092000, false, 'ACTIVE'),
    ('12000000-0000-0000-0000-000000000003', now(), now(), '10000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-0000000000c2', 'Beauty Point Center', 'Zheltoksan Street 115', 43.2511000, 76.9387000, false, 'ACTIVE'),
    ('12000000-0000-0000-0000-000000000004', now(), now(), '10000000-0000-0000-0000-000000000004', '00000000-0000-0000-0000-0000000000c2', 'Fix Hub Almaty', 'Tole Bi Street 89', 43.2550000, 76.9286000, false, 'ACTIVE'),
    ('12000000-0000-0000-0000-000000000005', now(), now(), '10000000-0000-0000-0000-000000000004', '00000000-0000-0000-0000-0000000000c3', 'Fix Hub Astana', 'Mangilik El Avenue 37', 51.0907000, 71.4181000, false, 'ACTIVE');

INSERT INTO product (
    id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status
) VALUES
    ('13000000-0000-0000-0000-000000000001', now(), now(), '10000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-0000000000af', 'Running shoes', 'Trail Runner Pro', 'Water-resistant trail running shoes with aggressive grip.', 'TL-RUN-PRO', '{"terrain":"trail","waterResistant":true}', 'ACTIVE'),
    ('13000000-0000-0000-0000-000000000002', now(), now(), '10000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-0000000000af', 'Running shoes', 'Trail Runner Lite', 'Lightweight running shoes for parks and dry trails.', 'TL-RUN-LITE', '{"terrain":"mixed","waterResistant":false}', 'ACTIVE'),
    ('13000000-0000-0000-0000-000000000003', now(), now(), '10000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-0000000000af', 'Hiking footwear', 'Summit Hiking Boots', 'High-ankle waterproof boots for mountain hikes.', 'TL-HIKE-SUMMIT', '{"terrain":"mountain","waterResistant":true}', 'ACTIVE'),
    ('13000000-0000-0000-0000-000000000004', now(), now(), '10000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-0000000000a2', 'Headphones', 'Sony WH-1000XM5', 'Wireless noise-cancelling over-ear headphones.', 'NT-SONY-XM5', '{"wireless":true,"noiseCancelling":true}', 'ACTIVE'),
    ('13000000-0000-0000-0000-000000000005', now(), now(), '10000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-0000000000a2', 'Headphones', 'QuietSound 45 Headphones', 'Wireless over-ear headphones with active noise cancellation.', 'NT-QS45', '{"wireless":true,"noiseCancelling":true}', 'ACTIVE'),
    ('13000000-0000-0000-0000-000000000006', now(), now(), '10000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-0000000000a2', 'Earbuds', 'AirBeat Wireless Earbuds', 'Compact wireless earbuds with charging case.', 'NT-AIRBEAT', '{"wireless":true,"form":"earbuds"}', 'ACTIVE'),
    ('13000000-0000-0000-0000-000000000007', now(), now(), '10000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-0000000000a2', 'Laptops', 'WorkBook 14 Laptop', 'Portable 14-inch laptop with 16 GB RAM and 512 GB SSD.', 'NT-WB14', '{"ramGb":16,"storageGb":512}', 'ACTIVE'),
    ('13000000-0000-0000-0000-000000000008', now(), now(), '10000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-0000000000af', 'Running accessories', 'Hydration Running Vest 8L', 'Light running vest with two soft flasks.', 'TL-VEST-8L', '{"capacityLiters":8}', 'ACTIVE');

INSERT INTO product_tag (product_id, tag) VALUES
    ('13000000-0000-0000-0000-000000000001', 'trail running shoes'),
    ('13000000-0000-0000-0000-000000000001', 'waterproof runners'),
    ('13000000-0000-0000-0000-000000000002', 'running shoes'),
    ('13000000-0000-0000-0000-000000000002', 'budget runners'),
    ('13000000-0000-0000-0000-000000000003', 'hiking boots'),
    ('13000000-0000-0000-0000-000000000004', 'noise cancelling headphones'),
    ('13000000-0000-0000-0000-000000000005', 'wireless headphones'),
    ('13000000-0000-0000-0000-000000000006', 'wireless earbuds'),
    ('13000000-0000-0000-0000-000000000007', 'work laptop'),
    ('13000000-0000-0000-0000-000000000008', 'running vest');

INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, price, enabled, status) VALUES
    ('14000000-0000-0000-0000-000000000001', now(), now(), '13000000-0000-0000-0000-000000000001', '12000000-0000-0000-0000-000000000001', 49990, true, 'ACTIVE'),
    ('14000000-0000-0000-0000-000000000002', now(), now(), '13000000-0000-0000-0000-000000000002', '12000000-0000-0000-0000-000000000001', 34990, true, 'ACTIVE'),
    ('14000000-0000-0000-0000-000000000003', now(), now(), '13000000-0000-0000-0000-000000000003', '12000000-0000-0000-0000-000000000001', 69990, true, 'ACTIVE'),
    ('14000000-0000-0000-0000-000000000004', now(), now(), '13000000-0000-0000-0000-000000000004', '12000000-0000-0000-0000-000000000002', 189990, true, 'ACTIVE'),
    ('14000000-0000-0000-0000-000000000005', now(), now(), '13000000-0000-0000-0000-000000000005', '12000000-0000-0000-0000-000000000002', 89990, true, 'ACTIVE'),
    ('14000000-0000-0000-0000-000000000006', now(), now(), '13000000-0000-0000-0000-000000000006', '12000000-0000-0000-0000-000000000002', 34990, true, 'ACTIVE'),
    ('14000000-0000-0000-0000-000000000007', now(), now(), '13000000-0000-0000-0000-000000000007', '12000000-0000-0000-0000-000000000002', 429990, true, 'ACTIVE'),
    ('14000000-0000-0000-0000-000000000008', now(), now(), '13000000-0000-0000-0000-000000000008', '12000000-0000-0000-0000-000000000001', 27990, true, 'ACTIVE');

INSERT INTO service_offering (
    id, created_at, updated_at, business_id, category_id, name, description, status
) VALUES
    ('15000000-0000-0000-0000-000000000001', now(), now(), '10000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-0000000000a3', 'Women Haircut', 'Haircut with consultation and styling.', 'ACTIVE'),
    ('15000000-0000-0000-0000-000000000002', now(), now(), '10000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-0000000000a3', 'Classic Manicure', 'Classic manicure with gel polish.', 'ACTIVE'),
    ('15000000-0000-0000-0000-000000000003', now(), now(), '10000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-0000000000a3', 'Hair Coloring', 'Single-tone coloring with color consultation.', 'ACTIVE'),
    ('15000000-0000-0000-0000-000000000004', now(), now(), '10000000-0000-0000-0000-000000000004', '00000000-0000-0000-0000-0000000000a4', 'Laptop Diagnostics', 'Hardware and software fault diagnostics.', 'ACTIVE'),
    ('15000000-0000-0000-0000-000000000005', now(), now(), '10000000-0000-0000-0000-000000000004', '00000000-0000-0000-0000-0000000000a4', 'Laptop Screen Replacement', 'Replacement service for common 13 to 16-inch laptop screens.', 'ACTIVE'),
    ('15000000-0000-0000-0000-000000000006', now(), now(), '10000000-0000-0000-0000-000000000004', '00000000-0000-0000-0000-0000000000a4', 'Phone Battery Replacement', 'Battery diagnostics and replacement service.', 'ACTIVE');

INSERT INTO service_branch_offer (
    id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, active, status
) VALUES
    ('16000000-0000-0000-0000-000000000001', now(), now(), '15000000-0000-0000-0000-000000000001', '12000000-0000-0000-0000-000000000003', 'ON_DEMAND', 12000, true, 'ACTIVE'),
    ('16000000-0000-0000-0000-000000000002', now(), now(), '15000000-0000-0000-0000-000000000002', '12000000-0000-0000-0000-000000000003', 'ON_DEMAND', 9000, true, 'ACTIVE'),
    ('16000000-0000-0000-0000-000000000003', now(), now(), '15000000-0000-0000-0000-000000000003', '12000000-0000-0000-0000-000000000003', 'ON_DEMAND', 25000, true, 'ACTIVE'),
    ('16000000-0000-0000-0000-000000000004', now(), now(), '15000000-0000-0000-0000-000000000004', '12000000-0000-0000-0000-000000000004', 'ON_DEMAND', 5000, true, 'ACTIVE'),
    ('16000000-0000-0000-0000-000000000005', now(), now(), '15000000-0000-0000-0000-000000000005', '12000000-0000-0000-0000-000000000004', 'ON_DEMAND', 45000, true, 'ACTIVE'),
    ('16000000-0000-0000-0000-000000000006', now(), now(), '15000000-0000-0000-0000-000000000006', '12000000-0000-0000-0000-000000000005', 'ON_DEMAND', 18000, true, 'ACTIVE');

INSERT INTO search_document (
    id, created_at, updated_at, document_type, product_offer_id, title, summary, category_label, sku,
    characteristics_json, business_id, branch_id, price, status, source, aggregate_id, document_version,
    normalized_title, brand, category_path, business_name, branch_name, verified_attributes, aliases,
    currency, latitude, longitude, availability_status, availability_source, last_business_updated_at, indexed_at
)
SELECT
    ('17000000-0000-0000-0000-' || lpad(row_number() OVER (ORDER BY product_offer.id)::text, 12, '0'))::uuid,
    now(), now(), 'PRODUCT', product_offer.id, product.name, product.description, product.category_label, product.sku,
    product.characteristics_json, business.id, branch.id, product_offer.price, 'ACTIVE', 'CATALOG', product_offer.id, 1,
    lower(product.name), business.name, product.category_label, business.name, branch.name,
    coalesce(product.characteristics_json, '{}')::jsonb,
    coalesce((SELECT string_agg(tag, ' ') FROM product_tag WHERE product_id = product.id), ''),
    'KZT', branch.latitude, branch.longitude,
    CASE WHEN product.sku IN ('TL-RUN-PRO', 'NT-SONY-XM5', 'NT-AIRBEAT', 'TL-VEST-8L') THEN 'AVAILABLE' ELSE 'UNKNOWN' END,
    CASE WHEN product.sku IN ('TL-RUN-PRO', 'NT-SONY-XM5', 'NT-AIRBEAT', 'TL-VEST-8L') THEN 'BUSINESS' ELSE 'UNKNOWN' END,
    CASE WHEN product.sku IN ('TL-RUN-PRO', 'NT-SONY-XM5', 'NT-AIRBEAT', 'TL-VEST-8L') THEN now() ELSE null END,
    now()
FROM product_offer
JOIN product ON product.id = product_offer.product_id
JOIN business ON business.id = product.business_id
JOIN business_branch branch ON branch.id = product_offer.branch_id
WHERE product.id::text LIKE '13000000-0000-0000-0000-%';

INSERT INTO search_document (
    id, created_at, updated_at, document_type, service_branch_offer_id, title, summary, category_label,
    business_id, branch_id, price, status, source, aggregate_id, document_version, normalized_title, brand,
    category_path, business_name, branch_name, verified_attributes, aliases, currency, latitude, longitude,
    availability_status, availability_source, last_business_updated_at, indexed_at
)
SELECT
    ('18000000-0000-0000-0000-' || lpad(row_number() OVER (ORDER BY service_branch_offer.id)::text, 12, '0'))::uuid,
    now(), now(), 'SERVICE', service_branch_offer.id, service_offering.name, service_offering.description, category.name,
    business.id, branch.id, service_branch_offer.base_price, 'ACTIVE', 'CATALOG', service_branch_offer.id, 1,
    lower(service_offering.name), business.name, category.name, business.name, branch.name,
    jsonb_build_object('serviceMode', service_branch_offer.service_mode),
    lower(service_offering.name || ' ' || coalesce(service_offering.description, '')), 'KZT', branch.latitude, branch.longitude,
    CASE WHEN service_offering.name IN ('Women Haircut', 'Laptop Diagnostics') THEN 'AVAILABLE' ELSE 'UNKNOWN' END,
    CASE WHEN service_offering.name IN ('Women Haircut', 'Laptop Diagnostics') THEN 'BUSINESS' ELSE 'UNKNOWN' END,
    CASE WHEN service_offering.name IN ('Women Haircut', 'Laptop Diagnostics') THEN now() ELSE null END,
    now()
FROM service_branch_offer
JOIN service_offering ON service_offering.id = service_branch_offer.service_offering_id
JOIN category ON category.id = service_offering.category_id
JOIN business ON business.id = service_offering.business_id
JOIN business_branch branch ON branch.id = service_branch_offer.branch_id
WHERE service_offering.id::text LIKE '15000000-0000-0000-0000-%';

INSERT INTO search_document_token (search_document_id, token)
SELECT id, token
FROM search_document
CROSS JOIN LATERAL regexp_split_to_table(lower(title || ' ' || aliases), '\\s+') AS token
WHERE id::text LIKE '17000000-0000-0000-0000-%'
   OR id::text LIKE '18000000-0000-0000-0000-%';

INSERT INTO search_query_alias (id, created_at, updated_at, alias_value, target_query, status) VALUES
    ('19000000-0000-0000-0000-000000000001', now(), now(), 'runners', 'running shoes', 'ACTIVE'),
    ('19000000-0000-0000-0000-000000000002', now(), now(), 'anc headphones', 'noise cancelling headphones', 'ACTIVE'),
    ('19000000-0000-0000-0000-000000000003', now(), now(), 'notebook repair', 'laptop repair', 'ACTIVE'),
    ('19000000-0000-0000-0000-000000000004', now(), now(), 'hair cut', 'haircut', 'ACTIVE');

COMMIT;
