import express from 'express';
import { pool } from '../db/pool.js';
import { httpError } from '../utils/httpError.js';

const router = express.Router();

const LOGIN_REQUIRED_MESSAGE = 'Uživatelské jméno a heslo jsou povinné.';
const INVALID_LOGIN_MESSAGE = 'Neplatné přihlašovací údaje.';
const INACTIVE_USER_MESSAGE = 'Uživatel není aktivní.';

router.post('/login', async (req, res, next) => {
  try {
    const username = String(req.body.username || '').trim();
    const password = String(req.body.password || '');

    if (!username || !password) {
      throw httpError(400, LOGIN_REQUIRED_MESSAGE);
    }

    const result = await pool.query(
      `
      SELECT
        u.id,
        u.username,
        u.password,
        u.first_name,
        u.last_name,
        u.active,
        r.name AS role_name
      FROM users u
      JOIN roles r ON r.id = u.role_id
      WHERE u.username = $1
      LIMIT 1
      `,
      [username]
    );

    if (result.rows.length === 0) {
      throw httpError(401, INVALID_LOGIN_MESSAGE);
    }

    const user = result.rows[0];

    if (!user.active) {
      throw httpError(403, INACTIVE_USER_MESSAGE);
    }

    if (user.password !== password) {
      throw httpError(401, INVALID_LOGIN_MESSAGE);
    }

    res.json({
      id: user.id,
      username: user.username,
      first_name: user.first_name,
      last_name: user.last_name,
      role_name: user.role_name
    });
  } catch (error) {
    next(error);
  }
});

export default router;