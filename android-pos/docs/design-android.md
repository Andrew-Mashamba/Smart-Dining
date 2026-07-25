# POS Android Design Guidelines

## Minimalist Design Principles for Android

This document outlines the core design principles and guidelines for the POS Android application, focusing on minimalist aesthetics and optimal user experience using Android-native components.

---

## 1. Monochrome Color Palette

Our design philosophy centers on a sophisticated monochrome color scheme defined in `res/values/colors.xml`.

### Color Specifications

| Token | Hex Value | Usage |
|-------|-----------|-------|
| `background_primary` | `#FAFAFA` | Main screen backgrounds |
| `background_secondary` | `#EBEBEB` | Secondary/alternate backgrounds |
| `surface_white` | `#FFFFFF` | Cards, buttons, elevated surfaces |
| `surface_elevated` | `#F5F5F5` | Elevated containers |
| `text_primary` | `#1A1A1A` | Primary text, titles |
| `text_secondary` | `#666666` | Subtitles, labels |
| `text_tertiary` | `#999999` | Hints, captions |
| `icon_background` | `#1A1A1A` | Icon containers (dark) |
| `icon_tint_white` | `#FFFFFF` | Icons on dark backgrounds |
| `divider` | `#E0E0E0` | Divider lines |
| `error` | `#D32F2F` | Error states only |

### XML Usage

```xml
android:background="@color/background_primary"
android:textColor="@color/text_primary"
app:cardBackgroundColor="@color/surface_white"
```

### Design Rationale
- **No Colorful Elements**: Eliminated orange, green, blue buttons for visual harmony
- **Professional Appearance**: Monochrome palette conveys trust for POS applications
- **Reduced Cognitive Load**: Minimal color variation helps staff focus on tasks

---

## 2. Typography System

Defined in `res/values/styles.xml` with consistent text appearances.

### Text Size Scale (dimens.xml)

| Token | Size | Usage |
|-------|------|-------|
| `text_heading` | 20sp | Large headings |
| `text_title` | 15sp | Card titles, button labels |
| `text_subtitle` | 11sp | Subtitles, secondary info |
| `text_body` | 13sp | Body text, content |
| `text_caption` | 12sp | Captions, timestamps |

### Text Appearance Styles

```xml
<!-- Primary Heading -->
<style name="TextAppearance.POS.TitleLarge">
    <item name="android:textColor">@color/text_primary</item>
    <item name="android:textSize">20sp</item>
    <item name="android:fontFamily">sans-serif-medium</item>
</style>

<!-- Card/Button Title -->
<style name="TextAppearance.POS.Title">
    <item name="android:textColor">@color/text_primary</item>
    <item name="android:textSize">@dimen/text_title</item>
    <item name="android:fontFamily">sans-serif-medium</item>
</style>

<!-- Subtitle/Secondary -->
<style name="TextAppearance.POS.Subtitle">
    <item name="android:textColor">@color/text_secondary</item>
    <item name="android:textSize">@dimen/text_subtitle</item>
    <item name="android:fontFamily">sans-serif</item>
</style>

<!-- Body Text -->
<style name="TextAppearance.POS.Body">
    <item name="android:textColor">@color/text_primary</item>
    <item name="android:textSize">@dimen/text_body</item>
    <item name="android:fontFamily">sans-serif</item>
</style>
```

### Overflow Prevention

Always include overflow handling for text:

```xml
<TextView
    android:maxLines="1"
    android:ellipsize="end"
    android:textAppearance="@style/TextAppearance.POS.Title"/>
```

---

## 3. Spacing System

Consistent spacing values in `res/values/dimens.xml`.

| Token | Value | Usage |
|-------|-------|-------|
| `spacing_xs` | 4dp | Tight spacing, line gaps |
| `spacing_sm` | 8dp | Small gaps, list padding |
| `spacing_md` | 12dp | Medium gaps, card padding |
| `spacing_lg` | 16dp | Standard padding, margins |
| `spacing_xl` | 24dp | Section spacing |
| `spacing_xxl` | 32dp | Major section dividers |

---

## 4. Component Specifications

### 4.1 Cards

```xml
<style name="Widget.POS.Card" parent="Widget.MaterialComponents.CardView">
    <item name="cardBackgroundColor">@color/surface_white</item>
    <item name="cardCornerRadius">@dimen/corner_radius_medium</item>
    <item name="cardElevation">@dimen/elevation_card</item>
</style>

<style name="Widget.POS.Card.Large">
    <item name="cardCornerRadius">@dimen/corner_radius_large</item>
    <item name="cardElevation">@dimen/elevation_button</item>
</style>
```

**Dimensions:**
- `corner_radius_small`: 8dp
- `corner_radius_medium`: 12dp
- `corner_radius_large`: 16dp
- `elevation_card`: 2dp
- `elevation_button`: 4dp

### 4.2 Buttons

**Navigation Button Template (Dashboard):**

```xml
<androidx.cardview.widget.CardView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:foreground="?attr/selectableItemBackground"
    style="@style/Widget.POS.Card.Large">

    <androidx.constraintlayout.widget.ConstraintLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:minHeight="@dimen/button_height_standard"
        android:padding="@dimen/spacing_lg">

        <!-- Icon Container (Dark background) -->
        <androidx.cardview.widget.CardView
            android:id="@+id/iconContainer"
            android:layout_width="@dimen/icon_size_large"
            android:layout_height="@dimen/icon_size_large"
            app:cardBackgroundColor="@color/icon_background"
            app:cardCornerRadius="@dimen/corner_radius_medium"
            app:cardElevation="0dp">

            <ImageView
                android:layout_width="@dimen/icon_size_small"
                android:layout_height="@dimen/icon_size_small"
                android:layout_gravity="center"
                android:tint="@color/icon_tint_white"
                android:src="@drawable/ic_icon"/>
        </androidx.cardview.widget.CardView>

        <!-- Title -->
        <TextView
            android:id="@+id/tvTitle"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_marginStart="@dimen/spacing_lg"
            android:ellipsize="end"
            android:maxLines="1"
            android:textAppearance="@style/TextAppearance.POS.Title"/>

        <!-- Subtitle -->
        <TextView
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_marginStart="@dimen/spacing_lg"
            android:ellipsize="end"
            android:maxLines="1"
            android:textAppearance="@style/TextAppearance.POS.Subtitle"/>
    </androidx.constraintlayout.widget.ConstraintLayout>
</androidx.cardview.widget.CardView>
```

**Primary Action Button:**

```xml
<com.google.android.material.button.MaterialButton
    android:layout_width="match_parent"
    android:layout_height="@dimen/button_height_standard"
    android:textAppearance="@style/TextAppearance.POS.Title"
    android:textColor="@color/icon_tint_white"
    app:backgroundTint="@color/icon_background"
    app:cornerRadius="@dimen/corner_radius_large"/>
```

**Secondary/Outlined Button:**

```xml
<com.google.android.material.button.MaterialButton
    android:layout_width="match_parent"
    android:layout_height="@dimen/button_height_standard"
    android:textColor="@color/text_primary"
    app:backgroundTint="@color/surface_white"
    app:strokeColor="@color/divider"
    app:strokeWidth="1dp"
    app:cornerRadius="@dimen/corner_radius_large"
    style="@style/Widget.MaterialComponents.Button.OutlinedButton"/>
```

**Button Dimensions:**
- `button_height_standard`: 72dp
- `button_height_max`: 80dp
- `touch_target_min`: 48dp

### 4.3 Toolbar

```xml
<com.google.android.material.appbar.MaterialToolbar
    android:layout_width="match_parent"
    android:layout_height="?attr/actionBarSize"
    android:background="@color/surface_white"
    android:elevation="0dp"
    app:titleTextColor="@color/text_primary"
    app:subtitleTextColor="@color/text_secondary"/>
```

### 4.4 Tabs / Chips

**TabLayout:**

```xml
<com.google.android.material.tabs.TabLayout
    android:layout_width="match_parent"
    android:layout_height="@dimen/touch_target_min"
    android:background="@color/surface_white"
    app:tabMode="scrollable"
    app:tabTextColor="@color/text_secondary"
    app:tabSelectedTextColor="@color/text_primary"
    app:tabIndicatorColor="@color/icon_background"/>
```

**Filter Chips:**

```xml
<com.google.android.material.chip.Chip
    style="@style/Widget.MaterialComponents.Chip.Choice"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:minHeight="40dp"
    android:textSize="13sp"
    app:chipBackgroundColor="@color/chip_choice_background"
    app:chipStrokeWidth="0dp"
    app:chipCornerRadius="@dimen/corner_radius_medium"
    app:checkedIconVisible="false"
    app:chipMinTouchTargetSize="40dp"/>
```

### 4.5 Text Input

```xml
<com.google.android.material.textfield.TextInputLayout
    style="@style/Widget.MaterialComponents.TextInputLayout.OutlinedBox"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:boxCornerRadiusBottomEnd="@dimen/corner_radius_medium"
    app:boxCornerRadiusBottomStart="@dimen/corner_radius_medium"
    app:boxCornerRadiusTopEnd="@dimen/corner_radius_medium"
    app:boxCornerRadiusTopStart="@dimen/corner_radius_medium">

    <com.google.android.material.textfield.TextInputEditText
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:maxLines="1"/>
</com.google.android.material.textfield.TextInputLayout>
```

### 4.6 Loading States

**Overlay Loading:**

```xml
<FrameLayout
    android:id="@+id/loadingOverlay"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/overlay_dark"
    android:clickable="true"
    android:focusable="true"
    android:visibility="gone">

    <androidx.cardview.widget.CardView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="center"
        app:cardBackgroundColor="@color/surface_white"
        app:cardCornerRadius="@dimen/corner_radius_large"
        app:cardElevation="@dimen/elevation_dialog">

        <LinearLayout
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:gravity="center"
            android:orientation="vertical"
            android:padding="@dimen/spacing_xl">

            <ProgressBar
                android:layout_width="@dimen/icon_size_large"
                android:layout_height="@dimen/icon_size_large"/>

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginTop="@dimen/spacing_md"
                android:textAppearance="@style/TextAppearance.POS.Body"/>
        </LinearLayout>
    </androidx.cardview.widget.CardView>
</FrameLayout>
```

### 4.7 Empty States

```xml
<LinearLayout
    android:id="@+id/emptyState"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:layout_gravity="center"
    android:orientation="vertical"
    android:gravity="center"
    android:padding="@dimen/spacing_xl"
    android:visibility="gone">

    <ImageView
        android:layout_width="48dp"
        android:layout_height="48dp"
        android:tint="@color/text_tertiary"
        android:contentDescription="@null"/>

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="@dimen/spacing_md"
        android:maxLines="1"
        android:ellipsize="end"
        android:textAppearance="@style/TextAppearance.POS.Title"
        android:textColor="@color/text_secondary"/>
</LinearLayout>
```

---

## 5. Screen Layout Templates

### 5.1 Standard Page Structure

```xml
<androidx.constraintlayout.widget.ConstraintLayout
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/background_primary">

    <com.google.android.material.appbar.MaterialToolbar
        android:id="@+id/toolbar"
        android:layout_width="match_parent"
        android:layout_height="?attr/actionBarSize"
        android:background="@color/surface_white"
        android:elevation="0dp"
        app:layout_constraintTop_toTopOf="parent"/>

    <androidx.core.widget.NestedScrollView
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:fillViewport="true"
        app:layout_constraintTop_toBottomOf="@id/toolbar"
        app:layout_constraintBottom_toBottomOf="parent">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:padding="@dimen/spacing_lg">

            <!-- Content Cards -->

        </LinearLayout>
    </androidx.core.widget.NestedScrollView>
</androidx.constraintlayout.widget.ConstraintLayout>
```

### 5.2 List Page Structure (with Tabs/Filters)

```xml
<androidx.coordinatorlayout.widget.CoordinatorLayout
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/background_primary">

    <com.google.android.material.appbar.AppBarLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:background="@color/surface_white"
        android:elevation="0dp">

        <com.google.android.material.appbar.MaterialToolbar
            android:id="@+id/toolbar"
            android:layout_width="match_parent"
            android:layout_height="?attr/actionBarSize"/>

        <com.google.android.material.tabs.TabLayout
            android:id="@+id/tabLayout"
            android:layout_width="match_parent"
            android:layout_height="@dimen/touch_target_min"/>
    </com.google.android.material.appbar.AppBarLayout>

    <androidx.swiperefreshlayout.widget.SwipeRefreshLayout
        android:id="@+id/swipeRefresh"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:layout_behavior="@string/appbar_scrolling_view_behavior">

        <FrameLayout
            android:layout_width="match_parent"
            android:layout_height="match_parent">

            <androidx.recyclerview.widget.RecyclerView
                android:id="@+id/recyclerView"
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:padding="@dimen/spacing_sm"
                android:clipToPadding="false"/>

            <!-- Empty State -->
            <!-- Progress Bar -->
        </FrameLayout>
    </androidx.swiperefreshlayout.widget.SwipeRefreshLayout>
</androidx.coordinatorlayout.widget.CoordinatorLayout>
```

### 5.3 Dashboard Structure (40% Header)

```xml
<androidx.constraintlayout.widget.ConstraintLayout
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/background_primary">

    <com.google.android.material.appbar.MaterialToolbar
        android:id="@+id/toolbar"
        android:layout_width="match_parent"
        android:layout_height="?attr/actionBarSize"
        app:layout_constraintTop_toTopOf="parent"/>

    <!-- Header Section (40% of screen) -->
    <LinearLayout
        android:id="@+id/layoutHeader"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:orientation="vertical"
        android:gravity="center"
        android:padding="@dimen/spacing_xl"
        app:layout_constraintHeight_percent="0.4"
        app:layout_constraintTop_toBottomOf="@id/toolbar">

        <!-- Logo Card -->
        <androidx.cardview.widget.CardView
            android:layout_width="72dp"
            android:layout_height="72dp"
            app:cardBackgroundColor="@color/surface_white"
            app:cardCornerRadius="@dimen/corner_radius_large"
            app:cardElevation="@dimen/elevation_card">

            <ImageView
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:padding="@dimen/spacing_lg"
                android:src="@mipmap/ic_launcher"/>
        </androidx.cardview.widget.CardView>

        <!-- Title & Subtitle -->
        <TextView
            android:layout_marginTop="@dimen/spacing_md"
            android:textAppearance="@style/TextAppearance.POS.TitleLarge"/>

        <TextView
            android:layout_marginTop="@dimen/spacing_xs"
            android:textAppearance="@style/TextAppearance.POS.Subtitle"/>
    </LinearLayout>

    <!-- Content Section (Scrollable) -->
    <androidx.core.widget.NestedScrollView
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:fillViewport="true"
        android:clipToPadding="false"
        android:paddingStart="@dimen/spacing_xxl"
        android:paddingEnd="@dimen/spacing_xxl"
        app:layout_constraintTop_toBottomOf="@id/layoutHeader"
        app:layout_constraintBottom_toBottomOf="parent">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:paddingBottom="@dimen/spacing_xl">

            <!-- Navigation Buttons -->

        </LinearLayout>
    </androidx.core.widget.NestedScrollView>
</androidx.constraintlayout.widget.ConstraintLayout>
```

---

## 6. User Journey Screens

### Journey 1: Authentication Flow

#### LoginActivity
**File:** `activity_login.xml`

**Structure:**
- Header (centered): Logo card (64dp) + App title + "Staff Login" subtitle
- Main card containing:
  - Staff dropdown (MaterialAutoCompleteTextView)
  - 4-digit PIN input (centered, 22sp)
- Loading overlay with progress indicator

**Key Design Elements:**
```xml
<!-- Staff Dropdown -->
<com.google.android.material.textfield.TextInputLayout
    style="@style/Widget.MaterialComponents.TextInputLayout.OutlinedBox.ExposedDropdownMenu"
    app:boxCornerRadiusBottomEnd="@dimen/corner_radius_medium"
    app:boxCornerRadiusBottomStart="@dimen/corner_radius_medium"
    app:boxCornerRadiusTopEnd="@dimen/corner_radius_medium"
    app:boxCornerRadiusTopStart="@dimen/corner_radius_medium">

<!-- PIN Input -->
<TextInputEditText
    android:gravity="center"
    android:inputType="numberPassword"
    android:maxLength="4"
    android:textSize="22sp"
    android:fontFamily="sans-serif-medium"/>
```

#### MainActivity (Dashboard)
**File:** `activity_main.xml`

**Structure:**
- Toolbar with menu options (Sync, Logout)
- 40% Header: Logo + Welcome message + "Select action"
- Scrollable button grid:
  - Tables button
  - Orders button
  - Menu button
  - Payments button (Manager/Admin only - `visibility="gone"`)

**Navigation Button Spacing:**
```xml
android:layout_marginStart="@dimen/spacing_lg"
android:layout_marginEnd="@dimen/spacing_lg"
android:layout_marginBottom="@dimen/spacing_md"
```

---

### Journey 2: Table-to-Order Flow

#### TablesActivity
**File:** `activity_tables.xml`

**Structure:**
- AppBarLayout with:
  - Toolbar
  - ChipGroup filters (All, Available, Occupied)
- SwipeRefreshLayout containing:
  - RecyclerView (GridLayoutManager, 3 columns)
  - Empty state
  - Progress indicator

**Chip Styling:**
```xml
android:minHeight="40dp"
android:textSize="13sp"
app:chipBackgroundColor="@color/chip_choice_background"
app:chipCornerRadius="@dimen/corner_radius_medium"
app:checkedIconVisible="false"
```

#### OrderActivity
**File:** `activity_order.xml`

**Structure:**
- AppBarLayout with:
  - Toolbar (Table name as subtitle)
  - TabLayout for categories (All, Appetizer, Main, Dessert, Drink)
- Horizontal split layout (50/50):
  - Left: Menu items RecyclerView + existing orders section
  - Right: Cart section with:
    - "Cart" title
    - Cart items RecyclerView
    - Empty cart state
    - Summary card (Subtotal, Tax, Total)
    - "Place Order" button

**Split Layout:**
```xml
<LinearLayout
    android:orientation="horizontal"
    android:weightSum="2">

    <LinearLayout
        android:layout_width="0dp"
        android:layout_weight="1">
        <!-- Menu Section -->
    </LinearLayout>

    <LinearLayout
        android:layout_width="0dp"
        android:layout_weight="1"
        android:background="@color/surface_variant">
        <!-- Cart Section -->
    </LinearLayout>
</LinearLayout>
```

---

### Journey 3: Order Management

#### OrdersActivity
**File:** `activity_orders.xml`

**Structure:**
- AppBarLayout with:
  - Toolbar
  - TabLayout for status filters (All, Pending, Preparing, Ready, Served)
- SwipeRefreshLayout containing:
  - RecyclerView (LinearLayoutManager)
  - Empty state
  - Progress indicator

#### OrderDetailsActivity
**File:** `activity_order_details.xml`

**Structure:**
- Toolbar with close navigation icon
- NestedScrollView containing:
  - Order Info card (ID, Table, Status chip, Created time)
  - Order Items card (RecyclerView + Divider + Total)
  - Special Notes card (visibility conditional)
- Bottom action bar:
  - "Mark Served" button (primary, dark)
  - "Generate Bill" button (outlined)

**Bottom Action Bar:**
```xml
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="horizontal"
    android:padding="@dimen/spacing_lg"
    android:background="@color/surface_white"
    android:elevation="@dimen/elevation_button"
    app:layout_constraintBottom_toBottomOf="parent">

    <MaterialButton
        android:layout_width="0dp"
        android:layout_weight="1"
        android:layout_height="@dimen/button_height_standard"
        android:textColor="@color/icon_tint_white"
        app:backgroundTint="@color/icon_background"/>

    <MaterialButton
        android:layout_width="0dp"
        android:layout_weight="1"
        android:layout_height="@dimen/button_height_standard"
        style="@style/Widget.MaterialComponents.Button.OutlinedButton"/>
</LinearLayout>
```

---

### Journey 4: Payment & Tips

#### PaymentActivity
**File:** `activity_payment.xml`

**Structure:**
- Toolbar with "Payment" title and close icon
- NestedScrollView containing:
  - Bill Summary card (Subtotal, VAT 18%, Divider, Total)
  - Payment Method card (RadioGroup: Cash, Card, Mobile)
  - Cash Payment card (Amount input, Change display)
  - Tip card:
    - Quick tip buttons (10%, 15%, 20%)
    - Custom tip input
  - "Confirm Payment" button (full width, primary)

**Tip Buttons:**
```xml
<LinearLayout
    android:orientation="horizontal"
    android:gravity="center">

    <MaterialButton
        android:layout_width="0dp"
        android:layout_weight="1"
        android:text="10%"
        android:textColor="@color/text_primary"
        app:backgroundTint="@color/surface_white"
        app:strokeColor="@color/divider"
        app:strokeWidth="1dp"
        style="@style/Widget.MaterialComponents.Button.OutlinedButton"/>
    <!-- Repeat for 15%, 20% -->
</LinearLayout>
```

---

### Journey 5: Tips Tracking

#### TipsActivity
**File:** `activity_tips.xml`

**Structure:**
- Toolbar with "My Tips" title and close icon
- NestedScrollView containing:
  - Today's Tips summary card (large, centered):
    - "Today's Tips" label
    - Total amount (32sp)
    - Order count
  - Period selector (TabLayout: Today, Week, Month)
  - Stats grid (2 columns):
    - Average tip card
    - Highest tip card
  - Recent Tips card:
    - RecyclerView for tip history
    - Empty state

**Large Summary Card:**
```xml
<androidx.cardview.widget.CardView
    style="@style/Widget.POS.Card.Large">

    <LinearLayout
        android:gravity="center"
        android:padding="@dimen/spacing_xl">

        <TextView
            android:textAppearance="@style/TextAppearance.POS.Subtitle"/>

        <TextView
            android:textAppearance="@style/TextAppearance.POS.Heading"
            android:textSize="32sp"/>

        <TextView
            android:textAppearance="@style/TextAppearance.POS.Caption"/>
    </LinearLayout>
</androidx.cardview.widget.CardView>
```

---

### Journey 6: Menu Management

#### MenuActivity
**File:** `activity_menu.xml`

**Structure:**
- AppBarLayout with:
  - Toolbar
  - SearchView
  - TabLayout for categories
- SwipeRefreshLayout containing:
  - RecyclerView (LinearLayoutManager)
  - Empty state
  - Progress indicator

**SearchView Styling:**
```xml
<androidx.appcompat.widget.SearchView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginStart="@dimen/spacing_lg"
    android:layout_marginEnd="@dimen/spacing_lg"
    android:background="@color/surface_variant"
    android:minHeight="@dimen/touch_target_min"
    app:queryHint="@string/search_menu"/>
```

---

### Journey 7: Logout Flow

**Implementation:** Menu option in MainActivity toolbar

```kotlin
// In MainActivity
override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
        R.id.action_logout -> {
            showLogoutConfirmation()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
}

private fun showLogoutConfirmation() {
    MaterialAlertDialogBuilder(this)
        .setTitle("Logout")
        .setMessage("Are you sure you want to logout?")
        .setPositiveButton("Logout") { _, _ ->
            // Clear session and navigate to LoginActivity
        }
        .setNegativeButton("Cancel", null)
        .show()
}
```

---

## 7. Accessibility Guidelines

### Touch Targets
- Minimum touch target: 48dp x 48dp
- Button height: 72dp minimum
- Chip minimum touch target: 40dp

### Contrast Ratios
- Primary text on background: 12.6:1 (#1A1A1A on #FAFAFA)
- Secondary text on background: 5.7:1 (#666666 on #FAFAFA)
- All ratios exceed WCAG AA (4.5:1)

### Content Descriptions
```xml
<!-- Decorative icons -->
android:contentDescription="@null"

<!-- Functional icons -->
android:contentDescription="@string/navigate_back"
```

---

## 8. Performance Guidelines

### RecyclerView Optimization
- Use `setHasFixedSize(true)` when list size is constant
- Implement `DiffUtil` for list updates
- Use `RecycledViewPool` for shared item types

### Layout Optimization
- Prefer `ConstraintLayout` over nested `LinearLayout`
- Use `merge` tag for included layouts
- Avoid `wrap_content` on RecyclerView items

### Memory Management
- Use `ViewBinding` instead of `findViewById`
- Clear references in `onDestroyView`
- Use `lifecycleScope` for coroutines

---

## 9. Version History

- **v1.0.0** (2024-12): Initial Android design guidelines
  - Implemented monochrome color palette
  - Created component library
  - Documented all user journeys

---

## 10. Safe Area Guidelines

### Overview

All screens implement safe area handling to ensure content is not obscured by system UI elements (status bar, navigation bar, display cutouts).

### Implementation

#### Root Layout
Add `fitsSystemWindows="true"` to all root layouts:

```xml
<androidx.constraintlayout.widget.ConstraintLayout
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:fitsSystemWindows="true">
```

#### CoordinatorLayout with AppBarLayout
For screens using CoordinatorLayout, add `fitsSystemWindows` to both:

```xml
<androidx.coordinatorlayout.widget.CoordinatorLayout
    android:fitsSystemWindows="true">

    <com.google.android.material.appbar.AppBarLayout
        android:fitsSystemWindows="true">
        <!-- Toolbar content -->
    </com.google.android.material.appbar.AppBarLayout>
</androidx.coordinatorlayout.widget.CoordinatorLayout>
```

### Theme Configuration

#### Base Theme (values/themes.xml)
```xml
<style name="Theme.POS" parent="Theme.MaterialComponents.Light.NoActionBar">
    <!-- Safe Area / Edge-to-Edge Support -->
    <item name="android:statusBarColor">@color/surface_white</item>
    <item name="android:navigationBarColor">@color/background_primary</item>
    <item name="android:windowLightStatusBar">true</item>
</style>
```

#### API 27+ (values-v27/themes.xml)
```xml
<style name="Theme.POS" parent="Theme.MaterialComponents.Light.NoActionBar">
    <!-- Light Navigation Bar Support -->
    <item name="android:windowLightNavigationBar">true</item>
</style>
```

#### API 29+ (values-v29/themes.xml)
```xml
<style name="Theme.POS" parent="Theme.MaterialComponents.Light.NoActionBar">
    <!-- Gesture Navigation Support -->
    <item name="android:enforceNavigationBarContrast">false</item>
    <item name="android:enforceStatusBarContrast">false</item>
</style>
```

### Applied Screens

| Screen | Root Layout | fitsSystemWindows |
|--------|-------------|-------------------|
| LoginActivity | ConstraintLayout | Yes |
| MainActivity | ConstraintLayout | Yes |
| TablesActivity | CoordinatorLayout + AppBarLayout | Yes (both) |
| OrderActivity | CoordinatorLayout + AppBarLayout | Yes (both) |
| OrdersActivity | CoordinatorLayout + AppBarLayout | Yes (both) |
| OrderDetailsActivity | ConstraintLayout | Yes |
| PaymentActivity | ConstraintLayout | Yes |
| TipsActivity | ConstraintLayout | Yes |
| MenuActivity | CoordinatorLayout + AppBarLayout | Yes (both) |

### Programmatic Edge-to-Edge (Optional)

For full edge-to-edge support with WindowInsets:

```kotlin
// In Activity onCreate()
WindowCompat.setDecorFitsSystemWindows(window, false)

// Handle insets manually
ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, windowInsets ->
    val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
    view.updatePadding(
        left = insets.left,
        top = insets.top,
        right = insets.right,
        bottom = insets.bottom
    )
    WindowInsetsCompat.CONSUMED
}
```

---

## 11. Quick Reference

### Color Tokens
| Usage | Color Token |
|-------|-------------|
| Screen background | `background_primary` |
| Card background | `surface_white` |
| Primary text | `text_primary` |
| Secondary text | `text_secondary` |
| Icon container | `icon_background` |
| Icon on dark | `icon_tint_white` |
| Primary button bg | `icon_background` |
| Primary button text | `icon_tint_white` |

### Spacing Tokens
| Usage | Token |
|-------|-------|
| Card padding | `spacing_lg` (16dp) |
| Button margin | `spacing_md` (12dp) |
| Section gap | `spacing_xl` (24dp) |
| List item gap | `spacing_sm` (8dp) |

### Corner Radius
| Component | Radius |
|-----------|--------|
| Cards | `corner_radius_medium` (12dp) |
| Large buttons | `corner_radius_large` (16dp) |
| Chips | `corner_radius_medium` (12dp) |
| Input fields | `corner_radius_medium` (12dp) |
