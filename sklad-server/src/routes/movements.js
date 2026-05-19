import express from 'express';
import { pool } from '../db/pool.js';
import { getWarehouseId, getLimit } from '../utils/request.js';
import { httpError } from '../utils/httpError.js';

const router = express.Router();

function getOptionalNumberQuery(req, key) {
  const value = req.query[key];

  if (value === undefined || value === null || value === '') {
    return null;
  }

  const numberValue = Number(value);

  return Number.isFinite(numberValue) && numberValue > 0
    ? numberValue
    : null;
}

router.get('/', async (req, res, next) => {
  try {
    const warehouseId = getWarehouseId(req);
    const limit = getLimit(req);
    const itemId = getOptionalNumberQuery(req, 'itemId');

    const params = [warehouseId, limit];

    let itemFilterSql = '';

    if (itemId) {
      params.push(itemId);
      itemFilterSql = 'AND sdi.item_id = $3';
    }

    const result = await pool.query(
      `
      SELECT
        sdi.id,
        mt.code AS type,
        mt.name AS type_name,
        mt.direction,
        sdi.quantity,
        COALESCE(sdi.note, sd.note) AS note,
        sd.created_at,
        sd.updated_at,

        i.id AS item_id,
        i.name AS item_name,
        (
          SELECT ic.code
          FROM item_codes ic
          WHERE ic.item_id = i.id
            AND ic.active = true
          ORDER BY ic.id
          LIMIT 1
        ) AS item_code,

        cu.id AS user_id,
        cu.first_name,
        cu.last_name,

        uu.id AS updated_by_user_id,
        uu.first_name AS updated_by_first_name,
        uu.last_name AS updated_by_last_name,

        w.id AS warehouse_id,
        w.name AS warehouse_name,

        sd.id AS document_id,
        sd.document_number,
        sd.status
      FROM stock_document_items sdi
      JOIN stock_documents sd ON sd.id = sdi.document_id
      JOIN movement_types mt ON mt.id = sd.movement_type_id
      JOIN items i ON i.id = sdi.item_id
      JOIN users cu ON cu.id = sd.created_by_user_id
      JOIN users uu ON uu.id = sd.updated_by_user_id
      JOIN warehouses w ON w.id = sd.warehouse_id
      WHERE sd.warehouse_id = $1
        AND sd.status = 'CONFIRMED'
        ${itemFilterSql}
      ORDER BY sd.created_at DESC, sdi.id DESC
      LIMIT $2
      `,
      params
    );

    res.json(result.rows);
  } catch (error) {
    next(error);
  }
});

router.post('/in', (req, res, next) => {
  next(httpError(410, 'Tento endpoint neexistuje.'));
});

router.post('/out', (req, res, next) => {
  next(httpError(410, 'Tento endpoint neexistuje.'));
});

export default router;