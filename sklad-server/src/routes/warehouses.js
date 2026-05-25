import express from 'express';
import { pool } from '../db/pool.js';
import { getBooleanQuery } from '../utils/request.js';

const router = express.Router();

router.get('/', async (req, res, next) => {
  try {
    const includeInactive = getBooleanQuery(req, 'includeInactive');

    const result = await pool.query(
      `
      SELECT
        id,
        name,
        code,
        location,
        active,
        created_at,
        updated_at
      FROM warehouses
      WHERE ($1::boolean = true OR active = true)
      ORDER BY name
      `,
      [includeInactive]
    );

    res.json(result.rows);
  } catch (error) {
    next(error);
  }
});

router.get('/:warehouseId/locations', async (req, res, next) => {
  try {
    const warehouseId = Number(req.params.warehouseId);
    const includeInactive = getBooleanQuery(req, 'includeInactive');

    if (!Number.isInteger(warehouseId) || warehouseId <= 0) {
      return res.status(400).json({
        message: 'Neplatné ID skladu.'
      });
    }

    const result = await pool.query(
      `
      SELECT
        id,
        warehouse_id AS "warehouseId",
        code,
        name,
        active,
        created_at AS "createdAt",
        updated_at AS "updatedAt"
      FROM warehouse_locations
      WHERE warehouse_id = $1
        AND ($2::boolean = true OR active = true)
      ORDER BY code
      `,
      [warehouseId, includeInactive]
    );

    res.json(result.rows);
  } catch (error) {
    next(error);
  }
});

export default router;