import express from 'express';
import cors from 'cors';
import dotenv from 'dotenv';

import itemsRouter from './routes/items.js';
import usersRouter from './routes/users.js';
import movementsRouter from './routes/movements.js';
import warehousesRouter from './routes/warehouses.js';
import documentsRouter from './routes/documents.js';
import authRouter from './routes/auth.js';

import path from 'path';
import { fileURLToPath } from 'url';

dotenv.config();

const app = express();

app.use(cors());
app.use(express.json());

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

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

app.use((req, res) => {
  res.status(404).json({ message: 'Endpoint nebyl nalezen.' });
});

app.use((error, req, res, next) => {
  console.error(error);

  res.status(error.statusCode || 500).json({
    message: error.message || 'Nastala chyba serveru.'
  });
});

const port = process.env.PORT || 3000;

app.listen(port, '0.0.0.0', () => {
  console.log(`Server běží na portu ${port}`);
});