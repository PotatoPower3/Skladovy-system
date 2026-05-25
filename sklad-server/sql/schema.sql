DROP TABLE IF EXISTS stock_movements CASCADE;
DROP TABLE IF EXISTS stock_document_items CASCADE;
DROP TABLE IF EXISTS stock_documents CASCADE;
DROP TABLE IF EXISTS warehouse_items CASCADE;
DROP TABLE IF EXISTS item_codes CASCADE;
DROP TABLE IF EXISTS code_types CASCADE;
DROP TABLE IF EXISTS items CASCADE;
DROP TABLE IF EXISTS movement_types CASCADE;
DROP TABLE IF EXISTS warehouse_locations CASCADE;
DROP TABLE IF EXISTS warehouses CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS roles CASCADE;

CREATE EXTENSION IF NOT EXISTS unaccent;

CREATE TABLE roles (
  id SERIAL PRIMARY KEY,
  name VARCHAR(50) NOT NULL UNIQUE,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE users (
  id SERIAL PRIMARY KEY,
  username VARCHAR(100) NOT NULL UNIQUE,
  password VARCHAR(100) NOT NULL,
  first_name VARCHAR(100) NOT NULL,
  last_name VARCHAR(100) NOT NULL,
  role_id INTEGER NOT NULL REFERENCES roles(id),
  active BOOLEAN NOT NULL DEFAULT true,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE warehouses (
  id SERIAL PRIMARY KEY,
  name VARCHAR(150) NOT NULL,
  code VARCHAR(50) NOT NULL UNIQUE,
  location VARCHAR(150),
  active BOOLEAN NOT NULL DEFAULT true,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE warehouse_locations (
  id SERIAL PRIMARY KEY,
  warehouse_id INTEGER NOT NULL REFERENCES warehouses(id) ON DELETE CASCADE,
  code VARCHAR(50) NOT NULL,
  name VARCHAR(100) NOT NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW(),

  CONSTRAINT warehouse_locations_unique_code_per_warehouse
    UNIQUE (warehouse_id, code)
);

CREATE TABLE movement_types (
  id SERIAL PRIMARY KEY,
  code VARCHAR(50) NOT NULL UNIQUE,
  name VARCHAR(100) NOT NULL,
  direction VARCHAR(20) NOT NULL CHECK (direction IN ('IN', 'OUT', 'NEUTRAL')),
  active BOOLEAN NOT NULL DEFAULT true,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE code_types (
  id SERIAL PRIMARY KEY,
  code VARCHAR(50) NOT NULL UNIQUE,
  name VARCHAR(100) NOT NULL,
  active BOOLEAN NOT NULL DEFAULT true,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE items (
  id SERIAL PRIMARY KEY,
  name VARCHAR(150) NOT NULL,
  unit VARCHAR(20) NOT NULL DEFAULT 'ks',
  weight_per_unit NUMERIC(12, 3) NOT NULL DEFAULT 0 CHECK (weight_per_unit >= 0),
  image_filename TEXT,
  note TEXT,
  active BOOLEAN NOT NULL DEFAULT true,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE item_codes (
  id SERIAL PRIMARY KEY,
  item_id INTEGER NOT NULL REFERENCES items(id) ON DELETE CASCADE,
  code VARCHAR(150) NOT NULL UNIQUE,
  code_type_id INTEGER NOT NULL REFERENCES code_types(id),
  active BOOLEAN NOT NULL DEFAULT true,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE warehouse_items (
  id SERIAL PRIMARY KEY,
  warehouse_id INTEGER NOT NULL REFERENCES warehouses(id) ON DELETE CASCADE,
  item_id INTEGER NOT NULL REFERENCES items(id) ON DELETE CASCADE,
  quantity NUMERIC(12, 2) NOT NULL DEFAULT 0 CHECK (quantity >= 0),
  location VARCHAR(100),
  min_quantity NUMERIC(12, 2) NOT NULL DEFAULT 0 CHECK (min_quantity >= 0),
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
  UNIQUE (warehouse_id, item_id)
);

CREATE TABLE stock_documents (
  id SERIAL PRIMARY KEY,
  document_number VARCHAR(50) UNIQUE,
  warehouse_id INTEGER NOT NULL REFERENCES warehouses(id),
  movement_type_id INTEGER NOT NULL REFERENCES movement_types(id),
  status VARCHAR(30) NOT NULL DEFAULT 'CONFIRMED'
    CHECK (status IN ('DRAFT', 'CONFIRMED', 'CANCELLED')),
  note TEXT,
  created_by_user_id INTEGER NOT NULL REFERENCES users(id),
  updated_by_user_id INTEGER NOT NULL REFERENCES users(id),
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE stock_document_items (
  id SERIAL PRIMARY KEY,
  document_id INTEGER NOT NULL REFERENCES stock_documents(id) ON DELETE CASCADE,
  item_id INTEGER NOT NULL REFERENCES items(id),
  quantity NUMERIC(12, 2) NOT NULL CHECK (quantity > 0),
  note TEXT,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
  UNIQUE (document_id, item_id)
);

CREATE INDEX idx_users_role_id ON users(role_id);
CREATE INDEX idx_users_username ON users(username);

CREATE INDEX idx_warehouses_code ON warehouses(code);

CREATE INDEX idx_warehouse_locations_warehouse_id ON warehouse_locations(warehouse_id);
CREATE INDEX idx_warehouse_locations_active ON warehouse_locations(active);

CREATE INDEX idx_items_name ON items(name);
CREATE INDEX idx_items_active ON items(active);

CREATE INDEX idx_code_types_code ON code_types(code);
CREATE INDEX idx_item_codes_code ON item_codes(code);
CREATE INDEX idx_item_codes_item_id ON item_codes(item_id);
CREATE INDEX idx_item_codes_code_type_id ON item_codes(code_type_id);

CREATE INDEX idx_warehouse_items_warehouse_id ON warehouse_items(warehouse_id);
CREATE INDEX idx_warehouse_items_item_id ON warehouse_items(item_id);

CREATE INDEX idx_movement_types_code ON movement_types(code);
CREATE INDEX idx_movement_types_direction ON movement_types(direction);

CREATE INDEX idx_stock_documents_warehouse_id ON stock_documents(warehouse_id);
CREATE INDEX idx_stock_documents_movement_type_id ON stock_documents(movement_type_id);
CREATE INDEX idx_stock_documents_status ON stock_documents(status);
CREATE INDEX idx_stock_documents_created_at ON stock_documents(created_at DESC);
CREATE INDEX idx_stock_documents_created_by_user_id ON stock_documents(created_by_user_id);
CREATE INDEX idx_stock_documents_updated_by_user_id ON stock_documents(updated_by_user_id);

CREATE INDEX idx_stock_document_items_document_id ON stock_document_items(document_id);
CREATE INDEX idx_stock_document_items_item_id ON stock_document_items(item_id);