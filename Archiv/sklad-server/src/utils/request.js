export function getWarehouseId(req) {
  return Number(req.query.warehouseId || 1);
}

export function getLimit(req, defaultValue = 20, maxValue = 100) {
  const limit = Number(req.query.limit || defaultValue);

  return Number.isFinite(limit) && limit > 0
    ? Math.min(limit, maxValue)
    : defaultValue;
}

export function getStringQuery(req, key, defaultValue = '') {
  return String(req.query[key] || defaultValue).trim();
}

export function getBooleanQuery(req, key, defaultValue = false) {
  const value = req.query[key];

  if (value === undefined) {
    return defaultValue;
  }

  return value === 'true' || value === true;
}