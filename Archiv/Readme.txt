SKLADOVÝ SYSTÉM

Semestrální projekt – skladový systém pro evidenci položek, příjmů, výdejů a historie dokladů.

Projekt se skládá ze dvou částí:

1) Android aplikace
   - Kotlin
   - Jetpack Compose
   - Retrofit
   - CameraX / ML Kit pro skenování kódů

2) Server
   - Node.js
   - Express
   - PostgreSQL
   - Multer pro nahrávání obrázků


============================================================
1. POŽADAVKY
============================================================

Pro spuštění serveru je potřeba:

- Node.js
- npm
- PostgreSQL
- psql klient

Pro spuštění Android aplikace je potřeba:

- Android Studio
- JDK
- Android SDK
- Android emulátor nebo fyzické Android zařízení


============================================================
2. SERVEROVÉ ZÁVISLOSTI
============================================================

Server používá tyto hlavní balíčky:

- express
- cors
- dotenv
- helmet
- multer
- pg

Pro vývoj:

- nodemon

Závislosti se nainstalují příkazem:

npm install


============================================================
3. PACKAGE.JSON SERVERU
============================================================

Server obsahuje tyto npm skripty:

npm start
- spustí server pomocí node

npm run dev
- spustí server přes nodemon

npm run db:reset
- spustí sql/schema.sql a znovu vytvoří databázovou strukturu

npm run db:seed
- spustí sql/seed.sql a vloží základní testovací data

npm run db:setup
- spustí db:reset a potom db:seed

Pozor:
Příkaz db:setup smaže existující tabulky a znovu je vytvoří.
Používej ho pouze při resetu testovací databáze.


============================================================
4. DATABÁZE
============================================================

Databáze se vytváří v PostgreSQL, ne jako soubor ve složce projektu.

Nejdříve je potřeba vytvořit PostgreSQL databázi, například:

CREATE DATABASE sklad_db;

Poté ve složce serveru spusť:

npm run db:setup

Tento příkaz provede obnovu databáze podle souborů:

sql/schema.sql
sql/seed.sql

schema.sql:
- smaže existující tabulky
- vytvoří databázové tabulky
- vytvoří indexy
- nastaví vazby mezi tabulkami

seed.sql:
- vloží základní role
- vloží testovacího uživatele
- vloží hlavní sklad
- vloží typy pohybů
- vloží typy kódů
- vloží ukázkové položky
- vloží ukázkové doklady


============================================================
5. SERVER
============================================================

Přejdi do složky serveru:

cd server

Nainstaluj závislosti:

npm install

Zkontroluj nebo vytvoř soubor .env.

Příklad .env souboru:

PORT=3000
DB_HOST=localhost
DB_PORT=5432
DB_NAME=sklad_db
DB_USER=uzivatel_postgresql
DB_PASSWORD=heslo

Spuštění serveru pro vývoj:

npm run dev

Spuštění serveru bez nodemonu:

npm start

Po spuštění server vypíše port a dostupnou IP adresu, například:

Server běží na portu 3000
Adresa serveru: http://192.168.1.20:3000

Tuto adresu je potřeba nastavit v Android aplikaci jako BASE_URL.


============================================================
6. ANDROID APLIKACE
============================================================

Otevři Android projekt v Android Studiu.

V souboru:

data/remote/ApiClient.kt

nastav adresu serveru:

const val BASE_URL = "http://192.168.1.20:3000/"

Adresa musí odpovídat IP adrese, kterou vypíše server.

Pro fyzický telefon:
- telefon a počítač musí být ve stejné síti
- například stejná Wi-Fi nebo hotspot
- doporučené je použít IP adresu počítače, kterou vypíše server

Poznámka k localhost:
Na fyzickém telefonu lze někdy použít localhost, například při přesměrování přes USB / ADB.
Je ale nutné počítat s tím, že při použití localhost mohou nastat problémy s načítáním fotek.
Pro běžné testování na telefonu je spolehlivější použít IP adresu počítače.

Pro Android emulátor se obvykle používá:

const val BASE_URL = "http://10.0.2.2:3000/"

Poté spusť aplikaci z Android Studia.


============================================================
7. TESTOVACÍ PŘIHLÁŠENÍ
============================================================

Výchozí testovací uživatel ze seed.sql:

Uživatel: petr
Heslo: 1234

Nového uživatele aktuálně nelze založit přímo v aplikaci.
Pokud je potřeba přidat dalšího uživatele, musí se vytvořit přímo v databázi v tabulce users.


============================================================
8. HLAVNÍ FUNKCE APLIKACE
============================================================

Aplikace umožňuje:

- přihlášení uživatele
- zobrazení skladových položek
- vyhledávání položek podle názvu nebo kódu
- zobrazení detailu položky
- úpravu položky
- nahrání obrázku položky z galerie
- vyfocení obrázku položky
- vytvoření příjmu
- vytvoření výdeje
- skenování čárových a QR kódů
- ruční vyhledání produktu
- založení nového produktu při příjmu
- přiřazení nového kódu k existujícímu produktu
- zobrazení historie dokladů
- zobrazení detailu dokladu
- editaci existujícího dokladu


============================================================
9. SERVEROVÉ ENDPOINTY
============================================================

Základní endpointy:

GET    /health
POST   /auth/login

GET    /items
GET    /items/search?q=
GET    /items/code/:code
GET    /items/:id
POST   /items
PUT    /items/:id
POST   /items/:id/image
POST   /items/:id/codes
DELETE /items/:itemId/codes/:codeId

GET    /documents
GET    /documents/:id
POST   /documents/in
POST   /documents/out
PUT    /documents/:id

GET    /movements
GET    /users
GET    /warehouses


============================================================
10. FOTKY POLOŽEK
============================================================

Fotky položek se ukládají na server do složky:

uploads/items

Server fotky zpřístupňuje přes URL:

http://IP_ADRESA_SERVERU:3000/uploads/items/nazev-souboru.jpg

Příklad:

http://192.168.1.20:3000/uploads/items/item-1-123456789.jpg


============================================================
11. POZNÁMKY K BEZPEČNOSTI
============================================================

Hesla jsou v této školní/testovací verzi uložená jednoduše v databázi.
V produkční verzi by bylo vhodné použít hashování hesel, například pomocí knihovny bcrypt.
Server i aplikace jsou určeny hlavně pro lokální síťové testování.


============================================================
12. DOPORUČENÝ POSTUP SPUŠTĚNÍ
============================================================

1) Spustit PostgreSQL

2) Vytvořit databázi:

CREATE DATABASE sklad_db;

3) Ve složce serveru nainstalovat závislosti:

npm install

4) Zkontrolovat .env soubor

5) Připravit databázi:

npm run db:setup

6) Spustit server:

npm run dev

7) Opsat IP adresu serveru z konzole

8) Nastavit BASE_URL v Android aplikaci

9) Spustit Android aplikaci z Android Studia

10) Přihlásit se testovacím uživatelem:

petr / 1234