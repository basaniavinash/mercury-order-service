CREATE TABLE IF NOT EXISTS mercury.status (
  name TEXT PRIMARY KEY NOT NULL,
  created_at timestamptz NOT NULL DEFAULT now(),
  modified_at timestamptz NOT NULL DEFAULT now()
);

INSERT INTO mercury.status (name) VALUES
  ('NEW'),
  ('CREATED'),
  ('PAID'),
  ('SHIPPED'),
  ('COMPLETED'),
  ('CANCELLED'),
  ('REFUNDED')
ON CONFLICT (name) DO NOTHING;

CREATE TABLE IF NOT EXISTS mercury.orders (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  user_id BIGINT,
  status TEXT NOT NULL,
  subtotal_amount NUMERIC(12,2) NOT NULL,
  discount_amount NUMERIC(12,2) NOT NULL DEFAULT 0 CHECK (discount_amount >= 0),
  tax_amount NUMERIC(12,2) NOT NULL,
  total_amount NUMERIC(12,2) NOT NULL,
  created_at timestamptz NOT NULL DEFAULT now(),
  modified_at timestamptz NOT NULL DEFAULT now(),
  CONSTRAINT fk_order_status
    FOREIGN KEY (status)
    REFERENCES mercury.status (name)
);

CREATE TABLE IF NOT EXISTS mercury.order_items (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  order_id BIGINT NOT NULL,
  item_id BIGINT NOT NULL,
  qty INT NOT NULL CHECK (qty > 0),
  sku TEXT NOT NULL,
  unit_price NUMERIC(12,2) NOT NULL,
  line_total NUMERIC(12,2) NOT NULL,
  created_at timestamptz NOT NULL DEFAULT now(),
  modified_at timestamptz NOT NULL DEFAULT now(),

  CONSTRAINT fk_order_items_orders
    FOREIGN KEY (order_id)
    REFERENCES mercury.orders (id)
    ON DELETE CASCADE,

  CONSTRAINT fk_order_items_items
    FOREIGN KEY (item_id)
    REFERENCES mercury.items (id),

  CONSTRAINT uq_order_item
    UNIQUE (order_id, item_id)
);