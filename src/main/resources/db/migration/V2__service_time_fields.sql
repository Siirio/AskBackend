-- Add requested_start_at to customer_request (customer's desired time)
ALTER TABLE customer_request ADD COLUMN requested_start_at TIMESTAMPTZ;

-- Add time fields to supplier_response
ALTER TABLE supplier_response ADD COLUMN proposed_start_at   TIMESTAMPTZ;
ALTER TABLE supplier_response ADD COLUMN confirmed_start_at  TIMESTAMPTZ;
ALTER TABLE supplier_response ADD COLUMN confirmed_end_at    TIMESTAMPTZ;
