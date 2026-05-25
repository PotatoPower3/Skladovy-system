import dotenv from 'dotenv';
import { spawnSync } from 'child_process';

dotenv.config();

const sqlFile = process.argv[2];

if (!sqlFile) {
  console.error('Chybí cesta k SQL souboru.');
  process.exit(1);
}

const {
  DB_HOST = 'localhost',
  DB_PORT = '5432',
  DB_NAME,
  DB_USER,
  DB_PASSWORD
} = process.env;

if (!DB_NAME || !DB_USER) {
  console.error('V .env musí být nastaveno DB_NAME a DB_USER.');
  process.exit(1);
}

const result = spawnSync(
  'psql',
  [
    '-h', DB_HOST,
    '-p', DB_PORT,
    '-U', DB_USER,
    '-d', DB_NAME,
    '-f', sqlFile
  ],
  {
    stdio: 'inherit',
    env: {
      ...process.env,
      PGPASSWORD: DB_PASSWORD || ''
    }
  }
);

process.exit(result.status ?? 1);