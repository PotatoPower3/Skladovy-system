import express from 'express';
import { pool } from '../db/pool.js';

const router = express.Router();

router.get('/', async (req, res, next) => {
  try {
    const includeInactive = req.query.includeInactive === 'true';

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

export default router;