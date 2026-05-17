# Sklad Server

Lokální backend pro Android skladový systém.

## Technologie

- Node.js
- Express
- PostgreSQL
- REST API

## Co server umí

- seznam skladových položek
- hledání položky podle QR / čárového kódu
- detail položky
- vytvoření a úprava položky
- seznam uživatelů
- příjem materiálu
- výdej materiálu
- historie skladových pohybů
- kontrola, že při výdeji není vydáno více kusů, než je na skladě

## 1. Instalace PostgreSQL

Vytvoř databázi například s názvem:

```sql
CREATE DATABASE sklad_db;
```

Potom spusť SQL soubory v tomto pořadí:

```bash
psql -U postgres -d sklad_db -f sql/schema.sql
psql -U postgres -d sklad_db -f sql/seed.sql
```

## 2. Nastavení serveru

Zkopíruj soubor `.env.example` jako `.env`:

```bash
cp .env.example .env
```

Uprav přihlašovací údaje k databázi:

```env
PORT=3000
DB_HOST=localhost
DB_PORT=5432
DB_NAME=sklad_db
DB_USER=postgres
DB_PASSWORD=postgres
```

## 3. Instalace závislostí

```bash
npm install
```

## 4. Spuštění serveru

Vývojové spuštění:

```bash
npm run dev
```

Normální spuštění:

```bash
npm start
```

Server poběží na:

```text
http://localhost:3000
```

Z telefonu ve stejné Wi-Fi se bude používat IP adresa počítače, například:

```text
http://192.168.1.50:3000
```

## Test serveru

```bash
GET /health
```

Očekávaná odpověď:

```json
{
  "status": "ok",
  "app": "sklad-server"
}
```

## API endpointy

### Položky

```http
GET /items
```

Vrátí seznam položek.

```http
GET /items?search=sroub
```

Vyhledá položky podle názvu, kódu nebo umístění.

```http
GET /items/1
```

Vrátí detail položky podle ID.

```http
GET /items/code/859000000001
```

Vrátí položku podle QR / čárového kódu.

```http
POST /items
Content-Type: application/json

{
  "name": "Nová položka",
  "code": "123456789",
  "quantity": 10,
  "unit": "ks",
  "location": "Regál C1",
  "note": "Poznámka"
}
```

Vytvoří novou položku.

```http
PUT /items/1
Content-Type: application/json

{
  "name": "Upravený název",
  "location": "Regál A3"
}
```

Upraví položku.

### Uživatelé

```http
GET /users
```

Vrátí aktivní uživatele. Zatím existuje role `skladnik`.

### Skladové pohyby

```http
GET /movements
```

Vrátí historii pohybů.

```http
GET /movements?limit=20
```

Vrátí posledních 20 pohybů.

### Příjem materiálu

```http
POST /movements/in
Content-Type: application/json

{
  "itemId": 1,
  "userId": 1,
  "quantity": 5,
  "note": "Doplnění skladu"
}
```

Zvýší množství položky a uloží pohyb do historie.

### Výdej materiálu

```http
POST /movements/out
Content-Type: application/json

{
  "itemId": 1,
  "userId": 1,
  "quantity": 2,
  "note": "Výdej do výroby"
}
```

Sníží množství položky a uloží pohyb do historie.

Pokud není na skladě dost kusů, server vrátí chybu.

## Doporučený první test přes Postman / Insomnia

1. `GET http://localhost:3000/health`
2. `GET http://localhost:3000/items`
3. `GET http://localhost:3000/items/code/859000000001`
4. `POST http://localhost:3000/movements/in`
5. `POST http://localhost:3000/movements/out`
6. `GET http://localhost:3000/movements`

## Poznámka pro Android aplikaci

Android aplikace bude volat stejná API. Například po naskenování kódu:

```text
GET /items/code/{naskenovanyKod}
```

Potom při příjmu:

```text
POST /movements/in
```

A při výdeji:

```text
POST /movements/out
```
