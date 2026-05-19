import express from 'express';
import multer from 'multer';
import path from 'path';
import fs from 'fs';
import { pool } from '../db/pool.js';
import { getWarehouseId, getStringQuery } from '../utils/request.js';
import { httpError } from '../utils/httpError.js';

const router = express.Router();

const ITEM_NOT_FOUND_MESSAGE = 'Položka nebyla nalezena.';
const CODE_NOT_FOUND_MESSAGE = 'Kód nebyl nalezen.';
const CODE_REQUIRED_MESSAGE = 'Kód je povinný.';
const ITEM_NAME_REQUIRED_MESSAGE = 'Název položky je povinný.';
const DUPLICATE_CODE_MESSAGE = 'Tento kód už existuje.';
const DUPLICATE_CODE_OTHER_ITEM_MESSAGE = 'Tento kód už existuje u jiné položky.';
const IMAGE_REQUIRED_MESSAGE = 'Soubor obrázku je povinný.';
const INVALID_IMAGE_TYPE_MESSAGE = 'Podporované jsou jen obrázky JPG, PNG nebo WEBP.';

const uploadDir = path.join(process.cwd(), 'uploads/items');

if (!fs.existsSync(uploadDir)) {
  fs.mkdirSync(uploadDir, { recursive: true });
}

const allowedImageMimeTypes = new Set([
  'image/jpeg',
  'image/png',
  'image/webp'
]);

const storage = multer.diskStorage({
  destination: (req, file, cb) => {
    cb(null, uploadDir);
  },
  filename: (req, file, cb) => {
    const itemId = req.params.id;
    const ext = path.extname(file.originalname || '').toLowerCase() || '.jpg';
    const safeExt = ['.jpg', '.jpeg', '.png', '.webp'].includes(ext) ? ext : '.jpg';

    cb(null, `item-${itemId}-${Date.now()}${safeExt}`);
  }
});

const upload = multer({
  storage,
  limits: {
    fileSize: 5 * 1024 * 1024
  },
  fileFilter: (req, file, cb) => {
    if (!allowedImageMimeTypes.has(file.mimetype)) {
      return cb(new Error(INVALID_IMAGE_TYPE_MESSAGE));
    }

    cb(null, true);
  }
});

const itemSelectSql = `
  SELECT
    i.id,
    i.name,
    COALESCE(
      json_agg(
        json_build_object(
          'id', ic.id,
          'code', ic.code,
          'code_type_id', ct.id,
          'code_type_code', ct.code,
          'code_type_name', ct.name
        )
        ORDER BY ic.id
      ) FILTER (WHERE ic.id IS NOT NULL),
      '[]'
    ) AS codes,
    wi.quantity,
    i.unit,
    wi.location,
    wi.min_quantity,
    i.image_filename,
    i.note,
    i.active,
    w.id AS warehouse_id,
    w.name AS warehouse_name,
    i.created_at,
    i.updated_at
  FROM warehouse_items wi
  JOIN items i ON i.id = wi.item_id
  JOIN warehouses w ON w.id = wi.warehouse_id
  LEFT JOIN item_codes ic ON ic.item_id = i.id AND ic.active = true
  LEFT JOIN code_types ct ON ct.id = ic.code_type_id
`;

const itemGroupBySql = `
  GROUP BY
    i.id,
    wi.id,
    w.id
`;

function validateItemName(name) {
  if (!name || !String(name).trim()) {
    throw httpError(400, ITEM_NAME_REQUIRED_MESSAGE);
  }
}

function validateCode(code) {
  if (!code || !String(code).trim()) {
    throw httpError(400, CODE_REQUIRED_MESSAGE);
  }
}

function deleteFileSilently(filePath) {
  if (filePath) {
    fs.unlink(filePath, () => {});
  }
}

function deleteOldImageIfNeeded(oldImageFilename, newImagePath) {
  if (!oldImageFilename) {
    return;
  }

  const oldImagePath = path.join(uploadDir, oldImageFilename);

  if (oldImagePath !== newImagePath) {
    deleteFileSilently(oldImagePath);
  }
}

function handleDuplicateCodeError(error, next, message = DUPLICATE_CODE_MESSAGE) {
  if (error.code === '23505') {
    return next(httpError(409, message));
  }

  return next(error);
}

async function getCodeTypeId(client, codeTypeCode = 'UNKNOWN') {
  const result = await client.query(
    `
    SELECT id
    FROM code_types
    WHERE code = $1
      AND active = true
    LIMIT 1
    `,
    [codeTypeCode]
  );

  if (result.rows.length > 0) {
    return result.rows[0].id;
  }

  const fallback = await client.query(
    `
    SELECT id
    FROM code_types
    WHERE code = 'UNKNOWN'
    LIMIT 1
    `
  );

  if (fallback.rows.length === 0) {
    throw httpError(500, 'Typ kódu UNKNOWN nebyl nalezen v databázi.');
  }

  return fallback.rows[0].id;
}

async function getFullItem(warehouseId, itemId, onlyActive = false) {
  const activeCondition = onlyActive ? 'AND i.active = true' : '';

  const result = await pool.query(
    `
    ${itemSelectSql}
    WHERE wi.warehouse_id = $1
      AND i.id = $2
      ${activeCondition}
    ${itemGroupBySql}
    LIMIT 1
    `,
    [warehouseId, itemId]
  );

  return result.rows[0] || null;
}

async function getActiveItemForUpdate(client, itemId) {
  const result = await client.query(
    `
    SELECT id, image_filename
    FROM items
    WHERE id = $1
      AND active = true
    FOR UPDATE
    `,
    [itemId]
  );

  return result.rows[0] || null;
}

async function ensureItemExists(client, itemId) {
  const result = await client.query(
    `
    SELECT id
    FROM items
    WHERE id = $1
    `,
    [itemId]
  );

  if (result.rows.length === 0) {
    throw httpError(404, ITEM_NOT_FOUND_MESSAGE);
  }
}

async function insertItemCode(client, itemId, code, codeType = 'UNKNOWN') {
  validateCode(code);

  const codeTypeId = await getCodeTypeId(client, codeType);

  const result = await client.query(
    `
    INSERT INTO item_codes (item_id, code, code_type_id)
    VALUES ($1, $2, $3)
    RETURNING *
    `,
    [itemId, code, codeTypeId]
  );

  return result.rows[0];
}

async function createWarehouseItem(
  client,
  warehouseId,
  itemId,
  quantity,
  location,
  minQuantity
) {
  await client.query(
    `
    INSERT INTO warehouse_items
      (warehouse_id, item_id, quantity, location, min_quantity)
    VALUES ($1, $2, $3, $4, $5)
    `,
    [warehouseId, itemId, quantity, location, minQuantity]
  );
}

router.get('/', async (req, res, next) => {
  try {
    const warehouseId = getWarehouseId(req);

    const result = await pool.query(
      `
      ${itemSelectSql}
      WHERE wi.warehouse_id = $1
        AND i.active = true
      ${itemGroupBySql}
      ORDER BY i.name
      `,
      [warehouseId]
    );

    res.json(result.rows);
  } catch (error) {
    next(error);
  }
});

router.get('/search', async (req, res, next) => {
  try {
    const warehouseId = getWarehouseId(req);
    const query = getStringQuery(req, 'q');

    if (query.length < 1) {
      return res.json([]);
    }

    const searchValue = `%${query}%`;

    const result = await pool.query(
      `
      ${itemSelectSql}
      WHERE wi.warehouse_id = $1
        AND i.active = true
        AND (
          unaccent(i.name) ILIKE unaccent($2)
          OR EXISTS (
            SELECT 1
            FROM item_codes search_codes
            WHERE search_codes.item_id = i.id
              AND search_codes.active = true
              AND search_codes.code ILIKE $2
          )
        )
      ${itemGroupBySql}
      ORDER BY i.name
      LIMIT 20
      `,
      [warehouseId, searchValue]
    );

    res.json(result.rows);
  } catch (error) {
    next(error);
  }
});

router.get('/code/:code', async (req, res, next) => {
  try {
    const warehouseId = getWarehouseId(req);
    const { code } = req.params;

    const result = await pool.query(
      `
      ${itemSelectSql}
      JOIN item_codes search_code
        ON search_code.item_id = i.id
       AND search_code.active = true
      WHERE wi.warehouse_id = $1
        AND i.active = true
        AND search_code.code = $2
      ${itemGroupBySql}
      LIMIT 1
      `,
      [warehouseId, code]
    );

    if (result.rows.length === 0) {
      throw httpError(404, ITEM_NOT_FOUND_MESSAGE);
    }

    res.json(result.rows[0]);
  } catch (error) {
    next(error);
  }
});

router.post('/:id/image', upload.single('image'), async (req, res, next) => {
  const client = await pool.connect();

  try {
    const { id } = req.params;
    const warehouseId = getWarehouseId(req);

    if (!req.file) {
      throw httpError(400, IMAGE_REQUIRED_MESSAGE);
    }

    await client.query('BEGIN');

    const item = await getActiveItemForUpdate(client, id);

    if (!item) {
      deleteFileSilently(req.file.path);
      throw httpError(404, ITEM_NOT_FOUND_MESSAGE);
    }

    const oldImageFilename = item.image_filename;

    await client.query(
      `
      UPDATE items
      SET image_filename = $1,
          updated_at = NOW()
      WHERE id = $2
      `,
      [req.file.filename, id]
    );

    await client.query('COMMIT');

    deleteOldImageIfNeeded(oldImageFilename, req.file.path);

    const fullItem = await getFullItem(warehouseId, id);

    res.json(fullItem);
  } catch (error) {
    await client.query('ROLLBACK');

    if (req.file?.path) {
      deleteFileSilently(req.file.path);
    }

    next(error);
  } finally {
    client.release();
  }
});

router.get('/:id', async (req, res, next) => {
  try {
    const warehouseId = getWarehouseId(req);
    const { id } = req.params;

    const item = await getFullItem(warehouseId, id, true);

    if (!item) {
      throw httpError(404, ITEM_NOT_FOUND_MESSAGE);
    }

    res.json(item);
  } catch (error) {
    next(error);
  }
});

router.post('/', async (req, res, next) => {
  const client = await pool.connect();

  try {
    const {
      name,
      code = null,
      codeType = 'UNKNOWN',
      unit = 'ks',
      imageFilename = null,
      note = null,
      warehouseId = 1,
      quantity = 0,
      location = null,
      minQuantity = 0
    } = req.body;

    validateItemName(name);

    await client.query('BEGIN');

    const itemResult = await client.query(
      `
      INSERT INTO items (name, unit, image_filename, note)
      VALUES ($1, $2, $3, $4)
      RETURNING *
      `,
      [name, unit, imageFilename, note]
    );

    const item = itemResult.rows[0];

    if (code) {
      await insertItemCode(client, item.id, code, codeType);
    }

    await createWarehouseItem(
      client,
      warehouseId,
      item.id,
      quantity,
      location,
      minQuantity
    );

    await client.query('COMMIT');

    const fullItem = await getFullItem(warehouseId, item.id);

    res.status(201).json(fullItem);
  } catch (error) {
    await client.query('ROLLBACK');
    return handleDuplicateCodeError(error, next, DUPLICATE_CODE_OTHER_ITEM_MESSAGE);
  } finally {
    client.release();
  }
});

router.put('/:id', async (req, res, next) => {
  const client = await pool.connect();

  try {
    const { id } = req.params;
    const {
      name,
      unit,
      imageFilename,
      note,
      active,
      warehouseId = 1,
      location,
      minQuantity
    } = req.body;

    await client.query('BEGIN');

    const itemResult = await client.query(
      `
      UPDATE items
      SET
        name = COALESCE($1, name),
        unit = COALESCE($2, unit),
        image_filename = COALESCE($3, image_filename),
        note = COALESCE($4, note),
        active = COALESCE($5, active),
        updated_at = NOW()
      WHERE id = $6
      RETURNING *
      `,
      [name, unit, imageFilename, note, active, id]
    );

    if (itemResult.rows.length === 0) {
      throw httpError(404, ITEM_NOT_FOUND_MESSAGE);
    }

    await client.query(
      `
      UPDATE warehouse_items
      SET
        location = COALESCE($1, location),
        min_quantity = COALESCE($2, min_quantity),
        updated_at = NOW()
      WHERE warehouse_id = $3
        AND item_id = $4
      `,
      [location, minQuantity, warehouseId, id]
    );

    await client.query('COMMIT');

    const fullItem = await getFullItem(warehouseId, id);

    res.json(fullItem);
  } catch (error) {
    await client.query('ROLLBACK');
    next(error);
  } finally {
    client.release();
  }
});

router.post('/:id/codes', async (req, res, next) => {
  const client = await pool.connect();

  try {
    const { id } = req.params;
    const {
      code,
      codeType = 'UNKNOWN'
    } = req.body;

    validateCode(code);

    await client.query('BEGIN');

    await ensureItemExists(client, id);

    const newCode = await insertItemCode(
      client,
      id,
      code,
      codeType
    );

    await client.query('COMMIT');

    res.status(201).json(newCode);
  } catch (error) {
    await client.query('ROLLBACK');
    return handleDuplicateCodeError(error, next, DUPLICATE_CODE_MESSAGE);
  } finally {
    client.release();
  }
});

router.delete('/:itemId/codes/:codeId', async (req, res, next) => {
  try {
    const { itemId, codeId } = req.params;

    const result = await pool.query(
      `
      UPDATE item_codes
      SET active = false,
          updated_at = NOW()
      WHERE id = $1
        AND item_id = $2
      RETURNING *
      `,
      [codeId, itemId]
    );

    if (result.rows.length === 0) {
      throw httpError(404, CODE_NOT_FOUND_MESSAGE);
    }

    res.json({ message: 'Kód byl odebrán.' });
  } catch (error) {
    next(error);
  }
});

router.use((error, req, res, next) => {
  if (error instanceof multer.MulterError) {
    if (error.code === 'LIMIT_FILE_SIZE') {
      return next(httpError(400, 'Obrázek je moc velký. Maximum je 5 MB.'));
    }

    return next(httpError(400, error.message));
  }

  if (error.message === INVALID_IMAGE_TYPE_MESSAGE) {
    return next(httpError(400, error.message));
  }

  next(error);
});

export default router;