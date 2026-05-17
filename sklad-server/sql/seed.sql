INSERT INTO roles (name)
VALUES
  ('skladnik'),
  ('admin')
ON CONFLICT (name) DO NOTHING;

INSERT INTO users (username, password, first_name, last_name, role_id)
SELECT 'petr', '1234', 'Petr', 'Schöpp', id
FROM roles
WHERE name = 'skladnik'
ON CONFLICT (username) DO NOTHING;

INSERT INTO warehouses (name, code, location)
VALUES
  ('Hlavní sklad', 'MAIN', 'Výchozí sklad')
ON CONFLICT (code) DO NOTHING;

INSERT INTO movement_types (code, name, direction)
VALUES
  ('IN', 'Příjem', 'IN'),
  ('OUT', 'Výdej', 'OUT'),
  ('TRANSFER_IN', 'Převod - příjem', 'IN'),
  ('TRANSFER_OUT', 'Převod - výdej', 'OUT'),
  ('DISPOSAL', 'Vyhození', 'OUT'),
  ('ADJUSTMENT_IN', 'Inventurní přebytek', 'IN'),
  ('ADJUSTMENT_OUT', 'Inventurní manko', 'OUT')
ON CONFLICT (code) DO NOTHING;

INSERT INTO code_types (code, name)
VALUES
  ('EAN', 'EAN kód'),
  ('QR', 'QR kód'),
  ('INTERNAL', 'Interní kód'),
  ('UNKNOWN', 'Neznámý typ')
ON CONFLICT (code) DO NOTHING;

INSERT INTO items (name, unit, image_filename, note)
VALUES
  ('Šroub M6', 'ks', 'sroub-m6.jpg', 'Testovací položka'),
  ('Matice M6', 'ks', NULL, 'Testovací položka'),
  ('Lepicí páska', 'ks', NULL, 'Položka bez kódu');

INSERT INTO item_codes (item_id, code, code_type_id)
SELECT i.id, '859000000001', ct.id
FROM items i
JOIN code_types ct ON ct.code = 'EAN'
WHERE i.name = 'Šroub M6'
ON CONFLICT (code) DO NOTHING;

INSERT INTO item_codes (item_id, code, code_type_id)
SELECT i.id, 'QR-SROUB-M6', ct.id
FROM items i
JOIN code_types ct ON ct.code = 'QR'
WHERE i.name = 'Šroub M6'
ON CONFLICT (code) DO NOTHING;

INSERT INTO item_codes (item_id, code, code_type_id)
SELECT i.id, '859000000002', ct.id
FROM items i
JOIN code_types ct ON ct.code = 'EAN'
WHERE i.name = 'Matice M6'
ON CONFLICT (code) DO NOTHING;

INSERT INTO warehouse_items (warehouse_id, item_id, quantity, location, min_quantity)
SELECT w.id, i.id, 120, 'Regál A1', 10
FROM warehouses w
JOIN items i ON i.name = 'Šroub M6'
WHERE w.code = 'MAIN'
ON CONFLICT (warehouse_id, item_id) DO NOTHING;

INSERT INTO warehouse_items (warehouse_id, item_id, quantity, location, min_quantity)
SELECT w.id, i.id, 80, 'Regál A2', 10
FROM warehouses w
JOIN items i ON i.name = 'Matice M6'
WHERE w.code = 'MAIN'
ON CONFLICT (warehouse_id, item_id) DO NOTHING;

INSERT INTO warehouse_items (warehouse_id, item_id, quantity, location, min_quantity)
SELECT w.id, i.id, 15, 'Regál B1', 2
FROM warehouses w
JOIN items i ON i.name = 'Lepicí páska'
WHERE w.code = 'MAIN'
ON CONFLICT (warehouse_id, item_id) DO NOTHING;

INSERT INTO stock_documents (
  document_number,
  warehouse_id,
  movement_type_id,
  status,
  note,
  created_by_user_id,
  updated_by_user_id
)
SELECT
  'I-26050001',
  w.id,
  mt.id,
  'CONFIRMED',
  'Testovací příjem',
  u.id,
  u.id
FROM warehouses w
JOIN movement_types mt ON mt.code = 'IN'
JOIN users u ON u.username = 'petr'
WHERE w.code = 'MAIN'
ON CONFLICT (document_number) DO NOTHING;

INSERT INTO stock_document_items (document_id, item_id, quantity, note)
SELECT
  sd.id,
  i.id,
  5,
  'Testovací položka příjmu'
FROM stock_documents sd
JOIN items i ON i.name = 'Šroub M6'
WHERE sd.document_number = 'I-26050001'
ON CONFLICT (document_id, item_id) DO NOTHING;

INSERT INTO stock_document_items (document_id, item_id, quantity, note)
SELECT
  sd.id,
  i.id,
  20,
  'Testovací položka příjmu'
FROM stock_documents sd
JOIN items i ON i.name = 'Matice M6'
WHERE sd.document_number = 'I-26050001'
ON CONFLICT (document_id, item_id) DO NOTHING;

INSERT INTO stock_documents (
  document_number,
  warehouse_id,
  movement_type_id,
  status,
  note,
  created_by_user_id,
  updated_by_user_id
)
SELECT
  'O-26050001',
  w.id,
  mt.id,
  'CONFIRMED',
  'Testovací výdej',
  u.id,
  u.id
FROM warehouses w
JOIN movement_types mt ON mt.code = 'OUT'
JOIN users u ON u.username = 'petr'
WHERE w.code = 'MAIN'
ON CONFLICT (document_number) DO NOTHING;

INSERT INTO stock_document_items (document_id, item_id, quantity, note)
SELECT
  sd.id,
  i.id,
  2,
  'Testovací položka výdeje'
FROM stock_documents sd
JOIN items i ON i.name = 'Šroub M6'
WHERE sd.document_number = 'O-26050001'
ON CONFLICT (document_id, item_id) DO NOTHING;