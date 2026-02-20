# Data sync and backend as source of truth

No reference data is hardcoded in the app. Tables, menu items, and staff are stored in the local Room database and are loaded from the backend API.

**Alignment with backend:** The API contract (paths, response shapes, nullable fields) was confirmed with the Smart-Dining backend via the [Assistant Endpoint](ASSISTANT_ENDPOINT_SKILL.md) (`ask_backend.sh`). Use that flow for any future backend changes or questions.

## Flow

1. **Backend** – Tables, menu, and staff are managed on the server (e.g. Smart-Dining API). The app does not seed or define this data in code.
2. **Sync** – After login and on app start, `SyncWorker` runs and:
   - Fetches **tables** from `GET /tables` and saves them to the `tables` table.
   - Fetches **menu items** from `GET /menu` and saves them to the `menu_items` table.
   - Fetches **staff** (for PIN login) from `GET /auth/staff-list` and saves them to the `staff` table.
3. **UI** – The Tables, Menu, and PIN login screens read from the local database. Repositories may also try to refresh from the API when loading (e.g. `TableRepository.getTables()`, `MenuRepository.getMenuItems()`), then emit what’s in the DB.

## When sync runs

- **After login** (email/password or PIN): immediate one-time sync is triggered.
- **On app start**: immediate one-time sync is triggered (if not logged in, the worker does nothing).
- **Periodically**: `SyncManager` schedules a periodic WorkManager job (interval from `BuildConfig.SYNC_INTERVAL`) so data is refreshed in the background.

## Backend endpoints used for sync

(Aligned with backend via Assistant Endpoint; see [ASSISTANT_ENDPOINT_SKILL.md](ASSISTANT_ENDPOINT_SKILL.md).)

| Data   | Method | Path                | Auth  | Response (example)     |
|--------|--------|---------------------|-------|-------------------------|
| Tables | GET    | `/api/tables`       | Yes   | `{ "tables": [...], "total": N }` |
| Menu   | GET    | `/api/menu/items`   | No    | `{ "items": [...], "total": N }` (preferred for POS sync) |
| Staff  | GET    | `/api/auth/staff-list` | No | `{ "staff": [...] }`    |

All require a valid auth token (Bearer) except endpoints used before login. The sync worker runs only when the user is logged in.

### Menu items response (verified)

`GET /api/menu/items` returns `{ "items": [...], "total": N }`. Each item has: `id`, `name`, `description`, `price` (number), `category` (`{ "id", "name" }`), `prep_area`, `prep_time_minutes` (number, nullable in app), `image_url`, `available`, `is_popular`, `dietary_info`, `created_at`, `updated_at`. The app maps these to `MenuItemDto` and then to `MenuItemEntity`; `price` may be integer in JSON (e.g. 15000) and is parsed as `Double`; `prep_time_minutes` is optional and defaults to 0 in the entity.

## How to refresh the app database

To pick up new data from the backend (e.g. new tables, menu changes):

1. **Pull to refresh (Tables / Menu)**  
   On the **Tables** or **Menu** screen, **pull down** to refresh. The app will call the API, update the local DB, and re-render. This refreshes only that screen’s data (tables or menu).

2. **Re-login**  
   Log out and log in again. Login triggers a **full sync** (tables, menu, staff) via `SyncWorker`, so the local database is repopulated from the backend.

3. **Restart the app**  
   On launch (when already logged in), the app triggers an immediate sync. So fully closing and reopening the app will run a sync and refresh all reference data.

4. **Clear app data (nuclear)**  
   Settings → Apps → SeaCliff POS → Storage → **Clear data**. Next login will sync everything from scratch. Use only if the DB is stuck or corrupted.

**Summary:** For new tables/nomenclature, use **pull-to-refresh on the Tables screen** or **log out and log in again** (or restart the app).

---

## Empty state

If the backend is unreachable or returns no data, the local database may be empty. The UI should handle empty lists (e.g. empty state on Tables and Menu screens). The first successful sync after login or when the backend is available will populate the database.

## Configuration

- API base URL: `BuildConfig.API_BASE_URL` (e.g. from `local.properties`: `api.base.url`).
- Sync interval: `BuildConfig.SYNC_INTERVAL` (seconds) for the periodic job.

## Test credentials (backend-provided)

For automated or manual testing of authenticated endpoints (e.g. `GET /api/tables`), the backend provides:

| Field    | Value |
|----------|--------|
| **Email**    | `pos-test@seacliff.com` |
| **Password** | `password` |
| **Role**     | waiter |
| **PIN (Staff Login)** | `1234` (for POS Test Waiter; only staff with PIN set appear in Staff Login) |

**Get a token (login):**
```bash
curl -s -X POST "https://zima-uat.site/Smart-Dining/api/auth/login" \
  -H "Accept: application/json" -H "Content-Type: application/json" \
  -d '{"email":"pos-test@seacliff.com","password":"password","device_name":"Android POS"}'
```
Use the `token` from the response as `Authorization: Bearer <token>`.

**Long-lived token (on server):** Run `php artisan pos:test-credentials` on the backend; it prints a Bearer token (and revokes the previous one). Save that token for curl/Postman if needed.

**Tables nomenclature:** Backend tables are seeded with T0001–T0008 (indoor), OT001–OT003 (outdoor), BT01–BT03 (bar). Reseed with `php artisan db:seed --class=TableSeeder` on the server if required.
