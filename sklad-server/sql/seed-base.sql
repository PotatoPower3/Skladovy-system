-- ============================================================
-- ZÁKLADNÍ DATA SYSTÉMU
-- ============================================================
-- Data nutná pro běh aplikace nebo data, která se aktuálně
-- nepřidávají přímo z mobilní aplikace.

INSERT INTO roles (name)
VALUES
  ('skladnik'),
  ('admin')
ON CONFLICT (name) DO NOTHING;

INSERT INTO users (username, password, first_name, last_name, role_id)
SELECT 'petr', '1234', 'Petr', 'Schöpp (A23B0102P)', id
FROM roles
WHERE name = 'skladnik'
ON CONFLICT (username) DO UPDATE
SET
  password = EXCLUDED.password,
  first_name = EXCLUDED.first_name,
  last_name = EXCLUDED.last_name,
  role_id = EXCLUDED.role_id,
  active = TRUE,
  updated_at = NOW();

INSERT INTO warehouses (name, code, location)
VALUES
  ('Hlavní sklad', 'MAIN', 'Výchozí sklad')
ON CONFLICT (code) DO UPDATE
SET
  name = EXCLUDED.name,
  location = EXCLUDED.location,
  active = TRUE,
  updated_at = NOW();

INSERT INTO warehouse_locations (warehouse_id, code, name)
SELECT
  w.id,
  location_data.code,
  location_data.name
FROM warehouses w
CROSS JOIN (
  VALUES
    ('A1', 'Regál A1'),
    ('A2', 'Regál A2'),
    ('A3', 'Regál A3'),
    ('A4', 'Regál A4'),
    ('B1', 'Regál B1'),
    ('B2', 'Regál B2'),
    ('B3', 'Regál B3'),
    ('B4', 'Regál B4'),
    ('C1', 'Regál C1'),
    ('C2', 'Regál C2'),
    ('C3', 'Regál C3'),
    ('C4', 'Regál C4'),
    ('D1', 'Regál D1'),
    ('D2', 'Regál D2'),
    ('D3', 'Regál D3'),
    ('D4', 'Regál D4')
) AS location_data(code, name)
WHERE w.code = 'MAIN'
ON CONFLICT (warehouse_id, code) DO UPDATE
SET
  name = EXCLUDED.name,
  active = TRUE,
  updated_at = NOW();

INSERT INTO movement_types (code, name, direction)
VALUES
  ('IN', 'Příjem', 'IN'),
  ('OUT', 'Výdej', 'OUT'),
  ('TRANSFER_IN', 'Převod - příjem', 'IN'),
  ('TRANSFER_OUT', 'Převod - výdej', 'OUT'),
  ('DISPOSAL', 'Vyhození', 'OUT'),
  ('ADJUSTMENT_IN', 'Inventurní přebytek', 'IN'),
  ('ADJUSTMENT_OUT', 'Inventurní manko', 'OUT')
ON CONFLICT (code) DO UPDATE
SET
  name = EXCLUDED.name,
  direction = EXCLUDED.direction,
  active = TRUE,
  updated_at = NOW();

INSERT INTO code_types (code, name)
VALUES
  ('EAN', 'EAN kód'),
  ('QR', 'QR kód'),
  ('INTERNAL', 'Interní kód'),
  ('UNKNOWN', 'Neznámý typ')
ON CONFLICT (code) DO UPDATE
SET
  name = EXCLUDED.name,
  active = TRUE,
  updated_at = NOW();