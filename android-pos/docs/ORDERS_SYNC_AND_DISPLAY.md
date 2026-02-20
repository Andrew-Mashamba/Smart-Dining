# Orders: backend response, sync, and display

## What the backend returns (GET /api/orders)

Confirmed with the backend via Assistant Endpoint. Laravel returns **paginated JSON** (no wrapper):

- **Top-level:** `current_page`, `data` (array of orders), `last_page`, `per_page`, `total`, `first_page_url`, `from`, `to`, `next_page_url`, `prev_page_url`, `path`, `links`.
- **Each order in `data`:** `id`, `order_number`, `table_id`, `guest_id`, `waiter_id`, `order_source`, `status`, `subtotal`, `tax`, `total` (decimals as strings), `special_instructions`, `created_at`, `updated_at` (ISO 8601), `order_items[]`, `guest`, `table`, `waiter`.
- **Query params:** `status`, `table_id`, `waiter_id`, `date` (YYYY-MM-DD). Per-page 20; ordered by `created_at` desc. Waiters only see their own orders; others see all (filtered by params).

## How we sync

1. **Orders screen load / pull-to-refresh:** `OrderViewModel.syncAndLoadTodayOrders()` runs.
2. **Repository:** `OrderRepository.syncOrders()` calls `GET /api/orders?date=YYYY-MM-DD` (today) so the first page is today’s orders. Requires auth (Bearer).
3. **Mapping:** Response body is `PaginatedOrdersResponse` (`data`: `List<OrderDto>`). Each `OrderDto` is converted to `OrderEntity` via `OrderDto.toEntity()`:
   - Scalars mapped; `created_at` string parsed with `parseCreatedAt()` (ISO 8601 → `Date`) so “today” filter works.
   - Relations (`order_items`, `guest`, `table`, `waiter`) are not stored on `OrderEntity`; only the order row is persisted.
4. **Storage:** Each entity is inserted with `orderDao.insertOrder(it)` (REPLACE conflict), so local DB is updated with today’s orders from the API.
5. **After sync:** We then load the list from local DB via `getTodayOrders()` (Room query: `DATE(created_at/1000,'unixepoch') = DATE('now')`) and set `_orders.value`, so the UI shows orders for “today” from local.

**Create order path:** When `POST /api/orders` returns 201, we build `OrderEntity` from the create response (`OrderSummaryDetailDto.toEntity()`), parse `created_at` (or use `Date()` as fallback), insert the order and its items locally so the new order appears in “today” without waiting for a full sync.

## How we show

- **Screen:** `OrdersActivity`; tabs: All, Pending, Preparing, Ready, Served.
- **All tab:** `loadTodayOrders()` → `orderRepository.getTodayOrders()` (Flow from Room). List shows orders with `created_at` = today (device date in SQLite).
- **Other tabs:** `loadOrdersByStatus(status)` → `orderDao.getOrdersByStatus(status)` (same local DB).
- **List item:** `OrderListAdapter` binds `OrderEntity`: order #, table ID, total, status, time (`createdAt` formatted), status color, sync indicator. Tap opens `OrderDetailsActivity` with `ORDER_ID`.

## Alignment checklist

| Backend | App |
|--------|-----|
| Pagination: `data`, `current_page`, `per_page`, `total`, `last_page` | `PaginatedOrdersResponse` + `OrderDto` match. |
| Order: `id`, `created_at` (ISO), `subtotal`/`tax`/`total` (string) | Parsed/stored; Gson coerces numeric strings to Double. |
| `order_items`, `guest`, `table`, `waiter` | DTO has them; we only persist the order row for list; details can load order by id. |
| Filter `date` (YYYY-MM-DD) | We do not pass date; we fetch recent orders and filter today locally. |

## Notes

- Sync only fetches **first page** (up to 20 orders). Passing `date=today` keeps that page relevant for the “today” list.
- If sync fails (e.g. 401), we fall back to local `getAllOrders()` for the return value but the UI list is still driven by `getTodayOrders()` / `getOrdersByStatus()` from local DB only.
- `created_at` must be non-null for an order to appear in “All” (today); we set it from API or `Date()` when creating/syncing.
