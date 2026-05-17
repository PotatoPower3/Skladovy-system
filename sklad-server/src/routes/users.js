import express from 'express';
import { pool } from '../db/pool.js';

const router = express.Router();

router.get('/', async (req, res, next) => {
  try {
    const includeInactive = req.query.includeInactive === 'true';

    const result = await pool.query(
      `
      SELECT
        u.id,
        u.first_name,
        u.last_name,
        u.active,
        u.created_at,
        u.updated_at,
        r.id AS role_id,
        r.name AS role_name
      FROM users u
      JOIN roles r ON r.id = u.role_id
      WHERE ($1::boolean = true OR u.active = true)
      ORDER BY u.last_name, u.first_name
      `,
      [includeInactive]
    );

    res.json(result.rows);
  } catch (error) {
    next(error);
  }
});

export default router;