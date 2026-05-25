import express from 'express';
import { pool } from '../db/pool.js';
import { getWarehouseId, getLimit } from '../utils/request.js';
import { httpError } from '../utils/httpError.js';

const router = express.Router();

const INVALID_DOCUMENT_ITEM_MESSAGE =
  'Každá položka musí mít platné itemId a quantity větší než 0.';

const INVALID_ORIGINAL_DOCUMENT_ITEM_MESSAGE =
  'Původní položka dokladu nemá platné itemId a quantity.';

async function getMovementTypeByCode(client, code) {
  const result = await client.query(
    `
    SELECT id, code, name, direction
    FROM movement_types
    WHERE code = $1
      AND active = true
    LIMIT 1
    `,
    [code]
  );

  if (result.rows.length === 0) {
    throw httpError(400, `Typ pohybu ${code} nebyl nalezen.`);
  }

  return result.rows[0];
}

async function generateDocumentNumber(client, movementTypeCode) {
  const prefix = movementTypeCode === 'IN' ? 'I' : 'O';
  const now = new Date();

  const yearShort = String(now.getFullYear()).slice(-2);
  const month = String(now.getMonth() + 1).padStart(2, '0');

  const documentPrefix = `${prefix}-${yearShort}${month}`;

  const result = await client.query(
    `
    SELECT COUNT(*)::int AS count
    FROM stock_documents sd
    JOIN movement_types mt ON mt.id = sd.movement_type_id
    WHERE mt.code = $1
      AND sd.document_number LIKE $2
    `,
    [
      movementTypeCode,
      `${documentPrefix}%`
    ]
  );

  const nextNumber = result.rows[0].count + 1;

  return `${documentPrefix}${String(nextNumber).padStart(4, '0')}`;
}

function validateUserId(userId) {
  if (!userId) {
    throw httpError(400, 'userId je povinné.');
  }
}

function normalizeDocumentItem(
  item,
  errorMessage = INVALID_DOCUMENT_ITEM_MESSAGE
) {
  const itemId = Number(item.itemId ?? item.item_id);
  const quantity = Number(item.quantity);

  if (!itemId || !quantity || quantity <= 0) {
    throw httpError(400, errorMessage);
  }

  return {
    itemId,
    quantity,
    note: item.note || null
  };
}

function validateDocumentItems(items) {
  if (!Array.isArray(items) || items.length === 0) {
    throw httpError(400, 'Doklad musí obsahovat alespoň jednu položku.');
  }

  return items.map((item) => normalizeDocumentItem(item));
}

function getMovementDelta(movementDirection, quantity) {
  if (movementDirection === 'IN') {
    return quantity;
  }

  if (movementDirection === 'OUT') {
    return -quantity;
  }

  throw httpError(400, 'Typ pohybu není podporovaný.');
}

async function getWarehouseItemQuantityForUpdate(client, warehouseId, itemId) {
  const stockResult = await client.query(
    `
    SELECT quantity
    FROM warehouse_items
    WHERE warehouse_id = $1
      AND item_id = $2
    FOR UPDATE
    `,
    [warehouseId, itemId]
  );

  if (stockResult.rows.length === 0) {
    throw httpError(404, `Položka ID ${itemId} není v tomto skladu.`);
  }

  return Number(stockResult.rows[0].quantity);
}

async function updateWarehouseItemQuantity(client, warehouseId, itemId, newQuantity) {
  await client.query(
    `
    UPDATE warehouse_items
    SET quantity = $1,
        updated_at = NOW()
    WHERE warehouse_id = $2
      AND item_id = $3
    `,
    [newQuantity, warehouseId, itemId]
  );
}

async function applyWarehouseDelta(client, warehouseId, itemId, delta) {
  if (delta === 0) {
    return;
  }

  const currentQuantity = await getWarehouseItemQuantityForUpdate(
    client,
    warehouseId,
    itemId
  );

  const newQuantity = currentQuantity + delta;

  if (newQuantity < 0) {
    throw httpError(400, `Nedostatečné množství na skladě pro položku ID ${itemId}.`);
  }

  await updateWarehouseItemQuantity(
    client,
    warehouseId,
    itemId,
    newQuantity
  );
}

async function getDocumentDetail(documentId) {
  const documentResult = await pool.query(
    `
    SELECT
      sd.id,
      sd.document_number,
      sd.status,
      sd.note,
      sd.created_at,
      sd.updated_at,

      w.id AS warehouse_id,
      w.name AS warehouse_name,

      mt.id AS movement_type_id,
      mt.code AS movement_type_code,
      mt.name AS movement_type_name,
      mt.direction AS movement_type_direction,

      cu.id AS created_by_user_id,
      cu.first_name AS created_by_first_name,
      cu.last_name AS created_by_last_name,

      uu.id AS updated_by_user_id,
      uu.first_name AS updated_by_first_name,
      uu.last_name AS updated_by_last_name
    FROM stock_documents sd
    JOIN warehouses w ON w.id = sd.warehouse_id
    JOIN movement_types mt ON mt.id = sd.movement_type_id
    JOIN users cu ON cu.id = sd.created_by_user_id
    JOIN users uu ON uu.id = sd.updated_by_user_id
    WHERE sd.id = $1
    LIMIT 1
    `,
    [documentId]
  );

  if (documentResult.rows.length === 0) {
    return null;
  }

  const itemsResult = await pool.query(
    `
    SELECT
      sdi.id,
      sdi.item_id,
      i.name AS item_name,
      i.unit,
      sdi.quantity,
      sdi.note,
      sdi.created_at,
      sdi.updated_at
    FROM stock_document_items sdi
    JOIN items i ON i.id = sdi.item_id
    WHERE sdi.document_id = $1
    ORDER BY i.name
    `,
    [documentId]
  );

  return {
    ...documentResult.rows[0],
    items: itemsResult.rows
  };
}

async function applyDocumentImpact(
  client,
  warehouseId,
  movementDirection,
  items,
  reverse = false
) {
  const normalizedItems = items.map((item) => normalizeDocumentItem(item));

  for (const item of normalizedItems) {
    let delta = getMovementDelta(movementDirection, item.quantity);

    if (reverse) {
      delta *= -1;
    }

    await applyWarehouseDelta(
      client,
      warehouseId,
      item.itemId,
      delta
    );
  }
}

async function applyDocumentNetImpact(
  client,
  warehouseId,
  movementDirection,
  oldItems,
  newItems
) {
  const deltasByItemId = new Map();

  function addDelta(itemId, delta) {
    const current = deltasByItemId.get(itemId) || 0;
    deltasByItemId.set(itemId, current + delta);
  }

  const normalizedOldItems = oldItems.map((item) =>
    normalizeDocumentItem(
      item,
      INVALID_ORIGINAL_DOCUMENT_ITEM_MESSAGE
    )
  );

  const normalizedNewItems = newItems.map((item) =>
    normalizeDocumentItem(item)
  );

  for (const item of normalizedOldItems) {
    addDelta(
      item.itemId,
      -getMovementDelta(movementDirection, item.quantity)
    );
  }

  for (const item of normalizedNewItems) {
    addDelta(
      item.itemId,
      getMovementDelta(movementDirection, item.quantity)
    );
  }

  for (const [itemId, delta] of deltasByItemId.entries()) {
    await applyWarehouseDelta(
      client,
      warehouseId,
      itemId,
      delta
    );
  }
}

async function insertDocumentItems(client, documentId, items) {
  for (const item of items) {
    await client.query(
      `
      INSERT INTO stock_document_items (
        document_id,
        item_id,
        quantity,
        note,
        created_at,
        updated_at
      )
      VALUES ($1, $2, $3, $4, NOW(), NOW())
      `,
      [
        documentId,
        item.itemId,
        item.quantity,
        item.note
      ]
    );
  }
}

async function createDocument({ movementTypeCode, body }) {
  const client = await pool.connect();

  try {
    const {
      warehouseId = 1,
      userId,
      note = null,
      items = []
    } = body;

    validateUserId(userId);
    const normalizedItems = validateDocumentItems(items);

    await client.query('BEGIN');

    const movementType = await getMovementTypeByCode(client, movementTypeCode);
    const documentNumber = await generateDocumentNumber(client, movementTypeCode);

    await applyDocumentImpact(
      client,
      warehouseId,
      movementType.direction,
      normalizedItems,
      false
    );

    const documentResult = await client.query(
      `
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
      VALUES ($1, $2, $3, 'CONFIRMED', $4, $5, $5, NOW(), NOW())
      RETURNING id
      `,
      [
        documentNumber,
        warehouseId,
        movementType.id,
        note,
        userId
      ]
    );

    const documentId = documentResult.rows[0].id;

    await insertDocumentItems(
      client,
      documentId,
      normalizedItems
    );

    await client.query('COMMIT');

    return await getDocumentDetail(documentId);
  } catch (error) {
    await client.query('ROLLBACK');
    throw error;
  } finally {
    client.release();
  }
}

async function updateDocument(documentId, body) {
  const client = await pool.connect();

  try {
    const {
      userId,
      note = null,
      items = []
    } = body;

    validateUserId(userId);
    const normalizedItems = validateDocumentItems(items);

    await client.query('BEGIN');

    const documentResult = await client.query(
      `
      SELECT
        sd.id,
        sd.warehouse_id,
        sd.status,
        mt.direction
      FROM stock_documents sd
      JOIN movement_types mt ON mt.id = sd.movement_type_id
      WHERE sd.id = $1
      FOR UPDATE
      `,
      [documentId]
    );

    if (documentResult.rows.length === 0) {
      throw httpError(404, 'Doklad nebyl nalezen.');
    }

    const document = documentResult.rows[0];

    if (document.status !== 'CONFIRMED') {
      throw httpError(400, 'Upravovat lze zatím jen potvrzené doklady.');
    }

    const oldItemsResult = await client.query(
      `
      SELECT item_id, quantity
      FROM stock_document_items
      WHERE document_id = $1
      `,
      [documentId]
    );

    await applyDocumentNetImpact(
      client,
      document.warehouse_id,
      document.direction,
      oldItemsResult.rows,
      normalizedItems
    );

    await client.query(
      `
      DELETE FROM stock_document_items
      WHERE document_id = $1
      `,
      [documentId]
    );

    await insertDocumentItems(
      client,
      documentId,
      normalizedItems
    );

    await client.query(
      `
      UPDATE stock_documents
      SET note = $1,
          updated_by_user_id = $2,
          updated_at = NOW()
      WHERE id = $3
      `,
      [note, userId, documentId]
    );

    await client.query('COMMIT');

    return await getDocumentDetail(documentId);
  } catch (error) {
    await client.query('ROLLBACK');
    throw error;
  } finally {
    client.release();
  }
}

router.get('/', async (req, res, next) => {
  try {
    const warehouseId = getWarehouseId(req);
    const limit = getLimit(req);
    const movementTypeCode = req.query.movementTypeCode || null;

    const result = await pool.query(
      `
      SELECT
        sd.id,
        sd.document_number,
        sd.status,
        sd.note,
        sd.created_at,
        sd.updated_at,

        mt.id AS movement_type_id,
        mt.code AS movement_type_code,
        mt.name AS movement_type_name,
        mt.direction AS movement_type_direction,

        cu.id AS created_by_user_id,
        cu.first_name AS created_by_first_name,
        cu.last_name AS created_by_last_name,

        uu.id AS updated_by_user_id,
        uu.first_name AS updated_by_first_name,
        uu.last_name AS updated_by_last_name,

        COUNT(sdi.id)::int AS items_count,
        COALESCE(SUM(sdi.quantity), 0)::numeric(12, 2) AS total_quantity
      FROM stock_documents sd
      JOIN movement_types mt ON mt.id = sd.movement_type_id
      JOIN users cu ON cu.id = sd.created_by_user_id
      JOIN users uu ON uu.id = sd.updated_by_user_id
      LEFT JOIN stock_document_items sdi ON sdi.document_id = sd.id
      WHERE sd.warehouse_id = $1
        AND ($3::text IS NULL OR mt.code = $3)
      GROUP BY sd.id, mt.id, cu.id, uu.id
      ORDER BY sd.created_at DESC
      LIMIT $2
      `,
      [warehouseId, limit, movementTypeCode]
    );

    res.json(result.rows);
  } catch (error) {
    next(error);
  }
});

router.get('/:id', async (req, res, next) => {
  try {
    const document = await getDocumentDetail(req.params.id);

    if (!document) {
      throw httpError(404, 'Doklad nebyl nalezen.');
    }

    res.json(document);
  } catch (error) {
    next(error);
  }
});

router.post('/in', async (req, res, next) => {
  try {
    const document = await createDocument({
      movementTypeCode: 'IN',
      body: req.body
    });

    res.status(201).json(document);
  } catch (error) {
    next(error);
  }
});

router.post('/out', async (req, res, next) => {
  try {
    const document = await createDocument({
      movementTypeCode: 'OUT',
      body: req.body
    });

    res.status(201).json(document);
  } catch (error) {
    next(error);
  }
});

router.put('/:id', async (req, res, next) => {
  try {
    const document = await updateDocument(req.params.id, req.body);
    res.json(document);
  } catch (error) {
    next(error);
  }
});

export default router;