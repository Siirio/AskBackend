CREATE TABLE search_query_alias (
    id           UUID         PRIMARY KEY,
    created_at   TIMESTAMPTZ  NOT NULL,
    updated_at   TIMESTAMPTZ  NOT NULL,
    alias_value  VARCHAR(255) NOT NULL,
    target_query VARCHAR(255) NOT NULL,
    status       VARCHAR(50)  NOT NULL
);

CREATE INDEX idx_search_query_alias_value ON search_query_alias (alias_value, status);

INSERT INTO search_query_alias (id, created_at, updated_at, alias_value, target_query, status) VALUES
  ('00000000-0000-0000-0000-000000000301', now(), now(), 'барбершоп', 'стрижка', 'ACTIVE'),
  ('00000000-0000-0000-0000-000000000302', now(), now(), 'barbershop', 'стрижка', 'ACTIVE'),
  ('00000000-0000-0000-0000-000000000303', now(), now(), 'барбер', 'стрижка', 'ACTIVE'),
  ('00000000-0000-0000-0000-000000000304', now(), now(), 'салон красоты', 'стрижка', 'ACTIVE');

INSERT INTO product (id, created_at, updated_at, business_id, category_id, category_label, name, description, sku, characteristics_json, status) VALUES
  ('00000000-0000-0000-0000-000000000310', now(), now(), '00000000-0000-0000-0000-0000000000b3', '00000000-0000-0000-0000-0000000000a2', 'Умные часы', 'Умные часы Apple Watch SE 44mm', 'GPS, 44mm, спортивный ремешок', 'APL-WATCH-SE-44', '{"бренд":"Apple","тип":"умные часы","размер":"44mm"}', 'ACTIVE');

INSERT INTO product_offer (id, created_at, updated_at, product_id, branch_id, data_source_id, price, enabled, status) VALUES
  ('00000000-0000-0000-0000-000000000311', now(), now(), '00000000-0000-0000-0000-000000000310', '00000000-0000-0000-0000-000000000013', '00000000-0000-0000-0000-000000000033', 149000, true, 'ACTIVE');

INSERT INTO search_document (id, created_at, updated_at, document_type, product_offer_id, service_branch_offer_id, title, summary, category_label, sku, characteristics_json, business_id, branch_id, price, status, source, public_note) VALUES
  ('00000000-0000-0000-0000-000000000312', now(), now(), 'PRODUCT', '00000000-0000-0000-0000-000000000311', null, 'Умные часы Apple Watch SE 44mm', 'GPS, 44mm, спортивный ремешок', 'Умные часы', 'APL-WATCH-SE-44', '{"бренд":"Apple","тип":"умные часы","размер":"44mm"}', '00000000-0000-0000-0000-0000000000b3', '00000000-0000-0000-0000-000000000013', 149000, 'ACTIVE', 'CATALOG', '');

INSERT INTO search_document_token (search_document_id, token) VALUES
  ('00000000-0000-0000-0000-000000000312', 'часы'),
  ('00000000-0000-0000-0000-000000000312', 'умные'),
  ('00000000-0000-0000-0000-000000000312', 'apple'),
  ('00000000-0000-0000-0000-000000000312', 'watch'),
  ('00000000-0000-0000-0000-000000000312', 'se'),
  ('00000000-0000-0000-0000-000000000312', '44mm');
