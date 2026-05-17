import express from 'express';
import { pool } from '../db/pool.js';

const router = express.Router();

router.post('/login', async (req, res, next) => {
  try {
    const username = String(req.body.username || '').trim();
    const password = String(req.body.password || '');

    if (!username || !password) {
      return res.status(400).json({ message: 'Uživatelské jméno a heslo jsou povinné.' });
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
      return res.status(401).json({ message: 'Neplatné přihlašovací údaje.' });
    }

    const user = result.rows[0];

    if (!user.active) {
      return res.status(403).json({ message: 'Uživatel není aktivní.' });
    }

    if (user.password !== password) {
      return res.status(401).json({ message: 'Neplatné přihlašovací údaje.' });
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