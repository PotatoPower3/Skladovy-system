import express from 'express';
import cors from 'cors';
import dotenv from 'dotenv';
import path from 'path';
import { fileURLToPath } from 'url';
import os from 'os';

import itemsRouter from './routes/items.js';
import usersRouter from './routes/users.js';
import movementsRouter from './routes/movements.js';
import warehousesRouter from './routes/warehouses.js';
import documentsRouter from './routes/documents.js';
import authRouter from './routes/auth.js';
import { httpError } from './utils/httpError.js';

dotenv.config();

const app = express();

const port = process.env.PORT || 3000;

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

function getLocalIpAddresses() {
  const interfaces = os.networkInterfaces();
  const addresses = [];

  for (const networkInterface of Object.values(interfaces)) {
    if (!networkInterface) {
      continue;
    }

    for (const address of networkInterface) {
      if (
        address.family === 'IPv4' &&
        !address.internal
      ) {
        addresses.push(address.address);
      }
    }
  }

  return addresses;
}

function printServerUrls(port) {
  console.log(`Server běží na portu ${port}`);

  const ipAddresses = getLocalIpAddresses();

  for (const ipAddress of ipAddresses) {
    console.log(`Adresa serveru: http://${ipAddress}:${port}`);
  }
}

app.use(cors());
app.use(express.json());

app.use('/uploads', express.static(path.join(__dirname, '../uploads')));

app.get('/health', (req, res) => {
  res.json({ status: 'ok' });
});

app.use('/auth', authRouter);
app.use('/items', itemsRouter);
app.use('/users', usersRouter);
app.use('/movements', movementsRouter);
app.use('/warehouses', warehousesRouter);
app.use('/documents', documentsRouter);

app.use((req, res, next) => {
  next(httpError(404, 'Endpoint nebyl nalezen.'));
});

app.use((error, req, res, next) => {
  console.error(error);

  const statusCode = error.statusCode || 500;

  res.status(statusCode).json({
    message: error.message || 'Nastala chyba serveru.'
  });
});

app.listen(port, '0.0.0.0', () => {
  printServerUrls(port);
});