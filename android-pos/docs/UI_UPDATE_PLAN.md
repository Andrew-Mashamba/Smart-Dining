# UI Update Plan: Existing Pages

**Goal:** Update all existing screens and list items to match [ANDROID_DESIGN_GUIDELINES.md](ANDROID_DESIGN_GUIDELINES.md). Monochrome palette, consistent spacing, typography scale, touch targets, and component patterns.

**Reference:** Single source of truth = `docs/ANDROID_DESIGN_GUIDELINES.md`.

---

## Scope: Pages & Items

### Activities (9)

| # | Layout | Activity | Priority | Notes |
|---|--------|----------|----------|--------|
| 1 | `activity_login.xml` | LoginActivity | High | Entry point; header, cards, PIN, loading overlay |
| 2 | `activity_main.xml` | MainActivity | High | Dashboard; toolbar, header, action cards (Tables, Orders, Menu, Payments) |
| 3 | `activity_tables.xml` | TablesActivity | High | Table grid; section header, list/grid, empty state |
| 4 | `activity_orders.xml` | OrdersActivity | High | Order list; filters, list, empty state |
| 5 | `activity_order.xml` | OrderActivity | High | Create order; menu + cart, section headers |
| 6 | `activity_order_details.xml` | OrderDetailsActivity | Medium | Order detail; info card, items, actions (Mark Served, Add Items, Payment) |
| 7 | `activity_menu.xml` | MenuActivity | Medium | Menu browse; categories, grid/list, search |
| 8 | `activity_payment.xml` | PaymentActivity | Medium | Payment; bill summary, payment method, tip |
| 9 | `activity_tips.xml` | TipsActivity | Medium | Tips dashboard; list, summary, empty state |

### List / Item layouts (6)

| # | Layout | Used in | Notes |
|---|--------|---------|--------|
| 1 | `item_table.xml` | TablesActivity | Table card: icon, name, info, status chip |
| 2 | `item_order.xml` | OrdersActivity | Order card: id, table, status, sync badge |
| 3 | `item_order_item.xml` | OrderDetailsActivity, cart | Line item: name, qty, price, notes |
| 4 | `item_menu.xml` | OrderActivity, MenuActivity | Menu item: image, name, price, category |
| 5 | `item_cart.xml` | OrderActivity | Cart row: name, qty controls, price |
| 6 | `item_tip.xml` | TipsActivity | Tip row: amount, order, date, method |

---

## Checklist per screen (apply everywhere)

### Colors & theme
- [ ] Background `#FAFAFA` (`@color/background_primary`)
- [ ] Cards/surfaces `#FFFFFF` (`@color/surface_white`)
- [ ] No orange/green/blue; use monochrome status colors only
- [ ] Text: primary `#1A1A1A`, secondary `#666666`, tertiary `#999999`

### Typography
- [ ] Page title: `TextAppearance.SeaCliff.Heading` or `TitleLarge` (20sp)
- [ ] Section headers: `TextAppearance.SeaCliff.TitleMedium` (16sp)
- [ ] Card/button title: `TextAppearance.SeaCliff.Title` (15sp)
- [ ] Subtitle / secondary: `TextAppearance.SeaCliff.Subtitle` or `LabelSmall` (11sp)
- [ ] Body: `TextAppearance.SeaCliff.Body` or `BodyLarge` (14sp)
- [ ] All dynamic text: `maxLines` + `ellipsize="end"`

### Spacing
- [ ] Screen horizontal padding: `@dimen/spacing_xl` (24dp)
- [ ] Card internal padding: `@dimen/spacing_lg` (16dp)
- [ ] Gap between cards: `@dimen/spacing_md` (12dp)
- [ ] Icon-to-text: `@dimen/spacing_sm` (8dp)
- [ ] Section margins: `@dimen/spacing_xl` or `@dimen/spacing_xxl`

### Touch & layout
- [ ] All tappable elements ≥ 48×48dp (`@dimen/touch_target_min`)
- [ ] Primary action buttons: min height `@dimen/button_height_standard` (72dp)
- [ ] Cards: `corner_radius_large` (16dp) or `corner_radius_medium` (12dp)
- [ ] Scrollable content in `NestedScrollView` / `ScrollView` where needed
- [ ] No overflow: use `layout_constraint*`, `maxLines`, and `ellipsize`

### AppBar (where used)
- [ ] Toolbar: elevation 0, background white, title centered, `titleTextColor` primary
- [ ] Back/actions: 48dp touch target, no colored icons (use `text_primary` / `text_tertiary`)

### Component patterns (from guidelines)
- [ ] **Action cards (main menu):** White card, 48dp dark circle icon, title 15sp, subtitle 11sp, 12dp gap icon–text
- [ ] **Info cards:** White, 16dp radius, optional border, icon 48×48 rounded square
- [ ] **Navigation cards:** Light icon bg (8% primary), title + subtitle, chevron right
- [ ] **Chips/badges:** `@color/overlay_chip`, 12dp radius, 8×4 padding
- [ ] **List tiles:** Leading 48×48 rounded (8dp), title + subtitle, 8dp vertical padding
- [ ] **Empty state:** Centered icon 64dp tertiary, title 18sp, description 14sp, 16+8dp gaps
- [ ] **Loading:** Centered `ProgressBar` or card with message; use `ProgressBar` style from theme

### Strings & a11y
- [ ] No hardcoded strings; use `@string/` (add to `strings.xml` where missing)
- [ ] Content descriptions on icons/images (or `contentDescription="@null"` for decorative)
- [ ] Minimum contrast; prefer existing semantic colors

---

## Suggested order of work

1. **Design tokens** (done) — `dimens.xml`, `colors.xml`, `styles.xml` aligned with guidelines.
2. **Main flow:** Login → Main → Tables → Order → OrderDetails → Payment. Update these activity layouts and their item layouts first.
3. **Secondary:** Orders list, Menu, Tips. Then polish toolbars, empty states, and loading states across all.
4. **Tablet:** Review `values-sw600dp/dimens.xml` and any layout variants; ensure grids and padding scale.

---

## Files to touch (summary)

- **Layouts:** `activity_*.xml` (9), `item_*.xml` (6).
- **Values:** `colors.xml`, `dimens.xml`, `styles.xml`, `themes.xml`, `strings.xml`.
- **Optional:** Reusable drawables for icon containers (`bg_icon_rounded_square`, `bg_icon_light_rounded`) if extracting from layouts.

---

## Done criteria

- Every activity and item layout passes the checklist above.
- All screens look consistent with ANDROID_DESIGN_GUIDELINES (monochrome, spacing, typography, touch targets).
- No new hardcoded colors or dimensions; use `@color/` and `@dimen/`.
- Empty and loading states implemented where applicable.
- Design checklist in ANDROID_DESIGN_GUIDELINES.md can be run mentally on each screen with “yes” for all items.
