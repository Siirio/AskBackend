-- =============================================================================
-- V4: Seed showcase data — 60+ companies with maximum variability
-- =============================================================================

-- Cities (2 new)
INSERT INTO city (id, created_at, updated_at, name, country_code, status) VALUES
  ('00000000-0000-0000-0001-1a904d507278', now(), now(), 'Шымкент', 'KZ', 'ACTIVE');
INSERT INTO city (id, created_at, updated_at, name, country_code, status) VALUES
  ('00000000-0000-0000-0001-02ba3c39b41d', now(), now(), 'Караганда', 'KZ', 'ACTIVE');

-- Categories (5 new)
INSERT INTO category (id, created_at, updated_at, parent_id, name, slug, status) VALUES
  ('00000000-0000-0000-0001-279580d8f28e', now(), now(), null, 'Продукты питания', 'food', 'ACTIVE');
INSERT INTO category (id, created_at, updated_at, parent_id, name, slug, status) VALUES
  ('00000000-0000-0000-0001-81548349b593', now(), now(), null, 'Медицинские услуги', 'medical', 'ACTIVE');
INSERT INTO category (id, created_at, updated_at, parent_id, name, slug, status) VALUES
  ('00000000-0000-0000-0001-49c510314da7', now(), now(), null, 'Образование', 'education', 'ACTIVE');
INSERT INTO category (id, created_at, updated_at, parent_id, name, slug, status) VALUES
  ('00000000-0000-0000-0001-9f59c39873da', now(), now(), null, 'Спорт и фитнес', 'sport', 'ACTIVE');
INSERT INTO category (id, created_at, updated_at, parent_id, name, slug, status) VALUES
  ('00000000-0000-0000-0001-a686baabac2c', now(), now(), null, 'IT услуги', 'it', 'ACTIVE');

-- ==============================
-- PRODUCT-ONLY COMPANIES (25)
-- ==============================

-- Компания-Товар-1 (ИП, Бытовая техника, Алматы)
INSERT INTO business (id, created_at, updated_at, name, legal_name, bin, status) VALUES
  ('00000000-0000-0000-0001-718d23ca5f33', now(), now(), 'Компания-Товар-1', 'ИП "Компания-Товар-1"', '234567890123', 'ACTIVE');
INSERT INTO business_branch (id, created_at, updated_at, business_id, city_id, name, address, latitude, longitude, online_only, status) VALUES
  ('00000000-0000-0000-0001-382aa021400a', now(), now(), '00000000-0000-0000-0001-718d23ca5f33', '00000000-0000-0000-0000-0000000000c2', 'Компания-Товар-1 - Офис', 'ул. Сатпаева, 90', 44.8412, 65.5010, false, 'ACTIVE');
INSERT INTO data_source (id, created_at, updated_at, business_id, source_type, name, status) VALUES
  ('00000000-0000-0000-0001-10fb60b169aa', now(), now(), '00000000-0000-0000-0001-718d23ca5f33', 'MANUAL', 'Ручной ввод Компания-Товар-1', 'ACTIVE');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-2cd84e631654', now(), now(), '00000000-0000-0000-0001-718d23ca5f33', '00000000-0000-0000-0000-0000000000a2', 'Бытовая техника', 'Холодильник LG 350L', 'Двухкамерный холодильник с No Frost', 'LG-RF-350', '{"бренд":"LG","объем":"350л","тип":"No Frost"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-dd57adb0f556', now(), now(), '00000000-0000-0000-0001-2cd84e631654', '00000000-0000-0000-0001-382aa021400a', '00000000-0000-0000-0001-10fb60b169aa', 189000, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-41a5b48eca7c', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-dd57adb0f556', null, 'Холодильник LG 350L', 'Двухкамерный холодильник с No Frost', 'Бытовая техника', 'LG-RF-350', '{"бренд":"LG","объем":"350л","тип":"No Frost"}', '00000000-0000-0000-0001-718d23ca5f33', '00000000-0000-0000-0001-382aa021400a', 189000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-41a5b48eca7c', 'холодильник'), ('00000000-0000-0000-0001-41a5b48eca7c', '350l'), ('00000000-0000-0000-0001-41a5b48eca7c', 'бытовая'), ('00000000-0000-0000-0001-41a5b48eca7c', 'техника');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-955b3fd8cb51', now(), now(), '00000000-0000-0000-0001-718d23ca5f33', '00000000-0000-0000-0000-0000000000a2', 'Бытовая техника', 'Стиральная машина Bosch', 'Фронтальная загрузка 7кг, 1200 об/мин', 'BOS-WM-7', '{"бренд":"Bosch","загрузка":"7кг","обороты":"1200"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-a3fefec83026', now(), now(), '00000000-0000-0000-0001-955b3fd8cb51', '00000000-0000-0000-0001-382aa021400a', '00000000-0000-0000-0001-10fb60b169aa', 245000, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-db03e777878a', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-a3fefec83026', null, 'Стиральная машина Bosch', 'Фронтальная загрузка 7кг, 1200 об/мин', 'Бытовая техника', 'BOS-WM-7', '{"бренд":"Bosch","загрузка":"7кг","обороты":"1200"}', '00000000-0000-0000-0001-718d23ca5f33', '00000000-0000-0000-0001-382aa021400a', 245000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-db03e777878a', 'стиральная'), ('00000000-0000-0000-0001-db03e777878a', 'машина'), ('00000000-0000-0000-0001-db03e777878a', 'bosch'), ('00000000-0000-0000-0001-db03e777878a', 'бытовая'), ('00000000-0000-0000-0001-db03e777878a', 'техника');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-348f1122aa41', now(), now(), '00000000-0000-0000-0001-718d23ca5f33', null, 'Бытовая техника', 'Микроволновая печь Samsung', 'СВЧ 23л, гриль, сенсорное управление', 'SAM-MW-23', '{"бренд":"Samsung","объем":"23л","гриль":"да"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-4a00b6b5fd7d', now(), now(), '00000000-0000-0000-0001-348f1122aa41', '00000000-0000-0000-0001-382aa021400a', '00000000-0000-0000-0001-10fb60b169aa', 45000, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-fa25a5bbfa83', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-4a00b6b5fd7d', null, 'Микроволновая печь Samsung', 'СВЧ 23л, гриль, сенсорное управление', 'Бытовая техника', 'SAM-MW-23', '{"бренд":"Samsung","объем":"23л","гриль":"да"}', '00000000-0000-0000-0001-718d23ca5f33', '00000000-0000-0000-0001-382aa021400a', 45000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-fa25a5bbfa83', 'микроволновая'), ('00000000-0000-0000-0001-fa25a5bbfa83', 'печь'), ('00000000-0000-0000-0001-fa25a5bbfa83', 'samsung'), ('00000000-0000-0000-0001-fa25a5bbfa83', 'бытовая'), ('00000000-0000-0000-0001-fa25a5bbfa83', 'техника');

-- Компания-Товар-2 (ТОО, Услуги красоты, Астана)
INSERT INTO business (id, created_at, updated_at, name, legal_name, bin, status) VALUES
  ('00000000-0000-0000-0001-5fef54fceac7', now(), now(), 'Компания-Товар-2', 'ТОО "Компания-Товар-2"', '345678901234', 'ACTIVE');
INSERT INTO business_branch (id, created_at, updated_at, business_id, city_id, name, address, latitude, longitude, online_only, status) VALUES
  ('00000000-0000-0000-0001-e6e84fa2ddbc', now(), now(), '00000000-0000-0000-0001-5fef54fceac7', '00000000-0000-0000-0000-0000000000c3', 'Компания-Товар-2 - Офис', 'пр. Туран, 45', 44.8422, 65.5020, false, 'ACTIVE');
INSERT INTO data_source (id, created_at, updated_at, business_id, source_type, name, status) VALUES
  ('00000000-0000-0000-0001-18aabe00e775', now(), now(), '00000000-0000-0000-0001-5fef54fceac7', 'MANUAL', 'Ручной ввод Компания-Товар-2', 'ACTIVE');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-360e96e372f6', now(), now(), '00000000-0000-0000-0001-5fef54fceac7', '00000000-0000-0000-0000-0000000000a3', 'Услуги красоты', 'Антивирус Kaspersky 1 год', 'Защита на 3 устройства, лицензия', 'KAS-AV-3D', '{"бренд":"Kaspersky","устройств":"3","срок":"1 год"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-303c88770dcc', now(), now(), '00000000-0000-0000-0001-360e96e372f6', '00000000-0000-0000-0001-e6e84fa2ddbc', '00000000-0000-0000-0001-18aabe00e775', 12000, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-1cb0954cef98', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-303c88770dcc', null, 'Антивирус Kaspersky 1 год', 'Защита на 3 устройства, лицензия', 'Услуги красоты', 'KAS-AV-3D', '{"бренд":"Kaspersky","устройств":"3","срок":"1 год"}', '00000000-0000-0000-0001-5fef54fceac7', '00000000-0000-0000-0001-e6e84fa2ddbc', 12000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-1cb0954cef98', 'антивирус'), ('00000000-0000-0000-0001-1cb0954cef98', 'kaspersky'), ('00000000-0000-0000-0001-1cb0954cef98', 'год'), ('00000000-0000-0000-0001-1cb0954cef98', 'услуги'), ('00000000-0000-0000-0001-1cb0954cef98', 'красоты');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-701530e6bef8', now(), now(), '00000000-0000-0000-0001-5fef54fceac7', null, 'Услуги красоты', 'Microsoft Office 365', 'Годовая подписка на 1 пользователя', 'MS-O365-1Y', '{"бренд":"Microsoft","пользователей":"1","срок":"1 год"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-95a3c4f864b8', now(), now(), '00000000-0000-0000-0001-701530e6bef8', '00000000-0000-0000-0001-e6e84fa2ddbc', '00000000-0000-0000-0001-18aabe00e775', 35000, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-d87c3170618b', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-95a3c4f864b8', null, 'Microsoft Office 365', 'Годовая подписка на 1 пользователя', 'Услуги красоты', 'MS-O365-1Y', '{"бренд":"Microsoft","пользователей":"1","срок":"1 год"}', '00000000-0000-0000-0001-5fef54fceac7', '00000000-0000-0000-0001-e6e84fa2ddbc', 35000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-d87c3170618b', 'microsoft'), ('00000000-0000-0000-0001-d87c3170618b', 'office'), ('00000000-0000-0000-0001-d87c3170618b', '365'), ('00000000-0000-0000-0001-d87c3170618b', 'услуги'), ('00000000-0000-0000-0001-d87c3170618b', 'красоты');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-b7e1b218985c', now(), now(), '00000000-0000-0000-0001-5fef54fceac7', '00000000-0000-0000-0000-0000000000a3', 'Услуги красоты', 'Windows 11 Pro лицензия', 'Цифровая лицензия, русский язык', 'WIN-11-PRO', '{"версия":"Pro","тип":"цифровая"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-3485ef91cd1d', now(), now(), '00000000-0000-0000-0001-b7e1b218985c', '00000000-0000-0000-0001-e6e84fa2ddbc', '00000000-0000-0000-0001-18aabe00e775', 55000, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-5fdba8a99e7f', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-3485ef91cd1d', null, 'Windows 11 Pro лицензия', 'Цифровая лицензия, русский язык', 'Услуги красоты', 'WIN-11-PRO', '{"версия":"Pro","тип":"цифровая"}', '00000000-0000-0000-0001-5fef54fceac7', '00000000-0000-0000-0001-e6e84fa2ddbc', 55000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-5fdba8a99e7f', 'windows'), ('00000000-0000-0000-0001-5fdba8a99e7f', 'pro'), ('00000000-0000-0000-0001-5fdba8a99e7f', 'лицензия'), ('00000000-0000-0000-0001-5fdba8a99e7f', 'услуги'), ('00000000-0000-0000-0001-5fdba8a99e7f', 'красоты');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-fba1764f805e', now(), now(), '00000000-0000-0000-0001-5fef54fceac7', '00000000-0000-0000-0000-0000000000a3', 'Услуги красоты', 'Беговая дорожка CardioFit', 'Электрическая, до 16 км/ч, складная', 'CF-TM-001', '{"тип":"электрическая","скорость":"16 км/ч","складная":"да"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-d4806a862094', now(), now(), '00000000-0000-0000-0001-fba1764f805e', '00000000-0000-0000-0001-e6e84fa2ddbc', '00000000-0000-0000-0001-18aabe00e775', 280000, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-871ccd9561c7', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-d4806a862094', null, 'Беговая дорожка CardioFit', 'Электрическая, до 16 км/ч, складная', 'Услуги красоты', 'CF-TM-001', '{"тип":"электрическая","скорость":"16 км/ч","складная":"да"}', '00000000-0000-0000-0001-5fef54fceac7', '00000000-0000-0000-0001-e6e84fa2ddbc', 280000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-871ccd9561c7', 'беговая'), ('00000000-0000-0000-0001-871ccd9561c7', 'дорожка'), ('00000000-0000-0000-0001-871ccd9561c7', 'cardiofit'), ('00000000-0000-0000-0001-871ccd9561c7', 'услуги'), ('00000000-0000-0000-0001-871ccd9561c7', 'красоты');

-- Компания-Товар-3 (ИП, Ремонт и сервис, Шымкент)
INSERT INTO business (id, created_at, updated_at, name, legal_name, bin, status) VALUES
  ('00000000-0000-0000-0001-65a15e534856', now(), now(), 'Компания-Товар-3', 'ИП "Компания-Товар-3"', '456789012345', 'ACTIVE');
INSERT INTO business_branch (id, created_at, updated_at, business_id, city_id, name, address, latitude, longitude, online_only, status) VALUES
  ('00000000-0000-0000-0001-44465f98236e', now(), now(), '00000000-0000-0000-0001-65a15e534856', '00000000-0000-0000-0001-1a904d507278', 'Компания-Товар-3 - Офис', 'пр. Кунаева, 120', 44.8432, 65.5030, false, 'ACTIVE');
INSERT INTO data_source (id, created_at, updated_at, business_id, source_type, name, status) VALUES
  ('00000000-0000-0000-0001-02fe6fee7805', now(), now(), '00000000-0000-0000-0001-65a15e534856', 'MANUAL', 'Ручной ввод Компания-Товар-3', 'ACTIVE');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-06c9e02a7860', now(), now(), '00000000-0000-0000-0001-65a15e534856', null, 'Ремонт и сервис', 'Дрель ударная Bosch 750Вт', 'Дрель ударная с реверсом, кейс', 'BOS-DR-750', '{"бренд":"Bosch","мощность":"750Вт","тип":"ударная"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-d1dfa9afcf1d', now(), now(), '00000000-0000-0000-0001-06c9e02a7860', '00000000-0000-0000-0001-44465f98236e', '00000000-0000-0000-0001-02fe6fee7805', 28000, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-de4ff5fe4b08', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-d1dfa9afcf1d', null, 'Дрель ударная Bosch 750Вт', 'Дрель ударная с реверсом, кейс', 'Ремонт и сервис', 'BOS-DR-750', '{"бренд":"Bosch","мощность":"750Вт","тип":"ударная"}', '00000000-0000-0000-0001-65a15e534856', '00000000-0000-0000-0001-44465f98236e', 28000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-de4ff5fe4b08', 'дрель'), ('00000000-0000-0000-0001-de4ff5fe4b08', 'ударная'), ('00000000-0000-0000-0001-de4ff5fe4b08', 'bosch'), ('00000000-0000-0000-0001-de4ff5fe4b08', '750вт'), ('00000000-0000-0000-0001-de4ff5fe4b08', 'ремонт'), ('00000000-0000-0000-0001-de4ff5fe4b08', 'сервис');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-ad8f59ab0c6b', now(), now(), '00000000-0000-0000-0001-65a15e534856', '00000000-0000-0000-0000-0000000000a4', 'Ремонт и сервис', 'Шуруповерт Makita 18В', 'Аккумуляторный, 2 батареи, кейс', 'MAK-SD-18', '{"бренд":"Makita","напряжение":"18В","акб":"2 шт"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-f3f7bb5fdde6', now(), now(), '00000000-0000-0000-0001-ad8f59ab0c6b', '00000000-0000-0000-0001-44465f98236e', '00000000-0000-0000-0001-02fe6fee7805', 45000, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-fe9d0f2987d8', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-f3f7bb5fdde6', null, 'Шуруповерт Makita 18В', 'Аккумуляторный, 2 батареи, кейс', 'Ремонт и сервис', 'MAK-SD-18', '{"бренд":"Makita","напряжение":"18В","акб":"2 шт"}', '00000000-0000-0000-0001-65a15e534856', '00000000-0000-0000-0001-44465f98236e', 45000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-fe9d0f2987d8', 'шуруповерт'), ('00000000-0000-0000-0001-fe9d0f2987d8', 'makita'), ('00000000-0000-0000-0001-fe9d0f2987d8', '18в'), ('00000000-0000-0000-0001-fe9d0f2987d8', 'ремонт'), ('00000000-0000-0000-0001-fe9d0f2987d8', 'сервис');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-6ed6536132b1', now(), now(), '00000000-0000-0000-0001-65a15e534856', '00000000-0000-0000-0000-0000000000a4', 'Ремонт и сервис', 'Болгарка УШМ 125мм', 'Угловая шлифмашина, 1100Вт', 'USH-125-1100', '{"диаметр":"125мм","мощность":"1100Вт"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-af6d82fbf779', now(), now(), '00000000-0000-0000-0001-6ed6536132b1', '00000000-0000-0000-0001-44465f98236e', '00000000-0000-0000-0001-02fe6fee7805', 15000, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-b0ddad8e4e24', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-af6d82fbf779', null, 'Болгарка УШМ 125мм', 'Угловая шлифмашина, 1100Вт', 'Ремонт и сервис', 'USH-125-1100', '{"диаметр":"125мм","мощность":"1100Вт"}', '00000000-0000-0000-0001-65a15e534856', '00000000-0000-0000-0001-44465f98236e', 15000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-b0ddad8e4e24', 'болгарка'), ('00000000-0000-0000-0001-b0ddad8e4e24', 'ушм'), ('00000000-0000-0000-0001-b0ddad8e4e24', '125мм'), ('00000000-0000-0000-0001-b0ddad8e4e24', 'ремонт'), ('00000000-0000-0000-0001-b0ddad8e4e24', 'сервис');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-410c642d8dcb', now(), now(), '00000000-0000-0000-0001-65a15e534856', null, 'Ремонт и сервис', 'Дрель ударная Bosch 750Вт', 'Дрель ударная с реверсом, кейс', 'BOS-DR-750', '{"бренд":"Bosch","мощность":"750Вт","тип":"ударная"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-dde612c0b8d6', now(), now(), '00000000-0000-0000-0001-410c642d8dcb', '00000000-0000-0000-0001-44465f98236e', '00000000-0000-0000-0001-02fe6fee7805', 28000, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-f76e69ceed04', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-dde612c0b8d6', null, 'Дрель ударная Bosch 750Вт', 'Дрель ударная с реверсом, кейс', 'Ремонт и сервис', 'BOS-DR-750', '{"бренд":"Bosch","мощность":"750Вт","тип":"ударная"}', '00000000-0000-0000-0001-65a15e534856', '00000000-0000-0000-0001-44465f98236e', 28000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-f76e69ceed04', 'дрель'), ('00000000-0000-0000-0001-f76e69ceed04', 'ударная'), ('00000000-0000-0000-0001-f76e69ceed04', 'bosch'), ('00000000-0000-0000-0001-f76e69ceed04', '750вт'), ('00000000-0000-0000-0001-f76e69ceed04', 'ремонт'), ('00000000-0000-0000-0001-f76e69ceed04', 'сервис');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-358e2ec550e1', now(), now(), '00000000-0000-0000-0001-65a15e534856', '00000000-0000-0000-0000-0000000000a4', 'Ремонт и сервис', 'Шуруповерт Makita 18В', 'Аккумуляторный, 2 батареи, кейс', 'MAK-SD-18', '{"бренд":"Makita","напряжение":"18В","акб":"2 шт"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-170dd23e7cc0', now(), now(), '00000000-0000-0000-0001-358e2ec550e1', '00000000-0000-0000-0001-44465f98236e', '00000000-0000-0000-0001-02fe6fee7805', 45000, false, 'ACTIVE');

-- Компания-Товар-4 (ТОО, Строительство, Караганда)
INSERT INTO business (id, created_at, updated_at, name, legal_name, bin, status) VALUES
  ('00000000-0000-0000-0001-7d917de4c3c1', now(), now(), 'Компания-Товар-4', 'ТОО "Компания-Товар-4"', '567890123456', 'ACTIVE');
INSERT INTO business_branch (id, created_at, updated_at, business_id, city_id, name, address, latitude, longitude, online_only, status) VALUES
  ('00000000-0000-0000-0001-34df9347f6b8', now(), now(), '00000000-0000-0000-0001-7d917de4c3c1', '00000000-0000-0000-0001-02ba3c39b41d', 'Компания-Товар-4 - Офис', 'ул. Чкалова, 18', 44.8442, 65.5040, false, 'ACTIVE');
INSERT INTO data_source (id, created_at, updated_at, business_id, source_type, name, status) VALUES
  ('00000000-0000-0000-0001-b79f06e42466', now(), now(), '00000000-0000-0000-0001-7d917de4c3c1', 'MANUAL', 'Ручной ввод Компания-Товар-4', 'ACTIVE');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-f89cdc1b8842', now(), now(), '00000000-0000-0000-0001-7d917de4c3c1', '00000000-0000-0000-0000-0000000000a5', 'Строительство', 'Гипсокартон Knauf 12.5мм', 'Влагостойкий лист 2500x1200 мм', 'KNF-GKL-12', '{"бренд":"Knauf","толщина":"12.5мм","тип":"влагостойкий"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-6e32b48aa750', now(), now(), '00000000-0000-0000-0001-f89cdc1b8842', '00000000-0000-0000-0001-34df9347f6b8', '00000000-0000-0000-0001-b79f06e42466', 4500, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-ff4c266b63c3', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-6e32b48aa750', null, 'Гипсокартон Knauf 12.5мм', 'Влагостойкий лист 2500x1200 мм', 'Строительство', 'KNF-GKL-12', '{"бренд":"Knauf","толщина":"12.5мм","тип":"влагостойкий"}', '00000000-0000-0000-0001-7d917de4c3c1', '00000000-0000-0000-0001-34df9347f6b8', 4500, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-ff4c266b63c3', 'гипсокартон'), ('00000000-0000-0000-0001-ff4c266b63c3', 'knauf'), ('00000000-0000-0000-0001-ff4c266b63c3', '12.5мм'), ('00000000-0000-0000-0001-ff4c266b63c3', 'строительство');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-1f2cf6d9b766', now(), now(), '00000000-0000-0000-0001-7d917de4c3c1', '00000000-0000-0000-0000-0000000000a5', 'Строительство', 'Профнастил С8 0.45мм', 'Оцинкованный профнастил для забора', 'PRF-C8-045', '{"марка":"С8","толщина":"0.45мм","тип":"оцинкованный"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-0bc6cd46bbac', now(), now(), '00000000-0000-0000-0001-1f2cf6d9b766', '00000000-0000-0000-0001-34df9347f6b8', '00000000-0000-0000-0001-b79f06e42466', 2800, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-43d4e3d77ed8', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-0bc6cd46bbac', null, 'Профнастил С8 0.45мм', 'Оцинкованный профнастил для забора', 'Строительство', 'PRF-C8-045', '{"марка":"С8","толщина":"0.45мм","тип":"оцинкованный"}', '00000000-0000-0000-0001-7d917de4c3c1', '00000000-0000-0000-0001-34df9347f6b8', 2800, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-43d4e3d77ed8', 'профнастил'), ('00000000-0000-0000-0001-43d4e3d77ed8', '0.45мм'), ('00000000-0000-0000-0001-43d4e3d77ed8', 'строительство');

-- Компания-Товар-5 (ИП, Продукты питания, Кызылорда)
INSERT INTO business (id, created_at, updated_at, name, legal_name, bin, status) VALUES
  ('00000000-0000-0000-0001-304475876a47', now(), now(), 'Компания-Товар-5', 'ИП "Компания-Товар-5"', '678901234567', 'ACTIVE');
INSERT INTO business_branch (id, created_at, updated_at, business_id, city_id, name, address, latitude, longitude, online_only, status) VALUES
  ('00000000-0000-0000-0001-491068624070', now(), now(), '00000000-0000-0000-0001-304475876a47', '00000000-0000-0000-0000-0000000000c1', 'Компания-Товар-5 - Офис', 'пр. Назарбаева, 120', 44.8452, 65.5050, false, 'ACTIVE');
INSERT INTO data_source (id, created_at, updated_at, business_id, source_type, name, status) VALUES
  ('00000000-0000-0000-0001-1fe0d6ac76a1', now(), now(), '00000000-0000-0000-0001-304475876a47', 'MANUAL', 'Ручной ввод Компания-Товар-5', 'ACTIVE');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-a82b232b786a', now(), now(), '00000000-0000-0000-0001-304475876a47', '00000000-0000-0000-0001-279580d8f28e', 'Продукты питания', 'Сахар-песок 1кг', 'Белый свекловичный сахар', 'SAH-BEL-1', '{"тип":"свекловичный","вес":"1кг"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-b04518c5a9e6', now(), now(), '00000000-0000-0000-0001-a82b232b786a', '00000000-0000-0000-0001-491068624070', '00000000-0000-0000-0001-1fe0d6ac76a1', 550, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-9ffc9970f611', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-b04518c5a9e6', null, 'Сахар-песок 1кг', 'Белый свекловичный сахар', 'Продукты питания', 'SAH-BEL-1', '{"тип":"свекловичный","вес":"1кг"}', '00000000-0000-0000-0001-304475876a47', '00000000-0000-0000-0001-491068624070', 550, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-9ffc9970f611', 'сахар'), ('00000000-0000-0000-0001-9ffc9970f611', 'песок'), ('00000000-0000-0000-0001-9ffc9970f611', '1кг'), ('00000000-0000-0000-0001-9ffc9970f611', 'продукты'), ('00000000-0000-0000-0001-9ffc9970f611', 'питания');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-ebd4c14df5e2', now(), now(), '00000000-0000-0000-0001-304475876a47', null, 'Продукты питания', 'Макароны спираль 400г', 'Из твердых сортов пшеницы', 'MAK-SP-400', '{"тип":"спираль","вес":"400г","сорт":"твердый"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-15cc845aa76f', now(), now(), '00000000-0000-0000-0001-ebd4c14df5e2', '00000000-0000-0000-0001-491068624070', '00000000-0000-0000-0001-1fe0d6ac76a1', 380, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-09c7cc4f4ff2', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-15cc845aa76f', null, 'Макароны спираль 400г', 'Из твердых сортов пшеницы', 'Продукты питания', 'MAK-SP-400', '{"тип":"спираль","вес":"400г","сорт":"твердый"}', '00000000-0000-0000-0001-304475876a47', '00000000-0000-0000-0001-491068624070', 380, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-09c7cc4f4ff2', 'макароны'), ('00000000-0000-0000-0001-09c7cc4f4ff2', 'спираль'), ('00000000-0000-0000-0001-09c7cc4f4ff2', '400г'), ('00000000-0000-0000-0001-09c7cc4f4ff2', 'продукты'), ('00000000-0000-0000-0001-09c7cc4f4ff2', 'питания');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-150d601fe2af', now(), now(), '00000000-0000-0000-0001-304475876a47', '00000000-0000-0000-0001-279580d8f28e', 'Продукты питания', 'Мука пшеничная высший сорт', 'Казахстанская мука, 50 кг мешок', 'MUK-VS-50', '{"сорт":"высший","вес":"50кг","страна":"Казахстан"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-77288b324153', now(), now(), '00000000-0000-0000-0001-150d601fe2af', '00000000-0000-0000-0001-491068624070', '00000000-0000-0000-0001-1fe0d6ac76a1', 12000, false, 'ACTIVE');

-- Компания-Товар-6 (ТОО, Медицинские услуги, Алматы)
INSERT INTO business (id, created_at, updated_at, name, legal_name, bin, status) VALUES
  ('00000000-0000-0000-0001-cdbc82ab0703', now(), now(), 'Компания-Товар-6', 'ТОО "Компания-Товар-6"', '789012345678', 'ACTIVE');
INSERT INTO business_branch (id, created_at, updated_at, business_id, city_id, name, address, latitude, longitude, online_only, status) VALUES
  ('00000000-0000-0000-0001-8cd98dcd471c', now(), now(), '00000000-0000-0000-0001-cdbc82ab0703', '00000000-0000-0000-0000-0000000000c2', 'Компания-Товар-6 - Филиал 1', 'мкр. Орбита-3, 12', 44.8462, 65.5060, false, 'ACTIVE');
INSERT INTO business_branch (id, created_at, updated_at, business_id, city_id, name, address, latitude, longitude, online_only, status) VALUES
  ('00000000-0000-0000-0001-3252a7e8155e', now(), now(), '00000000-0000-0000-0001-cdbc82ab0703', '00000000-0000-0000-0000-0000000000c2', 'Компания-Товар-6 - Филиал 2', 'мкр. Орбита-3, 12', 44.8462, 65.5060, false, 'ACTIVE');
INSERT INTO data_source (id, created_at, updated_at, business_id, source_type, name, status) VALUES
  ('00000000-0000-0000-0001-f6d91ccf5a41', now(), now(), '00000000-0000-0000-0001-cdbc82ab0703', 'MANUAL', 'Ручной ввод Компания-Товар-6', 'ACTIVE');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-dbcad15c03b1', now(), now(), '00000000-0000-0000-0001-cdbc82ab0703', null, 'Медицинские услуги', 'Масляный фильтр Sakura', 'Фильтр масляный для японских авто', 'SAK-OF-001', '{"бренд":"Sakura","тип":"масляный"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-8cd8c6d2b1ec', now(), now(), '00000000-0000-0000-0001-dbcad15c03b1', '00000000-0000-0000-0001-8cd98dcd471c', '00000000-0000-0000-0001-f6d91ccf5a41', 3500, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-7d49aee0ab66', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-8cd8c6d2b1ec', null, 'Масляный фильтр Sakura', 'Фильтр масляный для японских авто', 'Медицинские услуги', 'SAK-OF-001', '{"бренд":"Sakura","тип":"масляный"}', '00000000-0000-0000-0001-cdbc82ab0703', '00000000-0000-0000-0001-8cd98dcd471c', 3500, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-7d49aee0ab66', 'масляный'), ('00000000-0000-0000-0001-7d49aee0ab66', 'фильтр'), ('00000000-0000-0000-0001-7d49aee0ab66', 'sakura'), ('00000000-0000-0000-0001-7d49aee0ab66', 'медицинские'), ('00000000-0000-0000-0001-7d49aee0ab66', 'услуги');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-e8fa59374258', now(), now(), '00000000-0000-0000-0001-dbcad15c03b1', '00000000-0000-0000-0001-3252a7e8155e', '00000000-0000-0000-0001-f6d91ccf5a41', 3500, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-e3de2e91e74e', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-e8fa59374258', null, 'Масляный фильтр Sakura', 'Фильтр масляный для японских авто', 'Медицинские услуги', 'SAK-OF-001', '{"бренд":"Sakura","тип":"масляный"}', '00000000-0000-0000-0001-cdbc82ab0703', '00000000-0000-0000-0001-3252a7e8155e', 3500, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-e3de2e91e74e', 'масляный'), ('00000000-0000-0000-0001-e3de2e91e74e', 'фильтр'), ('00000000-0000-0000-0001-e3de2e91e74e', 'sakura'), ('00000000-0000-0000-0001-e3de2e91e74e', 'медицинские'), ('00000000-0000-0000-0001-e3de2e91e74e', 'услуги');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-750435925f4e', now(), now(), '00000000-0000-0000-0001-cdbc82ab0703', '00000000-0000-0000-0001-81548349b593', 'Медицинские услуги', 'Тормозные колодки TRW', 'Передние колодки для европейских авто', 'TRW-BP-202', '{"бренд":"TRW","ось":"перед","тип":"керамика"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-ff12aa9bd79d', now(), now(), '00000000-0000-0000-0001-750435925f4e', '00000000-0000-0000-0001-8cd98dcd471c', '00000000-0000-0000-0001-f6d91ccf5a41', 18500, false, 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-cd5b24c4d277', now(), now(), '00000000-0000-0000-0001-750435925f4e', '00000000-0000-0000-0001-3252a7e8155e', '00000000-0000-0000-0001-f6d91ccf5a41', 18500, false, 'ACTIVE');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-b94258aaa4e5', now(), now(), '00000000-0000-0000-0001-cdbc82ab0703', '00000000-0000-0000-0001-81548349b593', 'Медицинские услуги', 'Свечи зажигания NGK', 'Иридиевые свечи, комплект 4 шт', 'NGK-IR-4', '{"бренд":"NGK","тип":"иридиевые","комплект":"4 шт"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-1e90061abf78', now(), now(), '00000000-0000-0000-0001-b94258aaa4e5', '00000000-0000-0000-0001-8cd98dcd471c', '00000000-0000-0000-0001-f6d91ccf5a41', 12000, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-cd3b4adf3763', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-1e90061abf78', null, 'Свечи зажигания NGK', 'Иридиевые свечи, комплект 4 шт', 'Медицинские услуги', 'NGK-IR-4', '{"бренд":"NGK","тип":"иридиевые","комплект":"4 шт"}', '00000000-0000-0000-0001-cdbc82ab0703', '00000000-0000-0000-0001-8cd98dcd471c', 12000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-cd3b4adf3763', 'свечи'), ('00000000-0000-0000-0001-cd3b4adf3763', 'зажигания'), ('00000000-0000-0000-0001-cd3b4adf3763', 'ngk'), ('00000000-0000-0000-0001-cd3b4adf3763', 'медицинские'), ('00000000-0000-0000-0001-cd3b4adf3763', 'услуги');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-365fea1ad275', now(), now(), '00000000-0000-0000-0001-b94258aaa4e5', '00000000-0000-0000-0001-3252a7e8155e', '00000000-0000-0000-0001-f6d91ccf5a41', 12000, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-aa3fcd6207d7', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-365fea1ad275', null, 'Свечи зажигания NGK', 'Иридиевые свечи, комплект 4 шт', 'Медицинские услуги', 'NGK-IR-4', '{"бренд":"NGK","тип":"иридиевые","комплект":"4 шт"}', '00000000-0000-0000-0001-cdbc82ab0703', '00000000-0000-0000-0001-3252a7e8155e', 12000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-aa3fcd6207d7', 'свечи'), ('00000000-0000-0000-0001-aa3fcd6207d7', 'зажигания'), ('00000000-0000-0000-0001-aa3fcd6207d7', 'ngk'), ('00000000-0000-0000-0001-aa3fcd6207d7', 'медицинские'), ('00000000-0000-0000-0001-aa3fcd6207d7', 'услуги');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-a74567dea5fb', now(), now(), '00000000-0000-0000-0001-cdbc82ab0703', null, 'Медицинские услуги', 'Амортизатор Kayaba', 'Газовый амортизатор задний', 'KYB-GS-001', '{"бренд":"Kayaba","тип":"газовый","ось":"зад"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-e9f0dc8ebc49', now(), now(), '00000000-0000-0000-0001-a74567dea5fb', '00000000-0000-0000-0001-8cd98dcd471c', '00000000-0000-0000-0001-f6d91ccf5a41', 28000, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-f53f13c1a263', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-e9f0dc8ebc49', null, 'Амортизатор Kayaba', 'Газовый амортизатор задний', 'Медицинские услуги', 'KYB-GS-001', '{"бренд":"Kayaba","тип":"газовый","ось":"зад"}', '00000000-0000-0000-0001-cdbc82ab0703', '00000000-0000-0000-0001-8cd98dcd471c', 28000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-f53f13c1a263', 'амортизатор'), ('00000000-0000-0000-0001-f53f13c1a263', 'kayaba'), ('00000000-0000-0000-0001-f53f13c1a263', 'медицинские'), ('00000000-0000-0000-0001-f53f13c1a263', 'услуги');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-1ef15ba0d518', now(), now(), '00000000-0000-0000-0001-a74567dea5fb', '00000000-0000-0000-0001-3252a7e8155e', '00000000-0000-0000-0001-f6d91ccf5a41', 28000, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-f8d4378ae7d9', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-1ef15ba0d518', null, 'Амортизатор Kayaba', 'Газовый амортизатор задний', 'Медицинские услуги', 'KYB-GS-001', '{"бренд":"Kayaba","тип":"газовый","ось":"зад"}', '00000000-0000-0000-0001-cdbc82ab0703', '00000000-0000-0000-0001-3252a7e8155e', 28000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-f8d4378ae7d9', 'амортизатор'), ('00000000-0000-0000-0001-f8d4378ae7d9', 'kayaba'), ('00000000-0000-0000-0001-f8d4378ae7d9', 'медицинские'), ('00000000-0000-0000-0001-f8d4378ae7d9', 'услуги');

-- Компания-Товар-7 (ИП, Образование, Астана)
INSERT INTO business (id, created_at, updated_at, name, legal_name, bin, status) VALUES
  ('00000000-0000-0000-0001-d698dc5a269e', now(), now(), 'Компания-Товар-7', 'ИП "Компания-Товар-7"', '890123456789', 'ACTIVE');
INSERT INTO business_branch (id, created_at, updated_at, business_id, city_id, name, address, latitude, longitude, online_only, status) VALUES
  ('00000000-0000-0000-0001-4ccea096f7dd', now(), now(), '00000000-0000-0000-0001-d698dc5a269e', '00000000-0000-0000-0000-0000000000c3', 'Компания-Товар-7 - Офис', 'ул. Калдаякова, 17', 44.8472, 65.5070, false, 'ACTIVE');
INSERT INTO data_source (id, created_at, updated_at, business_id, source_type, name, status) VALUES
  ('00000000-0000-0000-0001-f0c18f903251', now(), now(), '00000000-0000-0000-0001-d698dc5a269e', 'MANUAL', 'Ручной ввод Компания-Товар-7', 'ACTIVE');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-2585b4bbbc87', now(), now(), '00000000-0000-0000-0001-d698dc5a269e', '00000000-0000-0000-0001-49c510314da7', 'Образование', 'Гипсокартон Knauf 12.5мм', 'Влагостойкий лист 2500x1200 мм', 'KNF-GKL-12', '{"бренд":"Knauf","толщина":"12.5мм","тип":"влагостойкий"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-10d9c6cc1fda', now(), now(), '00000000-0000-0000-0001-2585b4bbbc87', '00000000-0000-0000-0001-4ccea096f7dd', '00000000-0000-0000-0001-f0c18f903251', 4500, false, 'ACTIVE');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-24d2462faa3c', now(), now(), '00000000-0000-0000-0001-d698dc5a269e', '00000000-0000-0000-0001-49c510314da7', 'Образование', 'Профнастил С8 0.45мм', 'Оцинкованный профнастил для забора', 'PRF-C8-045', '{"марка":"С8","толщина":"0.45мм","тип":"оцинкованный"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-ec7c7540b9ec', now(), now(), '00000000-0000-0000-0001-24d2462faa3c', '00000000-0000-0000-0001-4ccea096f7dd', '00000000-0000-0000-0001-f0c18f903251', 2800, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-b4f123558e5d', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-ec7c7540b9ec', null, 'Профнастил С8 0.45мм', 'Оцинкованный профнастил для забора', 'Образование', 'PRF-C8-045', '{"марка":"С8","толщина":"0.45мм","тип":"оцинкованный"}', '00000000-0000-0000-0001-d698dc5a269e', '00000000-0000-0000-0001-4ccea096f7dd', 2800, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-b4f123558e5d', 'профнастил'), ('00000000-0000-0000-0001-b4f123558e5d', '0.45мм'), ('00000000-0000-0000-0001-b4f123558e5d', 'образование');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-404b172d0e06', now(), now(), '00000000-0000-0000-0001-d698dc5a269e', null, 'Образование', 'Металлочерепица Монтеррей', 'Полиэстер 0.5мм, коричневый', 'MCH-MT-05', '{"тип":"Монтеррей","толщина":"0.5мм","покрытие":"полиэстер"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-3ede1fb6a3f0', now(), now(), '00000000-0000-0000-0001-404b172d0e06', '00000000-0000-0000-0001-4ccea096f7dd', '00000000-0000-0000-0001-f0c18f903251', 5200, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-125647e18447', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-3ede1fb6a3f0', null, 'Металлочерепица Монтеррей', 'Полиэстер 0.5мм, коричневый', 'Образование', 'MCH-MT-05', '{"тип":"Монтеррей","толщина":"0.5мм","покрытие":"полиэстер"}', '00000000-0000-0000-0001-d698dc5a269e', '00000000-0000-0000-0001-4ccea096f7dd', 5200, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-125647e18447', 'металлочерепица'), ('00000000-0000-0000-0001-125647e18447', 'монтеррей'), ('00000000-0000-0000-0001-125647e18447', 'образование');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-f47eef6188aa', now(), now(), '00000000-0000-0000-0001-d698dc5a269e', '00000000-0000-0000-0001-49c510314da7', 'Образование', 'Клей плиточный Ceresit CM11', 'Для керамической плитки, 25 кг', 'CER-CM11-25', '{"бренд":"Ceresit","вес":"25кг","назначение":"плитка"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-d2a5a142edb6', now(), now(), '00000000-0000-0000-0001-f47eef6188aa', '00000000-0000-0000-0001-4ccea096f7dd', '00000000-0000-0000-0001-f0c18f903251', 3200, true, 'ARCHIVED');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-8b7eaf4f4201', now(), now(), '00000000-0000-0000-0001-d698dc5a269e', '00000000-0000-0000-0001-49c510314da7', 'Образование', 'Ламинат Classen 33 класс', 'Влагостойкий, дуб, 8мм, 2.22м²', 'CLS-LAM-33', '{"бренд":"Classen","класс":"33","толщина":"8мм","рисунок":"дуб"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-ee799c09e38c', now(), now(), '00000000-0000-0000-0001-8b7eaf4f4201', '00000000-0000-0000-0001-4ccea096f7dd', '00000000-0000-0000-0001-f0c18f903251', 8500, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-bfc47f64a011', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-ee799c09e38c', null, 'Ламинат Classen 33 класс', 'Влагостойкий, дуб, 8мм, 2.22м²', 'Образование', 'CLS-LAM-33', '{"бренд":"Classen","класс":"33","толщина":"8мм","рисунок":"дуб"}', '00000000-0000-0000-0001-d698dc5a269e', '00000000-0000-0000-0001-4ccea096f7dd', 8500, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-bfc47f64a011', 'ламинат'), ('00000000-0000-0000-0001-bfc47f64a011', 'classen'), ('00000000-0000-0000-0001-bfc47f64a011', 'класс'), ('00000000-0000-0000-0001-bfc47f64a011', 'образование');

-- Компания-Товар-8 (ТОО, Спорт и фитнес, Шымкент)
INSERT INTO business (id, created_at, updated_at, name, legal_name, bin, status) VALUES
  ('00000000-0000-0000-0001-a09740a9efbe', now(), now(), 'Компания-Товар-8', 'ТОО "Компания-Товар-8"', '901234567890', 'ACTIVE');
INSERT INTO business_branch (id, created_at, updated_at, business_id, city_id, name, address, latitude, longitude, online_only, status) VALUES
  ('00000000-0000-0000-0001-ab544b0eb50a', now(), now(), '00000000-0000-0000-0001-a09740a9efbe', '00000000-0000-0000-0001-1a904d507278', 'Компания-Товар-8 - Офис', 'мкр. Нурсат, 14', 44.8482, 65.5080, false, 'ACTIVE');
INSERT INTO data_source (id, created_at, updated_at, business_id, source_type, name, status) VALUES
  ('00000000-0000-0000-0001-a749f2cd1d7d', now(), now(), '00000000-0000-0000-0001-a09740a9efbe', 'MANUAL', 'Ручной ввод Компания-Товар-8', 'ACTIVE');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-84ddc1b106e2', now(), now(), '00000000-0000-0000-0001-a09740a9efbe', '00000000-0000-0000-0001-9f59c39873da', 'Спорт и фитнес', 'Беговая дорожка CardioFit', 'Электрическая, до 16 км/ч, складная', 'CF-TM-001', '{"тип":"электрическая","скорость":"16 км/ч","складная":"да"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-6e94bc33120f', now(), now(), '00000000-0000-0000-0001-84ddc1b106e2', '00000000-0000-0000-0001-ab544b0eb50a', '00000000-0000-0000-0001-a749f2cd1d7d', 280000, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-0f484a5400a8', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-6e94bc33120f', null, 'Беговая дорожка CardioFit', 'Электрическая, до 16 км/ч, складная', 'Спорт и фитнес', 'CF-TM-001', '{"тип":"электрическая","скорость":"16 км/ч","складная":"да"}', '00000000-0000-0000-0001-a09740a9efbe', '00000000-0000-0000-0001-ab544b0eb50a', 280000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-0f484a5400a8', 'беговая'), ('00000000-0000-0000-0001-0f484a5400a8', 'дорожка'), ('00000000-0000-0000-0001-0f484a5400a8', 'cardiofit'), ('00000000-0000-0000-0001-0f484a5400a8', 'спорт'), ('00000000-0000-0000-0001-0f484a5400a8', 'фитнес');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-b6d4dd47a7c9', now(), now(), '00000000-0000-0000-0001-a09740a9efbe', null, 'Спорт и фитнес', 'Гантели набор 20кг', 'Пара гантелей с блинами, обрезиненные', 'GNT-SET-20', '{"вес":"20кг","тип":"обрезиненные","в_комплекте":"2 шт"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-79266e78c35e', now(), now(), '00000000-0000-0000-0001-b6d4dd47a7c9', '00000000-0000-0000-0001-ab544b0eb50a', '00000000-0000-0000-0001-a749f2cd1d7d', 35000, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-56b2911a44ac', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-79266e78c35e', null, 'Гантели набор 20кг', 'Пара гантелей с блинами, обрезиненные', 'Спорт и фитнес', 'GNT-SET-20', '{"вес":"20кг","тип":"обрезиненные","в_комплекте":"2 шт"}', '00000000-0000-0000-0001-a09740a9efbe', '00000000-0000-0000-0001-ab544b0eb50a', 35000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-56b2911a44ac', 'гантели'), ('00000000-0000-0000-0001-56b2911a44ac', 'набор'), ('00000000-0000-0000-0001-56b2911a44ac', '20кг'), ('00000000-0000-0000-0001-56b2911a44ac', 'спорт'), ('00000000-0000-0000-0001-56b2911a44ac', 'фитнес');

-- Компания-Товар-9 (ИП, IT услуги, Караганда)
INSERT INTO business (id, created_at, updated_at, name, legal_name, bin, status) VALUES
  ('00000000-0000-0000-0001-9232a5977841', now(), now(), 'Компания-Товар-9', 'ИП "Компания-Товар-9"', '012345678901', 'ACTIVE');
INSERT INTO business_branch (id, created_at, updated_at, business_id, city_id, name, address, latitude, longitude, online_only, status) VALUES
  ('00000000-0000-0000-0001-c72d7f0d0733', now(), now(), '00000000-0000-0000-0001-9232a5977841', '00000000-0000-0000-0001-02ba3c39b41d', 'Компания-Товар-9 - Офис', 'пр. Нуркена Абдирова, 15', 44.8492, 65.5090, true, 'ACTIVE');
INSERT INTO data_source (id, created_at, updated_at, business_id, source_type, name, status) VALUES
  ('00000000-0000-0000-0001-76fb4e778209', now(), now(), '00000000-0000-0000-0001-9232a5977841', 'MANUAL', 'Ручной ввод Компания-Товар-9', 'ACTIVE');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-e12d85812450', now(), now(), '00000000-0000-0000-0001-9232a5977841', null, 'IT услуги', 'Антивирус Kaspersky 1 год', 'Защита на 3 устройства, лицензия', 'KAS-AV-3D', '{"бренд":"Kaspersky","устройств":"3","срок":"1 год"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-506274efd591', now(), now(), '00000000-0000-0000-0001-e12d85812450', '00000000-0000-0000-0001-c72d7f0d0733', '00000000-0000-0000-0001-76fb4e778209', 12000, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-4ea2f942f668', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-506274efd591', null, 'Антивирус Kaspersky 1 год', 'Защита на 3 устройства, лицензия', 'IT услуги', 'KAS-AV-3D', '{"бренд":"Kaspersky","устройств":"3","срок":"1 год"}', '00000000-0000-0000-0001-9232a5977841', '00000000-0000-0000-0001-c72d7f0d0733', 12000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-4ea2f942f668', 'антивирус'), ('00000000-0000-0000-0001-4ea2f942f668', 'kaspersky'), ('00000000-0000-0000-0001-4ea2f942f668', 'год'), ('00000000-0000-0000-0001-4ea2f942f668', 'услуги');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-b3535b28cec3', now(), now(), '00000000-0000-0000-0001-9232a5977841', '00000000-0000-0000-0001-a686baabac2c', 'IT услуги', 'Microsoft Office 365', 'Годовая подписка на 1 пользователя', 'MS-O365-1Y', '{"бренд":"Microsoft","пользователей":"1","срок":"1 год"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-92e60e4f9abb', now(), now(), '00000000-0000-0000-0001-b3535b28cec3', '00000000-0000-0000-0001-c72d7f0d0733', '00000000-0000-0000-0001-76fb4e778209', 35000, true, 'ARCHIVED');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-e1755add0678', now(), now(), '00000000-0000-0000-0001-9232a5977841', '00000000-0000-0000-0001-a686baabac2c', 'IT услуги', 'Windows 11 Pro лицензия', 'Цифровая лицензия, русский язык', 'WIN-11-PRO', '{"версия":"Pro","тип":"цифровая"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-641f26c9a404', now(), now(), '00000000-0000-0000-0001-e1755add0678', '00000000-0000-0000-0001-c72d7f0d0733', '00000000-0000-0000-0001-76fb4e778209', 55000, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-d58cb77c822d', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-641f26c9a404', null, 'Windows 11 Pro лицензия', 'Цифровая лицензия, русский язык', 'IT услуги', 'WIN-11-PRO', '{"версия":"Pro","тип":"цифровая"}', '00000000-0000-0000-0001-9232a5977841', '00000000-0000-0000-0001-c72d7f0d0733', 55000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-d58cb77c822d', 'windows'), ('00000000-0000-0000-0001-d58cb77c822d', 'pro'), ('00000000-0000-0000-0001-d58cb77c822d', 'лицензия'), ('00000000-0000-0000-0001-d58cb77c822d', 'услуги');

-- Компания-Товар-10 (ТОО, Автозапчасти, Кызылорда)
INSERT INTO business (id, created_at, updated_at, name, legal_name, bin, status) VALUES
  ('00000000-0000-0000-0001-1f8518fd5876', now(), now(), 'Компания-Товар-10', 'ТОО "Компания-Товар-10"', '123456789012', 'ACTIVE');
INSERT INTO business_branch (id, created_at, updated_at, business_id, city_id, name, address, latitude, longitude, online_only, status) VALUES
  ('00000000-0000-0000-0001-151edab18f33', now(), now(), '00000000-0000-0000-0001-1f8518fd5876', '00000000-0000-0000-0000-0000000000c1', 'Компания-Товар-10 - Офис', 'ул. Айтеке би, 5', 44.8502, 65.5100, false, 'ACTIVE');
INSERT INTO data_source (id, created_at, updated_at, business_id, source_type, name, status) VALUES
  ('00000000-0000-0000-0001-058da370be59', now(), now(), '00000000-0000-0000-0001-1f8518fd5876', 'MANUAL', 'Ручной ввод Компания-Товар-10', 'ACTIVE');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-f1eb5d020bbe', now(), now(), '00000000-0000-0000-0001-1f8518fd5876', '00000000-0000-0000-0000-0000000000a1', 'Автозапчасти', 'Ремень ГРМ Gates', 'Ремень ГРМ с роликом, комплект', 'GTS-TB-KIT', '{"бренд":"Gates","тип":"комплект"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-f00eeead7ebf', now(), now(), '00000000-0000-0000-0001-f1eb5d020bbe', '00000000-0000-0000-0001-151edab18f33', '00000000-0000-0000-0001-058da370be59', 22000, true, 'ARCHIVED');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-38f9fec69c0c', now(), now(), '00000000-0000-0000-0001-1f8518fd5876', '00000000-0000-0000-0000-0000000000a1', 'Автозапчасти', 'Масло подсолнечное 5л', 'Рафинированное дезодорированное', 'MAS-POD-5L', '{"тип":"рафинированное","объем":"5л"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-d4c659b99a4b', now(), now(), '00000000-0000-0000-0001-38f9fec69c0c', '00000000-0000-0000-0001-151edab18f33', '00000000-0000-0000-0001-058da370be59', 4500, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-0069aea61221', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-d4c659b99a4b', null, 'Масло подсолнечное 5л', 'Рафинированное дезодорированное', 'Автозапчасти', 'MAS-POD-5L', '{"тип":"рафинированное","объем":"5л"}', '00000000-0000-0000-0001-1f8518fd5876', '00000000-0000-0000-0001-151edab18f33', 4500, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-0069aea61221', 'масло'), ('00000000-0000-0000-0001-0069aea61221', 'подсолнечное'), ('00000000-0000-0000-0001-0069aea61221', 'автозапчасти');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-70c33b39cb41', now(), now(), '00000000-0000-0000-0001-1f8518fd5876', null, 'Автозапчасти', 'Масляный фильтр Sakura', 'Фильтр масляный для японских авто', 'SAK-OF-001', '{"бренд":"Sakura","тип":"масляный"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-1a89e2d798ab', now(), now(), '00000000-0000-0000-0001-70c33b39cb41', '00000000-0000-0000-0001-151edab18f33', '00000000-0000-0000-0001-058da370be59', 3500, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-81447bb40812', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-1a89e2d798ab', null, 'Масляный фильтр Sakura', 'Фильтр масляный для японских авто', 'Автозапчасти', 'SAK-OF-001', '{"бренд":"Sakura","тип":"масляный"}', '00000000-0000-0000-0001-1f8518fd5876', '00000000-0000-0000-0001-151edab18f33', 3500, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-81447bb40812', 'масляный'), ('00000000-0000-0000-0001-81447bb40812', 'фильтр'), ('00000000-0000-0000-0001-81447bb40812', 'sakura'), ('00000000-0000-0000-0001-81447bb40812', 'автозапчасти');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-e3ca3634e91a', now(), now(), '00000000-0000-0000-0001-1f8518fd5876', '00000000-0000-0000-0000-0000000000a1', 'Автозапчасти', 'Тормозные колодки TRW', 'Передние колодки для европейских авто', 'TRW-BP-202', '{"бренд":"TRW","ось":"перед","тип":"керамика"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-20856423a92a', now(), now(), '00000000-0000-0000-0001-e3ca3634e91a', '00000000-0000-0000-0001-151edab18f33', '00000000-0000-0000-0001-058da370be59', 18500, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-94e198d9365a', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-20856423a92a', null, 'Тормозные колодки TRW', 'Передние колодки для европейских авто', 'Автозапчасти', 'TRW-BP-202', '{"бренд":"TRW","ось":"перед","тип":"керамика"}', '00000000-0000-0000-0001-1f8518fd5876', '00000000-0000-0000-0001-151edab18f33', 18500, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-94e198d9365a', 'тормозные'), ('00000000-0000-0000-0001-94e198d9365a', 'колодки'), ('00000000-0000-0000-0001-94e198d9365a', 'trw'), ('00000000-0000-0000-0001-94e198d9365a', 'автозапчасти');

-- Компания-Товар-11 (ИП, Бытовая техника, Алматы)
INSERT INTO business (id, created_at, updated_at, name, legal_name, bin, status) VALUES
  ('00000000-0000-0000-0001-738225e5644a', now(), now(), 'Компания-Товар-11', 'ИП "Компания-Товар-11"', '234567890123', 'ACTIVE');
INSERT INTO business_branch (id, created_at, updated_at, business_id, city_id, name, address, latitude, longitude, online_only, status) VALUES
  ('00000000-0000-0000-0001-9b3738891458', now(), now(), '00000000-0000-0000-0001-738225e5644a', '00000000-0000-0000-0000-0000000000c2', 'Компания-Товар-11 - Офис', 'пр. Абая, 150', 44.8512, 65.5110, false, 'ACTIVE');
INSERT INTO data_source (id, created_at, updated_at, business_id, source_type, name, status) VALUES
  ('00000000-0000-0000-0001-6d0a0e77e47a', now(), now(), '00000000-0000-0000-0001-738225e5644a', 'MANUAL', 'Ручной ввод Компания-Товар-11', 'ACTIVE');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-a6c17ff9931b', now(), now(), '00000000-0000-0000-0001-738225e5644a', '00000000-0000-0000-0000-0000000000a2', 'Бытовая техника', 'Холодильник LG 350L', 'Двухкамерный холодильник с No Frost', 'LG-RF-350', '{"бренд":"LG","объем":"350л","тип":"No Frost"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-2b97529f09a8', now(), now(), '00000000-0000-0000-0001-a6c17ff9931b', '00000000-0000-0000-0001-9b3738891458', '00000000-0000-0000-0001-6d0a0e77e47a', 189000, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-227f00a1f1c5', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-2b97529f09a8', null, 'Холодильник LG 350L', 'Двухкамерный холодильник с No Frost', 'Бытовая техника', 'LG-RF-350', '{"бренд":"LG","объем":"350л","тип":"No Frost"}', '00000000-0000-0000-0001-738225e5644a', '00000000-0000-0000-0001-9b3738891458', 189000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-227f00a1f1c5', 'холодильник'), ('00000000-0000-0000-0001-227f00a1f1c5', '350l'), ('00000000-0000-0000-0001-227f00a1f1c5', 'бытовая'), ('00000000-0000-0000-0001-227f00a1f1c5', 'техника');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-91a5dc212729', now(), now(), '00000000-0000-0000-0001-738225e5644a', null, 'Бытовая техника', 'Стиральная машина Bosch', 'Фронтальная загрузка 7кг, 1200 об/мин', 'BOS-WM-7', '{"бренд":"Bosch","загрузка":"7кг","обороты":"1200"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-514cc2783521', now(), now(), '00000000-0000-0000-0001-91a5dc212729', '00000000-0000-0000-0001-9b3738891458', '00000000-0000-0000-0001-6d0a0e77e47a', 245000, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-b3bd7063967f', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-514cc2783521', null, 'Стиральная машина Bosch', 'Фронтальная загрузка 7кг, 1200 об/мин', 'Бытовая техника', 'BOS-WM-7', '{"бренд":"Bosch","загрузка":"7кг","обороты":"1200"}', '00000000-0000-0000-0001-738225e5644a', '00000000-0000-0000-0001-9b3738891458', 245000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-b3bd7063967f', 'стиральная'), ('00000000-0000-0000-0001-b3bd7063967f', 'машина'), ('00000000-0000-0000-0001-b3bd7063967f', 'bosch'), ('00000000-0000-0000-0001-b3bd7063967f', 'бытовая'), ('00000000-0000-0000-0001-b3bd7063967f', 'техника');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-ee1f5d54aaf3', now(), now(), '00000000-0000-0000-0001-738225e5644a', '00000000-0000-0000-0000-0000000000a2', 'Бытовая техника', 'Микроволновая печь Samsung', 'СВЧ 23л, гриль, сенсорное управление', 'SAM-MW-23', '{"бренд":"Samsung","объем":"23л","гриль":"да"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-cf700b726f3c', now(), now(), '00000000-0000-0000-0001-ee1f5d54aaf3', '00000000-0000-0000-0001-9b3738891458', '00000000-0000-0000-0001-6d0a0e77e47a', 45000, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-cb780f834f7f', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-cf700b726f3c', null, 'Микроволновая печь Samsung', 'СВЧ 23л, гриль, сенсорное управление', 'Бытовая техника', 'SAM-MW-23', '{"бренд":"Samsung","объем":"23л","гриль":"да"}', '00000000-0000-0000-0001-738225e5644a', '00000000-0000-0000-0001-9b3738891458', 45000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-cb780f834f7f', 'микроволновая'), ('00000000-0000-0000-0001-cb780f834f7f', 'печь'), ('00000000-0000-0000-0001-cb780f834f7f', 'samsung'), ('00000000-0000-0000-0001-cb780f834f7f', 'бытовая'), ('00000000-0000-0000-0001-cb780f834f7f', 'техника');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-1fc7a98bb369', now(), now(), '00000000-0000-0000-0001-738225e5644a', '00000000-0000-0000-0000-0000000000a2', 'Бытовая техника', 'Пылесос Dyson V15', 'Беспроводной вертикальный пылесос', 'DYS-V15', '{"бренд":"Dyson","тип":"вертикальный","аккумулятор":"Li-Ion"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-bf851671f67b', now(), now(), '00000000-0000-0000-0001-1fc7a98bb369', '00000000-0000-0000-0001-9b3738891458', '00000000-0000-0000-0001-6d0a0e77e47a', 320000, false, 'ACTIVE');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-fd0ef5e8747d', now(), now(), '00000000-0000-0000-0001-738225e5644a', null, 'Бытовая техника', 'Утюг Philips Azur', 'Паровой утюг с керамической подошвой', 'PHI-AZ-3000', '{"бренд":"Philips","тип":"паровой"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-c1eb7a60c5d4', now(), now(), '00000000-0000-0000-0001-fd0ef5e8747d', '00000000-0000-0000-0001-9b3738891458', '00000000-0000-0000-0001-6d0a0e77e47a', 28000, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-b80f91a16682', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-c1eb7a60c5d4', null, 'Утюг Philips Azur', 'Паровой утюг с керамической подошвой', 'Бытовая техника', 'PHI-AZ-3000', '{"бренд":"Philips","тип":"паровой"}', '00000000-0000-0000-0001-738225e5644a', '00000000-0000-0000-0001-9b3738891458', 28000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-b80f91a16682', 'утюг'), ('00000000-0000-0000-0001-b80f91a16682', 'philips'), ('00000000-0000-0000-0001-b80f91a16682', 'azur'), ('00000000-0000-0000-0001-b80f91a16682', 'бытовая'), ('00000000-0000-0000-0001-b80f91a16682', 'техника');

-- Компания-Товар-12 (ТОО, Услуги красоты, Астана)
INSERT INTO business (id, created_at, updated_at, name, legal_name, bin, status) VALUES
  ('00000000-0000-0000-0001-5084b52ea577', now(), now(), 'Компания-Товар-12', 'ТОО "Компания-Товар-12"', '345678901234', 'ACTIVE');
INSERT INTO business_branch (id, created_at, updated_at, business_id, city_id, name, address, latitude, longitude, online_only, status) VALUES
  ('00000000-0000-0000-0001-d5d53a374b4f', now(), now(), '00000000-0000-0000-0001-5084b52ea577', '00000000-0000-0000-0000-0000000000c3', 'Компания-Товар-12 - Филиал 1', 'мкр. Чубары, 8', 44.8522, 65.5120, false, 'ACTIVE');
INSERT INTO business_branch (id, created_at, updated_at, business_id, city_id, name, address, latitude, longitude, online_only, status) VALUES
  ('00000000-0000-0000-0001-0a01459330ba', now(), now(), '00000000-0000-0000-0001-5084b52ea577', '00000000-0000-0000-0000-0000000000c3', 'Компания-Товар-12 - Филиал 2', 'мкр. Чубары, 8', 44.8522, 65.5120, false, 'ACTIVE');
INSERT INTO data_source (id, created_at, updated_at, business_id, source_type, name, status) VALUES
  ('00000000-0000-0000-0001-527a8c40ca5d', now(), now(), '00000000-0000-0000-0001-5084b52ea577', 'MANUAL', 'Ручной ввод Компания-Товар-12', 'ACTIVE');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-4e8967b77d62', now(), now(), '00000000-0000-0000-0001-5084b52ea577', null, 'Услуги красоты', 'Масляный фильтр Sakura', 'Фильтр масляный для японских авто', 'SAK-OF-001', '{"бренд":"Sakura","тип":"масляный"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-432836ca2a27', now(), now(), '00000000-0000-0000-0001-4e8967b77d62', '00000000-0000-0000-0001-d5d53a374b4f', '00000000-0000-0000-0001-527a8c40ca5d', 3500, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-9efbe4e6fd09', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-432836ca2a27', null, 'Масляный фильтр Sakura', 'Фильтр масляный для японских авто', 'Услуги красоты', 'SAK-OF-001', '{"бренд":"Sakura","тип":"масляный"}', '00000000-0000-0000-0001-5084b52ea577', '00000000-0000-0000-0001-d5d53a374b4f', 3500, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-9efbe4e6fd09', 'масляный'), ('00000000-0000-0000-0001-9efbe4e6fd09', 'фильтр'), ('00000000-0000-0000-0001-9efbe4e6fd09', 'sakura'), ('00000000-0000-0000-0001-9efbe4e6fd09', 'услуги'), ('00000000-0000-0000-0001-9efbe4e6fd09', 'красоты');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-cfeece289253', now(), now(), '00000000-0000-0000-0001-4e8967b77d62', '00000000-0000-0000-0001-0a01459330ba', '00000000-0000-0000-0001-527a8c40ca5d', 3500, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-082164b0d914', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-cfeece289253', null, 'Масляный фильтр Sakura', 'Фильтр масляный для японских авто', 'Услуги красоты', 'SAK-OF-001', '{"бренд":"Sakura","тип":"масляный"}', '00000000-0000-0000-0001-5084b52ea577', '00000000-0000-0000-0001-0a01459330ba', 3500, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-082164b0d914', 'масляный'), ('00000000-0000-0000-0001-082164b0d914', 'фильтр'), ('00000000-0000-0000-0001-082164b0d914', 'sakura'), ('00000000-0000-0000-0001-082164b0d914', 'услуги'), ('00000000-0000-0000-0001-082164b0d914', 'красоты');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-1980dfafd441', now(), now(), '00000000-0000-0000-0001-5084b52ea577', '00000000-0000-0000-0000-0000000000a3', 'Услуги красоты', 'Тормозные колодки TRW', 'Передние колодки для европейских авто', 'TRW-BP-202', '{"бренд":"TRW","ось":"перед","тип":"керамика"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-b4a361a5b156', now(), now(), '00000000-0000-0000-0001-1980dfafd441', '00000000-0000-0000-0001-d5d53a374b4f', '00000000-0000-0000-0001-527a8c40ca5d', 18500, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-336ce8d8eff0', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-b4a361a5b156', null, 'Тормозные колодки TRW', 'Передние колодки для европейских авто', 'Услуги красоты', 'TRW-BP-202', '{"бренд":"TRW","ось":"перед","тип":"керамика"}', '00000000-0000-0000-0001-5084b52ea577', '00000000-0000-0000-0001-d5d53a374b4f', 18500, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-336ce8d8eff0', 'тормозные'), ('00000000-0000-0000-0001-336ce8d8eff0', 'колодки'), ('00000000-0000-0000-0001-336ce8d8eff0', 'trw'), ('00000000-0000-0000-0001-336ce8d8eff0', 'услуги'), ('00000000-0000-0000-0001-336ce8d8eff0', 'красоты');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-6b24c291af12', now(), now(), '00000000-0000-0000-0001-1980dfafd441', '00000000-0000-0000-0001-0a01459330ba', '00000000-0000-0000-0001-527a8c40ca5d', 18500, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-48d4c1931fc6', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-6b24c291af12', null, 'Тормозные колодки TRW', 'Передние колодки для европейских авто', 'Услуги красоты', 'TRW-BP-202', '{"бренд":"TRW","ось":"перед","тип":"керамика"}', '00000000-0000-0000-0001-5084b52ea577', '00000000-0000-0000-0001-0a01459330ba', 18500, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-48d4c1931fc6', 'тормозные'), ('00000000-0000-0000-0001-48d4c1931fc6', 'колодки'), ('00000000-0000-0000-0001-48d4c1931fc6', 'trw'), ('00000000-0000-0000-0001-48d4c1931fc6', 'услуги'), ('00000000-0000-0000-0001-48d4c1931fc6', 'красоты');

-- Компания-Товар-13 (ИП, Ремонт и сервис, Шымкент)
INSERT INTO business (id, created_at, updated_at, name, legal_name, bin, status) VALUES
  ('00000000-0000-0000-0001-97f587059e9c', now(), now(), 'Компания-Товар-13', 'ИП "Компания-Товар-13"', '456789012345', 'ACTIVE');
INSERT INTO business_branch (id, created_at, updated_at, business_id, city_id, name, address, latitude, longitude, online_only, status) VALUES
  ('00000000-0000-0000-0001-9db01387de3d', now(), now(), '00000000-0000-0000-0001-97f587059e9c', '00000000-0000-0000-0001-1a904d507278', 'Компания-Товар-13 - Офис', 'ул. Казыбек би, 35', 44.8532, 65.5130, false, 'ACTIVE');
INSERT INTO data_source (id, created_at, updated_at, business_id, source_type, name, status) VALUES
  ('00000000-0000-0000-0001-e8e268dcde1d', now(), now(), '00000000-0000-0000-0001-97f587059e9c', 'MANUAL', 'Ручной ввод Компания-Товар-13', 'ACTIVE');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-e45068b42861', now(), now(), '00000000-0000-0000-0001-97f587059e9c', '00000000-0000-0000-0000-0000000000a4', 'Ремонт и сервис', 'Шуруповерт Makita 18В', 'Аккумуляторный, 2 батареи, кейс', 'MAK-SD-18', '{"бренд":"Makita","напряжение":"18В","акб":"2 шт"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-3616e9ecd717', now(), now(), '00000000-0000-0000-0001-e45068b42861', '00000000-0000-0000-0001-9db01387de3d', '00000000-0000-0000-0001-e8e268dcde1d', 45000, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-d03656405807', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-3616e9ecd717', null, 'Шуруповерт Makita 18В', 'Аккумуляторный, 2 батареи, кейс', 'Ремонт и сервис', 'MAK-SD-18', '{"бренд":"Makita","напряжение":"18В","акб":"2 шт"}', '00000000-0000-0000-0001-97f587059e9c', '00000000-0000-0000-0001-9db01387de3d', 45000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-d03656405807', 'шуруповерт'), ('00000000-0000-0000-0001-d03656405807', 'makita'), ('00000000-0000-0000-0001-d03656405807', '18в'), ('00000000-0000-0000-0001-d03656405807', 'ремонт'), ('00000000-0000-0000-0001-d03656405807', 'сервис');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-88840812be86', now(), now(), '00000000-0000-0000-0001-97f587059e9c', '00000000-0000-0000-0000-0000000000a4', 'Ремонт и сервис', 'Болгарка УШМ 125мм', 'Угловая шлифмашина, 1100Вт', 'USH-125-1100', '{"диаметр":"125мм","мощность":"1100Вт"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-b0619eec6d98', now(), now(), '00000000-0000-0000-0001-88840812be86', '00000000-0000-0000-0001-9db01387de3d', '00000000-0000-0000-0001-e8e268dcde1d', 15000, false, 'ACTIVE');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-101710a96185', now(), now(), '00000000-0000-0000-0001-97f587059e9c', null, 'Ремонт и сервис', 'Дрель ударная Bosch 750Вт', 'Дрель ударная с реверсом, кейс', 'BOS-DR-750', '{"бренд":"Bosch","мощность":"750Вт","тип":"ударная"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-479195bd5f59', now(), now(), '00000000-0000-0000-0001-101710a96185', '00000000-0000-0000-0001-9db01387de3d', '00000000-0000-0000-0001-e8e268dcde1d', 28000, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-a29ac29db6f9', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-479195bd5f59', null, 'Дрель ударная Bosch 750Вт', 'Дрель ударная с реверсом, кейс', 'Ремонт и сервис', 'BOS-DR-750', '{"бренд":"Bosch","мощность":"750Вт","тип":"ударная"}', '00000000-0000-0000-0001-97f587059e9c', '00000000-0000-0000-0001-9db01387de3d', 28000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-a29ac29db6f9', 'дрель'), ('00000000-0000-0000-0001-a29ac29db6f9', 'ударная'), ('00000000-0000-0000-0001-a29ac29db6f9', 'bosch'), ('00000000-0000-0000-0001-a29ac29db6f9', '750вт'), ('00000000-0000-0000-0001-a29ac29db6f9', 'ремонт'), ('00000000-0000-0000-0001-a29ac29db6f9', 'сервис');

-- Компания-Товар-14 (ТОО, Строительство, Караганда)
INSERT INTO business (id, created_at, updated_at, name, legal_name, bin, status) VALUES
  ('00000000-0000-0000-0001-065e742bbf23', now(), now(), 'Компания-Товар-14', 'ТОО "Компания-Товар-14"', '567890123456', 'ACTIVE');
INSERT INTO business_branch (id, created_at, updated_at, business_id, city_id, name, address, latitude, longitude, online_only, status) VALUES
  ('00000000-0000-0000-0001-279373d83a3d', now(), now(), '00000000-0000-0000-0001-065e742bbf23', '00000000-0000-0000-0001-02ba3c39b41d', 'Компания-Товар-14 - Офис', 'ул. Ермекова, 40', 44.8542, 65.5140, false, 'ACTIVE');
INSERT INTO data_source (id, created_at, updated_at, business_id, source_type, name, status) VALUES
  ('00000000-0000-0000-0001-05dc3474cccb', now(), now(), '00000000-0000-0000-0001-065e742bbf23', 'MANUAL', 'Ручной ввод Компания-Товар-14', 'ACTIVE');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-9e12ce0578b8', now(), now(), '00000000-0000-0000-0001-065e742bbf23', '00000000-0000-0000-0000-0000000000a5', 'Строительство', 'Гипсокартон Knauf 12.5мм', 'Влагостойкий лист 2500x1200 мм', 'KNF-GKL-12', '{"бренд":"Knauf","толщина":"12.5мм","тип":"влагостойкий"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-1b3adbfaf595', now(), now(), '00000000-0000-0000-0001-9e12ce0578b8', '00000000-0000-0000-0001-279373d83a3d', '00000000-0000-0000-0001-05dc3474cccb', 4500, false, 'ACTIVE');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-ef325e564f2f', now(), now(), '00000000-0000-0000-0001-065e742bbf23', null, 'Строительство', 'Профнастил С8 0.45мм', 'Оцинкованный профнастил для забора', 'PRF-C8-045', '{"марка":"С8","толщина":"0.45мм","тип":"оцинкованный"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-ef290635a26d', now(), now(), '00000000-0000-0000-0001-ef325e564f2f', '00000000-0000-0000-0001-279373d83a3d', '00000000-0000-0000-0001-05dc3474cccb', 2800, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-3a8c143cd085', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-ef290635a26d', null, 'Профнастил С8 0.45мм', 'Оцинкованный профнастил для забора', 'Строительство', 'PRF-C8-045', '{"марка":"С8","толщина":"0.45мм","тип":"оцинкованный"}', '00000000-0000-0000-0001-065e742bbf23', '00000000-0000-0000-0001-279373d83a3d', 2800, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-3a8c143cd085', 'профнастил'), ('00000000-0000-0000-0001-3a8c143cd085', '0.45мм'), ('00000000-0000-0000-0001-3a8c143cd085', 'строительство');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-dbdb53daf241', now(), now(), '00000000-0000-0000-0001-065e742bbf23', '00000000-0000-0000-0000-0000000000a5', 'Строительство', 'Металлочерепица Монтеррей', 'Полиэстер 0.5мм, коричневый', 'MCH-MT-05', '{"тип":"Монтеррей","толщина":"0.5мм","покрытие":"полиэстер"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-74560797661d', now(), now(), '00000000-0000-0000-0001-dbdb53daf241', '00000000-0000-0000-0001-279373d83a3d', '00000000-0000-0000-0001-05dc3474cccb', 5200, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-70585ebf2dc6', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-74560797661d', null, 'Металлочерепица Монтеррей', 'Полиэстер 0.5мм, коричневый', 'Строительство', 'MCH-MT-05', '{"тип":"Монтеррей","толщина":"0.5мм","покрытие":"полиэстер"}', '00000000-0000-0000-0001-065e742bbf23', '00000000-0000-0000-0001-279373d83a3d', 5200, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-70585ebf2dc6', 'металлочерепица'), ('00000000-0000-0000-0001-70585ebf2dc6', 'монтеррей'), ('00000000-0000-0000-0001-70585ebf2dc6', 'строительство');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-13253bd831e2', now(), now(), '00000000-0000-0000-0001-065e742bbf23', '00000000-0000-0000-0000-0000000000a5', 'Строительство', 'Клей плиточный Ceresit CM11', 'Для керамической плитки, 25 кг', 'CER-CM11-25', '{"бренд":"Ceresit","вес":"25кг","назначение":"плитка"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-195b0f03e971', now(), now(), '00000000-0000-0000-0001-13253bd831e2', '00000000-0000-0000-0001-279373d83a3d', '00000000-0000-0000-0001-05dc3474cccb', 3200, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-835da34c2b38', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-195b0f03e971', null, 'Клей плиточный Ceresit CM11', 'Для керамической плитки, 25 кг', 'Строительство', 'CER-CM11-25', '{"бренд":"Ceresit","вес":"25кг","назначение":"плитка"}', '00000000-0000-0000-0001-065e742bbf23', '00000000-0000-0000-0001-279373d83a3d', 3200, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-835da34c2b38', 'клей'), ('00000000-0000-0000-0001-835da34c2b38', 'плиточный'), ('00000000-0000-0000-0001-835da34c2b38', 'ceresit'), ('00000000-0000-0000-0001-835da34c2b38', 'cm11'), ('00000000-0000-0000-0001-835da34c2b38', 'строительство');

-- Компания-Товар-15 (ИП, Продукты питания, Кызылорда)
INSERT INTO business (id, created_at, updated_at, name, legal_name, bin, status) VALUES
  ('00000000-0000-0000-0001-21acdd22ad76', now(), now(), 'Компания-Товар-15', 'ИП "Компания-Товар-15"', '678901234567', 'ACTIVE');
INSERT INTO business_branch (id, created_at, updated_at, business_id, city_id, name, address, latitude, longitude, online_only, status) VALUES
  ('00000000-0000-0000-0001-5404fd1544e4', now(), now(), '00000000-0000-0000-0001-21acdd22ad76', '00000000-0000-0000-0000-0000000000c1', 'Компания-Товар-15 - Офис', 'ул. Коркыт Ата, 34', 44.8552, 65.5150, false, 'ACTIVE');
INSERT INTO data_source (id, created_at, updated_at, business_id, source_type, name, status) VALUES
  ('00000000-0000-0000-0001-51624cfda8fd', now(), now(), '00000000-0000-0000-0001-21acdd22ad76', 'MANUAL', 'Ручной ввод Компания-Товар-15', 'ACTIVE');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-725a24283a1c', now(), now(), '00000000-0000-0000-0001-21acdd22ad76', null, 'Продукты питания', 'Сахар-песок 1кг', 'Белый свекловичный сахар', 'SAH-BEL-1', '{"тип":"свекловичный","вес":"1кг"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-80341cb81f39', now(), now(), '00000000-0000-0000-0001-725a24283a1c', '00000000-0000-0000-0001-5404fd1544e4', '00000000-0000-0000-0001-51624cfda8fd', 550, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-a8e1baedadff', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-80341cb81f39', null, 'Сахар-песок 1кг', 'Белый свекловичный сахар', 'Продукты питания', 'SAH-BEL-1', '{"тип":"свекловичный","вес":"1кг"}', '00000000-0000-0000-0001-21acdd22ad76', '00000000-0000-0000-0001-5404fd1544e4', 550, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-a8e1baedadff', 'сахар'), ('00000000-0000-0000-0001-a8e1baedadff', 'песок'), ('00000000-0000-0000-0001-a8e1baedadff', '1кг'), ('00000000-0000-0000-0001-a8e1baedadff', 'продукты'), ('00000000-0000-0000-0001-a8e1baedadff', 'питания');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-22887db23ed6', now(), now(), '00000000-0000-0000-0001-21acdd22ad76', '00000000-0000-0000-0001-279580d8f28e', 'Продукты питания', 'Макароны спираль 400г', 'Из твердых сортов пшеницы', 'MAK-SP-400', '{"тип":"спираль","вес":"400г","сорт":"твердый"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-746cf3ccffc3', now(), now(), '00000000-0000-0000-0001-22887db23ed6', '00000000-0000-0000-0001-5404fd1544e4', '00000000-0000-0000-0001-51624cfda8fd', 380, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-93232424e35f', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-746cf3ccffc3', null, 'Макароны спираль 400г', 'Из твердых сортов пшеницы', 'Продукты питания', 'MAK-SP-400', '{"тип":"спираль","вес":"400г","сорт":"твердый"}', '00000000-0000-0000-0001-21acdd22ad76', '00000000-0000-0000-0001-5404fd1544e4', 380, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-93232424e35f', 'макароны'), ('00000000-0000-0000-0001-93232424e35f', 'спираль'), ('00000000-0000-0000-0001-93232424e35f', '400г'), ('00000000-0000-0000-0001-93232424e35f', 'продукты'), ('00000000-0000-0000-0001-93232424e35f', 'питания');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-d60ba46289b0', now(), now(), '00000000-0000-0000-0001-21acdd22ad76', '00000000-0000-0000-0001-279580d8f28e', 'Продукты питания', 'Мука пшеничная высший сорт', 'Казахстанская мука, 50 кг мешок', 'MUK-VS-50', '{"сорт":"высший","вес":"50кг","страна":"Казахстан"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-b21c1731f4b4', now(), now(), '00000000-0000-0000-0001-d60ba46289b0', '00000000-0000-0000-0001-5404fd1544e4', '00000000-0000-0000-0001-51624cfda8fd', 12000, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-cf0d07b792b5', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-b21c1731f4b4', null, 'Мука пшеничная высший сорт', 'Казахстанская мука, 50 кг мешок', 'Продукты питания', 'MUK-VS-50', '{"сорт":"высший","вес":"50кг","страна":"Казахстан"}', '00000000-0000-0000-0001-21acdd22ad76', '00000000-0000-0000-0001-5404fd1544e4', 12000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-cf0d07b792b5', 'мука'), ('00000000-0000-0000-0001-cf0d07b792b5', 'пшеничная'), ('00000000-0000-0000-0001-cf0d07b792b5', 'высший'), ('00000000-0000-0000-0001-cf0d07b792b5', 'сорт'), ('00000000-0000-0000-0001-cf0d07b792b5', 'продукты'), ('00000000-0000-0000-0001-cf0d07b792b5', 'питания');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-48d631dc2e4a', now(), now(), '00000000-0000-0000-0001-21acdd22ad76', null, 'Продукты питания', 'Рис пропаренный 900г', 'Длиннозерный пропаренный рис', 'RIS-PR-900', '{"тип":"пропаренный","вес":"900г"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-ad95fa22367f', now(), now(), '00000000-0000-0000-0001-48d631dc2e4a', '00000000-0000-0000-0001-5404fd1544e4', '00000000-0000-0000-0001-51624cfda8fd', 850, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-03bcf5292d53', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-ad95fa22367f', null, 'Рис пропаренный 900г', 'Длиннозерный пропаренный рис', 'Продукты питания', 'RIS-PR-900', '{"тип":"пропаренный","вес":"900г"}', '00000000-0000-0000-0001-21acdd22ad76', '00000000-0000-0000-0001-5404fd1544e4', 850, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-03bcf5292d53', 'рис'), ('00000000-0000-0000-0001-03bcf5292d53', 'пропаренный'), ('00000000-0000-0000-0001-03bcf5292d53', '900г'), ('00000000-0000-0000-0001-03bcf5292d53', 'продукты'), ('00000000-0000-0000-0001-03bcf5292d53', 'питания');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-344e161bf345', now(), now(), '00000000-0000-0000-0001-21acdd22ad76', '00000000-0000-0000-0001-279580d8f28e', 'Продукты питания', 'Сахар-песок 1кг', 'Белый свекловичный сахар', 'SAH-BEL-1', '{"тип":"свекловичный","вес":"1кг"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-aef069a75a17', now(), now(), '00000000-0000-0000-0001-344e161bf345', '00000000-0000-0000-0001-5404fd1544e4', '00000000-0000-0000-0001-51624cfda8fd', 550, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-364de43e6c06', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-aef069a75a17', null, 'Сахар-песок 1кг', 'Белый свекловичный сахар', 'Продукты питания', 'SAH-BEL-1', '{"тип":"свекловичный","вес":"1кг"}', '00000000-0000-0000-0001-21acdd22ad76', '00000000-0000-0000-0001-5404fd1544e4', 550, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-364de43e6c06', 'сахар'), ('00000000-0000-0000-0001-364de43e6c06', 'песок'), ('00000000-0000-0000-0001-364de43e6c06', '1кг'), ('00000000-0000-0000-0001-364de43e6c06', 'продукты'), ('00000000-0000-0000-0001-364de43e6c06', 'питания');

-- Компания-Товар-16 (ТОО, Медицинские услуги, Алматы)
INSERT INTO business (id, created_at, updated_at, name, legal_name, bin, status) VALUES
  ('00000000-0000-0000-0001-0d57b854c8da', now(), now(), 'Компания-Товар-16', 'ТОО "Компания-Товар-16"', '789012345678', 'ACTIVE');
INSERT INTO business_branch (id, created_at, updated_at, business_id, city_id, name, address, latitude, longitude, online_only, status) VALUES
  ('00000000-0000-0000-0001-bcdb73655f4d', now(), now(), '00000000-0000-0000-0001-0d57b854c8da', '00000000-0000-0000-0000-0000000000c2', 'Компания-Товар-16 - Офис', 'пр. Аль-Фараби, 77', 44.8562, 65.5160, false, 'ACTIVE');
INSERT INTO data_source (id, created_at, updated_at, business_id, source_type, name, status) VALUES
  ('00000000-0000-0000-0001-334404dad01f', now(), now(), '00000000-0000-0000-0001-0d57b854c8da', 'MANUAL', 'Ручной ввод Компания-Товар-16', 'ACTIVE');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-90ec95d8cafa', now(), now(), '00000000-0000-0000-0001-0d57b854c8da', '00000000-0000-0000-0001-81548349b593', 'Медицинские услуги', 'Гипсокартон Knauf 12.5мм', 'Влагостойкий лист 2500x1200 мм', 'KNF-GKL-12', '{"бренд":"Knauf","толщина":"12.5мм","тип":"влагостойкий"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-b5ef983844fc', now(), now(), '00000000-0000-0000-0001-90ec95d8cafa', '00000000-0000-0000-0001-bcdb73655f4d', '00000000-0000-0000-0001-334404dad01f', 4500, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-f7f8a99ec175', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-b5ef983844fc', null, 'Гипсокартон Knauf 12.5мм', 'Влагостойкий лист 2500x1200 мм', 'Медицинские услуги', 'KNF-GKL-12', '{"бренд":"Knauf","толщина":"12.5мм","тип":"влагостойкий"}', '00000000-0000-0000-0001-0d57b854c8da', '00000000-0000-0000-0001-bcdb73655f4d', 4500, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-f7f8a99ec175', 'гипсокартон'), ('00000000-0000-0000-0001-f7f8a99ec175', 'knauf'), ('00000000-0000-0000-0001-f7f8a99ec175', '12.5мм'), ('00000000-0000-0000-0001-f7f8a99ec175', 'медицинские'), ('00000000-0000-0000-0001-f7f8a99ec175', 'услуги');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-6b0fd4194d86', now(), now(), '00000000-0000-0000-0001-0d57b854c8da', '00000000-0000-0000-0001-81548349b593', 'Медицинские услуги', 'Профнастил С8 0.45мм', 'Оцинкованный профнастил для забора', 'PRF-C8-045', '{"марка":"С8","толщина":"0.45мм","тип":"оцинкованный"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-f103e45cdf8e', now(), now(), '00000000-0000-0000-0001-6b0fd4194d86', '00000000-0000-0000-0001-bcdb73655f4d', '00000000-0000-0000-0001-334404dad01f', 2800, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-1cdfcf0776d2', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-f103e45cdf8e', null, 'Профнастил С8 0.45мм', 'Оцинкованный профнастил для забора', 'Медицинские услуги', 'PRF-C8-045', '{"марка":"С8","толщина":"0.45мм","тип":"оцинкованный"}', '00000000-0000-0000-0001-0d57b854c8da', '00000000-0000-0000-0001-bcdb73655f4d', 2800, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-1cdfcf0776d2', 'профнастил'), ('00000000-0000-0000-0001-1cdfcf0776d2', '0.45мм'), ('00000000-0000-0000-0001-1cdfcf0776d2', 'медицинские'), ('00000000-0000-0000-0001-1cdfcf0776d2', 'услуги');

-- Компания-Товар-17 (ИП, Образование, Астана)
INSERT INTO business (id, created_at, updated_at, name, legal_name, bin, status) VALUES
  ('00000000-0000-0000-0001-1f4ac2f3a0dd', now(), now(), 'Компания-Товар-17', 'ИП "Компания-Товар-17"', '890123456789', 'ACTIVE');
INSERT INTO business_branch (id, created_at, updated_at, business_id, city_id, name, address, latitude, longitude, online_only, status) VALUES
  ('00000000-0000-0000-0001-49ca3854fe6b', now(), now(), '00000000-0000-0000-0001-1f4ac2f3a0dd', '00000000-0000-0000-0000-0000000000c3', 'Компания-Товар-17 - Офис', 'ул. Сарайшык, 13', 44.8572, 65.5170, false, 'ACTIVE');
INSERT INTO data_source (id, created_at, updated_at, business_id, source_type, name, status) VALUES
  ('00000000-0000-0000-0001-371ed5c90fc9', now(), now(), '00000000-0000-0000-0001-1f4ac2f3a0dd', 'MANUAL', 'Ручной ввод Компания-Товар-17', 'ACTIVE');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-703cad8c26f1', now(), now(), '00000000-0000-0000-0001-1f4ac2f3a0dd', '00000000-0000-0000-0001-49c510314da7', 'Образование', 'Антивирус Kaspersky 1 год', 'Защита на 3 устройства, лицензия', 'KAS-AV-3D', '{"бренд":"Kaspersky","устройств":"3","срок":"1 год"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-6c3707ae93cc', now(), now(), '00000000-0000-0000-0001-703cad8c26f1', '00000000-0000-0000-0001-49ca3854fe6b', '00000000-0000-0000-0001-371ed5c90fc9', 12000, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-e74c8435c9ba', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-6c3707ae93cc', null, 'Антивирус Kaspersky 1 год', 'Защита на 3 устройства, лицензия', 'Образование', 'KAS-AV-3D', '{"бренд":"Kaspersky","устройств":"3","срок":"1 год"}', '00000000-0000-0000-0001-1f4ac2f3a0dd', '00000000-0000-0000-0001-49ca3854fe6b', 12000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-e74c8435c9ba', 'антивирус'), ('00000000-0000-0000-0001-e74c8435c9ba', 'kaspersky'), ('00000000-0000-0000-0001-e74c8435c9ba', 'год'), ('00000000-0000-0000-0001-e74c8435c9ba', 'образование');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-c021ebfd6d2b', now(), now(), '00000000-0000-0000-0001-1f4ac2f3a0dd', null, 'Образование', 'Microsoft Office 365', 'Годовая подписка на 1 пользователя', 'MS-O365-1Y', '{"бренд":"Microsoft","пользователей":"1","срок":"1 год"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-7c277622d5cb', now(), now(), '00000000-0000-0000-0001-c021ebfd6d2b', '00000000-0000-0000-0001-49ca3854fe6b', '00000000-0000-0000-0001-371ed5c90fc9', 35000, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-29a88daf304d', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-7c277622d5cb', null, 'Microsoft Office 365', 'Годовая подписка на 1 пользователя', 'Образование', 'MS-O365-1Y', '{"бренд":"Microsoft","пользователей":"1","срок":"1 год"}', '00000000-0000-0000-0001-1f4ac2f3a0dd', '00000000-0000-0000-0001-49ca3854fe6b', 35000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-29a88daf304d', 'microsoft'), ('00000000-0000-0000-0001-29a88daf304d', 'office'), ('00000000-0000-0000-0001-29a88daf304d', '365'), ('00000000-0000-0000-0001-29a88daf304d', 'образование');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-44b8cf5f3929', now(), now(), '00000000-0000-0000-0001-1f4ac2f3a0dd', '00000000-0000-0000-0001-49c510314da7', 'Образование', 'Windows 11 Pro лицензия', 'Цифровая лицензия, русский язык', 'WIN-11-PRO', '{"версия":"Pro","тип":"цифровая"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-1e27dc0bfce2', now(), now(), '00000000-0000-0000-0001-44b8cf5f3929', '00000000-0000-0000-0001-49ca3854fe6b', '00000000-0000-0000-0001-371ed5c90fc9', 55000, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-db15effa53b4', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-1e27dc0bfce2', null, 'Windows 11 Pro лицензия', 'Цифровая лицензия, русский язык', 'Образование', 'WIN-11-PRO', '{"версия":"Pro","тип":"цифровая"}', '00000000-0000-0000-0001-1f4ac2f3a0dd', '00000000-0000-0000-0001-49ca3854fe6b', 55000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-db15effa53b4', 'windows'), ('00000000-0000-0000-0001-db15effa53b4', 'pro'), ('00000000-0000-0000-0001-db15effa53b4', 'лицензия'), ('00000000-0000-0000-0001-db15effa53b4', 'образование');

-- Компания-Товар-18 (ТОО, Спорт и фитнес, Шымкент)
INSERT INTO business (id, created_at, updated_at, name, legal_name, bin, status) VALUES
  ('00000000-0000-0000-0001-4b270bb863a9', now(), now(), 'Компания-Товар-18', 'ТОО "Компания-Товар-18"', '901234567890', 'ACTIVE');
INSERT INTO business_branch (id, created_at, updated_at, business_id, city_id, name, address, latitude, longitude, online_only, status) VALUES
  ('00000000-0000-0000-0001-0e9af0f95246', now(), now(), '00000000-0000-0000-0001-4b270bb863a9', '00000000-0000-0000-0001-1a904d507278', 'Компания-Товар-18 - Филиал 1', 'пр. Тауке хана, 88', 44.8582, 65.5180, true, 'ACTIVE');
INSERT INTO business_branch (id, created_at, updated_at, business_id, city_id, name, address, latitude, longitude, online_only, status) VALUES
  ('00000000-0000-0000-0001-e4233ac7cd1f', now(), now(), '00000000-0000-0000-0001-4b270bb863a9', '00000000-0000-0000-0001-1a904d507278', 'Компания-Товар-18 - Филиал 2', 'пр. Тауке хана, 88', 44.8582, 65.5180, false, 'ACTIVE');
INSERT INTO data_source (id, created_at, updated_at, business_id, source_type, name, status) VALUES
  ('00000000-0000-0000-0001-a41224af48d1', now(), now(), '00000000-0000-0000-0001-4b270bb863a9', 'MANUAL', 'Ручной ввод Компания-Товар-18', 'ACTIVE');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-8637c75949f9', now(), now(), '00000000-0000-0000-0001-4b270bb863a9', null, 'Спорт и фитнес', 'Беговая дорожка CardioFit', 'Электрическая, до 16 км/ч, складная', 'CF-TM-001', '{"тип":"электрическая","скорость":"16 км/ч","складная":"да"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-39efece1836e', now(), now(), '00000000-0000-0000-0001-8637c75949f9', '00000000-0000-0000-0001-0e9af0f95246', '00000000-0000-0000-0001-a41224af48d1', 280000, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-c9a8947e6a44', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-39efece1836e', null, 'Беговая дорожка CardioFit', 'Электрическая, до 16 км/ч, складная', 'Спорт и фитнес', 'CF-TM-001', '{"тип":"электрическая","скорость":"16 км/ч","складная":"да"}', '00000000-0000-0000-0001-4b270bb863a9', '00000000-0000-0000-0001-0e9af0f95246', 280000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-c9a8947e6a44', 'беговая'), ('00000000-0000-0000-0001-c9a8947e6a44', 'дорожка'), ('00000000-0000-0000-0001-c9a8947e6a44', 'cardiofit'), ('00000000-0000-0000-0001-c9a8947e6a44', 'спорт'), ('00000000-0000-0000-0001-c9a8947e6a44', 'фитнес');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-6c7208d37005', now(), now(), '00000000-0000-0000-0001-8637c75949f9', '00000000-0000-0000-0001-e4233ac7cd1f', '00000000-0000-0000-0001-a41224af48d1', 280000, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-fa7a6d5f7e19', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-6c7208d37005', null, 'Беговая дорожка CardioFit', 'Электрическая, до 16 км/ч, складная', 'Спорт и фитнес', 'CF-TM-001', '{"тип":"электрическая","скорость":"16 км/ч","складная":"да"}', '00000000-0000-0000-0001-4b270bb863a9', '00000000-0000-0000-0001-e4233ac7cd1f', 280000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-fa7a6d5f7e19', 'беговая'), ('00000000-0000-0000-0001-fa7a6d5f7e19', 'дорожка'), ('00000000-0000-0000-0001-fa7a6d5f7e19', 'cardiofit'), ('00000000-0000-0000-0001-fa7a6d5f7e19', 'спорт'), ('00000000-0000-0000-0001-fa7a6d5f7e19', 'фитнес');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-91ffe0a137f5', now(), now(), '00000000-0000-0000-0001-4b270bb863a9', '00000000-0000-0000-0001-9f59c39873da', 'Спорт и фитнес', 'Гантели набор 20кг', 'Пара гантелей с блинами, обрезиненные', 'GNT-SET-20', '{"вес":"20кг","тип":"обрезиненные","в_комплекте":"2 шт"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-a40e30afa21f', now(), now(), '00000000-0000-0000-0001-91ffe0a137f5', '00000000-0000-0000-0001-0e9af0f95246', '00000000-0000-0000-0001-a41224af48d1', 35000, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-f6698c1622e6', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-a40e30afa21f', null, 'Гантели набор 20кг', 'Пара гантелей с блинами, обрезиненные', 'Спорт и фитнес', 'GNT-SET-20', '{"вес":"20кг","тип":"обрезиненные","в_комплекте":"2 шт"}', '00000000-0000-0000-0001-4b270bb863a9', '00000000-0000-0000-0001-0e9af0f95246', 35000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-f6698c1622e6', 'гантели'), ('00000000-0000-0000-0001-f6698c1622e6', 'набор'), ('00000000-0000-0000-0001-f6698c1622e6', '20кг'), ('00000000-0000-0000-0001-f6698c1622e6', 'спорт'), ('00000000-0000-0000-0001-f6698c1622e6', 'фитнес');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-888792b69879', now(), now(), '00000000-0000-0000-0001-91ffe0a137f5', '00000000-0000-0000-0001-e4233ac7cd1f', '00000000-0000-0000-0001-a41224af48d1', 35000, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-2bd857733cb0', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-888792b69879', null, 'Гантели набор 20кг', 'Пара гантелей с блинами, обрезиненные', 'Спорт и фитнес', 'GNT-SET-20', '{"вес":"20кг","тип":"обрезиненные","в_комплекте":"2 шт"}', '00000000-0000-0000-0001-4b270bb863a9', '00000000-0000-0000-0001-e4233ac7cd1f', 35000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-2bd857733cb0', 'гантели'), ('00000000-0000-0000-0001-2bd857733cb0', 'набор'), ('00000000-0000-0000-0001-2bd857733cb0', '20кг'), ('00000000-0000-0000-0001-2bd857733cb0', 'спорт'), ('00000000-0000-0000-0001-2bd857733cb0', 'фитнес');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-0f664d67c93f', now(), now(), '00000000-0000-0000-0001-4b270bb863a9', '00000000-0000-0000-0001-9f59c39873da', 'Спорт и фитнес', 'Коврик для йоги 6мм', 'Нескользящий, термопластичная резина', 'MAT-YG-6', '{"толщина":"6мм","материал":"TPE","нескользящий":"да"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-07577d79615a', now(), now(), '00000000-0000-0000-0001-0f664d67c93f', '00000000-0000-0000-0001-0e9af0f95246', '00000000-0000-0000-0001-a41224af48d1', 6500, true, 'ARCHIVED');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-4de91bd82767', now(), now(), '00000000-0000-0000-0001-0f664d67c93f', '00000000-0000-0000-0001-e4233ac7cd1f', '00000000-0000-0000-0001-a41224af48d1', 6500, true, 'ARCHIVED');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-cecc1612f7ab', now(), now(), '00000000-0000-0000-0001-4b270bb863a9', null, 'Спорт и фитнес', 'Фитнес-браслет Xiaomi Band', 'Шагомер, пульсометр, SpO2, водозащита', 'MI-BAND-8', '{"бренд":"Xiaomi","датчики":"пульс,SpO2","водозащита":"5ATM"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-b5b541859c77', now(), now(), '00000000-0000-0000-0001-cecc1612f7ab', '00000000-0000-0000-0001-0e9af0f95246', '00000000-0000-0000-0001-a41224af48d1', 18000, false, 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-9cf97ab460a8', now(), now(), '00000000-0000-0000-0001-cecc1612f7ab', '00000000-0000-0000-0001-e4233ac7cd1f', '00000000-0000-0000-0001-a41224af48d1', 18000, false, 'ACTIVE');

-- Компания-Товар-19 (ИП, IT услуги, Караганда)
INSERT INTO business (id, created_at, updated_at, name, legal_name, bin, status) VALUES
  ('00000000-0000-0000-0001-06bb0de7e528', now(), now(), 'Компания-Товар-19', 'ИП "Компания-Товар-19"', '012345678901', 'ACTIVE');
INSERT INTO business_branch (id, created_at, updated_at, business_id, city_id, name, address, latitude, longitude, online_only, status) VALUES
  ('00000000-0000-0000-0001-bbe34f4f51ff', now(), now(), '00000000-0000-0000-0001-06bb0de7e528', '00000000-0000-0000-0001-02ba3c39b41d', 'Компания-Товар-19 - Офис', 'ул. Гоголя, 25', 44.8592, 65.5190, false, 'ACTIVE');
INSERT INTO data_source (id, created_at, updated_at, business_id, source_type, name, status) VALUES
  ('00000000-0000-0000-0001-f8bfce6a8dde', now(), now(), '00000000-0000-0000-0001-06bb0de7e528', 'MANUAL', 'Ручной ввод Компания-Товар-19', 'ACTIVE');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-4a8237febb83', now(), now(), '00000000-0000-0000-0001-06bb0de7e528', '00000000-0000-0000-0001-a686baabac2c', 'IT услуги', 'Microsoft Office 365', 'Годовая подписка на 1 пользователя', 'MS-O365-1Y', '{"бренд":"Microsoft","пользователей":"1","срок":"1 год"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-431e049f09cf', now(), now(), '00000000-0000-0000-0001-4a8237febb83', '00000000-0000-0000-0001-bbe34f4f51ff', '00000000-0000-0000-0001-f8bfce6a8dde', 35000, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-51363069fa87', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-431e049f09cf', null, 'Microsoft Office 365', 'Годовая подписка на 1 пользователя', 'IT услуги', 'MS-O365-1Y', '{"бренд":"Microsoft","пользователей":"1","срок":"1 год"}', '00000000-0000-0000-0001-06bb0de7e528', '00000000-0000-0000-0001-bbe34f4f51ff', 35000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-51363069fa87', 'microsoft'), ('00000000-0000-0000-0001-51363069fa87', 'office'), ('00000000-0000-0000-0001-51363069fa87', '365'), ('00000000-0000-0000-0001-51363069fa87', 'услуги');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-226b73ecbf34', now(), now(), '00000000-0000-0000-0001-06bb0de7e528', '00000000-0000-0000-0001-a686baabac2c', 'IT услуги', 'Windows 11 Pro лицензия', 'Цифровая лицензия, русский язык', 'WIN-11-PRO', '{"версия":"Pro","тип":"цифровая"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-6403bf902726', now(), now(), '00000000-0000-0000-0001-226b73ecbf34', '00000000-0000-0000-0001-bbe34f4f51ff', '00000000-0000-0000-0001-f8bfce6a8dde', 55000, true, 'ARCHIVED');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-cae9bb416a8d', now(), now(), '00000000-0000-0000-0001-06bb0de7e528', null, 'IT услуги', 'Антивирус Kaspersky 1 год', 'Защита на 3 устройства, лицензия', 'KAS-AV-3D', '{"бренд":"Kaspersky","устройств":"3","срок":"1 год"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-e849fe0b500b', now(), now(), '00000000-0000-0000-0001-cae9bb416a8d', '00000000-0000-0000-0001-bbe34f4f51ff', '00000000-0000-0000-0001-f8bfce6a8dde', 12000, false, 'ACTIVE');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-61b5c9411a7c', now(), now(), '00000000-0000-0000-0001-06bb0de7e528', '00000000-0000-0000-0001-a686baabac2c', 'IT услуги', 'Microsoft Office 365', 'Годовая подписка на 1 пользователя', 'MS-O365-1Y', '{"бренд":"Microsoft","пользователей":"1","срок":"1 год"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-7431fda6a5d1', now(), now(), '00000000-0000-0000-0001-61b5c9411a7c', '00000000-0000-0000-0001-bbe34f4f51ff', '00000000-0000-0000-0001-f8bfce6a8dde', 35000, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-b8bc5b1e5d3a', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-7431fda6a5d1', null, 'Microsoft Office 365', 'Годовая подписка на 1 пользователя', 'IT услуги', 'MS-O365-1Y', '{"бренд":"Microsoft","пользователей":"1","срок":"1 год"}', '00000000-0000-0000-0001-06bb0de7e528', '00000000-0000-0000-0001-bbe34f4f51ff', 35000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-b8bc5b1e5d3a', 'microsoft'), ('00000000-0000-0000-0001-b8bc5b1e5d3a', 'office'), ('00000000-0000-0000-0001-b8bc5b1e5d3a', '365'), ('00000000-0000-0000-0001-b8bc5b1e5d3a', 'услуги');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-86900b22a391', now(), now(), '00000000-0000-0000-0001-06bb0de7e528', '00000000-0000-0000-0001-a686baabac2c', 'IT услуги', 'Windows 11 Pro лицензия', 'Цифровая лицензия, русский язык', 'WIN-11-PRO', '{"версия":"Pro","тип":"цифровая"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-4809461066f6', now(), now(), '00000000-0000-0000-0001-86900b22a391', '00000000-0000-0000-0001-bbe34f4f51ff', '00000000-0000-0000-0001-f8bfce6a8dde', 55000, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-8a6d8c3bb682', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-4809461066f6', null, 'Windows 11 Pro лицензия', 'Цифровая лицензия, русский язык', 'IT услуги', 'WIN-11-PRO', '{"версия":"Pro","тип":"цифровая"}', '00000000-0000-0000-0001-06bb0de7e528', '00000000-0000-0000-0001-bbe34f4f51ff', 55000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-8a6d8c3bb682', 'windows'), ('00000000-0000-0000-0001-8a6d8c3bb682', 'pro'), ('00000000-0000-0000-0001-8a6d8c3bb682', 'лицензия'), ('00000000-0000-0000-0001-8a6d8c3bb682', 'услуги');

-- Компания-Товар-20 (ТОО, Автозапчасти, Кызылорда)
INSERT INTO business (id, created_at, updated_at, name, legal_name, bin, status) VALUES
  ('00000000-0000-0000-0001-64e7859ba16e', now(), now(), 'Компания-Товар-20', 'ТОО "Компания-Товар-20"', '123456789012', 'ACTIVE');
INSERT INTO business_branch (id, created_at, updated_at, business_id, city_id, name, address, latitude, longitude, online_only, status) VALUES
  ('00000000-0000-0000-0001-ea7f4ffdd0cc', now(), now(), '00000000-0000-0000-0001-64e7859ba16e', '00000000-0000-0000-0000-0000000000c1', 'Компания-Товар-20 - Офис', 'ул. Толе би, 22', 44.8602, 65.5200, false, 'ACTIVE');
INSERT INTO data_source (id, created_at, updated_at, business_id, source_type, name, status) VALUES
  ('00000000-0000-0000-0001-1b82d75a1c8d', now(), now(), '00000000-0000-0000-0001-64e7859ba16e', 'MANUAL', 'Ручной ввод Компания-Товар-20', 'ACTIVE');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-08fd2b46dbe0', now(), now(), '00000000-0000-0000-0001-64e7859ba16e', '00000000-0000-0000-0000-0000000000a1', 'Автозапчасти', 'Свечи зажигания NGK', 'Иридиевые свечи, комплект 4 шт', 'NGK-IR-4', '{"бренд":"NGK","тип":"иридиевые","комплект":"4 шт"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-e7c515870abe', now(), now(), '00000000-0000-0000-0001-08fd2b46dbe0', '00000000-0000-0000-0001-ea7f4ffdd0cc', '00000000-0000-0000-0001-1b82d75a1c8d', 12000, true, 'ARCHIVED');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-7b2d59312167', now(), now(), '00000000-0000-0000-0001-64e7859ba16e', null, 'Автозапчасти', 'Амортизатор Kayaba', 'Газовый амортизатор задний', 'KYB-GS-001', '{"бренд":"Kayaba","тип":"газовый","ось":"зад"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-a87426349cca', now(), now(), '00000000-0000-0000-0001-7b2d59312167', '00000000-0000-0000-0001-ea7f4ffdd0cc', '00000000-0000-0000-0001-1b82d75a1c8d', 28000, false, 'ACTIVE');

-- Компания-Товар-21 (ИП, Бытовая техника, Алматы)
INSERT INTO business (id, created_at, updated_at, name, legal_name, bin, status) VALUES
  ('00000000-0000-0000-0001-3fd944392a90', now(), now(), 'Компания-Товар-21', 'ИП "Компания-Товар-21"', '234567890123', 'ACTIVE');
INSERT INTO business_branch (id, created_at, updated_at, business_id, city_id, name, address, latitude, longitude, online_only, status) VALUES
  ('00000000-0000-0000-0001-c82fd45f547a', now(), now(), '00000000-0000-0000-0001-3fd944392a90', '00000000-0000-0000-0000-0000000000c2', 'Компания-Товар-21 - Офис', 'ул. Жандосова, 55', 44.8612, 65.5210, false, 'ACTIVE');
INSERT INTO data_source (id, created_at, updated_at, business_id, source_type, name, status) VALUES
  ('00000000-0000-0000-0001-e49b8a0df2f3', now(), now(), '00000000-0000-0000-0001-3fd944392a90', 'MANUAL', 'Ручной ввод Компания-Товар-21', 'ACTIVE');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-6c1964994595', now(), now(), '00000000-0000-0000-0001-3fd944392a90', null, 'Бытовая техника', 'Холодильник LG 350L', 'Двухкамерный холодильник с No Frost', 'LG-RF-350', '{"бренд":"LG","объем":"350л","тип":"No Frost"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-38a43a1f2aa1', now(), now(), '00000000-0000-0000-0001-6c1964994595', '00000000-0000-0000-0001-c82fd45f547a', '00000000-0000-0000-0001-e49b8a0df2f3', 189000, false, 'ACTIVE');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-ef9c452006c0', now(), now(), '00000000-0000-0000-0001-3fd944392a90', '00000000-0000-0000-0000-0000000000a2', 'Бытовая техника', 'Стиральная машина Bosch', 'Фронтальная загрузка 7кг, 1200 об/мин', 'BOS-WM-7', '{"бренд":"Bosch","загрузка":"7кг","обороты":"1200"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-6a20ffc1f9bb', now(), now(), '00000000-0000-0000-0001-ef9c452006c0', '00000000-0000-0000-0001-c82fd45f547a', '00000000-0000-0000-0001-e49b8a0df2f3', 245000, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-71fea97a183c', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-6a20ffc1f9bb', null, 'Стиральная машина Bosch', 'Фронтальная загрузка 7кг, 1200 об/мин', 'Бытовая техника', 'BOS-WM-7', '{"бренд":"Bosch","загрузка":"7кг","обороты":"1200"}', '00000000-0000-0000-0001-3fd944392a90', '00000000-0000-0000-0001-c82fd45f547a', 245000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-71fea97a183c', 'стиральная'), ('00000000-0000-0000-0001-71fea97a183c', 'машина'), ('00000000-0000-0000-0001-71fea97a183c', 'bosch'), ('00000000-0000-0000-0001-71fea97a183c', 'бытовая'), ('00000000-0000-0000-0001-71fea97a183c', 'техника');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-ced34c62fb8b', now(), now(), '00000000-0000-0000-0001-3fd944392a90', '00000000-0000-0000-0000-0000000000a2', 'Бытовая техника', 'Микроволновая печь Samsung', 'СВЧ 23л, гриль, сенсорное управление', 'SAM-MW-23', '{"бренд":"Samsung","объем":"23л","гриль":"да"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-5cf7b9f2e0d2', now(), now(), '00000000-0000-0000-0001-ced34c62fb8b', '00000000-0000-0000-0001-c82fd45f547a', '00000000-0000-0000-0001-e49b8a0df2f3', 45000, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-3e0e1cf79566', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-5cf7b9f2e0d2', null, 'Микроволновая печь Samsung', 'СВЧ 23л, гриль, сенсорное управление', 'Бытовая техника', 'SAM-MW-23', '{"бренд":"Samsung","объем":"23л","гриль":"да"}', '00000000-0000-0000-0001-3fd944392a90', '00000000-0000-0000-0001-c82fd45f547a', 45000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-3e0e1cf79566', 'микроволновая'), ('00000000-0000-0000-0001-3e0e1cf79566', 'печь'), ('00000000-0000-0000-0001-3e0e1cf79566', 'samsung'), ('00000000-0000-0000-0001-3e0e1cf79566', 'бытовая'), ('00000000-0000-0000-0001-3e0e1cf79566', 'техника');

-- Компания-Товар-22 (ТОО, Услуги красоты, Астана)
INSERT INTO business (id, created_at, updated_at, name, legal_name, bin, status) VALUES
  ('00000000-0000-0000-0001-6c570587f520', now(), now(), 'Компания-Товар-22', 'ТОО "Компания-Товар-22"', '345678901234', 'ACTIVE');
INSERT INTO business_branch (id, created_at, updated_at, business_id, city_id, name, address, latitude, longitude, online_only, status) VALUES
  ('00000000-0000-0000-0001-d547858bf633', now(), now(), '00000000-0000-0000-0001-6c570587f520', '00000000-0000-0000-0000-0000000000c3', 'Компания-Товар-22 - Офис', 'пр. Республики, 60', 44.8622, 65.5220, false, 'ACTIVE');
INSERT INTO data_source (id, created_at, updated_at, business_id, source_type, name, status) VALUES
  ('00000000-0000-0000-0001-ff7f1b6cd603', now(), now(), '00000000-0000-0000-0001-6c570587f520', 'MANUAL', 'Ручной ввод Компания-Товар-22', 'ACTIVE');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-4cf63521ed31', now(), now(), '00000000-0000-0000-0001-6c570587f520', '00000000-0000-0000-0000-0000000000a3', 'Услуги красоты', 'Гипсокартон Knauf 12.5мм', 'Влагостойкий лист 2500x1200 мм', 'KNF-GKL-12', '{"бренд":"Knauf","толщина":"12.5мм","тип":"влагостойкий"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-52071f4e46f8', now(), now(), '00000000-0000-0000-0001-4cf63521ed31', '00000000-0000-0000-0001-d547858bf633', '00000000-0000-0000-0001-ff7f1b6cd603', 4500, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-500a8f3315e0', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-52071f4e46f8', null, 'Гипсокартон Knauf 12.5мм', 'Влагостойкий лист 2500x1200 мм', 'Услуги красоты', 'KNF-GKL-12', '{"бренд":"Knauf","толщина":"12.5мм","тип":"влагостойкий"}', '00000000-0000-0000-0001-6c570587f520', '00000000-0000-0000-0001-d547858bf633', 4500, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-500a8f3315e0', 'гипсокартон'), ('00000000-0000-0000-0001-500a8f3315e0', 'knauf'), ('00000000-0000-0000-0001-500a8f3315e0', '12.5мм'), ('00000000-0000-0000-0001-500a8f3315e0', 'услуги'), ('00000000-0000-0000-0001-500a8f3315e0', 'красоты');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-9803a89a15fb', now(), now(), '00000000-0000-0000-0001-6c570587f520', '00000000-0000-0000-0000-0000000000a3', 'Услуги красоты', 'Профнастил С8 0.45мм', 'Оцинкованный профнастил для забора', 'PRF-C8-045', '{"марка":"С8","толщина":"0.45мм","тип":"оцинкованный"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-427e7bfc2cce', now(), now(), '00000000-0000-0000-0001-9803a89a15fb', '00000000-0000-0000-0001-d547858bf633', '00000000-0000-0000-0001-ff7f1b6cd603', 2800, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-6b827d79e1a7', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-427e7bfc2cce', null, 'Профнастил С8 0.45мм', 'Оцинкованный профнастил для забора', 'Услуги красоты', 'PRF-C8-045', '{"марка":"С8","толщина":"0.45мм","тип":"оцинкованный"}', '00000000-0000-0000-0001-6c570587f520', '00000000-0000-0000-0001-d547858bf633', 2800, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-6b827d79e1a7', 'профнастил'), ('00000000-0000-0000-0001-6b827d79e1a7', '0.45мм'), ('00000000-0000-0000-0001-6b827d79e1a7', 'услуги'), ('00000000-0000-0000-0001-6b827d79e1a7', 'красоты');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-2407c8a06c7b', now(), now(), '00000000-0000-0000-0001-6c570587f520', null, 'Услуги красоты', 'Металлочерепица Монтеррей', 'Полиэстер 0.5мм, коричневый', 'MCH-MT-05', '{"тип":"Монтеррей","толщина":"0.5мм","покрытие":"полиэстер"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-424cd612bc80', now(), now(), '00000000-0000-0000-0001-2407c8a06c7b', '00000000-0000-0000-0001-d547858bf633', '00000000-0000-0000-0001-ff7f1b6cd603', 5200, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-083877ab5746', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-424cd612bc80', null, 'Металлочерепица Монтеррей', 'Полиэстер 0.5мм, коричневый', 'Услуги красоты', 'MCH-MT-05', '{"тип":"Монтеррей","толщина":"0.5мм","покрытие":"полиэстер"}', '00000000-0000-0000-0001-6c570587f520', '00000000-0000-0000-0001-d547858bf633', 5200, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-083877ab5746', 'металлочерепица'), ('00000000-0000-0000-0001-083877ab5746', 'монтеррей'), ('00000000-0000-0000-0001-083877ab5746', 'услуги'), ('00000000-0000-0000-0001-083877ab5746', 'красоты');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-5df080e78c16', now(), now(), '00000000-0000-0000-0001-6c570587f520', '00000000-0000-0000-0000-0000000000a3', 'Услуги красоты', 'Клей плиточный Ceresit CM11', 'Для керамической плитки, 25 кг', 'CER-CM11-25', '{"бренд":"Ceresit","вес":"25кг","назначение":"плитка"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-4f5df9811cdd', now(), now(), '00000000-0000-0000-0001-5df080e78c16', '00000000-0000-0000-0001-d547858bf633', '00000000-0000-0000-0001-ff7f1b6cd603', 3200, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-3eece70b685c', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-4f5df9811cdd', null, 'Клей плиточный Ceresit CM11', 'Для керамической плитки, 25 кг', 'Услуги красоты', 'CER-CM11-25', '{"бренд":"Ceresit","вес":"25кг","назначение":"плитка"}', '00000000-0000-0000-0001-6c570587f520', '00000000-0000-0000-0001-d547858bf633', 3200, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-3eece70b685c', 'клей'), ('00000000-0000-0000-0001-3eece70b685c', 'плиточный'), ('00000000-0000-0000-0001-3eece70b685c', 'ceresit'), ('00000000-0000-0000-0001-3eece70b685c', 'cm11'), ('00000000-0000-0000-0001-3eece70b685c', 'услуги'), ('00000000-0000-0000-0001-3eece70b685c', 'красоты');

-- Компания-Товар-23 (ИП, Ремонт и сервис, Шымкент)
INSERT INTO business (id, created_at, updated_at, name, legal_name, bin, status) VALUES
  ('00000000-0000-0000-0001-1cb5335a3de8', now(), now(), 'Компания-Товар-23', 'ИП "Компания-Товар-23"', '456789012345', 'ACTIVE');
INSERT INTO business_branch (id, created_at, updated_at, business_id, city_id, name, address, latitude, longitude, online_only, status) VALUES
  ('00000000-0000-0000-0001-d6bf414ca306', now(), now(), '00000000-0000-0000-0001-1cb5335a3de8', '00000000-0000-0000-0001-1a904d507278', 'Компания-Товар-23 - Офис', 'ул. Кулышова, 99', 44.8632, 65.5230, false, 'ACTIVE');
INSERT INTO data_source (id, created_at, updated_at, business_id, source_type, name, status) VALUES
  ('00000000-0000-0000-0001-0d93a9498b5d', now(), now(), '00000000-0000-0000-0001-1cb5335a3de8', 'MANUAL', 'Ручной ввод Компания-Товар-23', 'ACTIVE');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-eb7dedb636fb', now(), now(), '00000000-0000-0000-0001-1cb5335a3de8', '00000000-0000-0000-0000-0000000000a4', 'Ремонт и сервис', 'Болгарка УШМ 125мм', 'Угловая шлифмашина, 1100Вт', 'USH-125-1100', '{"диаметр":"125мм","мощность":"1100Вт"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-22b28ded7d8a', now(), now(), '00000000-0000-0000-0001-eb7dedb636fb', '00000000-0000-0000-0001-d6bf414ca306', '00000000-0000-0000-0001-0d93a9498b5d', 15000, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-be69fa0552a9', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-22b28ded7d8a', null, 'Болгарка УШМ 125мм', 'Угловая шлифмашина, 1100Вт', 'Ремонт и сервис', 'USH-125-1100', '{"диаметр":"125мм","мощность":"1100Вт"}', '00000000-0000-0000-0001-1cb5335a3de8', '00000000-0000-0000-0001-d6bf414ca306', 15000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-be69fa0552a9', 'болгарка'), ('00000000-0000-0000-0001-be69fa0552a9', 'ушм'), ('00000000-0000-0000-0001-be69fa0552a9', '125мм'), ('00000000-0000-0000-0001-be69fa0552a9', 'ремонт'), ('00000000-0000-0000-0001-be69fa0552a9', 'сервис');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-da7c81407008', now(), now(), '00000000-0000-0000-0001-1cb5335a3de8', null, 'Ремонт и сервис', 'Дрель ударная Bosch 750Вт', 'Дрель ударная с реверсом, кейс', 'BOS-DR-750', '{"бренд":"Bosch","мощность":"750Вт","тип":"ударная"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-0ea69a90c87a', now(), now(), '00000000-0000-0000-0001-da7c81407008', '00000000-0000-0000-0001-d6bf414ca306', '00000000-0000-0000-0001-0d93a9498b5d', 28000, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-44270fcd54c1', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-0ea69a90c87a', null, 'Дрель ударная Bosch 750Вт', 'Дрель ударная с реверсом, кейс', 'Ремонт и сервис', 'BOS-DR-750', '{"бренд":"Bosch","мощность":"750Вт","тип":"ударная"}', '00000000-0000-0000-0001-1cb5335a3de8', '00000000-0000-0000-0001-d6bf414ca306', 28000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-44270fcd54c1', 'дрель'), ('00000000-0000-0000-0001-44270fcd54c1', 'ударная'), ('00000000-0000-0000-0001-44270fcd54c1', 'bosch'), ('00000000-0000-0000-0001-44270fcd54c1', '750вт'), ('00000000-0000-0000-0001-44270fcd54c1', 'ремонт'), ('00000000-0000-0000-0001-44270fcd54c1', 'сервис');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-899235d24e9b', now(), now(), '00000000-0000-0000-0001-1cb5335a3de8', '00000000-0000-0000-0000-0000000000a4', 'Ремонт и сервис', 'Шуруповерт Makita 18В', 'Аккумуляторный, 2 батареи, кейс', 'MAK-SD-18', '{"бренд":"Makita","напряжение":"18В","акб":"2 шт"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-0006c5f9f55f', now(), now(), '00000000-0000-0000-0001-899235d24e9b', '00000000-0000-0000-0001-d6bf414ca306', '00000000-0000-0000-0001-0d93a9498b5d', 45000, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-3a798df5735f', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-0006c5f9f55f', null, 'Шуруповерт Makita 18В', 'Аккумуляторный, 2 батареи, кейс', 'Ремонт и сервис', 'MAK-SD-18', '{"бренд":"Makita","напряжение":"18В","акб":"2 шт"}', '00000000-0000-0000-0001-1cb5335a3de8', '00000000-0000-0000-0001-d6bf414ca306', 45000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-3a798df5735f', 'шуруповерт'), ('00000000-0000-0000-0001-3a798df5735f', 'makita'), ('00000000-0000-0000-0001-3a798df5735f', '18в'), ('00000000-0000-0000-0001-3a798df5735f', 'ремонт'), ('00000000-0000-0000-0001-3a798df5735f', 'сервис');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-8bc86261a2d6', now(), now(), '00000000-0000-0000-0001-1cb5335a3de8', '00000000-0000-0000-0000-0000000000a4', 'Ремонт и сервис', 'Болгарка УШМ 125мм', 'Угловая шлифмашина, 1100Вт', 'USH-125-1100', '{"диаметр":"125мм","мощность":"1100Вт"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-b7180549dff6', now(), now(), '00000000-0000-0000-0001-8bc86261a2d6', '00000000-0000-0000-0001-d6bf414ca306', '00000000-0000-0000-0001-0d93a9498b5d', 15000, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-e37eb759bb35', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-b7180549dff6', null, 'Болгарка УШМ 125мм', 'Угловая шлифмашина, 1100Вт', 'Ремонт и сервис', 'USH-125-1100', '{"диаметр":"125мм","мощность":"1100Вт"}', '00000000-0000-0000-0001-1cb5335a3de8', '00000000-0000-0000-0001-d6bf414ca306', 15000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-e37eb759bb35', 'болгарка'), ('00000000-0000-0000-0001-e37eb759bb35', 'ушм'), ('00000000-0000-0000-0001-e37eb759bb35', '125мм'), ('00000000-0000-0000-0001-e37eb759bb35', 'ремонт'), ('00000000-0000-0000-0001-e37eb759bb35', 'сервис');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-c221348b9928', now(), now(), '00000000-0000-0000-0001-1cb5335a3de8', null, 'Ремонт и сервис', 'Дрель ударная Bosch 750Вт', 'Дрель ударная с реверсом, кейс', 'BOS-DR-750', '{"бренд":"Bosch","мощность":"750Вт","тип":"ударная"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-97f39ab5e4fd', now(), now(), '00000000-0000-0000-0001-c221348b9928', '00000000-0000-0000-0001-d6bf414ca306', '00000000-0000-0000-0001-0d93a9498b5d', 28000, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-af7083e9c586', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-97f39ab5e4fd', null, 'Дрель ударная Bosch 750Вт', 'Дрель ударная с реверсом, кейс', 'Ремонт и сервис', 'BOS-DR-750', '{"бренд":"Bosch","мощность":"750Вт","тип":"ударная"}', '00000000-0000-0000-0001-1cb5335a3de8', '00000000-0000-0000-0001-d6bf414ca306', 28000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-af7083e9c586', 'дрель'), ('00000000-0000-0000-0001-af7083e9c586', 'ударная'), ('00000000-0000-0000-0001-af7083e9c586', 'bosch'), ('00000000-0000-0000-0001-af7083e9c586', '750вт'), ('00000000-0000-0000-0001-af7083e9c586', 'ремонт'), ('00000000-0000-0000-0001-af7083e9c586', 'сервис');

-- Компания-Товар-24 (ТОО, Строительство, Караганда)
INSERT INTO business (id, created_at, updated_at, name, legal_name, bin, status) VALUES
  ('00000000-0000-0000-0001-54a0424bf1c5', now(), now(), 'Компания-Товар-24', 'ТОО "Компания-Товар-24"', '567890123456', 'ACTIVE');
INSERT INTO business_branch (id, created_at, updated_at, business_id, city_id, name, address, latitude, longitude, online_only, status) VALUES
  ('00000000-0000-0000-0001-41a2091dedbb', now(), now(), '00000000-0000-0000-0001-54a0424bf1c5', '00000000-0000-0000-0001-02ba3c39b41d', 'Компания-Товар-24 - Филиал 1', 'пр. Бухар жырау, 52', 44.8642, 65.5240, false, 'ACTIVE');
INSERT INTO business_branch (id, created_at, updated_at, business_id, city_id, name, address, latitude, longitude, online_only, status) VALUES
  ('00000000-0000-0000-0001-9f751bc2faec', now(), now(), '00000000-0000-0000-0001-54a0424bf1c5', '00000000-0000-0000-0001-02ba3c39b41d', 'Компания-Товар-24 - Филиал 2', 'пр. Бухар жырау, 52', 44.8642, 65.5240, false, 'ACTIVE');
INSERT INTO data_source (id, created_at, updated_at, business_id, source_type, name, status) VALUES
  ('00000000-0000-0000-0001-005e85becae5', now(), now(), '00000000-0000-0000-0001-54a0424bf1c5', 'MANUAL', 'Ручной ввод Компания-Товар-24', 'ACTIVE');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-1d1e829ea030', now(), now(), '00000000-0000-0000-0001-54a0424bf1c5', null, 'Строительство', 'Гипсокартон Knauf 12.5мм', 'Влагостойкий лист 2500x1200 мм', 'KNF-GKL-12', '{"бренд":"Knauf","толщина":"12.5мм","тип":"влагостойкий"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-049531bc6d84', now(), now(), '00000000-0000-0000-0001-1d1e829ea030', '00000000-0000-0000-0001-41a2091dedbb', '00000000-0000-0000-0001-005e85becae5', 4500, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-e3ef09e0c2f4', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-049531bc6d84', null, 'Гипсокартон Knauf 12.5мм', 'Влагостойкий лист 2500x1200 мм', 'Строительство', 'KNF-GKL-12', '{"бренд":"Knauf","толщина":"12.5мм","тип":"влагостойкий"}', '00000000-0000-0000-0001-54a0424bf1c5', '00000000-0000-0000-0001-41a2091dedbb', 4500, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-e3ef09e0c2f4', 'гипсокартон'), ('00000000-0000-0000-0001-e3ef09e0c2f4', 'knauf'), ('00000000-0000-0000-0001-e3ef09e0c2f4', '12.5мм'), ('00000000-0000-0000-0001-e3ef09e0c2f4', 'строительство');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-84273449ef2e', now(), now(), '00000000-0000-0000-0001-1d1e829ea030', '00000000-0000-0000-0001-9f751bc2faec', '00000000-0000-0000-0001-005e85becae5', 4500, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-3308f5ed4d48', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-84273449ef2e', null, 'Гипсокартон Knauf 12.5мм', 'Влагостойкий лист 2500x1200 мм', 'Строительство', 'KNF-GKL-12', '{"бренд":"Knauf","толщина":"12.5мм","тип":"влагостойкий"}', '00000000-0000-0000-0001-54a0424bf1c5', '00000000-0000-0000-0001-9f751bc2faec', 4500, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-3308f5ed4d48', 'гипсокартон'), ('00000000-0000-0000-0001-3308f5ed4d48', 'knauf'), ('00000000-0000-0000-0001-3308f5ed4d48', '12.5мм'), ('00000000-0000-0000-0001-3308f5ed4d48', 'строительство');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-2272efbec676', now(), now(), '00000000-0000-0000-0001-54a0424bf1c5', '00000000-0000-0000-0000-0000000000a5', 'Строительство', 'Профнастил С8 0.45мм', 'Оцинкованный профнастил для забора', 'PRF-C8-045', '{"марка":"С8","толщина":"0.45мм","тип":"оцинкованный"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-3b7c218f27fe', now(), now(), '00000000-0000-0000-0001-2272efbec676', '00000000-0000-0000-0001-41a2091dedbb', '00000000-0000-0000-0001-005e85becae5', 2800, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-20fcb9a4781b', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-3b7c218f27fe', null, 'Профнастил С8 0.45мм', 'Оцинкованный профнастил для забора', 'Строительство', 'PRF-C8-045', '{"марка":"С8","толщина":"0.45мм","тип":"оцинкованный"}', '00000000-0000-0000-0001-54a0424bf1c5', '00000000-0000-0000-0001-41a2091dedbb', 2800, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-20fcb9a4781b', 'профнастил'), ('00000000-0000-0000-0001-20fcb9a4781b', '0.45мм'), ('00000000-0000-0000-0001-20fcb9a4781b', 'строительство');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-a1c032ba3beb', now(), now(), '00000000-0000-0000-0001-2272efbec676', '00000000-0000-0000-0001-9f751bc2faec', '00000000-0000-0000-0001-005e85becae5', 2800, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-e00fb8bea759', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-a1c032ba3beb', null, 'Профнастил С8 0.45мм', 'Оцинкованный профнастил для забора', 'Строительство', 'PRF-C8-045', '{"марка":"С8","толщина":"0.45мм","тип":"оцинкованный"}', '00000000-0000-0000-0001-54a0424bf1c5', '00000000-0000-0000-0001-9f751bc2faec', 2800, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-e00fb8bea759', 'профнастил'), ('00000000-0000-0000-0001-e00fb8bea759', '0.45мм'), ('00000000-0000-0000-0001-e00fb8bea759', 'строительство');

-- Компания-Товар-25 (ИП, Продукты питания, Кызылорда)
INSERT INTO business (id, created_at, updated_at, name, legal_name, bin, status) VALUES
  ('00000000-0000-0000-0001-8ca1f12cffe0', now(), now(), 'Компания-Товар-25', 'ИП "Компания-Товар-25"', '678901234567', 'ACTIVE');
INSERT INTO business_branch (id, created_at, updated_at, business_id, city_id, name, address, latitude, longitude, online_only, status) VALUES
  ('00000000-0000-0000-0001-5405cfa7c511', now(), now(), '00000000-0000-0000-0001-8ca1f12cffe0', '00000000-0000-0000-0000-0000000000c1', 'Компания-Товар-25 - Офис', 'ул. Байконур, 8', 44.8652, 65.5250, false, 'ACTIVE');
INSERT INTO data_source (id, created_at, updated_at, business_id, source_type, name, status) VALUES
  ('00000000-0000-0000-0001-2cdbd8317a78', now(), now(), '00000000-0000-0000-0001-8ca1f12cffe0', 'MANUAL', 'Ручной ввод Компания-Товар-25', 'ACTIVE');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-db7fb79f8570', now(), now(), '00000000-0000-0000-0001-8ca1f12cffe0', '00000000-0000-0000-0001-279580d8f28e', 'Продукты питания', 'Сахар-песок 1кг', 'Белый свекловичный сахар', 'SAH-BEL-1', '{"тип":"свекловичный","вес":"1кг"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-a925e40b9613', now(), now(), '00000000-0000-0000-0001-db7fb79f8570', '00000000-0000-0000-0001-5405cfa7c511', '00000000-0000-0000-0001-2cdbd8317a78', 550, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-e0258a696538', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-a925e40b9613', null, 'Сахар-песок 1кг', 'Белый свекловичный сахар', 'Продукты питания', 'SAH-BEL-1', '{"тип":"свекловичный","вес":"1кг"}', '00000000-0000-0000-0001-8ca1f12cffe0', '00000000-0000-0000-0001-5405cfa7c511', 550, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-e0258a696538', 'сахар'), ('00000000-0000-0000-0001-e0258a696538', 'песок'), ('00000000-0000-0000-0001-e0258a696538', '1кг'), ('00000000-0000-0000-0001-e0258a696538', 'продукты'), ('00000000-0000-0000-0001-e0258a696538', 'питания');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-5613af4f9641', now(), now(), '00000000-0000-0000-0001-8ca1f12cffe0', '00000000-0000-0000-0001-279580d8f28e', 'Продукты питания', 'Макароны спираль 400г', 'Из твердых сортов пшеницы', 'MAK-SP-400', '{"тип":"спираль","вес":"400г","сорт":"твердый"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-01cb4c471884', now(), now(), '00000000-0000-0000-0001-5613af4f9641', '00000000-0000-0000-0001-5405cfa7c511', '00000000-0000-0000-0001-2cdbd8317a78', 380, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-e4099f3283d0', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-01cb4c471884', null, 'Макароны спираль 400г', 'Из твердых сортов пшеницы', 'Продукты питания', 'MAK-SP-400', '{"тип":"спираль","вес":"400г","сорт":"твердый"}', '00000000-0000-0000-0001-8ca1f12cffe0', '00000000-0000-0000-0001-5405cfa7c511', 380, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-e4099f3283d0', 'макароны'), ('00000000-0000-0000-0001-e4099f3283d0', 'спираль'), ('00000000-0000-0000-0001-e4099f3283d0', '400г'), ('00000000-0000-0000-0001-e4099f3283d0', 'продукты'), ('00000000-0000-0000-0001-e4099f3283d0', 'питания');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-4d5539aaa883', now(), now(), '00000000-0000-0000-0001-8ca1f12cffe0', null, 'Продукты питания', 'Мука пшеничная высший сорт', 'Казахстанская мука, 50 кг мешок', 'MUK-VS-50', '{"сорт":"высший","вес":"50кг","страна":"Казахстан"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-00d23e288619', now(), now(), '00000000-0000-0000-0001-4d5539aaa883', '00000000-0000-0000-0001-5405cfa7c511', '00000000-0000-0000-0001-2cdbd8317a78', 12000, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-8dd186d8fe8e', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-00d23e288619', null, 'Мука пшеничная высший сорт', 'Казахстанская мука, 50 кг мешок', 'Продукты питания', 'MUK-VS-50', '{"сорт":"высший","вес":"50кг","страна":"Казахстан"}', '00000000-0000-0000-0001-8ca1f12cffe0', '00000000-0000-0000-0001-5405cfa7c511', 12000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-8dd186d8fe8e', 'мука'), ('00000000-0000-0000-0001-8dd186d8fe8e', 'пшеничная'), ('00000000-0000-0000-0001-8dd186d8fe8e', 'высший'), ('00000000-0000-0000-0001-8dd186d8fe8e', 'сорт'), ('00000000-0000-0000-0001-8dd186d8fe8e', 'продукты'), ('00000000-0000-0000-0001-8dd186d8fe8e', 'питания');

-- ==============================
-- SERVICE-ONLY COMPANIES (25)
-- ==============================

-- Компания-Услуга-1 (ИП, Медицинские услуги, Шымкент)
INSERT INTO business (id, created_at, updated_at, name, legal_name, bin, status) VALUES
  ('00000000-0000-0000-0001-6ac58ecadc22', now(), now(), 'Компания-Услуга-1', 'ИП "Компания-Услуга-1"', '234567890123', 'ACTIVE');
INSERT INTO business_branch (id, created_at, updated_at, business_id, city_id, name, address, latitude, longitude, online_only, status) VALUES
  ('00000000-0000-0000-0001-5dd698a511d4', now(), now(), '00000000-0000-0000-0001-6ac58ecadc22', '00000000-0000-0000-0001-1a904d507278', 'Компания-Услуга-1 - Офис', 'ул. Казыбек би, 35', 44.8412, 65.5010, false, 'ACTIVE');
INSERT INTO data_source (id, created_at, updated_at, business_id, source_type, name, status) VALUES
  ('00000000-0000-0000-0001-1021ac7581d2', now(), now(), '00000000-0000-0000-0001-6ac58ecadc22', 'MANUAL', 'Ручной ввод Компания-Услуга-1', 'ACTIVE');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-9f42dc717913', now(), now(), '00000000-0000-0000-0001-6ac58ecadc22', '00000000-0000-0000-0001-81548349b593', 'Консультация терапевта', 'Первичный прием, осмотр, назначение', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-eb287f37c29e', now(), now(), '00000000-0000-0000-0001-9f42dc717913', '00000000-0000-0000-0001-5dd698a511d4', 'ONLINE', 5000, 30, 'Пн-Пт 08:00-17:00', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-d970de797ae2', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-eb287f37c29e', 'Консультация терапевта', 'Первичный прием, осмотр, назначение', 'Медицинские услуги', null, null, '00000000-0000-0000-0001-6ac58ecadc22', '00000000-0000-0000-0001-5dd698a511d4', 5000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-d970de797ae2', 'консультация'), ('00000000-0000-0000-0001-d970de797ae2', 'терапевта'), ('00000000-0000-0000-0001-d970de797ae2', 'медицинские'), ('00000000-0000-0000-0001-d970de797ae2', 'услуги');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-540490f7abec', now(), now(), '00000000-0000-0000-0001-6ac58ecadc22', '00000000-0000-0000-0001-81548349b593', 'УЗИ брюшной полости', 'Комплексное УЗИ органов брюшной полости', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-947ca39b5985', now(), now(), '00000000-0000-0000-0001-540490f7abec', '00000000-0000-0000-0001-5dd698a511d4', 'ONLINE', 8000, 40, 'Пн-Сб 08:00-14:00', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-d607e96f98d6', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-947ca39b5985', 'УЗИ брюшной полости', 'Комплексное УЗИ органов брюшной полости', 'Медицинские услуги', null, null, '00000000-0000-0000-0001-6ac58ecadc22', '00000000-0000-0000-0001-5dd698a511d4', 8000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-d607e96f98d6', 'узи'), ('00000000-0000-0000-0001-d607e96f98d6', 'брюшной'), ('00000000-0000-0000-0001-d607e96f98d6', 'полости'), ('00000000-0000-0000-0001-d607e96f98d6', 'медицинские'), ('00000000-0000-0000-0001-d607e96f98d6', 'услуги');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-ea302b2e987d', now(), now(), '00000000-0000-0000-0001-6ac58ecadc22', '00000000-0000-0000-0001-81548349b593', 'Анализ крови общий', 'Забор крови + общий анализ, результат за 1 день', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-294f77a086a2', now(), now(), '00000000-0000-0000-0001-ea302b2e987d', '00000000-0000-0000-0001-5dd698a511d4', 'ON_SITE', 3500, 15, 'Пн-Пт 07:30-10:00', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-f0418ed212cf', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-294f77a086a2', 'Анализ крови общий', 'Забор крови + общий анализ, результат за 1 день', 'Медицинские услуги', null, null, '00000000-0000-0000-0001-6ac58ecadc22', '00000000-0000-0000-0001-5dd698a511d4', 3500, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-f0418ed212cf', 'анализ'), ('00000000-0000-0000-0001-f0418ed212cf', 'крови'), ('00000000-0000-0000-0001-f0418ed212cf', 'общий'), ('00000000-0000-0000-0001-f0418ed212cf', 'медицинские'), ('00000000-0000-0000-0001-f0418ed212cf', 'услуги');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-eed1492ce082', now(), now(), '00000000-0000-0000-0001-6ac58ecadc22', '00000000-0000-0000-0001-81548349b593', 'Прием кардиолога', 'Консультация, ЭКГ, расшифровка', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-eb254f1413d5', now(), now(), '00000000-0000-0000-0001-eed1492ce082', '00000000-0000-0000-0001-5dd698a511d4', 'ONLINE', 7000, 45, 'Пн-Пт 09:00-16:00', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-9ab19315679b', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-eb254f1413d5', 'Прием кардиолога', 'Консультация, ЭКГ, расшифровка', 'Медицинские услуги', null, null, '00000000-0000-0000-0001-6ac58ecadc22', '00000000-0000-0000-0001-5dd698a511d4', 7000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-9ab19315679b', 'прием'), ('00000000-0000-0000-0001-9ab19315679b', 'кардиолога'), ('00000000-0000-0000-0001-9ab19315679b', 'медицинские'), ('00000000-0000-0000-0001-9ab19315679b', 'услуги');

-- Компания-Услуга-2 (ТОО, Образование, Караганда)
INSERT INTO business (id, created_at, updated_at, name, legal_name, bin, status) VALUES
  ('00000000-0000-0000-0001-abf1caf0c94b', now(), now(), 'Компания-Услуга-2', 'ТОО "Компания-Услуга-2"', '345678901234', 'ACTIVE');
INSERT INTO business_branch (id, created_at, updated_at, business_id, city_id, name, address, latitude, longitude, online_only, status) VALUES
  ('00000000-0000-0000-0001-7ff08f592df1', now(), now(), '00000000-0000-0000-0001-abf1caf0c94b', '00000000-0000-0000-0001-02ba3c39b41d', 'Компания-Услуга-2 - Офис', 'ул. Ермекова, 40', 44.8422, 65.5020, false, 'ACTIVE');
INSERT INTO data_source (id, created_at, updated_at, business_id, source_type, name, status) VALUES
  ('00000000-0000-0000-0001-62100cb55ad9', now(), now(), '00000000-0000-0000-0001-abf1caf0c94b', 'MANUAL', 'Ручной ввод Компания-Услуга-2', 'ACTIVE');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-0f181d91b717', now(), now(), '00000000-0000-0000-0001-abf1caf0c94b', '00000000-0000-0000-0001-49c510314da7', 'Курс Python базовый', 'Основы программирования на Python, 16 часов', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-a77b26e817ee', now(), now(), '00000000-0000-0000-0001-0f181d91b717', '00000000-0000-0000-0001-7ff08f592df1', 'ONLINE', 45000, 960, 'Группы: утро/вечер', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-16bf3bb7218d', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-a77b26e817ee', 'Курс Python базовый', 'Основы программирования на Python, 16 часов', 'Образование', null, null, '00000000-0000-0000-0001-abf1caf0c94b', '00000000-0000-0000-0001-7ff08f592df1', 45000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-16bf3bb7218d', 'курс'), ('00000000-0000-0000-0001-16bf3bb7218d', 'python'), ('00000000-0000-0000-0001-16bf3bb7218d', 'базовый'), ('00000000-0000-0000-0001-16bf3bb7218d', 'образование');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-4696b8bdb805', now(), now(), '00000000-0000-0000-0001-abf1caf0c94b', '00000000-0000-0000-0001-49c510314da7', 'Английский язык Intermediate', 'Разговорный курс, 24 занятия по 90 мин', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-16badf135dde', now(), now(), '00000000-0000-0000-0001-4696b8bdb805', '00000000-0000-0000-0001-7ff08f592df1', 'ON_SITE', 65000, 1440, 'Пн-Ср-Пт или Вт-Чт-Сб', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-2b9b17a53b42', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-16badf135dde', 'Английский язык Intermediate', 'Разговорный курс, 24 занятия по 90 мин', 'Образование', null, null, '00000000-0000-0000-0001-abf1caf0c94b', '00000000-0000-0000-0001-7ff08f592df1', 65000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-2b9b17a53b42', 'английский'), ('00000000-0000-0000-0001-2b9b17a53b42', 'язык'), ('00000000-0000-0000-0001-2b9b17a53b42', 'intermediate'), ('00000000-0000-0000-0001-2b9b17a53b42', 'образование');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-ee038b7bc85f', now(), now(), '00000000-0000-0000-0001-abf1caf0c94b', '00000000-0000-0000-0001-49c510314da7', 'Подготовка к IELTS', 'Интенсивный курс, 20 занятий по 120 мин', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-00e65dc3caaa', now(), now(), '00000000-0000-0000-0001-ee038b7bc85f', '00000000-0000-0000-0001-7ff08f592df1', 'ONLINE', 85000, 2400, 'Утро 09:00 или вечер 18:00', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-b81db1157c9c', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-00e65dc3caaa', 'Подготовка к IELTS', 'Интенсивный курс, 20 занятий по 120 мин', 'Образование', null, null, '00000000-0000-0000-0001-abf1caf0c94b', '00000000-0000-0000-0001-7ff08f592df1', 85000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-b81db1157c9c', 'подготовка'), ('00000000-0000-0000-0001-b81db1157c9c', 'ielts'), ('00000000-0000-0000-0001-b81db1157c9c', 'образование');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-25039d81d518', now(), now(), '00000000-0000-0000-0001-abf1caf0c94b', '00000000-0000-0000-0001-49c510314da7', 'Репетиторство по математике', 'Подготовка к ЕНТ, индивидуально, 60 мин', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-eacac95748bf', now(), now(), '00000000-0000-0000-0001-25039d81d518', '00000000-0000-0000-0001-7ff08f592df1', 'ONLINE', 4000, 60, 'По согласованию', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-0221199bbc7e', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-eacac95748bf', 'Репетиторство по математике', 'Подготовка к ЕНТ, индивидуально, 60 мин', 'Образование', null, null, '00000000-0000-0000-0001-abf1caf0c94b', '00000000-0000-0000-0001-7ff08f592df1', 4000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-0221199bbc7e', 'репетиторство'), ('00000000-0000-0000-0001-0221199bbc7e', 'математике'), ('00000000-0000-0000-0001-0221199bbc7e', 'образование');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-ecd1ed384874', now(), now(), '00000000-0000-0000-0001-abf1caf0c94b', '00000000-0000-0000-0001-49c510314da7', 'Курс видеомонтажа DaVinci', 'Основы цветокоррекции и монтажа, 10 занятий', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-1d9f7d645875', now(), now(), '00000000-0000-0000-0001-ecd1ed384874', '00000000-0000-0000-0001-7ff08f592df1', 'ON_SITE', 35000, 600, 'Вт-Чт 18:00-20:00', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-1a3cfbece63e', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-1d9f7d645875', 'Курс видеомонтажа DaVinci', 'Основы цветокоррекции и монтажа, 10 занятий', 'Образование', null, null, '00000000-0000-0000-0001-abf1caf0c94b', '00000000-0000-0000-0001-7ff08f592df1', 35000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-1a3cfbece63e', 'курс'), ('00000000-0000-0000-0001-1a3cfbece63e', 'видеомонтажа'), ('00000000-0000-0000-0001-1a3cfbece63e', 'davinci'), ('00000000-0000-0000-0001-1a3cfbece63e', 'образование');

-- Компания-Услуга-3 (ИП, Спорт и фитнес, Кызылорда)
INSERT INTO business (id, created_at, updated_at, name, legal_name, bin, status) VALUES
  ('00000000-0000-0000-0001-19951e7b86a3', now(), now(), 'Компания-Услуга-3', 'ИП "Компания-Услуга-3"', '456789012345', 'ACTIVE');
INSERT INTO business_branch (id, created_at, updated_at, business_id, city_id, name, address, latitude, longitude, online_only, status) VALUES
  ('00000000-0000-0000-0001-2ddd03e599a5', now(), now(), '00000000-0000-0000-0001-19951e7b86a3', '00000000-0000-0000-0000-0000000000c1', 'Компания-Услуга-3 - Офис', 'ул. Жибек жолы, 10', 44.8432, 65.5030, false, 'ACTIVE');
INSERT INTO data_source (id, created_at, updated_at, business_id, source_type, name, status) VALUES
  ('00000000-0000-0000-0001-6fa80f8f71ff', now(), now(), '00000000-0000-0000-0001-19951e7b86a3', 'MANUAL', 'Ручной ввод Компания-Услуга-3', 'ACTIVE');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-47b0a23b9770', now(), now(), '00000000-0000-0000-0001-19951e7b86a3', '00000000-0000-0000-0001-9f59c39873da', 'Персональная тренировка', 'Индивидуальное занятие с тренером, 60 мин', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-40a422fc7539', now(), now(), '00000000-0000-0000-0001-47b0a23b9770', '00000000-0000-0000-0001-2ddd03e599a5', 'ON_SITE', 5000, 60, 'Ежедневно 06:00-23:00', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-2f714539d766', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-40a422fc7539', 'Персональная тренировка', 'Индивидуальное занятие с тренером, 60 мин', 'Спорт и фитнес', null, null, '00000000-0000-0000-0001-19951e7b86a3', '00000000-0000-0000-0001-2ddd03e599a5', 5000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-2f714539d766', 'персональная'), ('00000000-0000-0000-0001-2f714539d766', 'тренировка'), ('00000000-0000-0000-0001-2f714539d766', 'спорт'), ('00000000-0000-0000-0001-2f714539d766', 'фитнес');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-c801906544a1', now(), now(), '00000000-0000-0000-0001-19951e7b86a3', '00000000-0000-0000-0001-9f59c39873da', 'Абонемент в тренажерный зал', 'Месячный абонемент безлимит', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-f1a71f883be9', now(), now(), '00000000-0000-0000-0001-c801906544a1', '00000000-0000-0000-0001-2ddd03e599a5', 'ONLINE', 25000, 43200, 'Ежедневно 06:00-23:00', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-4acafed53706', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-f1a71f883be9', 'Абонемент в тренажерный зал', 'Месячный абонемент безлимит', 'Спорт и фитнес', null, null, '00000000-0000-0000-0001-19951e7b86a3', '00000000-0000-0000-0001-2ddd03e599a5', 25000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-4acafed53706', 'абонемент'), ('00000000-0000-0000-0001-4acafed53706', 'тренажерный'), ('00000000-0000-0000-0001-4acafed53706', 'зал'), ('00000000-0000-0000-0001-4acafed53706', 'спорт'), ('00000000-0000-0000-0001-4acafed53706', 'фитнес');

-- Компания-Услуга-4 (ТОО, IT услуги, Алматы)
INSERT INTO business (id, created_at, updated_at, name, legal_name, bin, status) VALUES
  ('00000000-0000-0000-0001-660da2e4b881', now(), now(), 'Компания-Услуга-4', 'ТОО "Компания-Услуга-4"', '567890123456', 'ACTIVE');
INSERT INTO business_branch (id, created_at, updated_at, business_id, city_id, name, address, latitude, longitude, online_only, status) VALUES
  ('00000000-0000-0000-0001-edfe528acd3d', now(), now(), '00000000-0000-0000-0001-660da2e4b881', '00000000-0000-0000-0000-0000000000c2', 'Компания-Услуга-4 - Офис', 'ул. Тимирязева, 42', 44.8442, 65.5040, false, 'ACTIVE');
INSERT INTO data_source (id, created_at, updated_at, business_id, source_type, name, status) VALUES
  ('00000000-0000-0000-0001-0410a6b0f278', now(), now(), '00000000-0000-0000-0001-660da2e4b881', 'MANUAL', 'Ручной ввод Компания-Услуга-4', 'ACTIVE');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-74e4b3e2286f', now(), now(), '00000000-0000-0000-0001-660da2e4b881', '00000000-0000-0000-0001-a686baabac2c', 'Разработка сайта-визитки', 'Лендинг до 5 страниц, адаптивный дизайн', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-0c6c4f87d13b', now(), now(), '00000000-0000-0000-0001-74e4b3e2286f', '00000000-0000-0000-0001-edfe528acd3d', 'ONLINE', 150000, 10080, 'По согласованию', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-81a27749c64c', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-0c6c4f87d13b', 'Разработка сайта-визитки', 'Лендинг до 5 страниц, адаптивный дизайн', 'IT услуги', null, null, '00000000-0000-0000-0001-660da2e4b881', '00000000-0000-0000-0001-edfe528acd3d', 150000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-81a27749c64c', 'разработка'), ('00000000-0000-0000-0001-81a27749c64c', 'сайта'), ('00000000-0000-0000-0001-81a27749c64c', 'визитки'), ('00000000-0000-0000-0001-81a27749c64c', 'услуги');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-e5b1da4dbe1e', now(), now(), '00000000-0000-0000-0001-660da2e4b881', '00000000-0000-0000-0001-a686baabac2c', 'Настройка таргет рекламы', 'Настройка и ведение рекламы в Instagram/Facebook', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-61ae9a160552', now(), now(), '00000000-0000-0000-0001-e5b1da4dbe1e', '00000000-0000-0000-0001-edfe528acd3d', 'ONLINE', 50000, 10080, 'Рабочие дни', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-0dc6f213f38e', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-61ae9a160552', 'Настройка таргет рекламы', 'Настройка и ведение рекламы в Instagram/Facebook', 'IT услуги', null, null, '00000000-0000-0000-0001-660da2e4b881', '00000000-0000-0000-0001-edfe528acd3d', 50000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-0dc6f213f38e', 'настройка'), ('00000000-0000-0000-0001-0dc6f213f38e', 'таргет'), ('00000000-0000-0000-0001-0dc6f213f38e', 'рекламы'), ('00000000-0000-0000-0001-0dc6f213f38e', 'услуги');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-80cc85a66910', now(), now(), '00000000-0000-0000-0001-660da2e4b881', '00000000-0000-0000-0001-a686baabac2c', 'SEO продвижение сайта', 'Аудит + базовая оптимизация, ежемесячно', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-5f6df7b94fc0', now(), now(), '00000000-0000-0000-0001-80cc85a66910', '00000000-0000-0000-0001-edfe528acd3d', 'ON_SITE', 80000, 43200, 'По согласованию', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-8e237b418bbb', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-5f6df7b94fc0', 'SEO продвижение сайта', 'Аудит + базовая оптимизация, ежемесячно', 'IT услуги', null, null, '00000000-0000-0000-0001-660da2e4b881', '00000000-0000-0000-0001-edfe528acd3d', 80000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-8e237b418bbb', 'seo'), ('00000000-0000-0000-0001-8e237b418bbb', 'продвижение'), ('00000000-0000-0000-0001-8e237b418bbb', 'сайта'), ('00000000-0000-0000-0001-8e237b418bbb', 'услуги');

-- Компания-Услуга-5 (ИП, Автозапчасти, Астана)
INSERT INTO business (id, created_at, updated_at, name, legal_name, bin, status) VALUES
  ('00000000-0000-0000-0001-316cce83b199', now(), now(), 'Компания-Услуга-5', 'ИП "Компания-Услуга-5"', '678901234567', 'ACTIVE');
INSERT INTO business_branch (id, created_at, updated_at, business_id, city_id, name, address, latitude, longitude, online_only, status) VALUES
  ('00000000-0000-0000-0001-b88b82da47de', now(), now(), '00000000-0000-0000-0001-316cce83b199', '00000000-0000-0000-0000-0000000000c3', 'Компания-Услуга-5 - Офис', 'ул. Бейбитшилик, 30', 44.8452, 65.5050, false, 'ACTIVE');
INSERT INTO data_source (id, created_at, updated_at, business_id, source_type, name, status) VALUES
  ('00000000-0000-0000-0001-ebcfd37b0277', now(), now(), '00000000-0000-0000-0001-316cce83b199', 'MANUAL', 'Ручной ввод Компания-Услуга-5', 'ACTIVE');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-779cdfdba62f', now(), now(), '00000000-0000-0000-0001-316cce83b199', '00000000-0000-0000-0000-0000000000a1', 'Абонемент в тренажерный зал', 'Месячный абонемент безлимит', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-c87e66e4b590', now(), now(), '00000000-0000-0000-0001-779cdfdba62f', '00000000-0000-0000-0001-b88b82da47de', 'ONLINE', 25000, 43200, 'Ежедневно 06:00-23:00', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-89824c1a8630', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-c87e66e4b590', 'Абонемент в тренажерный зал', 'Месячный абонемент безлимит', 'Автозапчасти', null, null, '00000000-0000-0000-0001-316cce83b199', '00000000-0000-0000-0001-b88b82da47de', 25000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-89824c1a8630', 'абонемент'), ('00000000-0000-0000-0001-89824c1a8630', 'тренажерный'), ('00000000-0000-0000-0001-89824c1a8630', 'зал'), ('00000000-0000-0000-0001-89824c1a8630', 'автозапчасти');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-528305efa2c8', now(), now(), '00000000-0000-0000-0001-316cce83b199', '00000000-0000-0000-0000-0000000000a1', 'Занятие по кроссфиту', 'Групповое занятие, 90 мин', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-6917a3a4d8c9', now(), now(), '00000000-0000-0000-0001-528305efa2c8', '00000000-0000-0000-0001-b88b82da47de', 'ON_SITE', 3500, 90, 'Пн-Ср-Пт 07:00, 19:00', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-f5830eaf2ef8', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-6917a3a4d8c9', 'Занятие по кроссфиту', 'Групповое занятие, 90 мин', 'Автозапчасти', null, null, '00000000-0000-0000-0001-316cce83b199', '00000000-0000-0000-0001-b88b82da47de', 3500, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-f5830eaf2ef8', 'занятие'), ('00000000-0000-0000-0001-f5830eaf2ef8', 'кроссфиту'), ('00000000-0000-0000-0001-f5830eaf2ef8', 'автозапчасти');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-cf7f104ada6a', now(), now(), '00000000-0000-0000-0001-316cce83b199', '00000000-0000-0000-0000-0000000000a1', 'Плавание дети 6-12 лет', 'Групповое занятие с тренером, 45 мин', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-6e3a42fa151d', now(), now(), '00000000-0000-0000-0001-cf7f104ada6a', '00000000-0000-0000-0001-b88b82da47de', 'ONLINE', 3000, 45, 'Сб-Вс 10:00-14:00', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-3c4a905cd593', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-6e3a42fa151d', 'Плавание дети 6-12 лет', 'Групповое занятие с тренером, 45 мин', 'Автозапчасти', null, null, '00000000-0000-0000-0001-316cce83b199', '00000000-0000-0000-0001-b88b82da47de', 3000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-3c4a905cd593', 'плавание'), ('00000000-0000-0000-0001-3c4a905cd593', 'дети'), ('00000000-0000-0000-0001-3c4a905cd593', 'лет'), ('00000000-0000-0000-0001-3c4a905cd593', 'автозапчасти');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-6db98e73d43f', now(), now(), '00000000-0000-0000-0001-316cce83b199', '00000000-0000-0000-0000-0000000000a1', 'Растяжка Stretching', 'Групповое занятие, 60 мин', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-dcb390fb05fd', now(), now(), '00000000-0000-0000-0001-6db98e73d43f', '00000000-0000-0000-0001-b88b82da47de', 'ONLINE', 2500, 60, 'Вт-Чт 18:00, Сб 10:00', false, 'ACTIVE');

-- Компания-Услуга-6 (ТОО, Бытовая техника, Шымкент)
INSERT INTO business (id, created_at, updated_at, name, legal_name, bin, status) VALUES
  ('00000000-0000-0000-0001-b48a641037ab', now(), now(), 'Компания-Услуга-6', 'ТОО "Компания-Услуга-6"', '789012345678', 'ACTIVE');
INSERT INTO business_branch (id, created_at, updated_at, business_id, city_id, name, address, latitude, longitude, online_only, status) VALUES
  ('00000000-0000-0000-0001-bd9faa335456', now(), now(), '00000000-0000-0000-0001-b48a641037ab', '00000000-0000-0000-0001-1a904d507278', 'Компания-Услуга-6 - Филиал 1', 'пр. Тауке хана, 88', 44.8462, 65.5060, false, 'ACTIVE');
INSERT INTO business_branch (id, created_at, updated_at, business_id, city_id, name, address, latitude, longitude, online_only, status) VALUES
  ('00000000-0000-0000-0001-c21575b384d7', now(), now(), '00000000-0000-0000-0001-b48a641037ab', '00000000-0000-0000-0001-1a904d507278', 'Компания-Услуга-6 - Филиал 2', 'пр. Тауке хана, 88', 44.8462, 65.5060, false, 'ACTIVE');
INSERT INTO data_source (id, created_at, updated_at, business_id, source_type, name, status) VALUES
  ('00000000-0000-0000-0001-f6934b9bd33e', now(), now(), '00000000-0000-0000-0001-b48a641037ab', 'MANUAL', 'Ручной ввод Компания-Услуга-6', 'ACTIVE');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-f1c3a951f5c5', now(), now(), '00000000-0000-0000-0001-b48a641037ab', '00000000-0000-0000-0000-0000000000a2', 'Установка кондиционера', 'Монтаж сплит-системы под ключ', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-3dc4aa434e8c', now(), now(), '00000000-0000-0000-0001-f1c3a951f5c5', '00000000-0000-0000-0001-bd9faa335456', 'ON_SITE', 25000, 180, 'Ежедневно, по записи', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-0d6d45ca974c', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-3dc4aa434e8c', 'Установка кондиционера', 'Монтаж сплит-системы под ключ', 'Бытовая техника', null, null, '00000000-0000-0000-0001-b48a641037ab', '00000000-0000-0000-0001-bd9faa335456', 25000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-0d6d45ca974c', 'установка'), ('00000000-0000-0000-0001-0d6d45ca974c', 'кондиционера'), ('00000000-0000-0000-0001-0d6d45ca974c', 'бытовая'), ('00000000-0000-0000-0001-0d6d45ca974c', 'техника');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-4c3c1d68e516', now(), now(), '00000000-0000-0000-0001-f1c3a951f5c5', '00000000-0000-0000-0001-c21575b384d7', 'ON_SITE', 25000, 180, 'Ежедневно, по записи', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-3af5defa6b42', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-4c3c1d68e516', 'Установка кондиционера', 'Монтаж сплит-системы под ключ', 'Бытовая техника', null, null, '00000000-0000-0000-0001-b48a641037ab', '00000000-0000-0000-0001-c21575b384d7', 25000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-3af5defa6b42', 'установка'), ('00000000-0000-0000-0001-3af5defa6b42', 'кондиционера'), ('00000000-0000-0000-0001-3af5defa6b42', 'бытовая'), ('00000000-0000-0000-0001-3af5defa6b42', 'техника');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-6fe445759150', now(), now(), '00000000-0000-0000-0001-b48a641037ab', '00000000-0000-0000-0000-0000000000a2', 'Замена электропроводки', 'Полная замена в 2-комнатной квартире', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-fefdd11c30b4', now(), now(), '00000000-0000-0000-0001-6fe445759150', '00000000-0000-0000-0001-bd9faa335456', 'ONLINE', 180000, 1440, 'По согласованию', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-66a6dc550598', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-fefdd11c30b4', 'Замена электропроводки', 'Полная замена в 2-комнатной квартире', 'Бытовая техника', null, null, '00000000-0000-0000-0001-b48a641037ab', '00000000-0000-0000-0001-bd9faa335456', 180000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-66a6dc550598', 'замена'), ('00000000-0000-0000-0001-66a6dc550598', 'электропроводки'), ('00000000-0000-0000-0001-66a6dc550598', 'бытовая'), ('00000000-0000-0000-0001-66a6dc550598', 'техника');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-c6c3d23284b9', now(), now(), '00000000-0000-0000-0001-6fe445759150', '00000000-0000-0000-0001-c21575b384d7', 'ONLINE', 180000, 1440, 'По согласованию', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-3d75be612e79', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-c6c3d23284b9', 'Замена электропроводки', 'Полная замена в 2-комнатной квартире', 'Бытовая техника', null, null, '00000000-0000-0000-0001-b48a641037ab', '00000000-0000-0000-0001-c21575b384d7', 180000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-3d75be612e79', 'замена'), ('00000000-0000-0000-0001-3d75be612e79', 'электропроводки'), ('00000000-0000-0000-0001-3d75be612e79', 'бытовая'), ('00000000-0000-0000-0001-3d75be612e79', 'техника');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-936f57b4c98f', now(), now(), '00000000-0000-0000-0001-b48a641037ab', '00000000-0000-0000-0000-0000000000a2', 'Сантехнические работы', 'Установка смесителя, ремонт бачка, прочистка', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-b9f3919c824a', now(), now(), '00000000-0000-0000-0001-936f57b4c98f', '00000000-0000-0000-0001-bd9faa335456', 'ONLINE', 7000, 90, 'Ежедневно 08:00-20:00', false, 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-bbcfb41fd70b', now(), now(), '00000000-0000-0000-0001-936f57b4c98f', '00000000-0000-0000-0001-c21575b384d7', 'ONLINE', 7000, 90, 'Ежедневно 08:00-20:00', false, 'ACTIVE');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-c2d4762906b4', now(), now(), '00000000-0000-0000-0001-b48a641037ab', '00000000-0000-0000-0000-0000000000a2', 'Отделка квартиры под ключ', 'Черновая + чистовая отделка, материал заказчика', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-a083be8766cb', now(), now(), '00000000-0000-0000-0001-c2d4762906b4', '00000000-0000-0000-0001-bd9faa335456', 'ON_SITE', 1500000, 43200, 'По договору', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-a38dbc9dfae3', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-a083be8766cb', 'Отделка квартиры под ключ', 'Черновая + чистовая отделка, материал заказчика', 'Бытовая техника', null, null, '00000000-0000-0000-0001-b48a641037ab', '00000000-0000-0000-0001-bd9faa335456', 1500000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-a38dbc9dfae3', 'отделка'), ('00000000-0000-0000-0001-a38dbc9dfae3', 'квартиры'), ('00000000-0000-0000-0001-a38dbc9dfae3', 'ключ'), ('00000000-0000-0000-0001-a38dbc9dfae3', 'бытовая'), ('00000000-0000-0000-0001-a38dbc9dfae3', 'техника');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-67349064b456', now(), now(), '00000000-0000-0000-0001-c2d4762906b4', '00000000-0000-0000-0001-c21575b384d7', 'ON_SITE', 1500000, 43200, 'По договору', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-318e94a797a8', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-67349064b456', 'Отделка квартиры под ключ', 'Черновая + чистовая отделка, материал заказчика', 'Бытовая техника', null, null, '00000000-0000-0000-0001-b48a641037ab', '00000000-0000-0000-0001-c21575b384d7', 1500000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-318e94a797a8', 'отделка'), ('00000000-0000-0000-0001-318e94a797a8', 'квартиры'), ('00000000-0000-0000-0001-318e94a797a8', 'ключ'), ('00000000-0000-0000-0001-318e94a797a8', 'бытовая'), ('00000000-0000-0000-0001-318e94a797a8', 'техника');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-a1d14f382301', now(), now(), '00000000-0000-0000-0001-b48a641037ab', '00000000-0000-0000-0000-0000000000a2', 'Монтаж гипсокартона', 'Потолок, 1м², включая профиль и саморезы', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-08c7ff229796', now(), now(), '00000000-0000-0000-0001-a1d14f382301', '00000000-0000-0000-0001-bd9faa335456', 'ONLINE', 3500, 60, 'Ежедневно 08:00-18:00', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-53c8f0460d8d', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-08c7ff229796', 'Монтаж гипсокартона', 'Потолок, 1м², включая профиль и саморезы', 'Бытовая техника', null, null, '00000000-0000-0000-0001-b48a641037ab', '00000000-0000-0000-0001-bd9faa335456', 3500, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-53c8f0460d8d', 'монтаж'), ('00000000-0000-0000-0001-53c8f0460d8d', 'гипсокартона'), ('00000000-0000-0000-0001-53c8f0460d8d', 'бытовая'), ('00000000-0000-0000-0001-53c8f0460d8d', 'техника');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-eeaaf1c260c1', now(), now(), '00000000-0000-0000-0001-a1d14f382301', '00000000-0000-0000-0001-c21575b384d7', 'ONLINE', 3500, 60, 'Ежедневно 08:00-18:00', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-a9f466875f5e', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-eeaaf1c260c1', 'Монтаж гипсокартона', 'Потолок, 1м², включая профиль и саморезы', 'Бытовая техника', null, null, '00000000-0000-0000-0001-b48a641037ab', '00000000-0000-0000-0001-c21575b384d7', 3500, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-a9f466875f5e', 'монтаж'), ('00000000-0000-0000-0001-a9f466875f5e', 'гипсокартона'), ('00000000-0000-0000-0001-a9f466875f5e', 'бытовая'), ('00000000-0000-0000-0001-a9f466875f5e', 'техника');

-- Компания-Услуга-7 (ИП, Услуги красоты, Караганда)
INSERT INTO business (id, created_at, updated_at, name, legal_name, bin, status) VALUES
  ('00000000-0000-0000-0001-7e4d65b2062d', now(), now(), 'Компания-Услуга-7', 'ИП "Компания-Услуга-7"', '890123456789', 'ACTIVE');
INSERT INTO business_branch (id, created_at, updated_at, business_id, city_id, name, address, latitude, longitude, online_only, status) VALUES
  ('00000000-0000-0000-0001-560ec5bafc12', now(), now(), '00000000-0000-0000-0001-7e4d65b2062d', '00000000-0000-0000-0001-02ba3c39b41d', 'Компания-Услуга-7 - Офис', 'ул. Гоголя, 25', 44.8472, 65.5070, false, 'ACTIVE');
INSERT INTO data_source (id, created_at, updated_at, business_id, source_type, name, status) VALUES
  ('00000000-0000-0000-0001-b62c7f0a1511', now(), now(), '00000000-0000-0000-0001-7e4d65b2062d', 'MANUAL', 'Ручной ввод Компания-Услуга-7', 'ACTIVE');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-92f27ce69fd9', now(), now(), '00000000-0000-0000-0001-7e4d65b2062d', '00000000-0000-0000-0000-0000000000a3', 'Стрижка женская модельная', 'Стрижка с укладкой и уходом, 60 мин', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-0df629abfcb4', now(), now(), '00000000-0000-0000-0001-92f27ce69fd9', '00000000-0000-0000-0001-560ec5bafc12', 'ONLINE', 6000, 60, 'Ежедневно 10:00-20:00', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-93de9f2c320e', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-0df629abfcb4', 'Стрижка женская модельная', 'Стрижка с укладкой и уходом, 60 мин', 'Услуги красоты', null, null, '00000000-0000-0000-0001-7e4d65b2062d', '00000000-0000-0000-0001-560ec5bafc12', 6000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-93de9f2c320e', 'стрижка'), ('00000000-0000-0000-0001-93de9f2c320e', 'женская'), ('00000000-0000-0000-0001-93de9f2c320e', 'модельная'), ('00000000-0000-0000-0001-93de9f2c320e', 'услуги'), ('00000000-0000-0000-0001-93de9f2c320e', 'красоты');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-13163b50f56e', now(), now(), '00000000-0000-0000-0001-7e4d65b2062d', '00000000-0000-0000-0000-0000000000a3', 'Маникюр аппаратный', 'Аппаратный маникюр + покрытие гель-лак', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-e8c962008711', now(), now(), '00000000-0000-0000-0001-13163b50f56e', '00000000-0000-0000-0001-560ec5bafc12', 'ONLINE', 5500, 75, 'Пн-Сб 09:00-18:00', false, 'ACTIVE');

-- Компания-Услуга-8 (ТОО, Ремонт и сервис, Кызылорда)
INSERT INTO business (id, created_at, updated_at, name, legal_name, bin, status) VALUES
  ('00000000-0000-0000-0001-3ffc24dc00b3', now(), now(), 'Компания-Услуга-8', 'ТОО "Компания-Услуга-8"', '901234567890', 'ACTIVE');
INSERT INTO business_branch (id, created_at, updated_at, business_id, city_id, name, address, latitude, longitude, online_only, status) VALUES
  ('00000000-0000-0000-0001-4c1039f61e00', now(), now(), '00000000-0000-0000-0001-3ffc24dc00b3', '00000000-0000-0000-0000-0000000000c1', 'Компания-Услуга-8 - Офис', 'мкр. Мерей, 15', 44.8482, 65.5080, false, 'ACTIVE');
INSERT INTO data_source (id, created_at, updated_at, business_id, source_type, name, status) VALUES
  ('00000000-0000-0000-0001-00d4841a8188', now(), now(), '00000000-0000-0000-0001-3ffc24dc00b3', 'MANUAL', 'Ручной ввод Компания-Услуга-8', 'ACTIVE');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-7bd1ea14c9d8', now(), now(), '00000000-0000-0000-0001-3ffc24dc00b3', '00000000-0000-0000-0000-0000000000a4', 'Ремонт стиральной машины', 'Диагностика + ремонт на месте', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-f77bdc30d541', now(), now(), '00000000-0000-0000-0001-7bd1ea14c9d8', '00000000-0000-0000-0001-4c1039f61e00', 'ONLINE', 5000, 60, 'Пн-Сб 09:00-18:00', false, 'ACTIVE');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-a5dba92be914', now(), now(), '00000000-0000-0000-0001-3ffc24dc00b3', '00000000-0000-0000-0000-0000000000a4', 'Установка кондиционера', 'Монтаж сплит-системы под ключ', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-bf3701cbfdcc', now(), now(), '00000000-0000-0000-0001-a5dba92be914', '00000000-0000-0000-0001-4c1039f61e00', 'ON_SITE', 25000, 180, 'Ежедневно, по записи', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-7204bcaa0ba3', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-bf3701cbfdcc', 'Установка кондиционера', 'Монтаж сплит-системы под ключ', 'Ремонт и сервис', null, null, '00000000-0000-0000-0001-3ffc24dc00b3', '00000000-0000-0000-0001-4c1039f61e00', 25000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-7204bcaa0ba3', 'установка'), ('00000000-0000-0000-0001-7204bcaa0ba3', 'кондиционера'), ('00000000-0000-0000-0001-7204bcaa0ba3', 'ремонт'), ('00000000-0000-0000-0001-7204bcaa0ba3', 'сервис');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-64040da9a84c', now(), now(), '00000000-0000-0000-0001-3ffc24dc00b3', '00000000-0000-0000-0000-0000000000a4', 'Замена электропроводки', 'Полная замена в 2-комнатной квартире', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-4f7a8d31028f', now(), now(), '00000000-0000-0000-0001-64040da9a84c', '00000000-0000-0000-0001-4c1039f61e00', 'ONLINE', 180000, 1440, 'По согласованию', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-e4e4ee2f63b2', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-4f7a8d31028f', 'Замена электропроводки', 'Полная замена в 2-комнатной квартире', 'Ремонт и сервис', null, null, '00000000-0000-0000-0001-3ffc24dc00b3', '00000000-0000-0000-0001-4c1039f61e00', 180000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-e4e4ee2f63b2', 'замена'), ('00000000-0000-0000-0001-e4e4ee2f63b2', 'электропроводки'), ('00000000-0000-0000-0001-e4e4ee2f63b2', 'ремонт'), ('00000000-0000-0000-0001-e4e4ee2f63b2', 'сервис');

-- Компания-Услуга-9 (ИП, Строительство, Алматы)
INSERT INTO business (id, created_at, updated_at, name, legal_name, bin, status) VALUES
  ('00000000-0000-0000-0001-90604230fc94', now(), now(), 'Компания-Услуга-9', 'ИП "Компания-Услуга-9"', '012345678901', 'ACTIVE');
INSERT INTO business_branch (id, created_at, updated_at, business_id, city_id, name, address, latitude, longitude, online_only, status) VALUES
  ('00000000-0000-0000-0001-8dac477f6db3', now(), now(), '00000000-0000-0000-0001-90604230fc94', '00000000-0000-0000-0000-0000000000c2', 'Компания-Услуга-9 - Офис', 'ул. Сатпаева, 90', 44.8492, 65.5090, true, 'ACTIVE');
INSERT INTO data_source (id, created_at, updated_at, business_id, source_type, name, status) VALUES
  ('00000000-0000-0000-0001-bac8faa2d79f', now(), now(), '00000000-0000-0000-0001-90604230fc94', 'MANUAL', 'Ручной ввод Компания-Услуга-9', 'ACTIVE');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-a00edecf7d98', now(), now(), '00000000-0000-0000-0001-90604230fc94', '00000000-0000-0000-0000-0000000000a5', 'Отделка квартиры под ключ', 'Черновая + чистовая отделка, материал заказчика', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-012b298a2115', now(), now(), '00000000-0000-0000-0001-a00edecf7d98', '00000000-0000-0000-0001-8dac477f6db3', 'ON_SITE', 1500000, 43200, 'По договору', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-17cf2181c873', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-012b298a2115', 'Отделка квартиры под ключ', 'Черновая + чистовая отделка, материал заказчика', 'Строительство', null, null, '00000000-0000-0000-0001-90604230fc94', '00000000-0000-0000-0001-8dac477f6db3', 1500000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-17cf2181c873', 'отделка'), ('00000000-0000-0000-0001-17cf2181c873', 'квартиры'), ('00000000-0000-0000-0001-17cf2181c873', 'ключ'), ('00000000-0000-0000-0001-17cf2181c873', 'строительство');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-1dd3d6392b3b', now(), now(), '00000000-0000-0000-0001-90604230fc94', '00000000-0000-0000-0000-0000000000a5', 'Монтаж гипсокартона', 'Потолок, 1м², включая профиль и саморезы', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-9fe1644b3903', now(), now(), '00000000-0000-0000-0001-1dd3d6392b3b', '00000000-0000-0000-0001-8dac477f6db3', 'ONLINE', 3500, 60, 'Ежедневно 08:00-18:00', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-3df634b23338', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-9fe1644b3903', 'Монтаж гипсокартона', 'Потолок, 1м², включая профиль и саморезы', 'Строительство', null, null, '00000000-0000-0000-0001-90604230fc94', '00000000-0000-0000-0001-8dac477f6db3', 3500, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-3df634b23338', 'монтаж'), ('00000000-0000-0000-0001-3df634b23338', 'гипсокартона'), ('00000000-0000-0000-0001-3df634b23338', 'строительство');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-cfca5108f515', now(), now(), '00000000-0000-0000-0001-90604230fc94', '00000000-0000-0000-0000-0000000000a5', 'Укладка ламината', 'Укладка с подложкой, 1м²', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-43404c5250bd', now(), now(), '00000000-0000-0000-0001-cfca5108f515', '00000000-0000-0000-0001-8dac477f6db3', 'ONLINE', 2500, 60, 'Ежедневно 08:00-18:00', true, 'ARCHIVED');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-de2fa3e6841d', now(), now(), '00000000-0000-0000-0001-90604230fc94', '00000000-0000-0000-0000-0000000000a5', 'Отделка квартиры под ключ', 'Черновая + чистовая отделка, материал заказчика', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-601e413211b6', now(), now(), '00000000-0000-0000-0001-de2fa3e6841d', '00000000-0000-0000-0001-8dac477f6db3', 'ON_SITE', 1500000, 43200, 'По договору', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-9fcc767ef19a', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-601e413211b6', 'Отделка квартиры под ключ', 'Черновая + чистовая отделка, материал заказчика', 'Строительство', null, null, '00000000-0000-0000-0001-90604230fc94', '00000000-0000-0000-0001-8dac477f6db3', 1500000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-9fcc767ef19a', 'отделка'), ('00000000-0000-0000-0001-9fcc767ef19a', 'квартиры'), ('00000000-0000-0000-0001-9fcc767ef19a', 'ключ'), ('00000000-0000-0000-0001-9fcc767ef19a', 'строительство');

-- Компания-Услуга-10 (ТОО, Продукты питания, Астана)
INSERT INTO business (id, created_at, updated_at, name, legal_name, bin, status) VALUES
  ('00000000-0000-0000-0001-fb7892bcfb3d', now(), now(), 'Компания-Услуга-10', 'ТОО "Компания-Услуга-10"', '123456789012', 'ACTIVE');
INSERT INTO business_branch (id, created_at, updated_at, business_id, city_id, name, address, latitude, longitude, online_only, status) VALUES
  ('00000000-0000-0000-0001-6f2e6f903116', now(), now(), '00000000-0000-0000-0001-fb7892bcfb3d', '00000000-0000-0000-0000-0000000000c3', 'Компания-Услуга-10 - Офис', 'пр. Туран, 45', 44.8502, 65.5100, false, 'ACTIVE');
INSERT INTO data_source (id, created_at, updated_at, business_id, source_type, name, status) VALUES
  ('00000000-0000-0000-0001-73b7659824f9', now(), now(), '00000000-0000-0000-0001-fb7892bcfb3d', 'MANUAL', 'Ручной ввод Компания-Услуга-10', 'ACTIVE');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-9caff7a4f958', now(), now(), '00000000-0000-0000-0001-fb7892bcfb3d', '00000000-0000-0000-0001-279580d8f28e', 'Кейтеринг на мероприятие', 'Выездное обслуживание до 50 человек', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-fa11d5c713c9', now(), now(), '00000000-0000-0000-0001-9caff7a4f958', '00000000-0000-0000-0001-6f2e6f903116', 'ONLINE', 150000, 360, 'По записи, за 3 дня', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-843479fb7964', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-fa11d5c713c9', 'Кейтеринг на мероприятие', 'Выездное обслуживание до 50 человек', 'Продукты питания', null, null, '00000000-0000-0000-0001-fb7892bcfb3d', '00000000-0000-0000-0001-6f2e6f903116', 150000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-843479fb7964', 'кейтеринг'), ('00000000-0000-0000-0001-843479fb7964', 'мероприятие'), ('00000000-0000-0000-0001-843479fb7964', 'продукты'), ('00000000-0000-0000-0001-843479fb7964', 'питания');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-7090c9648bbb', now(), now(), '00000000-0000-0000-0001-fb7892bcfb3d', '00000000-0000-0000-0001-279580d8f28e', 'Доставка продуктов на дом', 'Доставка в течение 2 часов по городу', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-b00c5eaaf8d6', now(), now(), '00000000-0000-0000-0001-7090c9648bbb', '00000000-0000-0000-0001-6f2e6f903116', 'ONLINE', 1500, 120, 'Ежедневно 08:00-22:00', true, 'ARCHIVED');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-af4520e7a3d8', now(), now(), '00000000-0000-0000-0001-fb7892bcfb3d', '00000000-0000-0000-0001-279580d8f28e', 'Кейтеринг на мероприятие', 'Выездное обслуживание до 50 человек', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-78492e3031c0', now(), now(), '00000000-0000-0000-0001-af4520e7a3d8', '00000000-0000-0000-0001-6f2e6f903116', 'ON_SITE', 150000, 360, 'По записи, за 3 дня', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-be35e123d363', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-78492e3031c0', 'Кейтеринг на мероприятие', 'Выездное обслуживание до 50 человек', 'Продукты питания', null, null, '00000000-0000-0000-0001-fb7892bcfb3d', '00000000-0000-0000-0001-6f2e6f903116', 150000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-be35e123d363', 'кейтеринг'), ('00000000-0000-0000-0001-be35e123d363', 'мероприятие'), ('00000000-0000-0000-0001-be35e123d363', 'продукты'), ('00000000-0000-0000-0001-be35e123d363', 'питания');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-f3418ed6fb1a', now(), now(), '00000000-0000-0000-0001-fb7892bcfb3d', '00000000-0000-0000-0001-279580d8f28e', 'Доставка продуктов на дом', 'Доставка в течение 2 часов по городу', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-1e2ade4ad4b5', now(), now(), '00000000-0000-0000-0001-f3418ed6fb1a', '00000000-0000-0000-0001-6f2e6f903116', 'ONLINE', 1500, 120, 'Ежедневно 08:00-22:00', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-fff827cc8fc2', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-1e2ade4ad4b5', 'Доставка продуктов на дом', 'Доставка в течение 2 часов по городу', 'Продукты питания', null, null, '00000000-0000-0000-0001-fb7892bcfb3d', '00000000-0000-0000-0001-6f2e6f903116', 1500, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-fff827cc8fc2', 'доставка'), ('00000000-0000-0000-0001-fff827cc8fc2', 'продуктов'), ('00000000-0000-0000-0001-fff827cc8fc2', 'дом'), ('00000000-0000-0000-0001-fff827cc8fc2', 'продукты'), ('00000000-0000-0000-0001-fff827cc8fc2', 'питания');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-b5a8cbfb5b27', now(), now(), '00000000-0000-0000-0001-fb7892bcfb3d', '00000000-0000-0000-0001-279580d8f28e', 'Кейтеринг на мероприятие', 'Выездное обслуживание до 50 человек', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-a69aae7410a6', now(), now(), '00000000-0000-0000-0001-b5a8cbfb5b27', '00000000-0000-0000-0001-6f2e6f903116', 'ONLINE', 150000, 360, 'По записи, за 3 дня', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-e3ad8faea05c', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-a69aae7410a6', 'Кейтеринг на мероприятие', 'Выездное обслуживание до 50 человек', 'Продукты питания', null, null, '00000000-0000-0000-0001-fb7892bcfb3d', '00000000-0000-0000-0001-6f2e6f903116', 150000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-e3ad8faea05c', 'кейтеринг'), ('00000000-0000-0000-0001-e3ad8faea05c', 'мероприятие'), ('00000000-0000-0000-0001-e3ad8faea05c', 'продукты'), ('00000000-0000-0000-0001-e3ad8faea05c', 'питания');

-- Компания-Услуга-11 (ИП, Медицинские услуги, Шымкент)
INSERT INTO business (id, created_at, updated_at, name, legal_name, bin, status) VALUES
  ('00000000-0000-0000-0001-930d803de510', now(), now(), 'Компания-Услуга-11', 'ИП "Компания-Услуга-11"', '234567890123', 'ACTIVE');
INSERT INTO business_branch (id, created_at, updated_at, business_id, city_id, name, address, latitude, longitude, online_only, status) VALUES
  ('00000000-0000-0000-0001-8f919d3b1df2', now(), now(), '00000000-0000-0000-0001-930d803de510', '00000000-0000-0000-0001-1a904d507278', 'Компания-Услуга-11 - Офис', 'ул. Кулышова, 99', 44.8512, 65.5110, false, 'ACTIVE');
INSERT INTO data_source (id, created_at, updated_at, business_id, source_type, name, status) VALUES
  ('00000000-0000-0000-0001-e83fcd084109', now(), now(), '00000000-0000-0000-0001-930d803de510', 'MANUAL', 'Ручной ввод Компания-Услуга-11', 'ACTIVE');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-3a7e4e5fc6e1', now(), now(), '00000000-0000-0000-0001-930d803de510', '00000000-0000-0000-0001-81548349b593', 'Консультация терапевта', 'Первичный прием, осмотр, назначение', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-2923f985970b', now(), now(), '00000000-0000-0000-0001-3a7e4e5fc6e1', '00000000-0000-0000-0001-8f919d3b1df2', 'ONLINE', 5000, 30, 'Пн-Пт 08:00-17:00', true, 'ARCHIVED');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-748d7d4dc95c', now(), now(), '00000000-0000-0000-0001-930d803de510', '00000000-0000-0000-0001-81548349b593', 'УЗИ брюшной полости', 'Комплексное УЗИ органов брюшной полости', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-51e27e76d064', now(), now(), '00000000-0000-0000-0001-748d7d4dc95c', '00000000-0000-0000-0001-8f919d3b1df2', 'ON_SITE', 8000, 40, 'Пн-Сб 08:00-14:00', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-9699f107d8be', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-51e27e76d064', 'УЗИ брюшной полости', 'Комплексное УЗИ органов брюшной полости', 'Медицинские услуги', null, null, '00000000-0000-0000-0001-930d803de510', '00000000-0000-0000-0001-8f919d3b1df2', 8000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-9699f107d8be', 'узи'), ('00000000-0000-0000-0001-9699f107d8be', 'брюшной'), ('00000000-0000-0000-0001-9699f107d8be', 'полости'), ('00000000-0000-0000-0001-9699f107d8be', 'медицинские'), ('00000000-0000-0000-0001-9699f107d8be', 'услуги');

-- Компания-Услуга-12 (ТОО, Образование, Караганда)
INSERT INTO business (id, created_at, updated_at, name, legal_name, bin, status) VALUES
  ('00000000-0000-0000-0001-66fd038a7d01', now(), now(), 'Компания-Услуга-12', 'ТОО "Компания-Услуга-12"', '345678901234', 'ACTIVE');
INSERT INTO business_branch (id, created_at, updated_at, business_id, city_id, name, address, latitude, longitude, online_only, status) VALUES
  ('00000000-0000-0000-0001-389ebdb97ac2', now(), now(), '00000000-0000-0000-0001-66fd038a7d01', '00000000-0000-0000-0001-02ba3c39b41d', 'Компания-Услуга-12 - Филиал 1', 'пр. Бухар жырау, 52', 44.8522, 65.5120, false, 'ACTIVE');
INSERT INTO business_branch (id, created_at, updated_at, business_id, city_id, name, address, latitude, longitude, online_only, status) VALUES
  ('00000000-0000-0000-0001-41ca4d0f9374', now(), now(), '00000000-0000-0000-0001-66fd038a7d01', '00000000-0000-0000-0001-02ba3c39b41d', 'Компания-Услуга-12 - Филиал 2', 'пр. Бухар жырау, 52', 44.8522, 65.5120, false, 'ACTIVE');
INSERT INTO data_source (id, created_at, updated_at, business_id, source_type, name, status) VALUES
  ('00000000-0000-0000-0001-c1a27979d180', now(), now(), '00000000-0000-0000-0001-66fd038a7d01', 'MANUAL', 'Ручной ввод Компания-Услуга-12', 'ACTIVE');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-3b51954c2688', now(), now(), '00000000-0000-0000-0001-66fd038a7d01', '00000000-0000-0000-0001-49c510314da7', 'Курс Python базовый', 'Основы программирования на Python, 16 часов', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-54a6b149cd1a', now(), now(), '00000000-0000-0000-0001-3b51954c2688', '00000000-0000-0000-0001-389ebdb97ac2', 'ON_SITE', 45000, 960, 'Группы: утро/вечер', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-c0c978119f21', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-54a6b149cd1a', 'Курс Python базовый', 'Основы программирования на Python, 16 часов', 'Образование', null, null, '00000000-0000-0000-0001-66fd038a7d01', '00000000-0000-0000-0001-389ebdb97ac2', 45000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-c0c978119f21', 'курс'), ('00000000-0000-0000-0001-c0c978119f21', 'python'), ('00000000-0000-0000-0001-c0c978119f21', 'базовый'), ('00000000-0000-0000-0001-c0c978119f21', 'образование');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-b537a8f90db0', now(), now(), '00000000-0000-0000-0001-3b51954c2688', '00000000-0000-0000-0001-41ca4d0f9374', 'ON_SITE', 45000, 960, 'Группы: утро/вечер', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-93f21b14b878', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-b537a8f90db0', 'Курс Python базовый', 'Основы программирования на Python, 16 часов', 'Образование', null, null, '00000000-0000-0000-0001-66fd038a7d01', '00000000-0000-0000-0001-41ca4d0f9374', 45000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-93f21b14b878', 'курс'), ('00000000-0000-0000-0001-93f21b14b878', 'python'), ('00000000-0000-0000-0001-93f21b14b878', 'базовый'), ('00000000-0000-0000-0001-93f21b14b878', 'образование');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-6a0537580e72', now(), now(), '00000000-0000-0000-0001-66fd038a7d01', '00000000-0000-0000-0001-49c510314da7', 'Английский язык Intermediate', 'Разговорный курс, 24 занятия по 90 мин', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-c45fe7ab2323', now(), now(), '00000000-0000-0000-0001-6a0537580e72', '00000000-0000-0000-0001-389ebdb97ac2', 'ONLINE', 65000, 1440, 'Пн-Ср-Пт или Вт-Чт-Сб', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-9134a288be93', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-c45fe7ab2323', 'Английский язык Intermediate', 'Разговорный курс, 24 занятия по 90 мин', 'Образование', null, null, '00000000-0000-0000-0001-66fd038a7d01', '00000000-0000-0000-0001-389ebdb97ac2', 65000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-9134a288be93', 'английский'), ('00000000-0000-0000-0001-9134a288be93', 'язык'), ('00000000-0000-0000-0001-9134a288be93', 'intermediate'), ('00000000-0000-0000-0001-9134a288be93', 'образование');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-41bd37a90921', now(), now(), '00000000-0000-0000-0001-6a0537580e72', '00000000-0000-0000-0001-41ca4d0f9374', 'ONLINE', 65000, 1440, 'Пн-Ср-Пт или Вт-Чт-Сб', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-13b5d4c774ec', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-41bd37a90921', 'Английский язык Intermediate', 'Разговорный курс, 24 занятия по 90 мин', 'Образование', null, null, '00000000-0000-0000-0001-66fd038a7d01', '00000000-0000-0000-0001-41ca4d0f9374', 65000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-13b5d4c774ec', 'английский'), ('00000000-0000-0000-0001-13b5d4c774ec', 'язык'), ('00000000-0000-0000-0001-13b5d4c774ec', 'intermediate'), ('00000000-0000-0000-0001-13b5d4c774ec', 'образование');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-b4951ac1f8d8', now(), now(), '00000000-0000-0000-0001-66fd038a7d01', '00000000-0000-0000-0001-49c510314da7', 'Подготовка к IELTS', 'Интенсивный курс, 20 занятий по 120 мин', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-0fb130b9289b', now(), now(), '00000000-0000-0000-0001-b4951ac1f8d8', '00000000-0000-0000-0001-389ebdb97ac2', 'ONLINE', 85000, 2400, 'Утро 09:00 или вечер 18:00', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-c3c4a32715ee', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-0fb130b9289b', 'Подготовка к IELTS', 'Интенсивный курс, 20 занятий по 120 мин', 'Образование', null, null, '00000000-0000-0000-0001-66fd038a7d01', '00000000-0000-0000-0001-389ebdb97ac2', 85000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-c3c4a32715ee', 'подготовка'), ('00000000-0000-0000-0001-c3c4a32715ee', 'ielts'), ('00000000-0000-0000-0001-c3c4a32715ee', 'образование');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-2a8dcb00da28', now(), now(), '00000000-0000-0000-0001-b4951ac1f8d8', '00000000-0000-0000-0001-41ca4d0f9374', 'ONLINE', 85000, 2400, 'Утро 09:00 или вечер 18:00', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-f941961c9046', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-2a8dcb00da28', 'Подготовка к IELTS', 'Интенсивный курс, 20 занятий по 120 мин', 'Образование', null, null, '00000000-0000-0000-0001-66fd038a7d01', '00000000-0000-0000-0001-41ca4d0f9374', 85000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-f941961c9046', 'подготовка'), ('00000000-0000-0000-0001-f941961c9046', 'ielts'), ('00000000-0000-0000-0001-f941961c9046', 'образование');

-- Компания-Услуга-13 (ИП, Спорт и фитнес, Кызылорда)
INSERT INTO business (id, created_at, updated_at, name, legal_name, bin, status) VALUES
  ('00000000-0000-0000-0001-a4a8fb0f11ae', now(), now(), 'Компания-Услуга-13', 'ИП "Компания-Услуга-13"', '456789012345', 'ACTIVE');
INSERT INTO business_branch (id, created_at, updated_at, business_id, city_id, name, address, latitude, longitude, online_only, status) VALUES
  ('00000000-0000-0000-0001-b295c1ae2c83', now(), now(), '00000000-0000-0000-0001-a4a8fb0f11ae', '00000000-0000-0000-0000-0000000000c1', 'Компания-Услуга-13 - Офис', 'ул. Сыганак, 45', 44.8532, 65.5130, false, 'ACTIVE');
INSERT INTO data_source (id, created_at, updated_at, business_id, source_type, name, status) VALUES
  ('00000000-0000-0000-0001-af8e81640b35', now(), now(), '00000000-0000-0000-0001-a4a8fb0f11ae', 'MANUAL', 'Ручной ввод Компания-Услуга-13', 'ACTIVE');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-ff8fe182ea5b', now(), now(), '00000000-0000-0000-0001-a4a8fb0f11ae', '00000000-0000-0000-0001-9f59c39873da', 'Персональная тренировка', 'Индивидуальное занятие с тренером, 60 мин', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-e15ce589ef17', now(), now(), '00000000-0000-0000-0001-ff8fe182ea5b', '00000000-0000-0000-0001-b295c1ae2c83', 'ONLINE', 5000, 60, 'Ежедневно 06:00-23:00', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-07eed3472203', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-e15ce589ef17', 'Персональная тренировка', 'Индивидуальное занятие с тренером, 60 мин', 'Спорт и фитнес', null, null, '00000000-0000-0000-0001-a4a8fb0f11ae', '00000000-0000-0000-0001-b295c1ae2c83', 5000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-07eed3472203', 'персональная'), ('00000000-0000-0000-0001-07eed3472203', 'тренировка'), ('00000000-0000-0000-0001-07eed3472203', 'спорт'), ('00000000-0000-0000-0001-07eed3472203', 'фитнес');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-6cd34b4755fe', now(), now(), '00000000-0000-0000-0001-a4a8fb0f11ae', '00000000-0000-0000-0001-9f59c39873da', 'Абонемент в тренажерный зал', 'Месячный абонемент безлимит', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-51b720899a47', now(), now(), '00000000-0000-0000-0001-6cd34b4755fe', '00000000-0000-0000-0001-b295c1ae2c83', 'ONLINE', 25000, 43200, 'Ежедневно 06:00-23:00', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-9514dcb43e64', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-51b720899a47', 'Абонемент в тренажерный зал', 'Месячный абонемент безлимит', 'Спорт и фитнес', null, null, '00000000-0000-0000-0001-a4a8fb0f11ae', '00000000-0000-0000-0001-b295c1ae2c83', 25000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-9514dcb43e64', 'абонемент'), ('00000000-0000-0000-0001-9514dcb43e64', 'тренажерный'), ('00000000-0000-0000-0001-9514dcb43e64', 'зал'), ('00000000-0000-0000-0001-9514dcb43e64', 'спорт'), ('00000000-0000-0000-0001-9514dcb43e64', 'фитнес');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-b9fc395d902c', now(), now(), '00000000-0000-0000-0001-a4a8fb0f11ae', '00000000-0000-0000-0001-9f59c39873da', 'Занятие по кроссфиту', 'Групповое занятие, 90 мин', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-4050b5422dfc', now(), now(), '00000000-0000-0000-0001-b9fc395d902c', '00000000-0000-0000-0001-b295c1ae2c83', 'ON_SITE', 3500, 90, 'Пн-Ср-Пт 07:00, 19:00', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-132fc2797961', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-4050b5422dfc', 'Занятие по кроссфиту', 'Групповое занятие, 90 мин', 'Спорт и фитнес', null, null, '00000000-0000-0000-0001-a4a8fb0f11ae', '00000000-0000-0000-0001-b295c1ae2c83', 3500, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-132fc2797961', 'занятие'), ('00000000-0000-0000-0001-132fc2797961', 'кроссфиту'), ('00000000-0000-0000-0001-132fc2797961', 'спорт'), ('00000000-0000-0000-0001-132fc2797961', 'фитнес');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-35193fb6a847', now(), now(), '00000000-0000-0000-0001-a4a8fb0f11ae', '00000000-0000-0000-0001-9f59c39873da', 'Плавание дети 6-12 лет', 'Групповое занятие с тренером, 45 мин', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-e5a0dc6e1e88', now(), now(), '00000000-0000-0000-0001-35193fb6a847', '00000000-0000-0000-0001-b295c1ae2c83', 'ONLINE', 3000, 45, 'Сб-Вс 10:00-14:00', false, 'ACTIVE');

-- Компания-Услуга-14 (ТОО, IT услуги, Алматы)
INSERT INTO business (id, created_at, updated_at, name, legal_name, bin, status) VALUES
  ('00000000-0000-0000-0001-f3b9ed5385eb', now(), now(), 'Компания-Услуга-14', 'ТОО "Компания-Услуга-14"', '567890123456', 'ACTIVE');
INSERT INTO business_branch (id, created_at, updated_at, business_id, city_id, name, address, latitude, longitude, online_only, status) VALUES
  ('00000000-0000-0000-0001-ef956de55b73', now(), now(), '00000000-0000-0000-0001-f3b9ed5385eb', '00000000-0000-0000-0000-0000000000c2', 'Компания-Услуга-14 - Офис', 'мкр. Орбита-3, 12', 44.8542, 65.5140, false, 'ACTIVE');
INSERT INTO data_source (id, created_at, updated_at, business_id, source_type, name, status) VALUES
  ('00000000-0000-0000-0001-487dc2386d2d', now(), now(), '00000000-0000-0000-0001-f3b9ed5385eb', 'MANUAL', 'Ручной ввод Компания-Услуга-14', 'ACTIVE');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-6a034671dda1', now(), now(), '00000000-0000-0000-0001-f3b9ed5385eb', '00000000-0000-0000-0001-a686baabac2c', 'Разработка сайта-визитки', 'Лендинг до 5 страниц, адаптивный дизайн', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-3a21b8a49757', now(), now(), '00000000-0000-0000-0001-6a034671dda1', '00000000-0000-0000-0001-ef956de55b73', 'ONLINE', 150000, 10080, 'По согласованию', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-b974e2b0ab65', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-3a21b8a49757', 'Разработка сайта-визитки', 'Лендинг до 5 страниц, адаптивный дизайн', 'IT услуги', null, null, '00000000-0000-0000-0001-f3b9ed5385eb', '00000000-0000-0000-0001-ef956de55b73', 150000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-b974e2b0ab65', 'разработка'), ('00000000-0000-0000-0001-b974e2b0ab65', 'сайта'), ('00000000-0000-0000-0001-b974e2b0ab65', 'визитки'), ('00000000-0000-0000-0001-b974e2b0ab65', 'услуги');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-1f4f14ebe51b', now(), now(), '00000000-0000-0000-0001-f3b9ed5385eb', '00000000-0000-0000-0001-a686baabac2c', 'Настройка таргет рекламы', 'Настройка и ведение рекламы в Instagram/Facebook', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-85f2a942d505', now(), now(), '00000000-0000-0000-0001-1f4f14ebe51b', '00000000-0000-0000-0001-ef956de55b73', 'ON_SITE', 50000, 10080, 'Рабочие дни', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-f44b8ae04b30', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-85f2a942d505', 'Настройка таргет рекламы', 'Настройка и ведение рекламы в Instagram/Facebook', 'IT услуги', null, null, '00000000-0000-0000-0001-f3b9ed5385eb', '00000000-0000-0000-0001-ef956de55b73', 50000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-f44b8ae04b30', 'настройка'), ('00000000-0000-0000-0001-f44b8ae04b30', 'таргет'), ('00000000-0000-0000-0001-f44b8ae04b30', 'рекламы'), ('00000000-0000-0000-0001-f44b8ae04b30', 'услуги');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-bac2e243cc8f', now(), now(), '00000000-0000-0000-0001-f3b9ed5385eb', '00000000-0000-0000-0001-a686baabac2c', 'SEO продвижение сайта', 'Аудит + базовая оптимизация, ежемесячно', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-b7e1ad9bad9a', now(), now(), '00000000-0000-0000-0001-bac2e243cc8f', '00000000-0000-0000-0001-ef956de55b73', 'ONLINE', 80000, 43200, 'По согласованию', false, 'ACTIVE');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-627e1d561a32', now(), now(), '00000000-0000-0000-0001-f3b9ed5385eb', '00000000-0000-0000-0001-a686baabac2c', 'Ремонт ноутбука', 'Диагностика + замена комплектующих', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-5657a46dd8ed', now(), now(), '00000000-0000-0000-0001-627e1d561a32', '00000000-0000-0000-0001-ef956de55b73', 'ONLINE', 5000, 60, 'Пн-Сб 10:00-19:00', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-b3a759cb8380', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-5657a46dd8ed', 'Ремонт ноутбука', 'Диагностика + замена комплектующих', 'IT услуги', null, null, '00000000-0000-0000-0001-f3b9ed5385eb', '00000000-0000-0000-0001-ef956de55b73', 5000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-b3a759cb8380', 'ремонт'), ('00000000-0000-0000-0001-b3a759cb8380', 'ноутбука'), ('00000000-0000-0000-0001-b3a759cb8380', 'услуги');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-d9e229d0ee67', now(), now(), '00000000-0000-0000-0001-f3b9ed5385eb', '00000000-0000-0000-0001-a686baabac2c', 'Установка и настройка 1С', 'Установка, настройка, обучение персонала', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-e9017e649afb', now(), now(), '00000000-0000-0000-0001-d9e229d0ee67', '00000000-0000-0000-0001-ef956de55b73', 'ON_SITE', 75000, 7200, 'По согласованию', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-36feff30c8e7', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-e9017e649afb', 'Установка и настройка 1С', 'Установка, настройка, обучение персонала', 'IT услуги', null, null, '00000000-0000-0000-0001-f3b9ed5385eb', '00000000-0000-0000-0001-ef956de55b73', 75000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-36feff30c8e7', 'установка'), ('00000000-0000-0000-0001-36feff30c8e7', 'настройка'), ('00000000-0000-0000-0001-36feff30c8e7', 'услуги');

-- Компания-Услуга-15 (ИП, Автозапчасти, Астана)
INSERT INTO business (id, created_at, updated_at, name, legal_name, bin, status) VALUES
  ('00000000-0000-0000-0001-cabdd829991f', now(), now(), 'Компания-Услуга-15', 'ИП "Компания-Услуга-15"', '678901234567', 'ACTIVE');
INSERT INTO business_branch (id, created_at, updated_at, business_id, city_id, name, address, latitude, longitude, online_only, status) VALUES
  ('00000000-0000-0000-0001-5503d0248a70', now(), now(), '00000000-0000-0000-0001-cabdd829991f', '00000000-0000-0000-0000-0000000000c3', 'Компания-Услуга-15 - Офис', 'ул. Калдаякова, 17', 44.8552, 65.5150, false, 'ACTIVE');
INSERT INTO data_source (id, created_at, updated_at, business_id, source_type, name, status) VALUES
  ('00000000-0000-0000-0001-0582a9d9fa93', now(), now(), '00000000-0000-0000-0001-cabdd829991f', 'MANUAL', 'Ручной ввод Компания-Услуга-15', 'ACTIVE');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-126ae57462d0', now(), now(), '00000000-0000-0000-0001-cabdd829991f', '00000000-0000-0000-0000-0000000000a1', 'Курс видеомонтажа DaVinci', 'Основы цветокоррекции и монтажа, 10 занятий', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-3041a3d6702b', now(), now(), '00000000-0000-0000-0001-126ae57462d0', '00000000-0000-0000-0001-5503d0248a70', 'ON_SITE', 35000, 600, 'Вт-Чт 18:00-20:00', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-38682bd63dbb', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-3041a3d6702b', 'Курс видеомонтажа DaVinci', 'Основы цветокоррекции и монтажа, 10 занятий', 'Автозапчасти', null, null, '00000000-0000-0000-0001-cabdd829991f', '00000000-0000-0000-0001-5503d0248a70', 35000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-38682bd63dbb', 'курс'), ('00000000-0000-0000-0001-38682bd63dbb', 'видеомонтажа'), ('00000000-0000-0000-0001-38682bd63dbb', 'davinci'), ('00000000-0000-0000-0001-38682bd63dbb', 'автозапчасти');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-cc37a7bc17c1', now(), now(), '00000000-0000-0000-0001-cabdd829991f', '00000000-0000-0000-0000-0000000000a1', 'Персональная тренировка', 'Индивидуальное занятие с тренером, 60 мин', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-d83c6abc957a', now(), now(), '00000000-0000-0000-0001-cc37a7bc17c1', '00000000-0000-0000-0001-5503d0248a70', 'ONLINE', 5000, 60, 'Ежедневно 06:00-23:00', false, 'ACTIVE');

-- Компания-Услуга-16 (ТОО, Бытовая техника, Шымкент)
INSERT INTO business (id, created_at, updated_at, name, legal_name, bin, status) VALUES
  ('00000000-0000-0000-0001-409b197a635a', now(), now(), 'Компания-Услуга-16', 'ТОО "Компания-Услуга-16"', '789012345678', 'ACTIVE');
INSERT INTO business_branch (id, created_at, updated_at, business_id, city_id, name, address, latitude, longitude, online_only, status) VALUES
  ('00000000-0000-0000-0001-0b6187519b14', now(), now(), '00000000-0000-0000-0001-409b197a635a', '00000000-0000-0000-0001-1a904d507278', 'Компания-Услуга-16 - Офис', 'ул. Байтурсынова, 67', 44.8562, 65.5160, false, 'ACTIVE');
INSERT INTO data_source (id, created_at, updated_at, business_id, source_type, name, status) VALUES
  ('00000000-0000-0000-0001-9ffd6a3b87a1', now(), now(), '00000000-0000-0000-0001-409b197a635a', 'MANUAL', 'Ручной ввод Компания-Услуга-16', 'ACTIVE');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-1a55f44feab9', now(), now(), '00000000-0000-0000-0001-409b197a635a', '00000000-0000-0000-0000-0000000000a2', 'Установка и настройка 1С', 'Установка, настройка, обучение персонала', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-87d4ac9939b9', now(), now(), '00000000-0000-0000-0001-1a55f44feab9', '00000000-0000-0000-0001-0b6187519b14', 'ONLINE', 75000, 7200, 'По согласованию', false, 'ACTIVE');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-ae895d737149', now(), now(), '00000000-0000-0000-0001-409b197a635a', '00000000-0000-0000-0000-0000000000a2', 'Ремонт стиральной машины', 'Диагностика + ремонт на месте', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-dec8dc62b2fe', now(), now(), '00000000-0000-0000-0001-ae895d737149', '00000000-0000-0000-0001-0b6187519b14', 'ONLINE', 5000, 60, 'Пн-Сб 09:00-18:00', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-e04ca9764ff8', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-dec8dc62b2fe', 'Ремонт стиральной машины', 'Диагностика + ремонт на месте', 'Бытовая техника', null, null, '00000000-0000-0000-0001-409b197a635a', '00000000-0000-0000-0001-0b6187519b14', 5000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-e04ca9764ff8', 'ремонт'), ('00000000-0000-0000-0001-e04ca9764ff8', 'стиральной'), ('00000000-0000-0000-0001-e04ca9764ff8', 'машины'), ('00000000-0000-0000-0001-e04ca9764ff8', 'бытовая'), ('00000000-0000-0000-0001-e04ca9764ff8', 'техника');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-8efe9988e09c', now(), now(), '00000000-0000-0000-0001-409b197a635a', '00000000-0000-0000-0000-0000000000a2', 'Установка кондиционера', 'Монтаж сплит-системы под ключ', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-3e958dc12256', now(), now(), '00000000-0000-0000-0001-8efe9988e09c', '00000000-0000-0000-0001-0b6187519b14', 'ON_SITE', 25000, 180, 'Ежедневно, по записи', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-d95cb48cabab', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-3e958dc12256', 'Установка кондиционера', 'Монтаж сплит-системы под ключ', 'Бытовая техника', null, null, '00000000-0000-0000-0001-409b197a635a', '00000000-0000-0000-0001-0b6187519b14', 25000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-d95cb48cabab', 'установка'), ('00000000-0000-0000-0001-d95cb48cabab', 'кондиционера'), ('00000000-0000-0000-0001-d95cb48cabab', 'бытовая'), ('00000000-0000-0000-0001-d95cb48cabab', 'техника');

-- Компания-Услуга-17 (ИП, Услуги красоты, Караганда)
INSERT INTO business (id, created_at, updated_at, name, legal_name, bin, status) VALUES
  ('00000000-0000-0000-0001-4ad1e70dd05c', now(), now(), 'Компания-Услуга-17', 'ИП "Компания-Услуга-17"', '890123456789', 'ACTIVE');
INSERT INTO business_branch (id, created_at, updated_at, business_id, city_id, name, address, latitude, longitude, online_only, status) VALUES
  ('00000000-0000-0000-0001-b8b61750efa8', now(), now(), '00000000-0000-0000-0001-4ad1e70dd05c', '00000000-0000-0000-0001-02ba3c39b41d', 'Компания-Услуга-17 - Офис', 'мкр. Гульдер, 7', 44.8572, 65.5170, false, 'ACTIVE');
INSERT INTO data_source (id, created_at, updated_at, business_id, source_type, name, status) VALUES
  ('00000000-0000-0000-0001-69d5d51e83bc', now(), now(), '00000000-0000-0000-0001-4ad1e70dd05c', 'MANUAL', 'Ручной ввод Компания-Услуга-17', 'ACTIVE');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-80907fe0341d', now(), now(), '00000000-0000-0000-0001-4ad1e70dd05c', '00000000-0000-0000-0000-0000000000a3', 'Стрижка женская модельная', 'Стрижка с укладкой и уходом, 60 мин', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-1c101bb83ffc', now(), now(), '00000000-0000-0000-0001-80907fe0341d', '00000000-0000-0000-0001-b8b61750efa8', 'ONLINE', 6000, 60, 'Ежедневно 10:00-20:00', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-ff91d20daf2e', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-1c101bb83ffc', 'Стрижка женская модельная', 'Стрижка с укладкой и уходом, 60 мин', 'Услуги красоты', null, null, '00000000-0000-0000-0001-4ad1e70dd05c', '00000000-0000-0000-0001-b8b61750efa8', 6000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-ff91d20daf2e', 'стрижка'), ('00000000-0000-0000-0001-ff91d20daf2e', 'женская'), ('00000000-0000-0000-0001-ff91d20daf2e', 'модельная'), ('00000000-0000-0000-0001-ff91d20daf2e', 'услуги'), ('00000000-0000-0000-0001-ff91d20daf2e', 'красоты');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-a7ab0ba3d2de', now(), now(), '00000000-0000-0000-0001-4ad1e70dd05c', '00000000-0000-0000-0000-0000000000a3', 'Маникюр аппаратный', 'Аппаратный маникюр + покрытие гель-лак', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-4428b22e0880', now(), now(), '00000000-0000-0000-0001-a7ab0ba3d2de', '00000000-0000-0000-0001-b8b61750efa8', 'ON_SITE', 5500, 75, 'Пн-Сб 09:00-18:00', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-99a2c68a84e3', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-4428b22e0880', 'Маникюр аппаратный', 'Аппаратный маникюр + покрытие гель-лак', 'Услуги красоты', null, null, '00000000-0000-0000-0001-4ad1e70dd05c', '00000000-0000-0000-0001-b8b61750efa8', 5500, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-99a2c68a84e3', 'маникюр'), ('00000000-0000-0000-0001-99a2c68a84e3', 'аппаратный'), ('00000000-0000-0000-0001-99a2c68a84e3', 'услуги'), ('00000000-0000-0000-0001-99a2c68a84e3', 'красоты');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-5934851e5746', now(), now(), '00000000-0000-0000-0001-4ad1e70dd05c', '00000000-0000-0000-0000-0000000000a3', 'Педикюр классический', 'Классический педикюр с покрытием', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-4e6c406ec510', now(), now(), '00000000-0000-0000-0001-5934851e5746', '00000000-0000-0000-0001-b8b61750efa8', 'ONLINE', 7000, 90, 'Ежедневно 10:00-20:00', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-96a09e44de60', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-4e6c406ec510', 'Педикюр классический', 'Классический педикюр с покрытием', 'Услуги красоты', null, null, '00000000-0000-0000-0001-4ad1e70dd05c', '00000000-0000-0000-0001-b8b61750efa8', 7000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-96a09e44de60', 'педикюр'), ('00000000-0000-0000-0001-96a09e44de60', 'классический'), ('00000000-0000-0000-0001-96a09e44de60', 'услуги'), ('00000000-0000-0000-0001-96a09e44de60', 'красоты');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-e3cea60aeea5', now(), now(), '00000000-0000-0000-0001-4ad1e70dd05c', '00000000-0000-0000-0000-0000000000a3', 'Наращивание ресниц 2D', 'Классическое наращивание, эффект 4 недели', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-5132bf56ff70', now(), now(), '00000000-0000-0000-0001-e3cea60aeea5', '00000000-0000-0000-0001-b8b61750efa8', 'ONLINE', 10000, 120, 'Пн-Сб 09:00-19:00', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-de23937b106f', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-5132bf56ff70', 'Наращивание ресниц 2D', 'Классическое наращивание, эффект 4 недели', 'Услуги красоты', null, null, '00000000-0000-0000-0001-4ad1e70dd05c', '00000000-0000-0000-0001-b8b61750efa8', 10000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-de23937b106f', 'наращивание'), ('00000000-0000-0000-0001-de23937b106f', 'ресниц'), ('00000000-0000-0000-0001-de23937b106f', 'услуги'), ('00000000-0000-0000-0001-de23937b106f', 'красоты');

-- Компания-Услуга-18 (ТОО, Ремонт и сервис, Кызылорда)
INSERT INTO business (id, created_at, updated_at, name, legal_name, bin, status) VALUES
  ('00000000-0000-0000-0001-92d5338264ba', now(), now(), 'Компания-Услуга-18', 'ТОО "Компания-Услуга-18"', '901234567890', 'ACTIVE');
INSERT INTO business_branch (id, created_at, updated_at, business_id, city_id, name, address, latitude, longitude, online_only, status) VALUES
  ('00000000-0000-0000-0001-5fc62b7b0bcb', now(), now(), '00000000-0000-0000-0001-92d5338264ba', '00000000-0000-0000-0000-0000000000c1', 'Компания-Услуга-18 - Филиал 1', 'пр. Абая, 18', 44.8582, 65.5180, true, 'ACTIVE');
INSERT INTO business_branch (id, created_at, updated_at, business_id, city_id, name, address, latitude, longitude, online_only, status) VALUES
  ('00000000-0000-0000-0001-7ad940124fd3', now(), now(), '00000000-0000-0000-0001-92d5338264ba', '00000000-0000-0000-0000-0000000000c1', 'Компания-Услуга-18 - Филиал 2', 'пр. Абая, 18', 44.8582, 65.5180, false, 'ACTIVE');
INSERT INTO data_source (id, created_at, updated_at, business_id, source_type, name, status) VALUES
  ('00000000-0000-0000-0001-7acbaf1b4867', now(), now(), '00000000-0000-0000-0001-92d5338264ba', 'MANUAL', 'Ручной ввод Компания-Услуга-18', 'ACTIVE');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-d793aa9af06a', now(), now(), '00000000-0000-0000-0001-92d5338264ba', '00000000-0000-0000-0000-0000000000a4', 'Ремонт стиральной машины', 'Диагностика + ремонт на месте', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-75f878599ac4', now(), now(), '00000000-0000-0000-0001-d793aa9af06a', '00000000-0000-0000-0001-5fc62b7b0bcb', 'ON_SITE', 5000, 60, 'Пн-Сб 09:00-18:00', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-b6efc14e8393', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-75f878599ac4', 'Ремонт стиральной машины', 'Диагностика + ремонт на месте', 'Ремонт и сервис', null, null, '00000000-0000-0000-0001-92d5338264ba', '00000000-0000-0000-0001-5fc62b7b0bcb', 5000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-b6efc14e8393', 'ремонт'), ('00000000-0000-0000-0001-b6efc14e8393', 'стиральной'), ('00000000-0000-0000-0001-b6efc14e8393', 'машины'), ('00000000-0000-0000-0001-b6efc14e8393', 'ремонт'), ('00000000-0000-0000-0001-b6efc14e8393', 'сервис');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-9b507695439b', now(), now(), '00000000-0000-0000-0001-d793aa9af06a', '00000000-0000-0000-0001-7ad940124fd3', 'ON_SITE', 5000, 60, 'Пн-Сб 09:00-18:00', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-d0aaa2f2933b', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-9b507695439b', 'Ремонт стиральной машины', 'Диагностика + ремонт на месте', 'Ремонт и сервис', null, null, '00000000-0000-0000-0001-92d5338264ba', '00000000-0000-0000-0001-7ad940124fd3', 5000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-d0aaa2f2933b', 'ремонт'), ('00000000-0000-0000-0001-d0aaa2f2933b', 'стиральной'), ('00000000-0000-0000-0001-d0aaa2f2933b', 'машины'), ('00000000-0000-0000-0001-d0aaa2f2933b', 'ремонт'), ('00000000-0000-0000-0001-d0aaa2f2933b', 'сервис');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-b3fe09b058b2', now(), now(), '00000000-0000-0000-0001-92d5338264ba', '00000000-0000-0000-0000-0000000000a4', 'Установка кондиционера', 'Монтаж сплит-системы под ключ', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-2513565d99b3', now(), now(), '00000000-0000-0000-0001-b3fe09b058b2', '00000000-0000-0000-0001-5fc62b7b0bcb', 'ONLINE', 25000, 180, 'Ежедневно, по записи', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-4c6b3bb17a79', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-2513565d99b3', 'Установка кондиционера', 'Монтаж сплит-системы под ключ', 'Ремонт и сервис', null, null, '00000000-0000-0000-0001-92d5338264ba', '00000000-0000-0000-0001-5fc62b7b0bcb', 25000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-4c6b3bb17a79', 'установка'), ('00000000-0000-0000-0001-4c6b3bb17a79', 'кондиционера'), ('00000000-0000-0000-0001-4c6b3bb17a79', 'ремонт'), ('00000000-0000-0000-0001-4c6b3bb17a79', 'сервис');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-93392faa371c', now(), now(), '00000000-0000-0000-0001-b3fe09b058b2', '00000000-0000-0000-0001-7ad940124fd3', 'ONLINE', 25000, 180, 'Ежедневно, по записи', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-86699252dd6f', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-93392faa371c', 'Установка кондиционера', 'Монтаж сплит-системы под ключ', 'Ремонт и сервис', null, null, '00000000-0000-0000-0001-92d5338264ba', '00000000-0000-0000-0001-7ad940124fd3', 25000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-86699252dd6f', 'установка'), ('00000000-0000-0000-0001-86699252dd6f', 'кондиционера'), ('00000000-0000-0000-0001-86699252dd6f', 'ремонт'), ('00000000-0000-0000-0001-86699252dd6f', 'сервис');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-06bb228626b9', now(), now(), '00000000-0000-0000-0001-92d5338264ba', '00000000-0000-0000-0000-0000000000a4', 'Замена электропроводки', 'Полная замена в 2-комнатной квартире', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-4d12e0c9c5ef', now(), now(), '00000000-0000-0000-0001-06bb228626b9', '00000000-0000-0000-0001-5fc62b7b0bcb', 'ONLINE', 180000, 1440, 'По согласованию', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-f55cd90e07a2', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-4d12e0c9c5ef', 'Замена электропроводки', 'Полная замена в 2-комнатной квартире', 'Ремонт и сервис', null, null, '00000000-0000-0000-0001-92d5338264ba', '00000000-0000-0000-0001-5fc62b7b0bcb', 180000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-f55cd90e07a2', 'замена'), ('00000000-0000-0000-0001-f55cd90e07a2', 'электропроводки'), ('00000000-0000-0000-0001-f55cd90e07a2', 'ремонт'), ('00000000-0000-0000-0001-f55cd90e07a2', 'сервис');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-04e7a60149ad', now(), now(), '00000000-0000-0000-0001-06bb228626b9', '00000000-0000-0000-0001-7ad940124fd3', 'ONLINE', 180000, 1440, 'По согласованию', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-9d69d098b8cb', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-04e7a60149ad', 'Замена электропроводки', 'Полная замена в 2-комнатной квартире', 'Ремонт и сервис', null, null, '00000000-0000-0000-0001-92d5338264ba', '00000000-0000-0000-0001-7ad940124fd3', 180000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-9d69d098b8cb', 'замена'), ('00000000-0000-0000-0001-9d69d098b8cb', 'электропроводки'), ('00000000-0000-0000-0001-9d69d098b8cb', 'ремонт'), ('00000000-0000-0000-0001-9d69d098b8cb', 'сервис');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-4d5010918bf1', now(), now(), '00000000-0000-0000-0001-92d5338264ba', '00000000-0000-0000-0000-0000000000a4', 'Сантехнические работы', 'Установка смесителя, ремонт бачка, прочистка', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-10e70eb2b3a2', now(), now(), '00000000-0000-0000-0001-4d5010918bf1', '00000000-0000-0000-0001-5fc62b7b0bcb', 'ON_SITE', 7000, 90, 'Ежедневно 08:00-20:00', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-38f4df24d5ac', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-10e70eb2b3a2', 'Сантехнические работы', 'Установка смесителя, ремонт бачка, прочистка', 'Ремонт и сервис', null, null, '00000000-0000-0000-0001-92d5338264ba', '00000000-0000-0000-0001-5fc62b7b0bcb', 7000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-38f4df24d5ac', 'сантехнические'), ('00000000-0000-0000-0001-38f4df24d5ac', 'работы'), ('00000000-0000-0000-0001-38f4df24d5ac', 'ремонт'), ('00000000-0000-0000-0001-38f4df24d5ac', 'сервис');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-98da2be942b8', now(), now(), '00000000-0000-0000-0001-4d5010918bf1', '00000000-0000-0000-0001-7ad940124fd3', 'ON_SITE', 7000, 90, 'Ежедневно 08:00-20:00', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-66552d850dac', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-98da2be942b8', 'Сантехнические работы', 'Установка смесителя, ремонт бачка, прочистка', 'Ремонт и сервис', null, null, '00000000-0000-0000-0001-92d5338264ba', '00000000-0000-0000-0001-7ad940124fd3', 7000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-66552d850dac', 'сантехнические'), ('00000000-0000-0000-0001-66552d850dac', 'работы'), ('00000000-0000-0000-0001-66552d850dac', 'ремонт'), ('00000000-0000-0000-0001-66552d850dac', 'сервис');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-cb5cda20b63f', now(), now(), '00000000-0000-0000-0001-92d5338264ba', '00000000-0000-0000-0000-0000000000a4', 'Ремонт стиральной машины', 'Диагностика + ремонт на месте', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-383b1e37792a', now(), now(), '00000000-0000-0000-0001-cb5cda20b63f', '00000000-0000-0000-0001-5fc62b7b0bcb', 'ONLINE', 5000, 60, 'Пн-Сб 09:00-18:00', true, 'ARCHIVED');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-f55f00b8c23f', now(), now(), '00000000-0000-0000-0001-cb5cda20b63f', '00000000-0000-0000-0001-7ad940124fd3', 'ONLINE', 5000, 60, 'Пн-Сб 09:00-18:00', true, 'ARCHIVED');

-- Компания-Услуга-19 (ИП, Строительство, Алматы)
INSERT INTO business (id, created_at, updated_at, name, legal_name, bin, status) VALUES
  ('00000000-0000-0000-0001-1b5d259969d9', now(), now(), 'Компания-Услуга-19', 'ИП "Компания-Услуга-19"', '012345678901', 'ACTIVE');
INSERT INTO business_branch (id, created_at, updated_at, business_id, city_id, name, address, latitude, longitude, online_only, status) VALUES
  ('00000000-0000-0000-0001-34199510ad08', now(), now(), '00000000-0000-0000-0001-1b5d259969d9', '00000000-0000-0000-0000-0000000000c2', 'Компания-Услуга-19 - Офис', 'пр. Абая, 150', 44.8592, 65.5190, false, 'ACTIVE');
INSERT INTO data_source (id, created_at, updated_at, business_id, source_type, name, status) VALUES
  ('00000000-0000-0000-0001-020b22fcb6d4', now(), now(), '00000000-0000-0000-0001-1b5d259969d9', 'MANUAL', 'Ручной ввод Компания-Услуга-19', 'ACTIVE');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-5abdfaebce3c', now(), now(), '00000000-0000-0000-0001-1b5d259969d9', '00000000-0000-0000-0000-0000000000a5', 'Монтаж гипсокартона', 'Потолок, 1м², включая профиль и саморезы', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-58fdf6af7ef5', now(), now(), '00000000-0000-0000-0001-5abdfaebce3c', '00000000-0000-0000-0001-34199510ad08', 'ONLINE', 3500, 60, 'Ежедневно 08:00-18:00', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-6f773b18526f', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-58fdf6af7ef5', 'Монтаж гипсокартона', 'Потолок, 1м², включая профиль и саморезы', 'Строительство', null, null, '00000000-0000-0000-0001-1b5d259969d9', '00000000-0000-0000-0001-34199510ad08', 3500, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-6f773b18526f', 'монтаж'), ('00000000-0000-0000-0001-6f773b18526f', 'гипсокартона'), ('00000000-0000-0000-0001-6f773b18526f', 'строительство');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-26c90e5bad76', now(), now(), '00000000-0000-0000-0001-1b5d259969d9', '00000000-0000-0000-0000-0000000000a5', 'Укладка ламината', 'Укладка с подложкой, 1м²', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-eea99b1a74da', now(), now(), '00000000-0000-0000-0001-26c90e5bad76', '00000000-0000-0000-0001-34199510ad08', 'ONLINE', 2500, 60, 'Ежедневно 08:00-18:00', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-411f62f8166e', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-eea99b1a74da', 'Укладка ламината', 'Укладка с подложкой, 1м²', 'Строительство', null, null, '00000000-0000-0000-0001-1b5d259969d9', '00000000-0000-0000-0001-34199510ad08', 2500, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-411f62f8166e', 'укладка'), ('00000000-0000-0000-0001-411f62f8166e', 'ламината'), ('00000000-0000-0000-0001-411f62f8166e', 'строительство');

-- Компания-Услуга-20 (ТОО, Продукты питания, Астана)
INSERT INTO business (id, created_at, updated_at, name, legal_name, bin, status) VALUES
  ('00000000-0000-0000-0001-345852dc1927', now(), now(), 'Компания-Услуга-20', 'ТОО "Компания-Услуга-20"', '123456789012', 'ACTIVE');
INSERT INTO business_branch (id, created_at, updated_at, business_id, city_id, name, address, latitude, longitude, online_only, status) VALUES
  ('00000000-0000-0000-0001-ae9a31c58fdf', now(), now(), '00000000-0000-0000-0001-345852dc1927', '00000000-0000-0000-0000-0000000000c3', 'Компания-Услуга-20 - Офис', 'мкр. Чубары, 8', 44.8602, 65.5200, false, 'ACTIVE');
INSERT INTO data_source (id, created_at, updated_at, business_id, source_type, name, status) VALUES
  ('00000000-0000-0000-0001-1828653f2721', now(), now(), '00000000-0000-0000-0001-345852dc1927', 'MANUAL', 'Ручной ввод Компания-Услуга-20', 'ACTIVE');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-90532ab173f5', now(), now(), '00000000-0000-0000-0001-345852dc1927', '00000000-0000-0000-0001-279580d8f28e', 'Кейтеринг на мероприятие', 'Выездное обслуживание до 50 человек', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-56b8e7e3b6f7', now(), now(), '00000000-0000-0000-0001-90532ab173f5', '00000000-0000-0000-0001-ae9a31c58fdf', 'ONLINE', 150000, 360, 'По записи, за 3 дня', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-fb0d04b29acf', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-56b8e7e3b6f7', 'Кейтеринг на мероприятие', 'Выездное обслуживание до 50 человек', 'Продукты питания', null, null, '00000000-0000-0000-0001-345852dc1927', '00000000-0000-0000-0001-ae9a31c58fdf', 150000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-fb0d04b29acf', 'кейтеринг'), ('00000000-0000-0000-0001-fb0d04b29acf', 'мероприятие'), ('00000000-0000-0000-0001-fb0d04b29acf', 'продукты'), ('00000000-0000-0000-0001-fb0d04b29acf', 'питания');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-fad91c29a127', now(), now(), '00000000-0000-0000-0001-345852dc1927', '00000000-0000-0000-0001-279580d8f28e', 'Доставка продуктов на дом', 'Доставка в течение 2 часов по городу', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-e4f932ce8f5c', now(), now(), '00000000-0000-0000-0001-fad91c29a127', '00000000-0000-0000-0001-ae9a31c58fdf', 'ON_SITE', 1500, 120, 'Ежедневно 08:00-22:00', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-6d6ed6fa2e64', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-e4f932ce8f5c', 'Доставка продуктов на дом', 'Доставка в течение 2 часов по городу', 'Продукты питания', null, null, '00000000-0000-0000-0001-345852dc1927', '00000000-0000-0000-0001-ae9a31c58fdf', 1500, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-6d6ed6fa2e64', 'доставка'), ('00000000-0000-0000-0001-6d6ed6fa2e64', 'продуктов'), ('00000000-0000-0000-0001-6d6ed6fa2e64', 'дом'), ('00000000-0000-0000-0001-6d6ed6fa2e64', 'продукты'), ('00000000-0000-0000-0001-6d6ed6fa2e64', 'питания');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-cced43d2c552', now(), now(), '00000000-0000-0000-0001-345852dc1927', '00000000-0000-0000-0001-279580d8f28e', 'Кейтеринг на мероприятие', 'Выездное обслуживание до 50 человек', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-c0fbfe0280e9', now(), now(), '00000000-0000-0000-0001-cced43d2c552', '00000000-0000-0000-0001-ae9a31c58fdf', 'ONLINE', 150000, 360, 'По записи, за 3 дня', true, 'ARCHIVED');

-- Компания-Услуга-21 (ИП, Медицинские услуги, Шымкент)
INSERT INTO business (id, created_at, updated_at, name, legal_name, bin, status) VALUES
  ('00000000-0000-0000-0001-a967e44bcc2d', now(), now(), 'Компания-Услуга-21', 'ИП "Компания-Услуга-21"', '234567890123', 'ACTIVE');
INSERT INTO business_branch (id, created_at, updated_at, business_id, city_id, name, address, latitude, longitude, online_only, status) VALUES
  ('00000000-0000-0000-0001-9ff595bce519', now(), now(), '00000000-0000-0000-0001-a967e44bcc2d', '00000000-0000-0000-0001-1a904d507278', 'Компания-Услуга-21 - Офис', 'пр. Кунаева, 120', 44.8612, 65.5210, false, 'ACTIVE');
INSERT INTO data_source (id, created_at, updated_at, business_id, source_type, name, status) VALUES
  ('00000000-0000-0000-0001-6fe1810ddd6c', now(), now(), '00000000-0000-0000-0001-a967e44bcc2d', 'MANUAL', 'Ручной ввод Компания-Услуга-21', 'ACTIVE');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-cb2146867350', now(), now(), '00000000-0000-0000-0001-a967e44bcc2d', '00000000-0000-0000-0001-81548349b593', 'Консультация терапевта', 'Первичный прием, осмотр, назначение', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-b2fb0b6d4849', now(), now(), '00000000-0000-0000-0001-cb2146867350', '00000000-0000-0000-0001-9ff595bce519', 'ON_SITE', 5000, 30, 'Пн-Пт 08:00-17:00', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-50c403d08752', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-b2fb0b6d4849', 'Консультация терапевта', 'Первичный прием, осмотр, назначение', 'Медицинские услуги', null, null, '00000000-0000-0000-0001-a967e44bcc2d', '00000000-0000-0000-0001-9ff595bce519', 5000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-50c403d08752', 'консультация'), ('00000000-0000-0000-0001-50c403d08752', 'терапевта'), ('00000000-0000-0000-0001-50c403d08752', 'медицинские'), ('00000000-0000-0000-0001-50c403d08752', 'услуги');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-d44084661fe6', now(), now(), '00000000-0000-0000-0001-a967e44bcc2d', '00000000-0000-0000-0001-81548349b593', 'УЗИ брюшной полости', 'Комплексное УЗИ органов брюшной полости', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-476794592726', now(), now(), '00000000-0000-0000-0001-d44084661fe6', '00000000-0000-0000-0001-9ff595bce519', 'ONLINE', 8000, 40, 'Пн-Сб 08:00-14:00', true, 'ARCHIVED');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-5c7a8d9e9e8d', now(), now(), '00000000-0000-0000-0001-a967e44bcc2d', '00000000-0000-0000-0001-81548349b593', 'Анализ крови общий', 'Забор крови + общий анализ, результат за 1 день', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-ccde4071cd31', now(), now(), '00000000-0000-0000-0001-5c7a8d9e9e8d', '00000000-0000-0000-0001-9ff595bce519', 'ONLINE', 3500, 15, 'Пн-Пт 07:30-10:00', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-2e45ee8b7f18', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-ccde4071cd31', 'Анализ крови общий', 'Забор крови + общий анализ, результат за 1 день', 'Медицинские услуги', null, null, '00000000-0000-0000-0001-a967e44bcc2d', '00000000-0000-0000-0001-9ff595bce519', 3500, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-2e45ee8b7f18', 'анализ'), ('00000000-0000-0000-0001-2e45ee8b7f18', 'крови'), ('00000000-0000-0000-0001-2e45ee8b7f18', 'общий'), ('00000000-0000-0000-0001-2e45ee8b7f18', 'медицинские'), ('00000000-0000-0000-0001-2e45ee8b7f18', 'услуги');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-b07d5c7de805', now(), now(), '00000000-0000-0000-0001-a967e44bcc2d', '00000000-0000-0000-0001-81548349b593', 'Прием кардиолога', 'Консультация, ЭКГ, расшифровка', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-f6458c04a665', now(), now(), '00000000-0000-0000-0001-b07d5c7de805', '00000000-0000-0000-0001-9ff595bce519', 'ON_SITE', 7000, 45, 'Пн-Пт 09:00-16:00', false, 'ACTIVE');

-- Компания-Услуга-22 (ТОО, Образование, Караганда)
INSERT INTO business (id, created_at, updated_at, name, legal_name, bin, status) VALUES
  ('00000000-0000-0000-0001-79b499996698', now(), now(), 'Компания-Услуга-22', 'ТОО "Компания-Услуга-22"', '345678901234', 'ACTIVE');
INSERT INTO business_branch (id, created_at, updated_at, business_id, city_id, name, address, latitude, longitude, online_only, status) VALUES
  ('00000000-0000-0000-0001-2593219266db', now(), now(), '00000000-0000-0000-0001-79b499996698', '00000000-0000-0000-0001-02ba3c39b41d', 'Компания-Услуга-22 - Офис', 'ул. Чкалова, 18', 44.8622, 65.5220, false, 'ACTIVE');
INSERT INTO data_source (id, created_at, updated_at, business_id, source_type, name, status) VALUES
  ('00000000-0000-0000-0001-4c5b3eef1803', now(), now(), '00000000-0000-0000-0001-79b499996698', 'MANUAL', 'Ручной ввод Компания-Услуга-22', 'ACTIVE');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-1ef6ece63b9a', now(), now(), '00000000-0000-0000-0001-79b499996698', '00000000-0000-0000-0001-49c510314da7', 'Курс Python базовый', 'Основы программирования на Python, 16 часов', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-1a96f87a173a', now(), now(), '00000000-0000-0000-0001-1ef6ece63b9a', '00000000-0000-0000-0001-2593219266db', 'ONLINE', 45000, 960, 'Группы: утро/вечер', true, 'ARCHIVED');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-c2353d23205f', now(), now(), '00000000-0000-0000-0001-79b499996698', '00000000-0000-0000-0001-49c510314da7', 'Английский язык Intermediate', 'Разговорный курс, 24 занятия по 90 мин', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-e2ac1ce96de0', now(), now(), '00000000-0000-0000-0001-c2353d23205f', '00000000-0000-0000-0001-2593219266db', 'ONLINE', 65000, 1440, 'Пн-Ср-Пт или Вт-Чт-Сб', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-4f64156a42d2', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-e2ac1ce96de0', 'Английский язык Intermediate', 'Разговорный курс, 24 занятия по 90 мин', 'Образование', null, null, '00000000-0000-0000-0001-79b499996698', '00000000-0000-0000-0001-2593219266db', 65000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-4f64156a42d2', 'английский'), ('00000000-0000-0000-0001-4f64156a42d2', 'язык'), ('00000000-0000-0000-0001-4f64156a42d2', 'intermediate'), ('00000000-0000-0000-0001-4f64156a42d2', 'образование');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-0380a2f67c95', now(), now(), '00000000-0000-0000-0001-79b499996698', '00000000-0000-0000-0001-49c510314da7', 'Подготовка к IELTS', 'Интенсивный курс, 20 занятий по 120 мин', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-d547a5428f92', now(), now(), '00000000-0000-0000-0001-0380a2f67c95', '00000000-0000-0000-0001-2593219266db', 'ON_SITE', 85000, 2400, 'Утро 09:00 или вечер 18:00', false, 'ACTIVE');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-813cdcb588b6', now(), now(), '00000000-0000-0000-0001-79b499996698', '00000000-0000-0000-0001-49c510314da7', 'Репетиторство по математике', 'Подготовка к ЕНТ, индивидуально, 60 мин', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-3975c338c0e0', now(), now(), '00000000-0000-0000-0001-813cdcb588b6', '00000000-0000-0000-0001-2593219266db', 'ONLINE', 4000, 60, 'По согласованию', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-b1871a86d2ac', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-3975c338c0e0', 'Репетиторство по математике', 'Подготовка к ЕНТ, индивидуально, 60 мин', 'Образование', null, null, '00000000-0000-0000-0001-79b499996698', '00000000-0000-0000-0001-2593219266db', 4000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-b1871a86d2ac', 'репетиторство'), ('00000000-0000-0000-0001-b1871a86d2ac', 'математике'), ('00000000-0000-0000-0001-b1871a86d2ac', 'образование');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-0aced094bbae', now(), now(), '00000000-0000-0000-0001-79b499996698', '00000000-0000-0000-0001-49c510314da7', 'Курс видеомонтажа DaVinci', 'Основы цветокоррекции и монтажа, 10 занятий', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-8f81c9b4f253', now(), now(), '00000000-0000-0000-0001-0aced094bbae', '00000000-0000-0000-0001-2593219266db', 'ONLINE', 35000, 600, 'Вт-Чт 18:00-20:00', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-bcc60dd40720', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-8f81c9b4f253', 'Курс видеомонтажа DaVinci', 'Основы цветокоррекции и монтажа, 10 занятий', 'Образование', null, null, '00000000-0000-0000-0001-79b499996698', '00000000-0000-0000-0001-2593219266db', 35000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-bcc60dd40720', 'курс'), ('00000000-0000-0000-0001-bcc60dd40720', 'видеомонтажа'), ('00000000-0000-0000-0001-bcc60dd40720', 'davinci'), ('00000000-0000-0000-0001-bcc60dd40720', 'образование');

-- Компания-Услуга-23 (ИП, Спорт и фитнес, Кызылорда)
INSERT INTO business (id, created_at, updated_at, name, legal_name, bin, status) VALUES
  ('00000000-0000-0000-0001-c13b7cf11efe', now(), now(), 'Компания-Услуга-23', 'ИП "Компания-Услуга-23"', '456789012345', 'ACTIVE');
INSERT INTO business_branch (id, created_at, updated_at, business_id, city_id, name, address, latitude, longitude, online_only, status) VALUES
  ('00000000-0000-0000-0001-f741adc3ab1d', now(), now(), '00000000-0000-0000-0001-c13b7cf11efe', '00000000-0000-0000-0000-0000000000c1', 'Компания-Услуга-23 - Офис', 'пр. Назарбаева, 120', 44.8632, 65.5230, false, 'ACTIVE');
INSERT INTO data_source (id, created_at, updated_at, business_id, source_type, name, status) VALUES
  ('00000000-0000-0000-0001-077728303c69', now(), now(), '00000000-0000-0000-0001-c13b7cf11efe', 'MANUAL', 'Ручной ввод Компания-Услуга-23', 'ACTIVE');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-3f1d1fd076ce', now(), now(), '00000000-0000-0000-0001-c13b7cf11efe', '00000000-0000-0000-0001-9f59c39873da', 'Персональная тренировка', 'Индивидуальное занятие с тренером, 60 мин', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-47f772fbdfa8', now(), now(), '00000000-0000-0000-0001-3f1d1fd076ce', '00000000-0000-0000-0001-f741adc3ab1d', 'ONLINE', 5000, 60, 'Ежедневно 06:00-23:00', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-2eee5360e7fd', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-47f772fbdfa8', 'Персональная тренировка', 'Индивидуальное занятие с тренером, 60 мин', 'Спорт и фитнес', null, null, '00000000-0000-0000-0001-c13b7cf11efe', '00000000-0000-0000-0001-f741adc3ab1d', 5000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-2eee5360e7fd', 'персональная'), ('00000000-0000-0000-0001-2eee5360e7fd', 'тренировка'), ('00000000-0000-0000-0001-2eee5360e7fd', 'спорт'), ('00000000-0000-0000-0001-2eee5360e7fd', 'фитнес');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-8ad72030e624', now(), now(), '00000000-0000-0000-0001-c13b7cf11efe', '00000000-0000-0000-0001-9f59c39873da', 'Абонемент в тренажерный зал', 'Месячный абонемент безлимит', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-be97a0619afb', now(), now(), '00000000-0000-0000-0001-8ad72030e624', '00000000-0000-0000-0001-f741adc3ab1d', 'ON_SITE', 25000, 43200, 'Ежедневно 06:00-23:00', false, 'ACTIVE');

-- Компания-Услуга-24 (ТОО, IT услуги, Алматы)
INSERT INTO business (id, created_at, updated_at, name, legal_name, bin, status) VALUES
  ('00000000-0000-0000-0001-38376a7e8af1', now(), now(), 'Компания-Услуга-24', 'ТОО "Компания-Услуга-24"', '567890123456', 'ACTIVE');
INSERT INTO business_branch (id, created_at, updated_at, business_id, city_id, name, address, latitude, longitude, online_only, status) VALUES
  ('00000000-0000-0000-0001-9e4eab4e5542', now(), now(), '00000000-0000-0000-0001-38376a7e8af1', '00000000-0000-0000-0000-0000000000c2', 'Компания-Услуга-24 - Филиал 1', 'пр. Аль-Фараби, 77', 44.8642, 65.5240, false, 'ACTIVE');
INSERT INTO business_branch (id, created_at, updated_at, business_id, city_id, name, address, latitude, longitude, online_only, status) VALUES
  ('00000000-0000-0000-0001-9a8961522bf7', now(), now(), '00000000-0000-0000-0001-38376a7e8af1', '00000000-0000-0000-0000-0000000000c2', 'Компания-Услуга-24 - Филиал 2', 'пр. Аль-Фараби, 77', 44.8642, 65.5240, false, 'ACTIVE');
INSERT INTO data_source (id, created_at, updated_at, business_id, source_type, name, status) VALUES
  ('00000000-0000-0000-0001-43b4324bec73', now(), now(), '00000000-0000-0000-0001-38376a7e8af1', 'MANUAL', 'Ручной ввод Компания-Услуга-24', 'ACTIVE');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-b567929af6f1', now(), now(), '00000000-0000-0000-0001-38376a7e8af1', '00000000-0000-0000-0001-a686baabac2c', 'Разработка сайта-визитки', 'Лендинг до 5 страниц, адаптивный дизайн', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-261e87aec120', now(), now(), '00000000-0000-0000-0001-b567929af6f1', '00000000-0000-0000-0001-9e4eab4e5542', 'ON_SITE', 150000, 10080, 'По согласованию', false, 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-ce0c3b493289', now(), now(), '00000000-0000-0000-0001-b567929af6f1', '00000000-0000-0000-0001-9a8961522bf7', 'ON_SITE', 150000, 10080, 'По согласованию', false, 'ACTIVE');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-5b819d68bd55', now(), now(), '00000000-0000-0000-0001-38376a7e8af1', '00000000-0000-0000-0001-a686baabac2c', 'Настройка таргет рекламы', 'Настройка и ведение рекламы в Instagram/Facebook', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-8e4d9553f9e5', now(), now(), '00000000-0000-0000-0001-5b819d68bd55', '00000000-0000-0000-0001-9e4eab4e5542', 'ONLINE', 50000, 10080, 'Рабочие дни', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-60a648c75e24', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-8e4d9553f9e5', 'Настройка таргет рекламы', 'Настройка и ведение рекламы в Instagram/Facebook', 'IT услуги', null, null, '00000000-0000-0000-0001-38376a7e8af1', '00000000-0000-0000-0001-9e4eab4e5542', 50000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-60a648c75e24', 'настройка'), ('00000000-0000-0000-0001-60a648c75e24', 'таргет'), ('00000000-0000-0000-0001-60a648c75e24', 'рекламы'), ('00000000-0000-0000-0001-60a648c75e24', 'услуги');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-b29d79e97715', now(), now(), '00000000-0000-0000-0001-5b819d68bd55', '00000000-0000-0000-0001-9a8961522bf7', 'ONLINE', 50000, 10080, 'Рабочие дни', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-41a4b574a7fc', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-b29d79e97715', 'Настройка таргет рекламы', 'Настройка и ведение рекламы в Instagram/Facebook', 'IT услуги', null, null, '00000000-0000-0000-0001-38376a7e8af1', '00000000-0000-0000-0001-9a8961522bf7', 50000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-41a4b574a7fc', 'настройка'), ('00000000-0000-0000-0001-41a4b574a7fc', 'таргет'), ('00000000-0000-0000-0001-41a4b574a7fc', 'рекламы'), ('00000000-0000-0000-0001-41a4b574a7fc', 'услуги');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-69ec392ef126', now(), now(), '00000000-0000-0000-0001-38376a7e8af1', '00000000-0000-0000-0001-a686baabac2c', 'SEO продвижение сайта', 'Аудит + базовая оптимизация, ежемесячно', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-c1f5ace4354c', now(), now(), '00000000-0000-0000-0001-69ec392ef126', '00000000-0000-0000-0001-9e4eab4e5542', 'ONLINE', 80000, 43200, 'По согласованию', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-bf57bdfe148b', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-c1f5ace4354c', 'SEO продвижение сайта', 'Аудит + базовая оптимизация, ежемесячно', 'IT услуги', null, null, '00000000-0000-0000-0001-38376a7e8af1', '00000000-0000-0000-0001-9e4eab4e5542', 80000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-bf57bdfe148b', 'seo'), ('00000000-0000-0000-0001-bf57bdfe148b', 'продвижение'), ('00000000-0000-0000-0001-bf57bdfe148b', 'сайта'), ('00000000-0000-0000-0001-bf57bdfe148b', 'услуги');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-890c89a8f9b0', now(), now(), '00000000-0000-0000-0001-69ec392ef126', '00000000-0000-0000-0001-9a8961522bf7', 'ONLINE', 80000, 43200, 'По согласованию', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-572aeb9e0040', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-890c89a8f9b0', 'SEO продвижение сайта', 'Аудит + базовая оптимизация, ежемесячно', 'IT услуги', null, null, '00000000-0000-0000-0001-38376a7e8af1', '00000000-0000-0000-0001-9a8961522bf7', 80000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-572aeb9e0040', 'seo'), ('00000000-0000-0000-0001-572aeb9e0040', 'продвижение'), ('00000000-0000-0000-0001-572aeb9e0040', 'сайта'), ('00000000-0000-0000-0001-572aeb9e0040', 'услуги');

-- Компания-Услуга-25 (ИП, Автозапчасти, Астана)
INSERT INTO business (id, created_at, updated_at, name, legal_name, bin, status) VALUES
  ('00000000-0000-0000-0001-e3a58a7a637c', now(), now(), 'Компания-Услуга-25', 'ИП "Компания-Услуга-25"', '678901234567', 'ACTIVE');
INSERT INTO business_branch (id, created_at, updated_at, business_id, city_id, name, address, latitude, longitude, online_only, status) VALUES
  ('00000000-0000-0000-0001-37c9856e9ec8', now(), now(), '00000000-0000-0000-0001-e3a58a7a637c', '00000000-0000-0000-0000-0000000000c3', 'Компания-Услуга-25 - Офис', 'ул. Сарайшык, 13', 44.8652, 65.5250, false, 'ACTIVE');
INSERT INTO data_source (id, created_at, updated_at, business_id, source_type, name, status) VALUES
  ('00000000-0000-0000-0001-49c7562072d1', now(), now(), '00000000-0000-0000-0001-e3a58a7a637c', 'MANUAL', 'Ручной ввод Компания-Услуга-25', 'ACTIVE');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-995ed0aef98c', now(), now(), '00000000-0000-0000-0001-e3a58a7a637c', '00000000-0000-0000-0000-0000000000a1', 'Подготовка к IELTS', 'Интенсивный курс, 20 занятий по 120 мин', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-c3c6d29262ab', now(), now(), '00000000-0000-0000-0001-995ed0aef98c', '00000000-0000-0000-0001-37c9856e9ec8', 'ONLINE', 85000, 2400, 'Утро 09:00 или вечер 18:00', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-fb3bd25573c6', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-c3c6d29262ab', 'Подготовка к IELTS', 'Интенсивный курс, 20 занятий по 120 мин', 'Автозапчасти', null, null, '00000000-0000-0000-0001-e3a58a7a637c', '00000000-0000-0000-0001-37c9856e9ec8', 85000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-fb3bd25573c6', 'подготовка'), ('00000000-0000-0000-0001-fb3bd25573c6', 'ielts'), ('00000000-0000-0000-0001-fb3bd25573c6', 'автозапчасти');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-770471dd9891', now(), now(), '00000000-0000-0000-0001-e3a58a7a637c', '00000000-0000-0000-0000-0000000000a1', 'Репетиторство по математике', 'Подготовка к ЕНТ, индивидуально, 60 мин', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-209d0253d30e', now(), now(), '00000000-0000-0000-0001-770471dd9891', '00000000-0000-0000-0001-37c9856e9ec8', 'ONLINE', 4000, 60, 'По согласованию', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-b9dd739090e2', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-209d0253d30e', 'Репетиторство по математике', 'Подготовка к ЕНТ, индивидуально, 60 мин', 'Автозапчасти', null, null, '00000000-0000-0000-0001-e3a58a7a637c', '00000000-0000-0000-0001-37c9856e9ec8', 4000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-b9dd739090e2', 'репетиторство'), ('00000000-0000-0000-0001-b9dd739090e2', 'математике'), ('00000000-0000-0000-0001-b9dd739090e2', 'автозапчасти');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-bc19455c25a0', now(), now(), '00000000-0000-0000-0001-e3a58a7a637c', '00000000-0000-0000-0000-0000000000a1', 'Курс видеомонтажа DaVinci', 'Основы цветокоррекции и монтажа, 10 занятий', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-82adfcbf102b', now(), now(), '00000000-0000-0000-0001-bc19455c25a0', '00000000-0000-0000-0001-37c9856e9ec8', 'ON_SITE', 35000, 600, 'Вт-Чт 18:00-20:00', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-c2491b307546', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-82adfcbf102b', 'Курс видеомонтажа DaVinci', 'Основы цветокоррекции и монтажа, 10 занятий', 'Автозапчасти', null, null, '00000000-0000-0000-0001-e3a58a7a637c', '00000000-0000-0000-0001-37c9856e9ec8', 35000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-c2491b307546', 'курс'), ('00000000-0000-0000-0001-c2491b307546', 'видеомонтажа'), ('00000000-0000-0000-0001-c2491b307546', 'davinci'), ('00000000-0000-0000-0001-c2491b307546', 'автозапчасти');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-a80616b94314', now(), now(), '00000000-0000-0000-0001-e3a58a7a637c', '00000000-0000-0000-0000-0000000000a1', 'Персональная тренировка', 'Индивидуальное занятие с тренером, 60 мин', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-fa654a0e6c74', now(), now(), '00000000-0000-0000-0001-a80616b94314', '00000000-0000-0000-0001-37c9856e9ec8', 'ONLINE', 5000, 60, 'Ежедневно 06:00-23:00', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-5a2b19e8d363', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-fa654a0e6c74', 'Персональная тренировка', 'Индивидуальное занятие с тренером, 60 мин', 'Автозапчасти', null, null, '00000000-0000-0000-0001-e3a58a7a637c', '00000000-0000-0000-0001-37c9856e9ec8', 5000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-5a2b19e8d363', 'персональная'), ('00000000-0000-0000-0001-5a2b19e8d363', 'тренировка'), ('00000000-0000-0000-0001-5a2b19e8d363', 'автозапчасти');

-- ==============================
-- MIXED COMPANIES — Products + Services (12)
-- ==============================

-- Компания-Микс-1 (ИП, Строительство, Кызылорда)
INSERT INTO business (id, created_at, updated_at, name, legal_name, bin, status) VALUES
  ('00000000-0000-0000-0001-e975dde95ade', now(), now(), 'Компания-Микс-1', 'ИП "Компания-Микс-1"', '234567890123', 'ACTIVE');
INSERT INTO business_branch (id, created_at, updated_at, business_id, city_id, name, address, latitude, longitude, online_only, status) VALUES
  ('00000000-0000-0000-0001-c6324b0fae75', now(), now(), '00000000-0000-0000-0001-e975dde95ade', '00000000-0000-0000-0000-0000000000c1', 'Компания-Микс-1 - Офис', 'ул. Айтеке би, 5', 44.8412, 65.5010, false, 'ACTIVE');
INSERT INTO data_source (id, created_at, updated_at, business_id, source_type, name, status) VALUES
  ('00000000-0000-0000-0001-07dd132330e5', now(), now(), '00000000-0000-0000-0001-e975dde95ade', 'MANUAL', 'Ручной ввод Компания-Микс-1', 'ACTIVE');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-c355ec2a0702', now(), now(), '00000000-0000-0000-0001-e975dde95ade', '00000000-0000-0000-0000-0000000000a5', 'Строительство', 'Гипсокартон Knauf 12.5мм', 'Влагостойкий лист 2500x1200 мм', 'KNF-GKL-12', '{"бренд":"Knauf","толщина":"12.5мм","тип":"влагостойкий"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-93e90e863da4', now(), now(), '00000000-0000-0000-0001-c355ec2a0702', '00000000-0000-0000-0001-c6324b0fae75', '00000000-0000-0000-0001-07dd132330e5', 4500, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-0dd68a789d1d', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-93e90e863da4', null, 'Гипсокартон Knauf 12.5мм', 'Влагостойкий лист 2500x1200 мм', 'Строительство', 'KNF-GKL-12', '{"бренд":"Knauf","толщина":"12.5мм","тип":"влагостойкий"}', '00000000-0000-0000-0001-e975dde95ade', '00000000-0000-0000-0001-c6324b0fae75', 4500, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-0dd68a789d1d', 'гипсокартон'), ('00000000-0000-0000-0001-0dd68a789d1d', 'knauf'), ('00000000-0000-0000-0001-0dd68a789d1d', '12.5мм'), ('00000000-0000-0000-0001-0dd68a789d1d', 'строительство');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-a460fbeec293', now(), now(), '00000000-0000-0000-0001-e975dde95ade', '00000000-0000-0000-0000-0000000000a5', 'Строительство', 'Профнастил С8 0.45мм', 'Оцинкованный профнастил для забора', 'PRF-C8-045', '{"марка":"С8","толщина":"0.45мм","тип":"оцинкованный"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-c524bb68d122', now(), now(), '00000000-0000-0000-0001-a460fbeec293', '00000000-0000-0000-0001-c6324b0fae75', '00000000-0000-0000-0001-07dd132330e5', 2800, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-5a21cb6b818c', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-c524bb68d122', null, 'Профнастил С8 0.45мм', 'Оцинкованный профнастил для забора', 'Строительство', 'PRF-C8-045', '{"марка":"С8","толщина":"0.45мм","тип":"оцинкованный"}', '00000000-0000-0000-0001-e975dde95ade', '00000000-0000-0000-0001-c6324b0fae75', 2800, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-5a21cb6b818c', 'профнастил'), ('00000000-0000-0000-0001-5a21cb6b818c', '0.45мм'), ('00000000-0000-0000-0001-5a21cb6b818c', 'строительство');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-53156aa8e5fb', now(), now(), '00000000-0000-0000-0001-e975dde95ade', null, 'Строительство', 'Металлочерепица Монтеррей', 'Полиэстер 0.5мм, коричневый', 'MCH-MT-05', '{"тип":"Монтеррей","толщина":"0.5мм","покрытие":"полиэстер"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-ffa40dbc7f20', now(), now(), '00000000-0000-0000-0001-53156aa8e5fb', '00000000-0000-0000-0001-c6324b0fae75', '00000000-0000-0000-0001-07dd132330e5', 5200, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-a0faaaec23ce', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-ffa40dbc7f20', null, 'Металлочерепица Монтеррей', 'Полиэстер 0.5мм, коричневый', 'Строительство', 'MCH-MT-05', '{"тип":"Монтеррей","толщина":"0.5мм","покрытие":"полиэстер"}', '00000000-0000-0000-0001-e975dde95ade', '00000000-0000-0000-0001-c6324b0fae75', 5200, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-a0faaaec23ce', 'металлочерепица'), ('00000000-0000-0000-0001-a0faaaec23ce', 'монтеррей'), ('00000000-0000-0000-0001-a0faaaec23ce', 'строительство');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-60eb26537863', now(), now(), '00000000-0000-0000-0001-e975dde95ade', '00000000-0000-0000-0000-0000000000a5', 'Монтаж гипсокартона', 'Потолок, 1м², включая профиль и саморезы', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-6d32553edf3f', now(), now(), '00000000-0000-0000-0001-60eb26537863', '00000000-0000-0000-0001-c6324b0fae75', 'ONLINE', 3500, 60, 'Ежедневно 08:00-18:00', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-b772cab70543', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-6d32553edf3f', 'Монтаж гипсокартона', 'Потолок, 1м², включая профиль и саморезы', 'Строительство', null, null, '00000000-0000-0000-0001-e975dde95ade', '00000000-0000-0000-0001-c6324b0fae75', 3500, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-b772cab70543', 'монтаж'), ('00000000-0000-0000-0001-b772cab70543', 'гипсокартона'), ('00000000-0000-0000-0001-b772cab70543', 'строительство');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-f833b93a357f', now(), now(), '00000000-0000-0000-0001-e975dde95ade', '00000000-0000-0000-0000-0000000000a5', 'Укладка ламината', 'Укладка с подложкой, 1м²', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-4e4247527b6f', now(), now(), '00000000-0000-0000-0001-f833b93a357f', '00000000-0000-0000-0001-c6324b0fae75', 'ONLINE', 2500, 60, 'Ежедневно 08:00-18:00', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-4a2f8ddb7041', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-4e4247527b6f', 'Укладка ламината', 'Укладка с подложкой, 1м²', 'Строительство', null, null, '00000000-0000-0000-0001-e975dde95ade', '00000000-0000-0000-0001-c6324b0fae75', 2500, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-4a2f8ddb7041', 'укладка'), ('00000000-0000-0000-0001-4a2f8ddb7041', 'ламината'), ('00000000-0000-0000-0001-4a2f8ddb7041', 'строительство');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-eb8f84b5242e', now(), now(), '00000000-0000-0000-0001-e975dde95ade', '00000000-0000-0000-0000-0000000000a5', 'Отделка квартиры под ключ', 'Черновая + чистовая отделка, материал заказчика', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-a1db1b348e1d', now(), now(), '00000000-0000-0000-0001-eb8f84b5242e', '00000000-0000-0000-0001-c6324b0fae75', 'ON_SITE', 1500000, 43200, 'По договору', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-b1cd0386e4f9', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-a1db1b348e1d', 'Отделка квартиры под ключ', 'Черновая + чистовая отделка, материал заказчика', 'Строительство', null, null, '00000000-0000-0000-0001-e975dde95ade', '00000000-0000-0000-0001-c6324b0fae75', 1500000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-b1cd0386e4f9', 'отделка'), ('00000000-0000-0000-0001-b1cd0386e4f9', 'квартиры'), ('00000000-0000-0000-0001-b1cd0386e4f9', 'ключ'), ('00000000-0000-0000-0001-b1cd0386e4f9', 'строительство');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-51343bccd138', now(), now(), '00000000-0000-0000-0001-e975dde95ade', '00000000-0000-0000-0000-0000000000a5', 'Монтаж гипсокартона', 'Потолок, 1м², включая профиль и саморезы', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-f00aca8b6481', now(), now(), '00000000-0000-0000-0001-51343bccd138', '00000000-0000-0000-0001-c6324b0fae75', 'ONLINE', 3500, 60, 'Ежедневно 08:00-18:00', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-9f61a89f28de', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-f00aca8b6481', 'Монтаж гипсокартона', 'Потолок, 1м², включая профиль и саморезы', 'Строительство', null, null, '00000000-0000-0000-0001-e975dde95ade', '00000000-0000-0000-0001-c6324b0fae75', 3500, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-9f61a89f28de', 'монтаж'), ('00000000-0000-0000-0001-9f61a89f28de', 'гипсокартона'), ('00000000-0000-0000-0001-9f61a89f28de', 'строительство');

-- Компания-Микс-2 (ТОО, Продукты питания, Алматы)
INSERT INTO business (id, created_at, updated_at, name, legal_name, bin, status) VALUES
  ('00000000-0000-0000-0001-38cfa1459105', now(), now(), 'Компания-Микс-2', 'ТОО "Компания-Микс-2"', '345678901234', 'ACTIVE');
INSERT INTO business_branch (id, created_at, updated_at, business_id, city_id, name, address, latitude, longitude, online_only, status) VALUES
  ('00000000-0000-0000-0001-c4a0ea4de4bf', now(), now(), '00000000-0000-0000-0001-38cfa1459105', '00000000-0000-0000-0000-0000000000c2', 'Компания-Микс-2 - Офис', 'ул. Розыбакиева, 210', 44.8422, 65.5020, false, 'ACTIVE');
INSERT INTO data_source (id, created_at, updated_at, business_id, source_type, name, status) VALUES
  ('00000000-0000-0000-0001-31c9fca77a88', now(), now(), '00000000-0000-0000-0001-38cfa1459105', 'MANUAL', 'Ручной ввод Компания-Микс-2', 'ACTIVE');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-eefc8722d83b', now(), now(), '00000000-0000-0000-0001-38cfa1459105', '00000000-0000-0000-0001-279580d8f28e', 'Продукты питания', 'Мука пшеничная высший сорт', 'Казахстанская мука, 50 кг мешок', 'MUK-VS-50', '{"сорт":"высший","вес":"50кг","страна":"Казахстан"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-91cd0757f267', now(), now(), '00000000-0000-0000-0001-eefc8722d83b', '00000000-0000-0000-0001-c4a0ea4de4bf', '00000000-0000-0000-0001-31c9fca77a88', 12000, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-de0a36d448db', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-91cd0757f267', null, 'Мука пшеничная высший сорт', 'Казахстанская мука, 50 кг мешок', 'Продукты питания', 'MUK-VS-50', '{"сорт":"высший","вес":"50кг","страна":"Казахстан"}', '00000000-0000-0000-0001-38cfa1459105', '00000000-0000-0000-0001-c4a0ea4de4bf', 12000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-de0a36d448db', 'мука'), ('00000000-0000-0000-0001-de0a36d448db', 'пшеничная'), ('00000000-0000-0000-0001-de0a36d448db', 'высший'), ('00000000-0000-0000-0001-de0a36d448db', 'сорт'), ('00000000-0000-0000-0001-de0a36d448db', 'продукты'), ('00000000-0000-0000-0001-de0a36d448db', 'питания');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-3d9248a78652', now(), now(), '00000000-0000-0000-0001-38cfa1459105', null, 'Продукты питания', 'Рис пропаренный 900г', 'Длиннозерный пропаренный рис', 'RIS-PR-900', '{"тип":"пропаренный","вес":"900г"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-b46fe372abfa', now(), now(), '00000000-0000-0000-0001-3d9248a78652', '00000000-0000-0000-0001-c4a0ea4de4bf', '00000000-0000-0000-0001-31c9fca77a88', 850, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-a8b8103126d2', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-b46fe372abfa', null, 'Рис пропаренный 900г', 'Длиннозерный пропаренный рис', 'Продукты питания', 'RIS-PR-900', '{"тип":"пропаренный","вес":"900г"}', '00000000-0000-0000-0001-38cfa1459105', '00000000-0000-0000-0001-c4a0ea4de4bf', 850, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-a8b8103126d2', 'рис'), ('00000000-0000-0000-0001-a8b8103126d2', 'пропаренный'), ('00000000-0000-0000-0001-a8b8103126d2', '900г'), ('00000000-0000-0000-0001-a8b8103126d2', 'продукты'), ('00000000-0000-0000-0001-a8b8103126d2', 'питания');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-f152d4e8f31c', now(), now(), '00000000-0000-0000-0001-38cfa1459105', '00000000-0000-0000-0001-279580d8f28e', 'Продукты питания', 'Сахар-песок 1кг', 'Белый свекловичный сахар', 'SAH-BEL-1', '{"тип":"свекловичный","вес":"1кг"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-e3f6031348ff', now(), now(), '00000000-0000-0000-0001-f152d4e8f31c', '00000000-0000-0000-0001-c4a0ea4de4bf', '00000000-0000-0000-0001-31c9fca77a88', 550, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-9191afa3f5d2', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-e3f6031348ff', null, 'Сахар-песок 1кг', 'Белый свекловичный сахар', 'Продукты питания', 'SAH-BEL-1', '{"тип":"свекловичный","вес":"1кг"}', '00000000-0000-0000-0001-38cfa1459105', '00000000-0000-0000-0001-c4a0ea4de4bf', 550, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-9191afa3f5d2', 'сахар'), ('00000000-0000-0000-0001-9191afa3f5d2', 'песок'), ('00000000-0000-0000-0001-9191afa3f5d2', '1кг'), ('00000000-0000-0000-0001-9191afa3f5d2', 'продукты'), ('00000000-0000-0000-0001-9191afa3f5d2', 'питания');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-18b8ae18235a', now(), now(), '00000000-0000-0000-0001-38cfa1459105', '00000000-0000-0000-0001-279580d8f28e', 'Продукты питания', 'Макароны спираль 400г', 'Из твердых сортов пшеницы', 'MAK-SP-400', '{"тип":"спираль","вес":"400г","сорт":"твердый"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-d5b69384ae0f', now(), now(), '00000000-0000-0000-0001-18b8ae18235a', '00000000-0000-0000-0001-c4a0ea4de4bf', '00000000-0000-0000-0001-31c9fca77a88', 380, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-136ba9b83bc4', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-d5b69384ae0f', null, 'Макароны спираль 400г', 'Из твердых сортов пшеницы', 'Продукты питания', 'MAK-SP-400', '{"тип":"спираль","вес":"400г","сорт":"твердый"}', '00000000-0000-0000-0001-38cfa1459105', '00000000-0000-0000-0001-c4a0ea4de4bf', 380, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-136ba9b83bc4', 'макароны'), ('00000000-0000-0000-0001-136ba9b83bc4', 'спираль'), ('00000000-0000-0000-0001-136ba9b83bc4', '400г'), ('00000000-0000-0000-0001-136ba9b83bc4', 'продукты'), ('00000000-0000-0000-0001-136ba9b83bc4', 'питания');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-2b0be7254907', now(), now(), '00000000-0000-0000-0001-38cfa1459105', '00000000-0000-0000-0001-279580d8f28e', 'Кейтеринг на мероприятие', 'Выездное обслуживание до 50 человек', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-a2fa45e9d23e', now(), now(), '00000000-0000-0000-0001-2b0be7254907', '00000000-0000-0000-0001-c4a0ea4de4bf', 'ONLINE', 150000, 360, 'По записи, за 3 дня', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-ea91a7ef8e08', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-a2fa45e9d23e', 'Кейтеринг на мероприятие', 'Выездное обслуживание до 50 человек', 'Продукты питания', null, null, '00000000-0000-0000-0001-38cfa1459105', '00000000-0000-0000-0001-c4a0ea4de4bf', 150000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-ea91a7ef8e08', 'кейтеринг'), ('00000000-0000-0000-0001-ea91a7ef8e08', 'мероприятие'), ('00000000-0000-0000-0001-ea91a7ef8e08', 'продукты'), ('00000000-0000-0000-0001-ea91a7ef8e08', 'питания');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-9ae8cc98e9f2', now(), now(), '00000000-0000-0000-0001-38cfa1459105', '00000000-0000-0000-0001-279580d8f28e', 'Доставка продуктов на дом', 'Доставка в течение 2 часов по городу', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-6b06a01dbb80', now(), now(), '00000000-0000-0000-0001-9ae8cc98e9f2', '00000000-0000-0000-0001-c4a0ea4de4bf', 'ON_SITE', 1500, 120, 'Ежедневно 08:00-22:00', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-00b4dbfa4e79', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-6b06a01dbb80', 'Доставка продуктов на дом', 'Доставка в течение 2 часов по городу', 'Продукты питания', null, null, '00000000-0000-0000-0001-38cfa1459105', '00000000-0000-0000-0001-c4a0ea4de4bf', 1500, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-00b4dbfa4e79', 'доставка'), ('00000000-0000-0000-0001-00b4dbfa4e79', 'продуктов'), ('00000000-0000-0000-0001-00b4dbfa4e79', 'дом'), ('00000000-0000-0000-0001-00b4dbfa4e79', 'продукты'), ('00000000-0000-0000-0001-00b4dbfa4e79', 'питания');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-da72906bf666', now(), now(), '00000000-0000-0000-0001-38cfa1459105', '00000000-0000-0000-0001-279580d8f28e', 'Кейтеринг на мероприятие', 'Выездное обслуживание до 50 человек', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-b8cd64af62cd', now(), now(), '00000000-0000-0000-0001-da72906bf666', '00000000-0000-0000-0001-c4a0ea4de4bf', 'ONLINE', 150000, 360, 'По записи, за 3 дня', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-cf2d7417ecdf', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-b8cd64af62cd', 'Кейтеринг на мероприятие', 'Выездное обслуживание до 50 человек', 'Продукты питания', null, null, '00000000-0000-0000-0001-38cfa1459105', '00000000-0000-0000-0001-c4a0ea4de4bf', 150000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-cf2d7417ecdf', 'кейтеринг'), ('00000000-0000-0000-0001-cf2d7417ecdf', 'мероприятие'), ('00000000-0000-0000-0001-cf2d7417ecdf', 'продукты'), ('00000000-0000-0000-0001-cf2d7417ecdf', 'питания');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-ca9bf48fe503', now(), now(), '00000000-0000-0000-0001-38cfa1459105', '00000000-0000-0000-0001-279580d8f28e', 'Доставка продуктов на дом', 'Доставка в течение 2 часов по городу', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-9d9cf2f39910', now(), now(), '00000000-0000-0000-0001-ca9bf48fe503', '00000000-0000-0000-0001-c4a0ea4de4bf', 'ONLINE', 1500, 120, 'Ежедневно 08:00-22:00', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-c8e0da1b7d08', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-9d9cf2f39910', 'Доставка продуктов на дом', 'Доставка в течение 2 часов по городу', 'Продукты питания', null, null, '00000000-0000-0000-0001-38cfa1459105', '00000000-0000-0000-0001-c4a0ea4de4bf', 1500, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-c8e0da1b7d08', 'доставка'), ('00000000-0000-0000-0001-c8e0da1b7d08', 'продуктов'), ('00000000-0000-0000-0001-c8e0da1b7d08', 'дом'), ('00000000-0000-0000-0001-c8e0da1b7d08', 'продукты'), ('00000000-0000-0000-0001-c8e0da1b7d08', 'питания');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-1b59424de334', now(), now(), '00000000-0000-0000-0001-38cfa1459105', '00000000-0000-0000-0001-279580d8f28e', 'Кейтеринг на мероприятие', 'Выездное обслуживание до 50 человек', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-1f4c0b69cff2', now(), now(), '00000000-0000-0000-0001-1b59424de334', '00000000-0000-0000-0001-c4a0ea4de4bf', 'ON_SITE', 150000, 360, 'По записи, за 3 дня', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-33c074e35f8e', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-1f4c0b69cff2', 'Кейтеринг на мероприятие', 'Выездное обслуживание до 50 человек', 'Продукты питания', null, null, '00000000-0000-0000-0001-38cfa1459105', '00000000-0000-0000-0001-c4a0ea4de4bf', 150000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-33c074e35f8e', 'кейтеринг'), ('00000000-0000-0000-0001-33c074e35f8e', 'мероприятие'), ('00000000-0000-0000-0001-33c074e35f8e', 'продукты'), ('00000000-0000-0000-0001-33c074e35f8e', 'питания');

-- Компания-Микс-3 (ИП, Медицинские услуги, Астана)
INSERT INTO business (id, created_at, updated_at, name, legal_name, bin, status) VALUES
  ('00000000-0000-0000-0001-341b655a7445', now(), now(), 'Компания-Микс-3', 'ИП "Компания-Микс-3"', '456789012345', 'ACTIVE');
INSERT INTO business_branch (id, created_at, updated_at, business_id, city_id, name, address, latitude, longitude, online_only, status) VALUES
  ('00000000-0000-0000-0001-8a085918963f', now(), now(), '00000000-0000-0000-0001-341b655a7445', '00000000-0000-0000-0000-0000000000c3', 'Компания-Микс-3 - Офис', 'ул. Сыганак, 25', 44.8432, 65.5030, false, 'ACTIVE');
INSERT INTO data_source (id, created_at, updated_at, business_id, source_type, name, status) VALUES
  ('00000000-0000-0000-0001-6e52588da4da', now(), now(), '00000000-0000-0000-0001-341b655a7445', 'MANUAL', 'Ручной ввод Компания-Микс-3', 'ACTIVE');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-1bb38c8969d6', now(), now(), '00000000-0000-0000-0001-341b655a7445', null, 'Медицинские услуги', 'Масляный фильтр Sakura', 'Фильтр масляный для японских авто', 'SAK-OF-001', '{"бренд":"Sakura","тип":"масляный"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-b5abeedc1af9', now(), now(), '00000000-0000-0000-0001-1bb38c8969d6', '00000000-0000-0000-0001-8a085918963f', '00000000-0000-0000-0001-6e52588da4da', 3500, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-d5d03769fc5e', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-b5abeedc1af9', null, 'Масляный фильтр Sakura', 'Фильтр масляный для японских авто', 'Медицинские услуги', 'SAK-OF-001', '{"бренд":"Sakura","тип":"масляный"}', '00000000-0000-0000-0001-341b655a7445', '00000000-0000-0000-0001-8a085918963f', 3500, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-d5d03769fc5e', 'масляный'), ('00000000-0000-0000-0001-d5d03769fc5e', 'фильтр'), ('00000000-0000-0000-0001-d5d03769fc5e', 'sakura'), ('00000000-0000-0000-0001-d5d03769fc5e', 'медицинские'), ('00000000-0000-0000-0001-d5d03769fc5e', 'услуги');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-f08f46377ee6', now(), now(), '00000000-0000-0000-0001-341b655a7445', '00000000-0000-0000-0001-81548349b593', 'Медицинские услуги', 'Тормозные колодки TRW', 'Передние колодки для европейских авто', 'TRW-BP-202', '{"бренд":"TRW","ось":"перед","тип":"керамика"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-ea32747fd9b9', now(), now(), '00000000-0000-0000-0001-f08f46377ee6', '00000000-0000-0000-0001-8a085918963f', '00000000-0000-0000-0001-6e52588da4da', 18500, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-0d8bc10fde90', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-ea32747fd9b9', null, 'Тормозные колодки TRW', 'Передние колодки для европейских авто', 'Медицинские услуги', 'TRW-BP-202', '{"бренд":"TRW","ось":"перед","тип":"керамика"}', '00000000-0000-0000-0001-341b655a7445', '00000000-0000-0000-0001-8a085918963f', 18500, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-0d8bc10fde90', 'тормозные'), ('00000000-0000-0000-0001-0d8bc10fde90', 'колодки'), ('00000000-0000-0000-0001-0d8bc10fde90', 'trw'), ('00000000-0000-0000-0001-0d8bc10fde90', 'медицинские'), ('00000000-0000-0000-0001-0d8bc10fde90', 'услуги');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-f575a54c7e68', now(), now(), '00000000-0000-0000-0001-341b655a7445', '00000000-0000-0000-0001-81548349b593', 'Медицинские услуги', 'Свечи зажигания NGK', 'Иридиевые свечи, комплект 4 шт', 'NGK-IR-4', '{"бренд":"NGK","тип":"иридиевые","комплект":"4 шт"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-a4eab231d50e', now(), now(), '00000000-0000-0000-0001-f575a54c7e68', '00000000-0000-0000-0001-8a085918963f', '00000000-0000-0000-0001-6e52588da4da', 12000, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-1dc29f01f6d1', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-a4eab231d50e', null, 'Свечи зажигания NGK', 'Иридиевые свечи, комплект 4 шт', 'Медицинские услуги', 'NGK-IR-4', '{"бренд":"NGK","тип":"иридиевые","комплект":"4 шт"}', '00000000-0000-0000-0001-341b655a7445', '00000000-0000-0000-0001-8a085918963f', 12000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-1dc29f01f6d1', 'свечи'), ('00000000-0000-0000-0001-1dc29f01f6d1', 'зажигания'), ('00000000-0000-0000-0001-1dc29f01f6d1', 'ngk'), ('00000000-0000-0000-0001-1dc29f01f6d1', 'медицинские'), ('00000000-0000-0000-0001-1dc29f01f6d1', 'услуги');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-cbd101529933', now(), now(), '00000000-0000-0000-0001-341b655a7445', null, 'Медицинские услуги', 'Амортизатор Kayaba', 'Газовый амортизатор задний', 'KYB-GS-001', '{"бренд":"Kayaba","тип":"газовый","ось":"зад"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-0b45e6511549', now(), now(), '00000000-0000-0000-0001-cbd101529933', '00000000-0000-0000-0001-8a085918963f', '00000000-0000-0000-0001-6e52588da4da', 28000, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-39827f06f1a4', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-0b45e6511549', null, 'Амортизатор Kayaba', 'Газовый амортизатор задний', 'Медицинские услуги', 'KYB-GS-001', '{"бренд":"Kayaba","тип":"газовый","ось":"зад"}', '00000000-0000-0000-0001-341b655a7445', '00000000-0000-0000-0001-8a085918963f', 28000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-39827f06f1a4', 'амортизатор'), ('00000000-0000-0000-0001-39827f06f1a4', 'kayaba'), ('00000000-0000-0000-0001-39827f06f1a4', 'медицинские'), ('00000000-0000-0000-0001-39827f06f1a4', 'услуги');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-f90f0d7df8d2', now(), now(), '00000000-0000-0000-0001-341b655a7445', '00000000-0000-0000-0001-81548349b593', 'Медицинские услуги', 'Ремень ГРМ Gates', 'Ремень ГРМ с роликом, комплект', 'GTS-TB-KIT', '{"бренд":"Gates","тип":"комплект"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-70a238fac852', now(), now(), '00000000-0000-0000-0001-f90f0d7df8d2', '00000000-0000-0000-0001-8a085918963f', '00000000-0000-0000-0001-6e52588da4da', 22000, false, 'ACTIVE');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-6960a2e3196b', now(), now(), '00000000-0000-0000-0001-341b655a7445', '00000000-0000-0000-0001-81548349b593', 'Консультация терапевта', 'Первичный прием, осмотр, назначение', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-bc57ffee80d6', now(), now(), '00000000-0000-0000-0001-6960a2e3196b', '00000000-0000-0000-0001-8a085918963f', 'ON_SITE', 5000, 30, 'Пн-Пт 08:00-17:00', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-144e3bc2af96', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-bc57ffee80d6', 'Консультация терапевта', 'Первичный прием, осмотр, назначение', 'Медицинские услуги', null, null, '00000000-0000-0000-0001-341b655a7445', '00000000-0000-0000-0001-8a085918963f', 5000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-144e3bc2af96', 'консультация'), ('00000000-0000-0000-0001-144e3bc2af96', 'терапевта'), ('00000000-0000-0000-0001-144e3bc2af96', 'медицинские'), ('00000000-0000-0000-0001-144e3bc2af96', 'услуги');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-70790d54aa1e', now(), now(), '00000000-0000-0000-0001-341b655a7445', '00000000-0000-0000-0001-81548349b593', 'УЗИ брюшной полости', 'Комплексное УЗИ органов брюшной полости', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-a8a73c7deba8', now(), now(), '00000000-0000-0000-0001-70790d54aa1e', '00000000-0000-0000-0001-8a085918963f', 'ONLINE', 8000, 40, 'Пн-Сб 08:00-14:00', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-e5031c6a521c', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-a8a73c7deba8', 'УЗИ брюшной полости', 'Комплексное УЗИ органов брюшной полости', 'Медицинские услуги', null, null, '00000000-0000-0000-0001-341b655a7445', '00000000-0000-0000-0001-8a085918963f', 8000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-e5031c6a521c', 'узи'), ('00000000-0000-0000-0001-e5031c6a521c', 'брюшной'), ('00000000-0000-0000-0001-e5031c6a521c', 'полости'), ('00000000-0000-0000-0001-e5031c6a521c', 'медицинские'), ('00000000-0000-0000-0001-e5031c6a521c', 'услуги');

-- Компания-Микс-4 (ТОО, Образование, Шымкент)
INSERT INTO business (id, created_at, updated_at, name, legal_name, bin, status) VALUES
  ('00000000-0000-0000-0001-4e375ae71578', now(), now(), 'Компания-Микс-4', 'ТОО "Компания-Микс-4"', '567890123456', 'ACTIVE');
INSERT INTO business_branch (id, created_at, updated_at, business_id, city_id, name, address, latitude, longitude, online_only, status) VALUES
  ('00000000-0000-0000-0001-ce7e1b078f36', now(), now(), '00000000-0000-0000-0001-4e375ae71578', '00000000-0000-0000-0001-1a904d507278', 'Компания-Микс-4 - Офис', 'ул. Байтурсынова, 67', 44.8442, 65.5040, false, 'ACTIVE');
INSERT INTO data_source (id, created_at, updated_at, business_id, source_type, name, status) VALUES
  ('00000000-0000-0000-0001-1f0bbbaded24', now(), now(), '00000000-0000-0000-0001-4e375ae71578', 'MANUAL', 'Ручной ввод Компания-Микс-4', 'ACTIVE');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-eb8b120816da', now(), now(), '00000000-0000-0000-0001-4e375ae71578', '00000000-0000-0000-0001-49c510314da7', 'Образование', 'Гипсокартон Knauf 12.5мм', 'Влагостойкий лист 2500x1200 мм', 'KNF-GKL-12', '{"бренд":"Knauf","толщина":"12.5мм","тип":"влагостойкий"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-293bf39cad4c', now(), now(), '00000000-0000-0000-0001-eb8b120816da', '00000000-0000-0000-0001-ce7e1b078f36', '00000000-0000-0000-0001-1f0bbbaded24', 4500, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-cd9571ea64ae', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-293bf39cad4c', null, 'Гипсокартон Knauf 12.5мм', 'Влагостойкий лист 2500x1200 мм', 'Образование', 'KNF-GKL-12', '{"бренд":"Knauf","толщина":"12.5мм","тип":"влагостойкий"}', '00000000-0000-0000-0001-4e375ae71578', '00000000-0000-0000-0001-ce7e1b078f36', 4500, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-cd9571ea64ae', 'гипсокартон'), ('00000000-0000-0000-0001-cd9571ea64ae', 'knauf'), ('00000000-0000-0000-0001-cd9571ea64ae', '12.5мм'), ('00000000-0000-0000-0001-cd9571ea64ae', 'образование');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-65da22c4307b', now(), now(), '00000000-0000-0000-0001-4e375ae71578', '00000000-0000-0000-0001-49c510314da7', 'Образование', 'Профнастил С8 0.45мм', 'Оцинкованный профнастил для забора', 'PRF-C8-045', '{"марка":"С8","толщина":"0.45мм","тип":"оцинкованный"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-cf840a51a54c', now(), now(), '00000000-0000-0000-0001-65da22c4307b', '00000000-0000-0000-0001-ce7e1b078f36', '00000000-0000-0000-0001-1f0bbbaded24', 2800, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-cb5e5c859797', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-cf840a51a54c', null, 'Профнастил С8 0.45мм', 'Оцинкованный профнастил для забора', 'Образование', 'PRF-C8-045', '{"марка":"С8","толщина":"0.45мм","тип":"оцинкованный"}', '00000000-0000-0000-0001-4e375ae71578', '00000000-0000-0000-0001-ce7e1b078f36', 2800, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-cb5e5c859797', 'профнастил'), ('00000000-0000-0000-0001-cb5e5c859797', '0.45мм'), ('00000000-0000-0000-0001-cb5e5c859797', 'образование');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-fd67533eef52', now(), now(), '00000000-0000-0000-0001-4e375ae71578', '00000000-0000-0000-0001-49c510314da7', 'Курс Python базовый', 'Основы программирования на Python, 16 часов', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-d672b8fb74ee', now(), now(), '00000000-0000-0000-0001-fd67533eef52', '00000000-0000-0000-0001-ce7e1b078f36', 'ONLINE', 45000, 960, 'Группы: утро/вечер', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-5c74317f1e6b', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-d672b8fb74ee', 'Курс Python базовый', 'Основы программирования на Python, 16 часов', 'Образование', null, null, '00000000-0000-0000-0001-4e375ae71578', '00000000-0000-0000-0001-ce7e1b078f36', 45000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-5c74317f1e6b', 'курс'), ('00000000-0000-0000-0001-5c74317f1e6b', 'python'), ('00000000-0000-0000-0001-5c74317f1e6b', 'базовый'), ('00000000-0000-0000-0001-5c74317f1e6b', 'образование');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-2c876c54ecb0', now(), now(), '00000000-0000-0000-0001-4e375ae71578', '00000000-0000-0000-0001-49c510314da7', 'Английский язык Intermediate', 'Разговорный курс, 24 занятия по 90 мин', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-895ee894a62f', now(), now(), '00000000-0000-0000-0001-2c876c54ecb0', '00000000-0000-0000-0001-ce7e1b078f36', 'ONLINE', 65000, 1440, 'Пн-Ср-Пт или Вт-Чт-Сб', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-98826274a397', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-895ee894a62f', 'Английский язык Intermediate', 'Разговорный курс, 24 занятия по 90 мин', 'Образование', null, null, '00000000-0000-0000-0001-4e375ae71578', '00000000-0000-0000-0001-ce7e1b078f36', 65000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-98826274a397', 'английский'), ('00000000-0000-0000-0001-98826274a397', 'язык'), ('00000000-0000-0000-0001-98826274a397', 'intermediate'), ('00000000-0000-0000-0001-98826274a397', 'образование');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-b0d995b192db', now(), now(), '00000000-0000-0000-0001-4e375ae71578', '00000000-0000-0000-0001-49c510314da7', 'Подготовка к IELTS', 'Интенсивный курс, 20 занятий по 120 мин', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-7b8c4011df24', now(), now(), '00000000-0000-0000-0001-b0d995b192db', '00000000-0000-0000-0001-ce7e1b078f36', 'ON_SITE', 85000, 2400, 'Утро 09:00 или вечер 18:00', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-0a0fd2adc0af', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-7b8c4011df24', 'Подготовка к IELTS', 'Интенсивный курс, 20 занятий по 120 мин', 'Образование', null, null, '00000000-0000-0000-0001-4e375ae71578', '00000000-0000-0000-0001-ce7e1b078f36', 85000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-0a0fd2adc0af', 'подготовка'), ('00000000-0000-0000-0001-0a0fd2adc0af', 'ielts'), ('00000000-0000-0000-0001-0a0fd2adc0af', 'образование');

-- Компания-Микс-5 (ИП, Спорт и фитнес, Караганда)
INSERT INTO business (id, created_at, updated_at, name, legal_name, bin, status) VALUES
  ('00000000-0000-0000-0001-513bd20f0b46', now(), now(), 'Компания-Микс-5', 'ИП "Компания-Микс-5"', '678901234567', 'ACTIVE');
INSERT INTO business_branch (id, created_at, updated_at, business_id, city_id, name, address, latitude, longitude, online_only, status) VALUES
  ('00000000-0000-0000-0001-74be3f5f8e75', now(), now(), '00000000-0000-0000-0001-513bd20f0b46', '00000000-0000-0000-0001-02ba3c39b41d', 'Компания-Микс-5 - Офис', 'мкр. Гульдер, 7', 44.8452, 65.5050, false, 'ACTIVE');
INSERT INTO data_source (id, created_at, updated_at, business_id, source_type, name, status) VALUES
  ('00000000-0000-0000-0001-5ca7d6d5f1a4', now(), now(), '00000000-0000-0000-0001-513bd20f0b46', 'MANUAL', 'Ручной ввод Компания-Микс-5', 'ACTIVE');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-7ea5b40c674d', now(), now(), '00000000-0000-0000-0001-513bd20f0b46', '00000000-0000-0000-0001-9f59c39873da', 'Спорт и фитнес', 'Коврик для йоги 6мм', 'Нескользящий, термопластичная резина', 'MAT-YG-6', '{"толщина":"6мм","материал":"TPE","нескользящий":"да"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-0d00278c9327', now(), now(), '00000000-0000-0000-0001-7ea5b40c674d', '00000000-0000-0000-0001-74be3f5f8e75', '00000000-0000-0000-0001-5ca7d6d5f1a4', 6500, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-54d2f7ae8bbe', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-0d00278c9327', null, 'Коврик для йоги 6мм', 'Нескользящий, термопластичная резина', 'Спорт и фитнес', 'MAT-YG-6', '{"толщина":"6мм","материал":"TPE","нескользящий":"да"}', '00000000-0000-0000-0001-513bd20f0b46', '00000000-0000-0000-0001-74be3f5f8e75', 6500, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-54d2f7ae8bbe', 'коврик'), ('00000000-0000-0000-0001-54d2f7ae8bbe', 'йоги'), ('00000000-0000-0000-0001-54d2f7ae8bbe', '6мм'), ('00000000-0000-0000-0001-54d2f7ae8bbe', 'спорт'), ('00000000-0000-0000-0001-54d2f7ae8bbe', 'фитнес');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-3b95b2d25857', now(), now(), '00000000-0000-0000-0001-513bd20f0b46', null, 'Спорт и фитнес', 'Фитнес-браслет Xiaomi Band', 'Шагомер, пульсометр, SpO2, водозащита', 'MI-BAND-8', '{"бренд":"Xiaomi","датчики":"пульс,SpO2","водозащита":"5ATM"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-6056422d6a51', now(), now(), '00000000-0000-0000-0001-3b95b2d25857', '00000000-0000-0000-0001-74be3f5f8e75', '00000000-0000-0000-0001-5ca7d6d5f1a4', 18000, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-1c5106a77809', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-6056422d6a51', null, 'Фитнес-браслет Xiaomi Band', 'Шагомер, пульсометр, SpO2, водозащита', 'Спорт и фитнес', 'MI-BAND-8', '{"бренд":"Xiaomi","датчики":"пульс,SpO2","водозащита":"5ATM"}', '00000000-0000-0000-0001-513bd20f0b46', '00000000-0000-0000-0001-74be3f5f8e75', 18000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-1c5106a77809', 'фитнес'), ('00000000-0000-0000-0001-1c5106a77809', 'браслет'), ('00000000-0000-0000-0001-1c5106a77809', 'xiaomi'), ('00000000-0000-0000-0001-1c5106a77809', 'band'), ('00000000-0000-0000-0001-1c5106a77809', 'спорт'), ('00000000-0000-0000-0001-1c5106a77809', 'фитнес');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-b90d58e1ce93', now(), now(), '00000000-0000-0000-0001-513bd20f0b46', '00000000-0000-0000-0001-9f59c39873da', 'Спорт и фитнес', 'Беговая дорожка CardioFit', 'Электрическая, до 16 км/ч, складная', 'CF-TM-001', '{"тип":"электрическая","скорость":"16 км/ч","складная":"да"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-fe63c3e7a215', now(), now(), '00000000-0000-0000-0001-b90d58e1ce93', '00000000-0000-0000-0001-74be3f5f8e75', '00000000-0000-0000-0001-5ca7d6d5f1a4', 280000, false, 'ACTIVE');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-e7644ad075c8', now(), now(), '00000000-0000-0000-0001-513bd20f0b46', '00000000-0000-0000-0001-9f59c39873da', 'Персональная тренировка', 'Индивидуальное занятие с тренером, 60 мин', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-3a0cc8e243ad', now(), now(), '00000000-0000-0000-0001-e7644ad075c8', '00000000-0000-0000-0001-74be3f5f8e75', 'ONLINE', 5000, 60, 'Ежедневно 06:00-23:00', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-4c2cd27a4c4c', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-3a0cc8e243ad', 'Персональная тренировка', 'Индивидуальное занятие с тренером, 60 мин', 'Спорт и фитнес', null, null, '00000000-0000-0000-0001-513bd20f0b46', '00000000-0000-0000-0001-74be3f5f8e75', 5000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-4c2cd27a4c4c', 'персональная'), ('00000000-0000-0000-0001-4c2cd27a4c4c', 'тренировка'), ('00000000-0000-0000-0001-4c2cd27a4c4c', 'спорт'), ('00000000-0000-0000-0001-4c2cd27a4c4c', 'фитнес');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-e6f7ca88ab52', now(), now(), '00000000-0000-0000-0001-513bd20f0b46', '00000000-0000-0000-0001-9f59c39873da', 'Абонемент в тренажерный зал', 'Месячный абонемент безлимит', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-7a8c63fcc048', now(), now(), '00000000-0000-0000-0001-e6f7ca88ab52', '00000000-0000-0000-0001-74be3f5f8e75', 'ON_SITE', 25000, 43200, 'Ежедневно 06:00-23:00', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-82c0a24c484e', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-7a8c63fcc048', 'Абонемент в тренажерный зал', 'Месячный абонемент безлимит', 'Спорт и фитнес', null, null, '00000000-0000-0000-0001-513bd20f0b46', '00000000-0000-0000-0001-74be3f5f8e75', 25000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-82c0a24c484e', 'абонемент'), ('00000000-0000-0000-0001-82c0a24c484e', 'тренажерный'), ('00000000-0000-0000-0001-82c0a24c484e', 'зал'), ('00000000-0000-0000-0001-82c0a24c484e', 'спорт'), ('00000000-0000-0000-0001-82c0a24c484e', 'фитнес');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-fd6ea98e9b68', now(), now(), '00000000-0000-0000-0001-513bd20f0b46', '00000000-0000-0000-0001-9f59c39873da', 'Занятие по кроссфиту', 'Групповое занятие, 90 мин', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-9a90e92e7da0', now(), now(), '00000000-0000-0000-0001-fd6ea98e9b68', '00000000-0000-0000-0001-74be3f5f8e75', 'ONLINE', 3500, 90, 'Пн-Ср-Пт 07:00, 19:00', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-820d14a32649', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-9a90e92e7da0', 'Занятие по кроссфиту', 'Групповое занятие, 90 мин', 'Спорт и фитнес', null, null, '00000000-0000-0000-0001-513bd20f0b46', '00000000-0000-0000-0001-74be3f5f8e75', 3500, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-820d14a32649', 'занятие'), ('00000000-0000-0000-0001-820d14a32649', 'кроссфиту'), ('00000000-0000-0000-0001-820d14a32649', 'спорт'), ('00000000-0000-0000-0001-820d14a32649', 'фитнес');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-da459ac3d6d4', now(), now(), '00000000-0000-0000-0001-513bd20f0b46', '00000000-0000-0000-0001-9f59c39873da', 'Плавание дети 6-12 лет', 'Групповое занятие с тренером, 45 мин', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-6405bbbf003e', now(), now(), '00000000-0000-0000-0001-da459ac3d6d4', '00000000-0000-0000-0001-74be3f5f8e75', 'ONLINE', 3000, 45, 'Сб-Вс 10:00-14:00', false, 'ACTIVE');

-- Компания-Микс-6 (ТОО, IT услуги, Кызылорда)
INSERT INTO business (id, created_at, updated_at, name, legal_name, bin, status) VALUES
  ('00000000-0000-0000-0001-a8be200cc8bd', now(), now(), 'Компания-Микс-6', 'ТОО "Компания-Микс-6"', '789012345678', 'ACTIVE');
INSERT INTO business_branch (id, created_at, updated_at, business_id, city_id, name, address, latitude, longitude, online_only, status) VALUES
  ('00000000-0000-0000-0001-0280b98e17e8', now(), now(), '00000000-0000-0000-0001-a8be200cc8bd', '00000000-0000-0000-0000-0000000000c1', 'Компания-Микс-6 - Филиал 1', 'ул. Коркыт Ата, 34', 44.8462, 65.5060, false, 'ACTIVE');
INSERT INTO business_branch (id, created_at, updated_at, business_id, city_id, name, address, latitude, longitude, online_only, status) VALUES
  ('00000000-0000-0000-0001-f5959dd0b520', now(), now(), '00000000-0000-0000-0001-a8be200cc8bd', '00000000-0000-0000-0000-0000000000c1', 'Компания-Микс-6 - Филиал 2', 'ул. Коркыт Ата, 34', 44.8462, 65.5060, false, 'ACTIVE');
INSERT INTO data_source (id, created_at, updated_at, business_id, source_type, name, status) VALUES
  ('00000000-0000-0000-0001-78c0f251953a', now(), now(), '00000000-0000-0000-0001-a8be200cc8bd', 'MANUAL', 'Ручной ввод Компания-Микс-6', 'ACTIVE');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-2d5839e4e9f5', now(), now(), '00000000-0000-0000-0001-a8be200cc8bd', null, 'IT услуги', 'Антивирус Kaspersky 1 год', 'Защита на 3 устройства, лицензия', 'KAS-AV-3D', '{"бренд":"Kaspersky","устройств":"3","срок":"1 год"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-047c103dbdb1', now(), now(), '00000000-0000-0000-0001-2d5839e4e9f5', '00000000-0000-0000-0001-0280b98e17e8', '00000000-0000-0000-0001-78c0f251953a', 12000, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-2b379d83733e', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-047c103dbdb1', null, 'Антивирус Kaspersky 1 год', 'Защита на 3 устройства, лицензия', 'IT услуги', 'KAS-AV-3D', '{"бренд":"Kaspersky","устройств":"3","срок":"1 год"}', '00000000-0000-0000-0001-a8be200cc8bd', '00000000-0000-0000-0001-0280b98e17e8', 12000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-2b379d83733e', 'антивирус'), ('00000000-0000-0000-0001-2b379d83733e', 'kaspersky'), ('00000000-0000-0000-0001-2b379d83733e', 'год'), ('00000000-0000-0000-0001-2b379d83733e', 'услуги');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-5e1410122c73', now(), now(), '00000000-0000-0000-0001-2d5839e4e9f5', '00000000-0000-0000-0001-f5959dd0b520', '00000000-0000-0000-0001-78c0f251953a', 12000, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-48cbc89e9bf0', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-5e1410122c73', null, 'Антивирус Kaspersky 1 год', 'Защита на 3 устройства, лицензия', 'IT услуги', 'KAS-AV-3D', '{"бренд":"Kaspersky","устройств":"3","срок":"1 год"}', '00000000-0000-0000-0001-a8be200cc8bd', '00000000-0000-0000-0001-f5959dd0b520', 12000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-48cbc89e9bf0', 'антивирус'), ('00000000-0000-0000-0001-48cbc89e9bf0', 'kaspersky'), ('00000000-0000-0000-0001-48cbc89e9bf0', 'год'), ('00000000-0000-0000-0001-48cbc89e9bf0', 'услуги');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-711923568654', now(), now(), '00000000-0000-0000-0001-a8be200cc8bd', '00000000-0000-0000-0001-a686baabac2c', 'IT услуги', 'Microsoft Office 365', 'Годовая подписка на 1 пользователя', 'MS-O365-1Y', '{"бренд":"Microsoft","пользователей":"1","срок":"1 год"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-2450af3353a5', now(), now(), '00000000-0000-0000-0001-711923568654', '00000000-0000-0000-0001-0280b98e17e8', '00000000-0000-0000-0001-78c0f251953a', 35000, false, 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-c14927008803', now(), now(), '00000000-0000-0000-0001-711923568654', '00000000-0000-0000-0001-f5959dd0b520', '00000000-0000-0000-0001-78c0f251953a', 35000, false, 'ACTIVE');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-60839b834280', now(), now(), '00000000-0000-0000-0001-a8be200cc8bd', '00000000-0000-0000-0001-a686baabac2c', 'IT услуги', 'Windows 11 Pro лицензия', 'Цифровая лицензия, русский язык', 'WIN-11-PRO', '{"версия":"Pro","тип":"цифровая"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-812a455c18ac', now(), now(), '00000000-0000-0000-0001-60839b834280', '00000000-0000-0000-0001-0280b98e17e8', '00000000-0000-0000-0001-78c0f251953a', 55000, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-1ac5fef6a1aa', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-812a455c18ac', null, 'Windows 11 Pro лицензия', 'Цифровая лицензия, русский язык', 'IT услуги', 'WIN-11-PRO', '{"версия":"Pro","тип":"цифровая"}', '00000000-0000-0000-0001-a8be200cc8bd', '00000000-0000-0000-0001-0280b98e17e8', 55000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-1ac5fef6a1aa', 'windows'), ('00000000-0000-0000-0001-1ac5fef6a1aa', 'pro'), ('00000000-0000-0000-0001-1ac5fef6a1aa', 'лицензия'), ('00000000-0000-0000-0001-1ac5fef6a1aa', 'услуги');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-fff2d2c6f3c4', now(), now(), '00000000-0000-0000-0001-60839b834280', '00000000-0000-0000-0001-f5959dd0b520', '00000000-0000-0000-0001-78c0f251953a', 55000, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-924629c7c5d2', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-fff2d2c6f3c4', null, 'Windows 11 Pro лицензия', 'Цифровая лицензия, русский язык', 'IT услуги', 'WIN-11-PRO', '{"версия":"Pro","тип":"цифровая"}', '00000000-0000-0000-0001-a8be200cc8bd', '00000000-0000-0000-0001-f5959dd0b520', 55000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-924629c7c5d2', 'windows'), ('00000000-0000-0000-0001-924629c7c5d2', 'pro'), ('00000000-0000-0000-0001-924629c7c5d2', 'лицензия'), ('00000000-0000-0000-0001-924629c7c5d2', 'услуги');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-83e4dd9b2505', now(), now(), '00000000-0000-0000-0001-a8be200cc8bd', null, 'IT услуги', 'Антивирус Kaspersky 1 год', 'Защита на 3 устройства, лицензия', 'KAS-AV-3D', '{"бренд":"Kaspersky","устройств":"3","срок":"1 год"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-43152e9fd6c2', now(), now(), '00000000-0000-0000-0001-83e4dd9b2505', '00000000-0000-0000-0001-0280b98e17e8', '00000000-0000-0000-0001-78c0f251953a', 12000, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-a18c879bb345', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-43152e9fd6c2', null, 'Антивирус Kaspersky 1 год', 'Защита на 3 устройства, лицензия', 'IT услуги', 'KAS-AV-3D', '{"бренд":"Kaspersky","устройств":"3","срок":"1 год"}', '00000000-0000-0000-0001-a8be200cc8bd', '00000000-0000-0000-0001-0280b98e17e8', 12000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-a18c879bb345', 'антивирус'), ('00000000-0000-0000-0001-a18c879bb345', 'kaspersky'), ('00000000-0000-0000-0001-a18c879bb345', 'год'), ('00000000-0000-0000-0001-a18c879bb345', 'услуги');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-64e38c8e2dbe', now(), now(), '00000000-0000-0000-0001-83e4dd9b2505', '00000000-0000-0000-0001-f5959dd0b520', '00000000-0000-0000-0001-78c0f251953a', 12000, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-4f82c971e1e6', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-64e38c8e2dbe', null, 'Антивирус Kaspersky 1 год', 'Защита на 3 устройства, лицензия', 'IT услуги', 'KAS-AV-3D', '{"бренд":"Kaspersky","устройств":"3","срок":"1 год"}', '00000000-0000-0000-0001-a8be200cc8bd', '00000000-0000-0000-0001-f5959dd0b520', 12000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-4f82c971e1e6', 'антивирус'), ('00000000-0000-0000-0001-4f82c971e1e6', 'kaspersky'), ('00000000-0000-0000-0001-4f82c971e1e6', 'год'), ('00000000-0000-0000-0001-4f82c971e1e6', 'услуги');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-736895850ecc', now(), now(), '00000000-0000-0000-0001-a8be200cc8bd', '00000000-0000-0000-0001-a686baabac2c', 'Разработка сайта-визитки', 'Лендинг до 5 страниц, адаптивный дизайн', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-804f67952807', now(), now(), '00000000-0000-0000-0001-736895850ecc', '00000000-0000-0000-0001-0280b98e17e8', 'ON_SITE', 150000, 10080, 'По согласованию', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-3026c033b4f9', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-804f67952807', 'Разработка сайта-визитки', 'Лендинг до 5 страниц, адаптивный дизайн', 'IT услуги', null, null, '00000000-0000-0000-0001-a8be200cc8bd', '00000000-0000-0000-0001-0280b98e17e8', 150000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-3026c033b4f9', 'разработка'), ('00000000-0000-0000-0001-3026c033b4f9', 'сайта'), ('00000000-0000-0000-0001-3026c033b4f9', 'визитки'), ('00000000-0000-0000-0001-3026c033b4f9', 'услуги');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-95db706da189', now(), now(), '00000000-0000-0000-0001-736895850ecc', '00000000-0000-0000-0001-f5959dd0b520', 'ON_SITE', 150000, 10080, 'По согласованию', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-e07db33f76eb', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-95db706da189', 'Разработка сайта-визитки', 'Лендинг до 5 страниц, адаптивный дизайн', 'IT услуги', null, null, '00000000-0000-0000-0001-a8be200cc8bd', '00000000-0000-0000-0001-f5959dd0b520', 150000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-e07db33f76eb', 'разработка'), ('00000000-0000-0000-0001-e07db33f76eb', 'сайта'), ('00000000-0000-0000-0001-e07db33f76eb', 'визитки'), ('00000000-0000-0000-0001-e07db33f76eb', 'услуги');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-d09509eb4a15', now(), now(), '00000000-0000-0000-0001-a8be200cc8bd', '00000000-0000-0000-0001-a686baabac2c', 'Настройка таргет рекламы', 'Настройка и ведение рекламы в Instagram/Facebook', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-c0085d011671', now(), now(), '00000000-0000-0000-0001-d09509eb4a15', '00000000-0000-0000-0001-0280b98e17e8', 'ONLINE', 50000, 10080, 'Рабочие дни', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-f69cf85ba2be', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-c0085d011671', 'Настройка таргет рекламы', 'Настройка и ведение рекламы в Instagram/Facebook', 'IT услуги', null, null, '00000000-0000-0000-0001-a8be200cc8bd', '00000000-0000-0000-0001-0280b98e17e8', 50000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-f69cf85ba2be', 'настройка'), ('00000000-0000-0000-0001-f69cf85ba2be', 'таргет'), ('00000000-0000-0000-0001-f69cf85ba2be', 'рекламы'), ('00000000-0000-0000-0001-f69cf85ba2be', 'услуги');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-d12215addd7f', now(), now(), '00000000-0000-0000-0001-d09509eb4a15', '00000000-0000-0000-0001-f5959dd0b520', 'ONLINE', 50000, 10080, 'Рабочие дни', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-f25db422d363', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-d12215addd7f', 'Настройка таргет рекламы', 'Настройка и ведение рекламы в Instagram/Facebook', 'IT услуги', null, null, '00000000-0000-0000-0001-a8be200cc8bd', '00000000-0000-0000-0001-f5959dd0b520', 50000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-f25db422d363', 'настройка'), ('00000000-0000-0000-0001-f25db422d363', 'таргет'), ('00000000-0000-0000-0001-f25db422d363', 'рекламы'), ('00000000-0000-0000-0001-f25db422d363', 'услуги');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-efc4d0884dc0', now(), now(), '00000000-0000-0000-0001-a8be200cc8bd', '00000000-0000-0000-0001-a686baabac2c', 'SEO продвижение сайта', 'Аудит + базовая оптимизация, ежемесячно', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-e0d16d661eb1', now(), now(), '00000000-0000-0000-0001-efc4d0884dc0', '00000000-0000-0000-0001-0280b98e17e8', 'ONLINE', 80000, 43200, 'По согласованию', false, 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-467b4ffd1dfe', now(), now(), '00000000-0000-0000-0001-efc4d0884dc0', '00000000-0000-0000-0001-f5959dd0b520', 'ONLINE', 80000, 43200, 'По согласованию', false, 'ACTIVE');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-b8645aa5257b', now(), now(), '00000000-0000-0000-0001-a8be200cc8bd', '00000000-0000-0000-0001-a686baabac2c', 'Ремонт ноутбука', 'Диагностика + замена комплектующих', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-c3488daa3aa8', now(), now(), '00000000-0000-0000-0001-b8645aa5257b', '00000000-0000-0000-0001-0280b98e17e8', 'ON_SITE', 5000, 60, 'Пн-Сб 10:00-19:00', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-1f1d3c411e7d', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-c3488daa3aa8', 'Ремонт ноутбука', 'Диагностика + замена комплектующих', 'IT услуги', null, null, '00000000-0000-0000-0001-a8be200cc8bd', '00000000-0000-0000-0001-0280b98e17e8', 5000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-1f1d3c411e7d', 'ремонт'), ('00000000-0000-0000-0001-1f1d3c411e7d', 'ноутбука'), ('00000000-0000-0000-0001-1f1d3c411e7d', 'услуги');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-3716cb64a924', now(), now(), '00000000-0000-0000-0001-b8645aa5257b', '00000000-0000-0000-0001-f5959dd0b520', 'ON_SITE', 5000, 60, 'Пн-Сб 10:00-19:00', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-8867f273ded3', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-3716cb64a924', 'Ремонт ноутбука', 'Диагностика + замена комплектующих', 'IT услуги', null, null, '00000000-0000-0000-0001-a8be200cc8bd', '00000000-0000-0000-0001-f5959dd0b520', 5000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-8867f273ded3', 'ремонт'), ('00000000-0000-0000-0001-8867f273ded3', 'ноутбука'), ('00000000-0000-0000-0001-8867f273ded3', 'услуги');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-01b479ccbfec', now(), now(), '00000000-0000-0000-0001-a8be200cc8bd', '00000000-0000-0000-0001-a686baabac2c', 'Установка и настройка 1С', 'Установка, настройка, обучение персонала', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-9f3617af0c91', now(), now(), '00000000-0000-0000-0001-01b479ccbfec', '00000000-0000-0000-0001-0280b98e17e8', 'ONLINE', 75000, 7200, 'По согласованию', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-317e8d6407f0', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-9f3617af0c91', 'Установка и настройка 1С', 'Установка, настройка, обучение персонала', 'IT услуги', null, null, '00000000-0000-0000-0001-a8be200cc8bd', '00000000-0000-0000-0001-0280b98e17e8', 75000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-317e8d6407f0', 'установка'), ('00000000-0000-0000-0001-317e8d6407f0', 'настройка'), ('00000000-0000-0000-0001-317e8d6407f0', 'услуги');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-b04206a0610a', now(), now(), '00000000-0000-0000-0001-01b479ccbfec', '00000000-0000-0000-0001-f5959dd0b520', 'ONLINE', 75000, 7200, 'По согласованию', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-a03e4a4b7cd5', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-b04206a0610a', 'Установка и настройка 1С', 'Установка, настройка, обучение персонала', 'IT услуги', null, null, '00000000-0000-0000-0001-a8be200cc8bd', '00000000-0000-0000-0001-f5959dd0b520', 75000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-a03e4a4b7cd5', 'установка'), ('00000000-0000-0000-0001-a03e4a4b7cd5', 'настройка'), ('00000000-0000-0000-0001-a03e4a4b7cd5', 'услуги');

-- Компания-Микс-7 (ИП, Автозапчасти, Алматы)
INSERT INTO business (id, created_at, updated_at, name, legal_name, bin, status) VALUES
  ('00000000-0000-0000-0001-8bb4e7d25ac2', now(), now(), 'Компания-Микс-7', 'ИП "Компания-Микс-7"', '890123456789', 'ACTIVE');
INSERT INTO business_branch (id, created_at, updated_at, business_id, city_id, name, address, latitude, longitude, online_only, status) VALUES
  ('00000000-0000-0000-0001-a379660d52fa', now(), now(), '00000000-0000-0000-0001-8bb4e7d25ac2', '00000000-0000-0000-0000-0000000000c2', 'Компания-Микс-7 - Офис', 'пр. Достык, 88', 44.8472, 65.5070, false, 'ACTIVE');
INSERT INTO data_source (id, created_at, updated_at, business_id, source_type, name, status) VALUES
  ('00000000-0000-0000-0001-73744e91820d', now(), now(), '00000000-0000-0000-0001-8bb4e7d25ac2', 'MANUAL', 'Ручной ввод Компания-Микс-7', 'ACTIVE');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-f5b06a2f00aa', now(), now(), '00000000-0000-0000-0001-8bb4e7d25ac2', '00000000-0000-0000-0000-0000000000a1', 'Автозапчасти', 'Ремень ГРМ Gates', 'Ремень ГРМ с роликом, комплект', 'GTS-TB-KIT', '{"бренд":"Gates","тип":"комплект"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-9f7a0878bcb9', now(), now(), '00000000-0000-0000-0001-f5b06a2f00aa', '00000000-0000-0000-0001-a379660d52fa', '00000000-0000-0000-0001-73744e91820d', 22000, false, 'ACTIVE');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-af56cdab299c', now(), now(), '00000000-0000-0000-0001-8bb4e7d25ac2', '00000000-0000-0000-0000-0000000000a1', 'Автозапчасти', 'Масло подсолнечное 5л', 'Рафинированное дезодорированное', 'MAS-POD-5L', '{"тип":"рафинированное","объем":"5л"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-5547d4032709', now(), now(), '00000000-0000-0000-0001-af56cdab299c', '00000000-0000-0000-0001-a379660d52fa', '00000000-0000-0000-0001-73744e91820d', 4500, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-f5865a492a55', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-5547d4032709', null, 'Масло подсолнечное 5л', 'Рафинированное дезодорированное', 'Автозапчасти', 'MAS-POD-5L', '{"тип":"рафинированное","объем":"5л"}', '00000000-0000-0000-0001-8bb4e7d25ac2', '00000000-0000-0000-0001-a379660d52fa', 4500, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-f5865a492a55', 'масло'), ('00000000-0000-0000-0001-f5865a492a55', 'подсолнечное'), ('00000000-0000-0000-0001-f5865a492a55', 'автозапчасти');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-3fbc402a0483', now(), now(), '00000000-0000-0000-0001-8bb4e7d25ac2', null, 'Автозапчасти', 'Масляный фильтр Sakura', 'Фильтр масляный для японских авто', 'SAK-OF-001', '{"бренд":"Sakura","тип":"масляный"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-e8fc9bf525a5', now(), now(), '00000000-0000-0000-0001-3fbc402a0483', '00000000-0000-0000-0001-a379660d52fa', '00000000-0000-0000-0001-73744e91820d', 3500, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-0abe24d5efd6', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-e8fc9bf525a5', null, 'Масляный фильтр Sakura', 'Фильтр масляный для японских авто', 'Автозапчасти', 'SAK-OF-001', '{"бренд":"Sakura","тип":"масляный"}', '00000000-0000-0000-0001-8bb4e7d25ac2', '00000000-0000-0000-0001-a379660d52fa', 3500, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-0abe24d5efd6', 'масляный'), ('00000000-0000-0000-0001-0abe24d5efd6', 'фильтр'), ('00000000-0000-0000-0001-0abe24d5efd6', 'sakura'), ('00000000-0000-0000-0001-0abe24d5efd6', 'автозапчасти');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-0500625e7f5e', now(), now(), '00000000-0000-0000-0001-8bb4e7d25ac2', '00000000-0000-0000-0000-0000000000a1', 'Автозапчасти', 'Тормозные колодки TRW', 'Передние колодки для европейских авто', 'TRW-BP-202', '{"бренд":"TRW","ось":"перед","тип":"керамика"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-70304ff9b8f9', now(), now(), '00000000-0000-0000-0001-0500625e7f5e', '00000000-0000-0000-0001-a379660d52fa', '00000000-0000-0000-0001-73744e91820d', 18500, true, 'ARCHIVED');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-1fc4d61ef2fd', now(), now(), '00000000-0000-0000-0001-8bb4e7d25ac2', '00000000-0000-0000-0000-0000000000a1', 'Автозапчасти', 'Свечи зажигания NGK', 'Иридиевые свечи, комплект 4 шт', 'NGK-IR-4', '{"бренд":"NGK","тип":"иридиевые","комплект":"4 шт"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-6375d16d0004', now(), now(), '00000000-0000-0000-0001-1fc4d61ef2fd', '00000000-0000-0000-0001-a379660d52fa', '00000000-0000-0000-0001-73744e91820d', 12000, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-6c132f3165e6', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-6375d16d0004', null, 'Свечи зажигания NGK', 'Иридиевые свечи, комплект 4 шт', 'Автозапчасти', 'NGK-IR-4', '{"бренд":"NGK","тип":"иридиевые","комплект":"4 шт"}', '00000000-0000-0000-0001-8bb4e7d25ac2', '00000000-0000-0000-0001-a379660d52fa', 12000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-6c132f3165e6', 'свечи'), ('00000000-0000-0000-0001-6c132f3165e6', 'зажигания'), ('00000000-0000-0000-0001-6c132f3165e6', 'ngk'), ('00000000-0000-0000-0001-6c132f3165e6', 'автозапчасти');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-eb66a1feb329', now(), now(), '00000000-0000-0000-0001-8bb4e7d25ac2', '00000000-0000-0000-0000-0000000000a1', 'Педикюр классический', 'Классический педикюр с покрытием', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-6bfea86d1f8a', now(), now(), '00000000-0000-0000-0001-eb66a1feb329', '00000000-0000-0000-0001-a379660d52fa', 'ONLINE', 7000, 90, 'Ежедневно 10:00-20:00', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-c3031da745bc', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-6bfea86d1f8a', 'Педикюр классический', 'Классический педикюр с покрытием', 'Автозапчасти', null, null, '00000000-0000-0000-0001-8bb4e7d25ac2', '00000000-0000-0000-0001-a379660d52fa', 7000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-c3031da745bc', 'педикюр'), ('00000000-0000-0000-0001-c3031da745bc', 'классический'), ('00000000-0000-0000-0001-c3031da745bc', 'автозапчасти');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-1f10e0b1536f', now(), now(), '00000000-0000-0000-0001-8bb4e7d25ac2', '00000000-0000-0000-0000-0000000000a1', 'Наращивание ресниц 2D', 'Классическое наращивание, эффект 4 недели', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-38ea4c5218b4', now(), now(), '00000000-0000-0000-0001-1f10e0b1536f', '00000000-0000-0000-0001-a379660d52fa', 'ONLINE', 10000, 120, 'Пн-Сб 09:00-19:00', false, 'ACTIVE');

-- Компания-Микс-8 (ТОО, Бытовая техника, Астана)
INSERT INTO business (id, created_at, updated_at, name, legal_name, bin, status) VALUES
  ('00000000-0000-0000-0001-62cc519aae18', now(), now(), 'Компания-Микс-8', 'ТОО "Компания-Микс-8"', '901234567890', 'ACTIVE');
INSERT INTO business_branch (id, created_at, updated_at, business_id, city_id, name, address, latitude, longitude, online_only, status) VALUES
  ('00000000-0000-0000-0001-2fbbb5877f27', now(), now(), '00000000-0000-0000-0001-62cc519aae18', '00000000-0000-0000-0000-0000000000c3', 'Компания-Микс-8 - Офис', 'пр. Кабанбай батыра, 58', 44.8482, 65.5080, false, 'ACTIVE');
INSERT INTO data_source (id, created_at, updated_at, business_id, source_type, name, status) VALUES
  ('00000000-0000-0000-0001-c9fa2a01dadc', now(), now(), '00000000-0000-0000-0001-62cc519aae18', 'MANUAL', 'Ручной ввод Компания-Микс-8', 'ACTIVE');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-1aa04295a9e6', now(), now(), '00000000-0000-0000-0001-62cc519aae18', '00000000-0000-0000-0000-0000000000a2', 'Бытовая техника', 'Холодильник LG 350L', 'Двухкамерный холодильник с No Frost', 'LG-RF-350', '{"бренд":"LG","объем":"350л","тип":"No Frost"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-2f60ca7136b5', now(), now(), '00000000-0000-0000-0001-1aa04295a9e6', '00000000-0000-0000-0001-2fbbb5877f27', '00000000-0000-0000-0001-c9fa2a01dadc', 189000, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-efc612ed18f7', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-2f60ca7136b5', null, 'Холодильник LG 350L', 'Двухкамерный холодильник с No Frost', 'Бытовая техника', 'LG-RF-350', '{"бренд":"LG","объем":"350л","тип":"No Frost"}', '00000000-0000-0000-0001-62cc519aae18', '00000000-0000-0000-0001-2fbbb5877f27', 189000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-efc612ed18f7', 'холодильник'), ('00000000-0000-0000-0001-efc612ed18f7', '350l'), ('00000000-0000-0000-0001-efc612ed18f7', 'бытовая'), ('00000000-0000-0000-0001-efc612ed18f7', 'техника');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-a6364930fe1f', now(), now(), '00000000-0000-0000-0001-62cc519aae18', null, 'Бытовая техника', 'Стиральная машина Bosch', 'Фронтальная загрузка 7кг, 1200 об/мин', 'BOS-WM-7', '{"бренд":"Bosch","загрузка":"7кг","обороты":"1200"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-83585a6651b3', now(), now(), '00000000-0000-0000-0001-a6364930fe1f', '00000000-0000-0000-0001-2fbbb5877f27', '00000000-0000-0000-0001-c9fa2a01dadc', 245000, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-b31a9480801c', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-83585a6651b3', null, 'Стиральная машина Bosch', 'Фронтальная загрузка 7кг, 1200 об/мин', 'Бытовая техника', 'BOS-WM-7', '{"бренд":"Bosch","загрузка":"7кг","обороты":"1200"}', '00000000-0000-0000-0001-62cc519aae18', '00000000-0000-0000-0001-2fbbb5877f27', 245000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-b31a9480801c', 'стиральная'), ('00000000-0000-0000-0001-b31a9480801c', 'машина'), ('00000000-0000-0000-0001-b31a9480801c', 'bosch'), ('00000000-0000-0000-0001-b31a9480801c', 'бытовая'), ('00000000-0000-0000-0001-b31a9480801c', 'техника');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-164ea8bd860a', now(), now(), '00000000-0000-0000-0001-62cc519aae18', '00000000-0000-0000-0000-0000000000a2', 'Подготовка к IELTS', 'Интенсивный курс, 20 занятий по 120 мин', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-dd01d45092cd', now(), now(), '00000000-0000-0000-0001-164ea8bd860a', '00000000-0000-0000-0001-2fbbb5877f27', 'ONLINE', 85000, 2400, 'Утро 09:00 или вечер 18:00', false, 'ACTIVE');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-21b88c3e9241', now(), now(), '00000000-0000-0000-0001-62cc519aae18', '00000000-0000-0000-0000-0000000000a2', 'Репетиторство по математике', 'Подготовка к ЕНТ, индивидуально, 60 мин', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-2d34585c7245', now(), now(), '00000000-0000-0000-0001-21b88c3e9241', '00000000-0000-0000-0001-2fbbb5877f27', 'ON_SITE', 4000, 60, 'По согласованию', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-fcaed6cad6d5', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-2d34585c7245', 'Репетиторство по математике', 'Подготовка к ЕНТ, индивидуально, 60 мин', 'Бытовая техника', null, null, '00000000-0000-0000-0001-62cc519aae18', '00000000-0000-0000-0001-2fbbb5877f27', 4000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-fcaed6cad6d5', 'репетиторство'), ('00000000-0000-0000-0001-fcaed6cad6d5', 'математике'), ('00000000-0000-0000-0001-fcaed6cad6d5', 'бытовая'), ('00000000-0000-0000-0001-fcaed6cad6d5', 'техника');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-315027eede07', now(), now(), '00000000-0000-0000-0001-62cc519aae18', '00000000-0000-0000-0000-0000000000a2', 'Курс видеомонтажа DaVinci', 'Основы цветокоррекции и монтажа, 10 занятий', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-915aba7620b4', now(), now(), '00000000-0000-0000-0001-315027eede07', '00000000-0000-0000-0001-2fbbb5877f27', 'ONLINE', 35000, 600, 'Вт-Чт 18:00-20:00', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-c782ab0eebe9', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-915aba7620b4', 'Курс видеомонтажа DaVinci', 'Основы цветокоррекции и монтажа, 10 занятий', 'Бытовая техника', null, null, '00000000-0000-0000-0001-62cc519aae18', '00000000-0000-0000-0001-2fbbb5877f27', 35000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-c782ab0eebe9', 'курс'), ('00000000-0000-0000-0001-c782ab0eebe9', 'видеомонтажа'), ('00000000-0000-0000-0001-c782ab0eebe9', 'davinci'), ('00000000-0000-0000-0001-c782ab0eebe9', 'бытовая'), ('00000000-0000-0000-0001-c782ab0eebe9', 'техника');

-- Компания-Микс-9 (ИП, Услуги красоты, Шымкент)
INSERT INTO business (id, created_at, updated_at, name, legal_name, bin, status) VALUES
  ('00000000-0000-0000-0001-1043e206087f', now(), now(), 'Компания-Микс-9', 'ИП "Компания-Микс-9"', '012345678901', 'ACTIVE');
INSERT INTO business_branch (id, created_at, updated_at, business_id, city_id, name, address, latitude, longitude, online_only, status) VALUES
  ('00000000-0000-0000-0001-93b60dcc0e12', now(), now(), '00000000-0000-0000-0001-1043e206087f', '00000000-0000-0000-0001-1a904d507278', 'Компания-Микс-9 - Офис', 'пр. Кунаева, 120', 44.8492, 65.5090, true, 'ACTIVE');
INSERT INTO data_source (id, created_at, updated_at, business_id, source_type, name, status) VALUES
  ('00000000-0000-0000-0001-0bc79fa42ed0', now(), now(), '00000000-0000-0000-0001-1043e206087f', 'MANUAL', 'Ручной ввод Компания-Микс-9', 'ACTIVE');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-b9ccf70dc84f', now(), now(), '00000000-0000-0000-0001-1043e206087f', null, 'Услуги красоты', 'Масляный фильтр Sakura', 'Фильтр масляный для японских авто', 'SAK-OF-001', '{"бренд":"Sakura","тип":"масляный"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-25f118d92302', now(), now(), '00000000-0000-0000-0001-b9ccf70dc84f', '00000000-0000-0000-0001-93b60dcc0e12', '00000000-0000-0000-0001-0bc79fa42ed0', 3500, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-20af138807f2', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-25f118d92302', null, 'Масляный фильтр Sakura', 'Фильтр масляный для японских авто', 'Услуги красоты', 'SAK-OF-001', '{"бренд":"Sakura","тип":"масляный"}', '00000000-0000-0000-0001-1043e206087f', '00000000-0000-0000-0001-93b60dcc0e12', 3500, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-20af138807f2', 'масляный'), ('00000000-0000-0000-0001-20af138807f2', 'фильтр'), ('00000000-0000-0000-0001-20af138807f2', 'sakura'), ('00000000-0000-0000-0001-20af138807f2', 'услуги'), ('00000000-0000-0000-0001-20af138807f2', 'красоты');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-bf573f6fd283', now(), now(), '00000000-0000-0000-0001-1043e206087f', '00000000-0000-0000-0000-0000000000a3', 'Услуги красоты', 'Тормозные колодки TRW', 'Передние колодки для европейских авто', 'TRW-BP-202', '{"бренд":"TRW","ось":"перед","тип":"керамика"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-06823ae7ab47', now(), now(), '00000000-0000-0000-0001-bf573f6fd283', '00000000-0000-0000-0001-93b60dcc0e12', '00000000-0000-0000-0001-0bc79fa42ed0', 18500, true, 'ARCHIVED');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-c7592ab9f7fb', now(), now(), '00000000-0000-0000-0001-1043e206087f', '00000000-0000-0000-0000-0000000000a3', 'Услуги красоты', 'Свечи зажигания NGK', 'Иридиевые свечи, комплект 4 шт', 'NGK-IR-4', '{"бренд":"NGK","тип":"иридиевые","комплект":"4 шт"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-3c20f6d1ece1', now(), now(), '00000000-0000-0000-0001-c7592ab9f7fb', '00000000-0000-0000-0001-93b60dcc0e12', '00000000-0000-0000-0001-0bc79fa42ed0', 12000, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-f4964baacece', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-3c20f6d1ece1', null, 'Свечи зажигания NGK', 'Иридиевые свечи, комплект 4 шт', 'Услуги красоты', 'NGK-IR-4', '{"бренд":"NGK","тип":"иридиевые","комплект":"4 шт"}', '00000000-0000-0000-0001-1043e206087f', '00000000-0000-0000-0001-93b60dcc0e12', 12000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-f4964baacece', 'свечи'), ('00000000-0000-0000-0001-f4964baacece', 'зажигания'), ('00000000-0000-0000-0001-f4964baacece', 'ngk'), ('00000000-0000-0000-0001-f4964baacece', 'услуги'), ('00000000-0000-0000-0001-f4964baacece', 'красоты');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-c8b3fa2baf13', now(), now(), '00000000-0000-0000-0001-1043e206087f', '00000000-0000-0000-0000-0000000000a3', 'Стрижка женская модельная', 'Стрижка с укладкой и уходом, 60 мин', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-d084ef4981ed', now(), now(), '00000000-0000-0000-0001-c8b3fa2baf13', '00000000-0000-0000-0001-93b60dcc0e12', 'ON_SITE', 6000, 60, 'Ежедневно 10:00-20:00', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-917b91c10ec9', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-d084ef4981ed', 'Стрижка женская модельная', 'Стрижка с укладкой и уходом, 60 мин', 'Услуги красоты', null, null, '00000000-0000-0000-0001-1043e206087f', '00000000-0000-0000-0001-93b60dcc0e12', 6000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-917b91c10ec9', 'стрижка'), ('00000000-0000-0000-0001-917b91c10ec9', 'женская'), ('00000000-0000-0000-0001-917b91c10ec9', 'модельная'), ('00000000-0000-0000-0001-917b91c10ec9', 'услуги'), ('00000000-0000-0000-0001-917b91c10ec9', 'красоты');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-410e60205052', now(), now(), '00000000-0000-0000-0001-1043e206087f', '00000000-0000-0000-0000-0000000000a3', 'Маникюр аппаратный', 'Аппаратный маникюр + покрытие гель-лак', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-50a98184080e', now(), now(), '00000000-0000-0000-0001-410e60205052', '00000000-0000-0000-0001-93b60dcc0e12', 'ONLINE', 5500, 75, 'Пн-Сб 09:00-18:00', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-967d17c12fa4', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-50a98184080e', 'Маникюр аппаратный', 'Аппаратный маникюр + покрытие гель-лак', 'Услуги красоты', null, null, '00000000-0000-0000-0001-1043e206087f', '00000000-0000-0000-0001-93b60dcc0e12', 5500, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-967d17c12fa4', 'маникюр'), ('00000000-0000-0000-0001-967d17c12fa4', 'аппаратный'), ('00000000-0000-0000-0001-967d17c12fa4', 'услуги'), ('00000000-0000-0000-0001-967d17c12fa4', 'красоты');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-d33d11569756', now(), now(), '00000000-0000-0000-0001-1043e206087f', '00000000-0000-0000-0000-0000000000a3', 'Педикюр классический', 'Классический педикюр с покрытием', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-46a8019dc729', now(), now(), '00000000-0000-0000-0001-d33d11569756', '00000000-0000-0000-0001-93b60dcc0e12', 'ONLINE', 7000, 90, 'Ежедневно 10:00-20:00', true, 'ARCHIVED');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-f10420ca949b', now(), now(), '00000000-0000-0000-0001-1043e206087f', '00000000-0000-0000-0000-0000000000a3', 'Наращивание ресниц 2D', 'Классическое наращивание, эффект 4 недели', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-4a4d6a822c24', now(), now(), '00000000-0000-0000-0001-f10420ca949b', '00000000-0000-0000-0001-93b60dcc0e12', 'ON_SITE', 10000, 120, 'Пн-Сб 09:00-19:00', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-a49bd822b80d', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-4a4d6a822c24', 'Наращивание ресниц 2D', 'Классическое наращивание, эффект 4 недели', 'Услуги красоты', null, null, '00000000-0000-0000-0001-1043e206087f', '00000000-0000-0000-0001-93b60dcc0e12', 10000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-a49bd822b80d', 'наращивание'), ('00000000-0000-0000-0001-a49bd822b80d', 'ресниц'), ('00000000-0000-0000-0001-a49bd822b80d', 'услуги'), ('00000000-0000-0000-0001-a49bd822b80d', 'красоты');

-- Компания-Микс-10 (ТОО, Ремонт и сервис, Караганда)
INSERT INTO business (id, created_at, updated_at, name, legal_name, bin, status) VALUES
  ('00000000-0000-0000-0001-df2453efe682', now(), now(), 'Компания-Микс-10', 'ТОО "Компания-Микс-10"', '123456789012', 'ACTIVE');
INSERT INTO business_branch (id, created_at, updated_at, business_id, city_id, name, address, latitude, longitude, online_only, status) VALUES
  ('00000000-0000-0000-0001-e3b16bac9fc2', now(), now(), '00000000-0000-0000-0001-df2453efe682', '00000000-0000-0000-0001-02ba3c39b41d', 'Компания-Микс-10 - Офис', 'ул. Чкалова, 18', 44.8502, 65.5100, false, 'ACTIVE');
INSERT INTO data_source (id, created_at, updated_at, business_id, source_type, name, status) VALUES
  ('00000000-0000-0000-0001-702714d117d8', now(), now(), '00000000-0000-0000-0001-df2453efe682', 'MANUAL', 'Ручной ввод Компания-Микс-10', 'ACTIVE');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-db96e6248bde', now(), now(), '00000000-0000-0000-0001-df2453efe682', '00000000-0000-0000-0000-0000000000a4', 'Ремонт и сервис', 'Шуруповерт Makita 18В', 'Аккумуляторный, 2 батареи, кейс', 'MAK-SD-18', '{"бренд":"Makita","напряжение":"18В","акб":"2 шт"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-b1263f2ec19e', now(), now(), '00000000-0000-0000-0001-db96e6248bde', '00000000-0000-0000-0001-e3b16bac9fc2', '00000000-0000-0000-0001-702714d117d8', 45000, true, 'ARCHIVED');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-a5e75d7c9b04', now(), now(), '00000000-0000-0000-0001-df2453efe682', '00000000-0000-0000-0000-0000000000a4', 'Ремонт и сервис', 'Болгарка УШМ 125мм', 'Угловая шлифмашина, 1100Вт', 'USH-125-1100', '{"диаметр":"125мм","мощность":"1100Вт"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-0b99e184e1c2', now(), now(), '00000000-0000-0000-0001-a5e75d7c9b04', '00000000-0000-0000-0001-e3b16bac9fc2', '00000000-0000-0000-0001-702714d117d8', 15000, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-c0404b89eb6a', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-0b99e184e1c2', null, 'Болгарка УШМ 125мм', 'Угловая шлифмашина, 1100Вт', 'Ремонт и сервис', 'USH-125-1100', '{"диаметр":"125мм","мощность":"1100Вт"}', '00000000-0000-0000-0001-df2453efe682', '00000000-0000-0000-0001-e3b16bac9fc2', 15000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-c0404b89eb6a', 'болгарка'), ('00000000-0000-0000-0001-c0404b89eb6a', 'ушм'), ('00000000-0000-0000-0001-c0404b89eb6a', '125мм'), ('00000000-0000-0000-0001-c0404b89eb6a', 'ремонт'), ('00000000-0000-0000-0001-c0404b89eb6a', 'сервис');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-7a881568b35a', now(), now(), '00000000-0000-0000-0001-df2453efe682', null, 'Ремонт и сервис', 'Дрель ударная Bosch 750Вт', 'Дрель ударная с реверсом, кейс', 'BOS-DR-750', '{"бренд":"Bosch","мощность":"750Вт","тип":"ударная"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-2a56e6441d50', now(), now(), '00000000-0000-0000-0001-7a881568b35a', '00000000-0000-0000-0001-e3b16bac9fc2', '00000000-0000-0000-0001-702714d117d8', 28000, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-5060a5cfac08', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-2a56e6441d50', null, 'Дрель ударная Bosch 750Вт', 'Дрель ударная с реверсом, кейс', 'Ремонт и сервис', 'BOS-DR-750', '{"бренд":"Bosch","мощность":"750Вт","тип":"ударная"}', '00000000-0000-0000-0001-df2453efe682', '00000000-0000-0000-0001-e3b16bac9fc2', 28000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-5060a5cfac08', 'дрель'), ('00000000-0000-0000-0001-5060a5cfac08', 'ударная'), ('00000000-0000-0000-0001-5060a5cfac08', 'bosch'), ('00000000-0000-0000-0001-5060a5cfac08', '750вт'), ('00000000-0000-0000-0001-5060a5cfac08', 'ремонт'), ('00000000-0000-0000-0001-5060a5cfac08', 'сервис');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-158ea13a5660', now(), now(), '00000000-0000-0000-0001-df2453efe682', '00000000-0000-0000-0000-0000000000a4', 'Ремонт и сервис', 'Шуруповерт Makita 18В', 'Аккумуляторный, 2 батареи, кейс', 'MAK-SD-18', '{"бренд":"Makita","напряжение":"18В","акб":"2 шт"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-bf1c7fec85a2', now(), now(), '00000000-0000-0000-0001-158ea13a5660', '00000000-0000-0000-0001-e3b16bac9fc2', '00000000-0000-0000-0001-702714d117d8', 45000, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-a83ee2efad73', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-bf1c7fec85a2', null, 'Шуруповерт Makita 18В', 'Аккумуляторный, 2 батареи, кейс', 'Ремонт и сервис', 'MAK-SD-18', '{"бренд":"Makita","напряжение":"18В","акб":"2 шт"}', '00000000-0000-0000-0001-df2453efe682', '00000000-0000-0000-0001-e3b16bac9fc2', 45000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-a83ee2efad73', 'шуруповерт'), ('00000000-0000-0000-0001-a83ee2efad73', 'makita'), ('00000000-0000-0000-0001-a83ee2efad73', '18в'), ('00000000-0000-0000-0001-a83ee2efad73', 'ремонт'), ('00000000-0000-0000-0001-a83ee2efad73', 'сервис');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-4e8fddb05b4f', now(), now(), '00000000-0000-0000-0001-df2453efe682', '00000000-0000-0000-0000-0000000000a4', 'Ремонт стиральной машины', 'Диагностика + ремонт на месте', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-3ba4a35927ff', now(), now(), '00000000-0000-0000-0001-4e8fddb05b4f', '00000000-0000-0000-0001-e3b16bac9fc2', 'ONLINE', 5000, 60, 'Пн-Сб 09:00-18:00', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-27f105155667', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-3ba4a35927ff', 'Ремонт стиральной машины', 'Диагностика + ремонт на месте', 'Ремонт и сервис', null, null, '00000000-0000-0000-0001-df2453efe682', '00000000-0000-0000-0001-e3b16bac9fc2', 5000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-27f105155667', 'ремонт'), ('00000000-0000-0000-0001-27f105155667', 'стиральной'), ('00000000-0000-0000-0001-27f105155667', 'машины'), ('00000000-0000-0000-0001-27f105155667', 'ремонт'), ('00000000-0000-0000-0001-27f105155667', 'сервис');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-66d0a208bc50', now(), now(), '00000000-0000-0000-0001-df2453efe682', '00000000-0000-0000-0000-0000000000a4', 'Установка кондиционера', 'Монтаж сплит-системы под ключ', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-9940a310dc1e', now(), now(), '00000000-0000-0000-0001-66d0a208bc50', '00000000-0000-0000-0001-e3b16bac9fc2', 'ONLINE', 25000, 180, 'Ежедневно, по записи', true, 'ARCHIVED');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-111ad95b6db0', now(), now(), '00000000-0000-0000-0001-df2453efe682', '00000000-0000-0000-0000-0000000000a4', 'Замена электропроводки', 'Полная замена в 2-комнатной квартире', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-9b97f1ab13ac', now(), now(), '00000000-0000-0000-0001-111ad95b6db0', '00000000-0000-0000-0001-e3b16bac9fc2', 'ON_SITE', 180000, 1440, 'По согласованию', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-ce1d613cd9f5', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-9b97f1ab13ac', 'Замена электропроводки', 'Полная замена в 2-комнатной квартире', 'Ремонт и сервис', null, null, '00000000-0000-0000-0001-df2453efe682', '00000000-0000-0000-0001-e3b16bac9fc2', 180000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-ce1d613cd9f5', 'замена'), ('00000000-0000-0000-0001-ce1d613cd9f5', 'электропроводки'), ('00000000-0000-0000-0001-ce1d613cd9f5', 'ремонт'), ('00000000-0000-0000-0001-ce1d613cd9f5', 'сервис');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-ac04036dcc47', now(), now(), '00000000-0000-0000-0001-df2453efe682', '00000000-0000-0000-0000-0000000000a4', 'Сантехнические работы', 'Установка смесителя, ремонт бачка, прочистка', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-b3416360b6ef', now(), now(), '00000000-0000-0000-0001-ac04036dcc47', '00000000-0000-0000-0001-e3b16bac9fc2', 'ONLINE', 7000, 90, 'Ежедневно 08:00-20:00', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-17abdd41399b', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-b3416360b6ef', 'Сантехнические работы', 'Установка смесителя, ремонт бачка, прочистка', 'Ремонт и сервис', null, null, '00000000-0000-0000-0001-df2453efe682', '00000000-0000-0000-0001-e3b16bac9fc2', 7000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-17abdd41399b', 'сантехнические'), ('00000000-0000-0000-0001-17abdd41399b', 'работы'), ('00000000-0000-0000-0001-17abdd41399b', 'ремонт'), ('00000000-0000-0000-0001-17abdd41399b', 'сервис');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-8c07e4b8c7e6', now(), now(), '00000000-0000-0000-0001-df2453efe682', '00000000-0000-0000-0000-0000000000a4', 'Ремонт стиральной машины', 'Диагностика + ремонт на месте', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-018285dc7370', now(), now(), '00000000-0000-0000-0001-8c07e4b8c7e6', '00000000-0000-0000-0001-e3b16bac9fc2', 'ONLINE', 5000, 60, 'Пн-Сб 09:00-18:00', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-cedec69e3198', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-018285dc7370', 'Ремонт стиральной машины', 'Диагностика + ремонт на месте', 'Ремонт и сервис', null, null, '00000000-0000-0000-0001-df2453efe682', '00000000-0000-0000-0001-e3b16bac9fc2', 5000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-cedec69e3198', 'ремонт'), ('00000000-0000-0000-0001-cedec69e3198', 'стиральной'), ('00000000-0000-0000-0001-cedec69e3198', 'машины'), ('00000000-0000-0000-0001-cedec69e3198', 'ремонт'), ('00000000-0000-0000-0001-cedec69e3198', 'сервис');

-- Компания-Микс-11 (ИП, Строительство, Кызылорда)
INSERT INTO business (id, created_at, updated_at, name, legal_name, bin, status) VALUES
  ('00000000-0000-0000-0001-10c26bf1b4d7', now(), now(), 'Компания-Микс-11', 'ИП "Компания-Микс-11"', '234567890123', 'ACTIVE');
INSERT INTO business_branch (id, created_at, updated_at, business_id, city_id, name, address, latitude, longitude, online_only, status) VALUES
  ('00000000-0000-0000-0001-8bb2645a4d19', now(), now(), '00000000-0000-0000-0001-10c26bf1b4d7', '00000000-0000-0000-0000-0000000000c1', 'Компания-Микс-11 - Офис', 'ул. Толе би, 22', 44.8512, 65.5110, false, 'ACTIVE');
INSERT INTO data_source (id, created_at, updated_at, business_id, source_type, name, status) VALUES
  ('00000000-0000-0000-0001-f79bdba76516', now(), now(), '00000000-0000-0000-0001-10c26bf1b4d7', 'MANUAL', 'Ручной ввод Компания-Микс-11', 'ACTIVE');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-873e71b1dc0f', now(), now(), '00000000-0000-0000-0001-10c26bf1b4d7', '00000000-0000-0000-0000-0000000000a5', 'Строительство', 'Гипсокартон Knauf 12.5мм', 'Влагостойкий лист 2500x1200 мм', 'KNF-GKL-12', '{"бренд":"Knauf","толщина":"12.5мм","тип":"влагостойкий"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-c6b09b69a415', now(), now(), '00000000-0000-0000-0001-873e71b1dc0f', '00000000-0000-0000-0001-8bb2645a4d19', '00000000-0000-0000-0001-f79bdba76516', 4500, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-1c68a40b7e69', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-c6b09b69a415', null, 'Гипсокартон Knauf 12.5мм', 'Влагостойкий лист 2500x1200 мм', 'Строительство', 'KNF-GKL-12', '{"бренд":"Knauf","толщина":"12.5мм","тип":"влагостойкий"}', '00000000-0000-0000-0001-10c26bf1b4d7', '00000000-0000-0000-0001-8bb2645a4d19', 4500, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-1c68a40b7e69', 'гипсокартон'), ('00000000-0000-0000-0001-1c68a40b7e69', 'knauf'), ('00000000-0000-0000-0001-1c68a40b7e69', '12.5мм'), ('00000000-0000-0000-0001-1c68a40b7e69', 'строительство');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-e02623b57b7e', now(), now(), '00000000-0000-0000-0001-10c26bf1b4d7', null, 'Строительство', 'Профнастил С8 0.45мм', 'Оцинкованный профнастил для забора', 'PRF-C8-045', '{"марка":"С8","толщина":"0.45мм","тип":"оцинкованный"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-d99019027fa3', now(), now(), '00000000-0000-0000-0001-e02623b57b7e', '00000000-0000-0000-0001-8bb2645a4d19', '00000000-0000-0000-0001-f79bdba76516', 2800, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-ddc41fe9128a', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-d99019027fa3', null, 'Профнастил С8 0.45мм', 'Оцинкованный профнастил для забора', 'Строительство', 'PRF-C8-045', '{"марка":"С8","толщина":"0.45мм","тип":"оцинкованный"}', '00000000-0000-0000-0001-10c26bf1b4d7', '00000000-0000-0000-0001-8bb2645a4d19', 2800, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-ddc41fe9128a', 'профнастил'), ('00000000-0000-0000-0001-ddc41fe9128a', '0.45мм'), ('00000000-0000-0000-0001-ddc41fe9128a', 'строительство');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-72d941421777', now(), now(), '00000000-0000-0000-0001-10c26bf1b4d7', '00000000-0000-0000-0000-0000000000a5', 'Строительство', 'Металлочерепица Монтеррей', 'Полиэстер 0.5мм, коричневый', 'MCH-MT-05', '{"тип":"Монтеррей","толщина":"0.5мм","покрытие":"полиэстер"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-e223c13652c9', now(), now(), '00000000-0000-0000-0001-72d941421777', '00000000-0000-0000-0001-8bb2645a4d19', '00000000-0000-0000-0001-f79bdba76516', 5200, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-e3cce465a5b5', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-e223c13652c9', null, 'Металлочерепица Монтеррей', 'Полиэстер 0.5мм, коричневый', 'Строительство', 'MCH-MT-05', '{"тип":"Монтеррей","толщина":"0.5мм","покрытие":"полиэстер"}', '00000000-0000-0000-0001-10c26bf1b4d7', '00000000-0000-0000-0001-8bb2645a4d19', 5200, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-e3cce465a5b5', 'металлочерепица'), ('00000000-0000-0000-0001-e3cce465a5b5', 'монтеррей'), ('00000000-0000-0000-0001-e3cce465a5b5', 'строительство');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-780431b312c9', now(), now(), '00000000-0000-0000-0001-10c26bf1b4d7', '00000000-0000-0000-0000-0000000000a5', 'Строительство', 'Клей плиточный Ceresit CM11', 'Для керамической плитки, 25 кг', 'CER-CM11-25', '{"бренд":"Ceresit","вес":"25кг","назначение":"плитка"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-d45c0aaa7e59', now(), now(), '00000000-0000-0000-0001-780431b312c9', '00000000-0000-0000-0001-8bb2645a4d19', '00000000-0000-0000-0001-f79bdba76516', 3200, false, 'ACTIVE');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-33383420678b', now(), now(), '00000000-0000-0000-0001-10c26bf1b4d7', null, 'Строительство', 'Ламинат Classen 33 класс', 'Влагостойкий, дуб, 8мм, 2.22м²', 'CLS-LAM-33', '{"бренд":"Classen","класс":"33","толщина":"8мм","рисунок":"дуб"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-dd59e36dcebd', now(), now(), '00000000-0000-0000-0001-33383420678b', '00000000-0000-0000-0001-8bb2645a4d19', '00000000-0000-0000-0001-f79bdba76516', 8500, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-4453baeeb8d2', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-dd59e36dcebd', null, 'Ламинат Classen 33 класс', 'Влагостойкий, дуб, 8мм, 2.22м²', 'Строительство', 'CLS-LAM-33', '{"бренд":"Classen","класс":"33","толщина":"8мм","рисунок":"дуб"}', '00000000-0000-0000-0001-10c26bf1b4d7', '00000000-0000-0000-0001-8bb2645a4d19', 8500, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-4453baeeb8d2', 'ламинат'), ('00000000-0000-0000-0001-4453baeeb8d2', 'classen'), ('00000000-0000-0000-0001-4453baeeb8d2', 'класс'), ('00000000-0000-0000-0001-4453baeeb8d2', 'строительство');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-08f2caf0b219', now(), now(), '00000000-0000-0000-0001-10c26bf1b4d7', '00000000-0000-0000-0000-0000000000a5', 'Укладка ламината', 'Укладка с подложкой, 1м²', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-8e24b93b4eae', now(), now(), '00000000-0000-0000-0001-08f2caf0b219', '00000000-0000-0000-0001-8bb2645a4d19', 'ONLINE', 2500, 60, 'Ежедневно 08:00-18:00', true, 'ARCHIVED');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-8ba8311fc11e', now(), now(), '00000000-0000-0000-0001-10c26bf1b4d7', '00000000-0000-0000-0000-0000000000a5', 'Отделка квартиры под ключ', 'Черновая + чистовая отделка, материал заказчика', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-463c5c905b49', now(), now(), '00000000-0000-0000-0001-8ba8311fc11e', '00000000-0000-0000-0001-8bb2645a4d19', 'ON_SITE', 1500000, 43200, 'По договору', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-a70a0645967e', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-463c5c905b49', 'Отделка квартиры под ключ', 'Черновая + чистовая отделка, материал заказчика', 'Строительство', null, null, '00000000-0000-0000-0001-10c26bf1b4d7', '00000000-0000-0000-0001-8bb2645a4d19', 1500000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-a70a0645967e', 'отделка'), ('00000000-0000-0000-0001-a70a0645967e', 'квартиры'), ('00000000-0000-0000-0001-a70a0645967e', 'ключ'), ('00000000-0000-0000-0001-a70a0645967e', 'строительство');

-- Компания-Микс-12 (ТОО, Продукты питания, Алматы)
INSERT INTO business (id, created_at, updated_at, name, legal_name, bin, status) VALUES
  ('00000000-0000-0000-0001-c9d49824314b', now(), now(), 'Компания-Микс-12', 'ТОО "Компания-Микс-12"', '345678901234', 'ACTIVE');
INSERT INTO business_branch (id, created_at, updated_at, business_id, city_id, name, address, latitude, longitude, online_only, status) VALUES
  ('00000000-0000-0000-0001-f7b2e76cbadd', now(), now(), '00000000-0000-0000-0001-c9d49824314b', '00000000-0000-0000-0000-0000000000c2', 'Компания-Микс-12 - Филиал 1', 'ул. Тимирязева, 42', 44.8522, 65.5120, false, 'ACTIVE');
INSERT INTO business_branch (id, created_at, updated_at, business_id, city_id, name, address, latitude, longitude, online_only, status) VALUES
  ('00000000-0000-0000-0001-ea091ecba558', now(), now(), '00000000-0000-0000-0001-c9d49824314b', '00000000-0000-0000-0000-0000000000c2', 'Компания-Микс-12 - Филиал 2', 'ул. Тимирязева, 42', 44.8522, 65.5120, false, 'ACTIVE');
INSERT INTO data_source (id, created_at, updated_at, business_id, source_type, name, status) VALUES
  ('00000000-0000-0000-0001-9926cffbb863', now(), now(), '00000000-0000-0000-0001-c9d49824314b', 'MANUAL', 'Ручной ввод Компания-Микс-12', 'ACTIVE');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-e6c844bf58cc', now(), now(), '00000000-0000-0000-0001-c9d49824314b', null, 'Продукты питания', 'Мука пшеничная высший сорт', 'Казахстанская мука, 50 кг мешок', 'MUK-VS-50', '{"сорт":"высший","вес":"50кг","страна":"Казахстан"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-2de24af64229', now(), now(), '00000000-0000-0000-0001-e6c844bf58cc', '00000000-0000-0000-0001-f7b2e76cbadd', '00000000-0000-0000-0001-9926cffbb863', 12000, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-ac5038bb9c54', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-2de24af64229', null, 'Мука пшеничная высший сорт', 'Казахстанская мука, 50 кг мешок', 'Продукты питания', 'MUK-VS-50', '{"сорт":"высший","вес":"50кг","страна":"Казахстан"}', '00000000-0000-0000-0001-c9d49824314b', '00000000-0000-0000-0001-f7b2e76cbadd', 12000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-ac5038bb9c54', 'мука'), ('00000000-0000-0000-0001-ac5038bb9c54', 'пшеничная'), ('00000000-0000-0000-0001-ac5038bb9c54', 'высший'), ('00000000-0000-0000-0001-ac5038bb9c54', 'сорт'), ('00000000-0000-0000-0001-ac5038bb9c54', 'продукты'), ('00000000-0000-0000-0001-ac5038bb9c54', 'питания');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-24b8affd79cf', now(), now(), '00000000-0000-0000-0001-e6c844bf58cc', '00000000-0000-0000-0001-ea091ecba558', '00000000-0000-0000-0001-9926cffbb863', 12000, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-2e429cf818fd', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-24b8affd79cf', null, 'Мука пшеничная высший сорт', 'Казахстанская мука, 50 кг мешок', 'Продукты питания', 'MUK-VS-50', '{"сорт":"высший","вес":"50кг","страна":"Казахстан"}', '00000000-0000-0000-0001-c9d49824314b', '00000000-0000-0000-0001-ea091ecba558', 12000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-2e429cf818fd', 'мука'), ('00000000-0000-0000-0001-2e429cf818fd', 'пшеничная'), ('00000000-0000-0000-0001-2e429cf818fd', 'высший'), ('00000000-0000-0000-0001-2e429cf818fd', 'сорт'), ('00000000-0000-0000-0001-2e429cf818fd', 'продукты'), ('00000000-0000-0000-0001-2e429cf818fd', 'питания');
INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0001-c75bc32c34bc', now(), now(), '00000000-0000-0000-0001-c9d49824314b', '00000000-0000-0000-0001-279580d8f28e', 'Продукты питания', 'Рис пропаренный 900г', 'Длиннозерный пропаренный рис', 'RIS-PR-900', '{"тип":"пропаренный","вес":"900г"}', 'ACTIVE');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-3903d9848440', now(), now(), '00000000-0000-0000-0001-c75bc32c34bc', '00000000-0000-0000-0001-f7b2e76cbadd', '00000000-0000-0000-0001-9926cffbb863', 850, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-a2c00a0138c7', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-3903d9848440', null, 'Рис пропаренный 900г', 'Длиннозерный пропаренный рис', 'Продукты питания', 'RIS-PR-900', '{"тип":"пропаренный","вес":"900г"}', '00000000-0000-0000-0001-c9d49824314b', '00000000-0000-0000-0001-f7b2e76cbadd', 850, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-a2c00a0138c7', 'рис'), ('00000000-0000-0000-0001-a2c00a0138c7', 'пропаренный'), ('00000000-0000-0000-0001-a2c00a0138c7', '900г'), ('00000000-0000-0000-0001-a2c00a0138c7', 'продукты'), ('00000000-0000-0000-0001-a2c00a0138c7', 'питания');
INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0001-67173e6441a7', now(), now(), '00000000-0000-0000-0001-c75bc32c34bc', '00000000-0000-0000-0001-ea091ecba558', '00000000-0000-0000-0001-9926cffbb863', 850, true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-ca1a109b6d67', now(), now(), 'PRODUCT', '00000000-0000-0000-0001-67173e6441a7', null, 'Рис пропаренный 900г', 'Длиннозерный пропаренный рис', 'Продукты питания', 'RIS-PR-900', '{"тип":"пропаренный","вес":"900г"}', '00000000-0000-0000-0001-c9d49824314b', '00000000-0000-0000-0001-ea091ecba558', 850, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-ca1a109b6d67', 'рис'), ('00000000-0000-0000-0001-ca1a109b6d67', 'пропаренный'), ('00000000-0000-0000-0001-ca1a109b6d67', '900г'), ('00000000-0000-0000-0001-ca1a109b6d67', 'продукты'), ('00000000-0000-0000-0001-ca1a109b6d67', 'питания');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-1864a030054a', now(), now(), '00000000-0000-0000-0001-c9d49824314b', '00000000-0000-0000-0001-279580d8f28e', 'Кейтеринг на мероприятие', 'Выездное обслуживание до 50 человек', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-41e44ca5790b', now(), now(), '00000000-0000-0000-0001-1864a030054a', '00000000-0000-0000-0001-f7b2e76cbadd', 'ON_SITE', 150000, 360, 'По записи, за 3 дня', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-cfa192912243', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-41e44ca5790b', 'Кейтеринг на мероприятие', 'Выездное обслуживание до 50 человек', 'Продукты питания', null, null, '00000000-0000-0000-0001-c9d49824314b', '00000000-0000-0000-0001-f7b2e76cbadd', 150000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-cfa192912243', 'кейтеринг'), ('00000000-0000-0000-0001-cfa192912243', 'мероприятие'), ('00000000-0000-0000-0001-cfa192912243', 'продукты'), ('00000000-0000-0000-0001-cfa192912243', 'питания');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-335d2529e90e', now(), now(), '00000000-0000-0000-0001-1864a030054a', '00000000-0000-0000-0001-ea091ecba558', 'ON_SITE', 150000, 360, 'По записи, за 3 дня', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-7133a3562a8b', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-335d2529e90e', 'Кейтеринг на мероприятие', 'Выездное обслуживание до 50 человек', 'Продукты питания', null, null, '00000000-0000-0000-0001-c9d49824314b', '00000000-0000-0000-0001-ea091ecba558', 150000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-7133a3562a8b', 'кейтеринг'), ('00000000-0000-0000-0001-7133a3562a8b', 'мероприятие'), ('00000000-0000-0000-0001-7133a3562a8b', 'продукты'), ('00000000-0000-0000-0001-7133a3562a8b', 'питания');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-40c84e8fca8f', now(), now(), '00000000-0000-0000-0001-c9d49824314b', '00000000-0000-0000-0001-279580d8f28e', 'Доставка продуктов на дом', 'Доставка в течение 2 часов по городу', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-8a895aedafe3', now(), now(), '00000000-0000-0000-0001-40c84e8fca8f', '00000000-0000-0000-0001-f7b2e76cbadd', 'ONLINE', 1500, 120, 'Ежедневно 08:00-22:00', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-e49de40b83c9', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-8a895aedafe3', 'Доставка продуктов на дом', 'Доставка в течение 2 часов по городу', 'Продукты питания', null, null, '00000000-0000-0000-0001-c9d49824314b', '00000000-0000-0000-0001-f7b2e76cbadd', 1500, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-e49de40b83c9', 'доставка'), ('00000000-0000-0000-0001-e49de40b83c9', 'продуктов'), ('00000000-0000-0000-0001-e49de40b83c9', 'дом'), ('00000000-0000-0000-0001-e49de40b83c9', 'продукты'), ('00000000-0000-0000-0001-e49de40b83c9', 'питания');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-2235e1b72dab', now(), now(), '00000000-0000-0000-0001-40c84e8fca8f', '00000000-0000-0000-0001-ea091ecba558', 'ONLINE', 1500, 120, 'Ежедневно 08:00-22:00', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-461bc19e6b50', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-2235e1b72dab', 'Доставка продуктов на дом', 'Доставка в течение 2 часов по городу', 'Продукты питания', null, null, '00000000-0000-0000-0001-c9d49824314b', '00000000-0000-0000-0001-ea091ecba558', 1500, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-461bc19e6b50', 'доставка'), ('00000000-0000-0000-0001-461bc19e6b50', 'продуктов'), ('00000000-0000-0000-0001-461bc19e6b50', 'дом'), ('00000000-0000-0000-0001-461bc19e6b50', 'продукты'), ('00000000-0000-0000-0001-461bc19e6b50', 'питания');
INSERT INTO service_offering (id, created_at, updated_at, business_id, category_id, name, description, status) VALUES
  ('00000000-0000-0000-0001-b2103b6d1e25', now(), now(), '00000000-0000-0000-0001-c9d49824314b', '00000000-0000-0000-0001-279580d8f28e', 'Кейтеринг на мероприятие', 'Выездное обслуживание до 50 человек', 'ACTIVE');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-ab88ba5b0b9e', now(), now(), '00000000-0000-0000-0001-b2103b6d1e25', '00000000-0000-0000-0001-f7b2e76cbadd', 'ONLINE', 150000, 360, 'По записи, за 3 дня', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-1a8724847f07', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-ab88ba5b0b9e', 'Кейтеринг на мероприятие', 'Выездное обслуживание до 50 человек', 'Продукты питания', null, null, '00000000-0000-0000-0001-c9d49824314b', '00000000-0000-0000-0001-f7b2e76cbadd', 150000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-1a8724847f07', 'кейтеринг'), ('00000000-0000-0000-0001-1a8724847f07', 'мероприятие'), ('00000000-0000-0000-0001-1a8724847f07', 'продукты'), ('00000000-0000-0000-0001-1a8724847f07', 'питания');
INSERT INTO service_branch_offer (id, created_at, updated_at, service_offering_id, branch_id, service_mode, base_price, duration_minutes, schedule_text, active, status) VALUES
  ('00000000-0000-0000-0001-880487eff985', now(), now(), '00000000-0000-0000-0001-b2103b6d1e25', '00000000-0000-0000-0001-ea091ecba558', 'ONLINE', 150000, 360, 'По записи, за 3 дня', true, 'ACTIVE');
INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status) VALUES
  ('00000000-0000-0000-0001-c9cd898e6fee', now(), now(), 'SERVICE', null, '00000000-0000-0000-0001-880487eff985', 'Кейтеринг на мероприятие', 'Выездное обслуживание до 50 человек', 'Продукты питания', null, null, '00000000-0000-0000-0001-c9d49824314b', '00000000-0000-0000-0001-ea091ecba558', 150000, 'ACTIVE');
INSERT INTO search_document_token (search_document_id, token) VALUES ('00000000-0000-0000-0001-c9cd898e6fee', 'кейтеринг'), ('00000000-0000-0000-0001-c9cd898e6fee', 'мероприятие'), ('00000000-0000-0000-0001-c9cd898e6fee', 'продукты'), ('00000000-0000-0000-0001-c9cd898e6fee', 'питания');
