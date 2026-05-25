-- ============================================================
-- DEMO DATA
-- ============================================================
-- Automaticky generovaná data pro naplnění aplikace.
-- Obsahují stovky položek, desítky příjmů a desítky výdejů.

-- ============================================================
-- GENEROVANÉ POLOŽKY
-- ============================================================

WITH name_parts AS (
  SELECT *
  FROM (
    VALUES
      (1, 'Šroub', 'M6', 'pozinkovaný'),
      (2, 'Šroub', 'M8', 'nerezový'),
      (3, 'Matice', 'M6', 'šestihranná'),
      (4, 'Matice', 'M8', 'pojistná'),
      (5, 'Podložka', 'M6', 'plochá'),
      (6, 'Podložka', 'M8', 'pružná'),
      (7, 'Vruty', '4x40', 'do dřeva'),
      (8, 'Hmoždinka', '8 mm', 'univerzální'),
      (9, 'Lepicí páska', '48 mm', 'transparentní'),
      (10, 'Izolační páska', '19 mm', 'černá'),
      (11, 'Stahovací páska', '200 mm', 'černá'),
      (12, 'Stahovací páska', '300 mm', 'bílá'),
      (13, 'Pracovní rukavice', 'velikost 9', 'kožené'),
      (14, 'Pracovní rukavice', 'velikost 10', 'textilní'),
      (15, 'Ochranné brýle', 'čiré', 'standardní'),
      (16, 'Respirátor', 'FFP2', 'balení'),
      (17, 'Fix', 'černý', 'permanentní'),
      (18, 'Popisovač', 'bílý', 'lakový'),
      (19, 'Kartonová krabice', 'malá', 'skládací'),
      (20, 'Kartonová krabice', 'velká', 'skládací'),
      (21, 'Stretch fólie', '500 mm', 'ruční'),
      (22, 'Bublinková fólie', '100 cm', 'role'),
      (23, 'Řezný kotouč', '125 mm', 'na kov'),
      (24, 'Brusný kotouč', '125 mm', 'lamelový'),
      (25, 'Vrták', '6 mm', 'do kovu'),
      (26, 'Vrták', '8 mm', 'do betonu'),
      (27, 'Kabel CYKY', '3x1,5', 'metr'),
      (28, 'Kabel CYKY', '3x2,5', 'metr'),
      (29, 'Svorka WAGO', '2pin', 'páková'),
      (30, 'Svorka WAGO', '3pin', 'páková'),
      (31, 'Sprej mazací', '400 ml', 'univerzální'),
      (32, 'Čistič brzd', '500 ml', 'technický'),
      (33, 'Těsnicí páska', '12 mm', 'teflonová'),
      (34, 'Silikon', 'transparentní', 'sanitární'),
      (35, 'Montážní pěna', '750 ml', 'nízkoexpanzní'),
      (36, 'Lepidlo', '250 ml', 'montážní'),
      (37, 'Náhradní čepel', '18 mm', 'odlamovací'),
      (38, 'Nůž odlamovací', '18 mm', 'plastový'),
      (39, 'Metr svinovací', '5 m', 'pogumovaný'),
      (40, 'Tužka tesařská', 'červená', 'značkovací'),
      (41, 'Zámek visací', '40 mm', 'mosazný'),
      (42, 'Karabina', '60 mm', 'ocelová'),
      (43, 'Řetěz', '4 mm', 'pozinkovaný'),
      (44, 'Lanko ocelové', '3 mm', 'potahované'),
      (45, 'Koleno PVC', '32 mm', 'odpadní'),
      (46, 'Trubka PVC', '32 mm', 'odpadní'),
      (47, 'T-kus PVC', '32 mm', 'odpadní'),
      (48, 'Hadice', '1/2"', 'zahradní'),
      (49, 'Spojka hadicová', '1/2"', 'rychlospojka'),
      (50, 'Páska výstražná', 'červeno-bílá', 'bez lepidla')
  ) AS p(base_index, product_name, variant, specification)
),
generated_items AS (
  SELECT
    n,
    np.product_name || ' ' || np.variant || ' - ' || np.specification ||
      ' #' || LPAD((((n - 1) / 50) + 1)::text, 2, '0') AS name,
    'ks' AS unit,
    ROUND((0.05 + (n % 50) * 0.08)::numeric, 3) AS weight_per_unit,
    'Automaticky generovaná demo položka pro naplnění skladu' AS note
  FROM generate_series(1, 250) AS n
  JOIN name_parts np ON np.base_index = ((n - 1) % 50) + 1
)
INSERT INTO items (name, unit, weight_per_unit, image_filename, note)
SELECT
  gi.name,
  gi.unit,
  gi.weight_per_unit,
  NULL,
  gi.note
FROM generated_items gi;

-- ============================================================
-- KÓDY POLOŽEK
-- ============================================================

WITH numbered_items AS (
  SELECT
    id,
    ROW_NUMBER() OVER (ORDER BY id) AS item_number
  FROM items
)
INSERT INTO item_codes (item_id, code, code_type_id)
SELECT
  ni.id,
  '859990' || LPAD(ni.item_number::text, 6, '0') AS code,
  ct.id
FROM numbered_items ni
JOIN code_types ct ON ct.code = 'EAN'
WHERE ni.item_number <= 250
ON CONFLICT (code) DO NOTHING;

WITH numbered_items AS (
  SELECT
    id,
    ROW_NUMBER() OVER (ORDER BY id) AS item_number
  FROM items
)
INSERT INTO item_codes (item_id, code, code_type_id)
SELECT
  ni.id,
  'QR-TEST-' || LPAD(ni.item_number::text, 3, '0') AS code,
  ct.id
FROM numbered_items ni
JOIN code_types ct ON ct.code = 'QR'
WHERE ni.item_number <= 100
ON CONFLICT (code) DO NOTHING;

-- ============================================================
-- GENEROVANÉ PŘÍJMOVÉ DOKLADY
-- ============================================================

INSERT INTO stock_documents (
  document_number,
  warehouse_id,
  movement_type_id,
  status,
  note,
  created_by_user_id,
  updated_by_user_id,
  created_at,
  updated_at
)
SELECT
  'GI-2605' || LPAD(n::text, 4, '0'),
  w.id,
  mt.id,
  'CONFIRMED',
  'Generovaný příjem ' || n,
  u.id,
  u.id,
  NOW() - ((120 - n) || ' hours')::interval,
  NOW() - ((120 - n) || ' hours')::interval
FROM generate_series(1, 80) AS n
JOIN warehouses w ON w.code = 'MAIN'
JOIN movement_types mt ON mt.code = 'IN'
JOIN users u ON u.username = 'petr'
ON CONFLICT (document_number) DO NOTHING;

WITH document_items AS (
  SELECT
    d.n AS document_index,
    p.pos AS item_position,
    ((d.n - 1) * 7 + p.pos - 1) % 250 + 1 AS item_index
  FROM generate_series(1, 80) AS d(n)
  CROSS JOIN generate_series(1, 6) AS p(pos)
),
numbered_items AS (
  SELECT
    id,
    ROW_NUMBER() OVER (ORDER BY id) AS item_number
  FROM items
)
INSERT INTO stock_document_items (
  document_id,
  item_id,
  quantity,
  note
)
SELECT
  sd.id,
  ni.id,
  15 + ((di.document_index + di.item_position) % 35),
  'Generovaná položka příjmu'
FROM document_items di
JOIN stock_documents sd
  ON sd.document_number = 'GI-2605' || LPAD(di.document_index::text, 4, '0')
JOIN numbered_items ni
  ON ni.item_number = di.item_index
ON CONFLICT (document_id, item_id) DO NOTHING;

-- ============================================================
-- GENEROVANÉ VÝDEJOVÉ DOKLADY
-- ============================================================

INSERT INTO stock_documents (
  document_number,
  warehouse_id,
  movement_type_id,
  status,
  note,
  created_by_user_id,
  updated_by_user_id,
  created_at,
  updated_at
)
SELECT
  'GO-2605' || LPAD(n::text, 4, '0'),
  w.id,
  mt.id,
  'CONFIRMED',
  'Generovaný výdej ' || n,
  u.id,
  u.id,
  NOW() - ((60 - n) || ' hours')::interval,
  NOW() - ((60 - n) || ' hours')::interval
FROM generate_series(1, 60) AS n
JOIN warehouses w ON w.code = 'MAIN'
JOIN movement_types mt ON mt.code = 'OUT'
JOIN users u ON u.username = 'petr'
ON CONFLICT (document_number) DO NOTHING;

WITH document_items AS (
  SELECT
    d.n AS document_index,
    p.pos AS item_position,
    ((d.n - 1) * 5 + p.pos + 40) % 250 + 1 AS item_index
  FROM generate_series(1, 60) AS d(n)
  CROSS JOIN generate_series(1, 4) AS p(pos)
),
numbered_items AS (
  SELECT
    id,
    ROW_NUMBER() OVER (ORDER BY id) AS item_number
  FROM items
)
INSERT INTO stock_document_items (
  document_id,
  item_id,
  quantity,
  note
)
SELECT
  sd.id,
  ni.id,
  1 + ((di.document_index + di.item_position) % 8),
  'Generovaná položka výdeje'
FROM document_items di
JOIN stock_documents sd
  ON sd.document_number = 'GO-2605' || LPAD(di.document_index::text, 4, '0')
JOIN numbered_items ni
  ON ni.item_number = di.item_index
ON CONFLICT (document_id, item_id) DO NOTHING;

-- ============================================================
-- DOPOČÍTÁNÍ AKTUÁLNÍHO STAVU SKLADU
-- ============================================================
-- Stav skladu se dopočítá z potvrzených dokladů.
-- Umístění se vybírá z připravených regálů.

WITH stock_state AS (
  SELECT
    sd.warehouse_id,
    sdi.item_id,
    SUM(
      CASE
        WHEN mt.direction = 'IN' THEN sdi.quantity
        WHEN mt.direction = 'OUT' THEN -sdi.quantity
        ELSE 0
      END
    ) AS quantity
  FROM stock_document_items sdi
  JOIN stock_documents sd ON sd.id = sdi.document_id
  JOIN movement_types mt ON mt.id = sd.movement_type_id
  WHERE sd.status = 'CONFIRMED'
  GROUP BY
    sd.warehouse_id,
    sdi.item_id
  HAVING SUM(
    CASE
      WHEN mt.direction = 'IN' THEN sdi.quantity
      WHEN mt.direction = 'OUT' THEN -sdi.quantity
      ELSE 0
    END
  ) >= 0
),
locations_numbered AS (
  SELECT
    wl.warehouse_id,
    wl.name,
    ROW_NUMBER() OVER (
      PARTITION BY wl.warehouse_id
      ORDER BY wl.code
    ) AS location_index,
    COUNT(*) OVER (
      PARTITION BY wl.warehouse_id
    ) AS location_count
  FROM warehouse_locations wl
  WHERE wl.active = TRUE
)
INSERT INTO warehouse_items (
  warehouse_id,
  item_id,
  quantity,
  location,
  min_quantity
)
SELECT
  ss.warehouse_id,
  ss.item_id,
  ss.quantity,
  ln.name,
  2 + (ss.item_id % 15)
FROM stock_state ss
JOIN locations_numbered ln
  ON ln.warehouse_id = ss.warehouse_id
 AND ln.location_index = ((ss.item_id - 1) % ln.location_count) + 1
ON CONFLICT (warehouse_id, item_id) DO UPDATE
SET
  quantity = EXCLUDED.quantity,
  location = EXCLUDED.location,
  min_quantity = EXCLUDED.min_quantity,
  updated_at = NOW();