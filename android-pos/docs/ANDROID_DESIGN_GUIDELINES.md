# SeaCliff POS - Android Design Guidelines

> **Note:** This document is the **single source of truth** for SeaCliff POS UI components. All screens, layouts, and features MUST follow these guidelines.

## Quick Reference

| Token | Value | Usage |
|-------|-------|-------|
| `background` | `#FAFAFA` | Scaffold, page backgrounds |
| `surface` | `#FFFFFF` | Cards, buttons, sheets |
| `primaryText` | `#1A1A1A` | Headings, titles, icons |
| `secondaryText` | `#666666` | Subtitles, descriptions |
| `tertiaryText` | `#999999` | Hints, tips, borders |
| `borderRadius.lg` | `16dp` | Cards, containers |
| `borderRadius.md` | `12dp` | Buttons, chips, small cards |
| `borderRadius.sm` | `8dp` | List tiles, inputs |
| `spacing.xs` | `4dp` | Tight spacing |
| `spacing.sm` | `8dp` | Icon-to-text gaps |
| `spacing.md` | `12dp` | Between cards/items |
| `spacing.lg` | `16dp` | Container padding |
| `spacing.xl` | `24dp` | Section margins |
| `spacing.xxl` | `32dp` | Large section spacing |
| `touchTarget` | `48×48dp` | Minimum for all tappable elements |

---

## Minimalist Design Principles

This document outlines the core design principles and guidelines for the SeaCliff POS Android application, focusing on minimalist aesthetics and optimal user experience for tablet and mobile devices.

---

## 🎨 Core Design Principles

### 1. Monochrome Color Palette

Our design philosophy centers on a sophisticated monochrome color scheme that reduces visual noise and creates a professional, focused user experience optimized for restaurant environments.

#### Color Specifications:

```xml
<!-- res/values/colors.xml -->
<resources>
    <!-- Primary Background -->
    <color name="background_primary">#FAFAFA</color>      <!-- Light gray base -->

    <!-- Surface Colors -->
    <color name="surface_white">#FFFFFF</color>           <!-- Pure white for cards/buttons -->
    <color name="surface_elevated">#F5F5F5</color>        <!-- Slightly elevated surfaces -->
    <color name="surface_variant">#F5F5F5</color>         <!-- Surface variant -->

    <!-- Text Colors -->
    <color name="text_primary">#1A1A1A</color>            <!-- Dark charcoal for maximum readability -->
    <color name="text_secondary">#666666</color>          <!-- Medium gray for supporting info -->
    <color name="text_tertiary">#999999</color>           <!-- Light gray for subtle elements -->
    <color name="text_disabled">#BDBDBD</color>           <!-- Disabled state -->

    <!-- Icon Background -->
    <color name="icon_background">#1A1A1A</color>         <!-- Consistent dark for all icons -->
    <color name="icon_tint_white">#FFFFFF</color>         <!-- White icons on dark background -->

    <!-- Dividers & Borders -->
    <color name="divider">#E0E0E0</color>
    <color name="border_light">#EEEEEE</color>
    <color name="border">#999999</color>                  <!-- Use with 0.3 opacity for subtle borders -->

    <!-- Status Colors (Monochrome variants) -->
    <color name="status_pending">#757575</color>          <!-- Medium gray -->
    <color name="status_ready">#424242</color>            <!-- Dark gray -->
    <color name="status_served">#9E9E9E</color>           <!-- Light gray -->

    <!-- Overlay & Shadow -->
    <color name="overlay_dark">#80000000</color>          <!-- 50% black overlay -->
    <color name="shadow_light">#10000000</color>          <!-- Subtle shadows (0.06–0.1 opacity) -->
    <color name="shadow_card">#0F000000</color>           <!-- Card shadow ~6% black -->
    <color name="overlay_chip">#141A1A1A</color>         <!-- Chip bg: primary at 8% opacity -->
</resources>
```

#### Design Rationale:
- **No Colorful Elements**: No orange, green, or blue buttons — monochrome only
- **Professional Appearance**: Conveys trust for financial/restaurant applications
- **Reduced Cognitive Load**: Users focus on content, not decoration

---

### 2. Typography

#### 2.1 Font Scale

| Style | Size | Weight | Color | Usage |
|-------|------|--------|-------|-------|
| `titleLarge` | 20sp | 700 (bold) | `#1A1A1A` | Page titles |
| `titleMedium` | 16sp | 600 (semi) | `#1A1A1A` | Section headers |
| `titleSmall` | 15sp | 600 | `#1A1A1A` | Card titles, button text |
| `bodyLarge` | 14sp | 400 | `#1A1A1A` | Body text |
| `bodyMedium` | 13sp | 500 | `#1A1A1A` | List items |
| `bodySmall` | 12sp | 400 | `#666666` | Descriptions, tips |
| `labelLarge` | 12sp | 500 | `#666666` | Labels, badges |
| `labelSmall` | 11sp | 400 | `#666666` | Subtitles, timestamps |
| `caption` | 10sp | 400 | `#999999` | Tertiary info |

#### 2.2 Text Overflow Rules

**MANDATORY for all dynamic text:**

```xml
<TextView
    android:maxLines="1"
    android:ellipsize="end"
    android:text="@string/dynamic_content"/>
<!-- Or maxLines="2" for descriptions -->
```

---

### 3. Spacing & Layout

#### 3.1 Spacing Scale

```xml
<!-- res/values/dimens.xml -->
<dimen name="spacing_xs">4dp</dimen>
<dimen name="spacing_sm">8dp</dimen>
<dimen name="spacing_md">12dp</dimen>
<dimen name="spacing_lg">16dp</dimen>
<dimen name="spacing_xl">24dp</dimen>
<dimen name="spacing_xxl">32dp</dimen>
```

#### 3.2 Common Padding Patterns

| Context | Padding |
|---------|---------|
| Screen horizontal | `24dp` |
| Container internal | `16dp` |
| Card internal | `16dp` |
| Section header | `20dp` start/end, `24dp` top, `16dp` bottom |
| List item | `16dp` horizontal, `12dp` vertical |
| Chip/Badge | `8dp` horizontal, `4dp` vertical |

#### 3.3 Gap Between Elements

| Elements | Gap |
|----------|-----|
| Cards in grid | `12dp` |
| List items | `8dp` |
| Icon to text | `8dp` |
| Title to subtitle | `4dp` |
| Sections | `24dp` |

---

### 4. Overflow Prevention Strategies

Preventing layout overflow is critical for maintaining a polished user experience across tablets and phones of varying sizes.

#### Implementation Guidelines:

**Fixed Header Dimensions:**
```xml
<!-- Use percentage-based heights in ConstraintLayout -->
<androidx.constraintlayout.widget.ConstraintLayout>
    <!-- Header occupies 40% of screen height -->
    <LinearLayout
        android:id="@+id/layoutHeader"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        app:layout_constraintHeight_percent="0.4"
        app:layout_constraintTop_toTopOf="parent">
        <!-- Header content -->
    </LinearLayout>
</androidx.constraintlayout.widget.ConstraintLayout>
```

**Text Overflow Handling:**
```xml
<!-- Always use ellipsize for dynamic text -->
<TextView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:ellipsize="end"
    android:maxLines="1"
    android:textSize="15sp"
    tools:text="Very Long Table Name That Might Overflow"/>

<!-- Multi-line text with constraints -->
<TextView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:ellipsize="end"
    android:maxLines="2"
    android:textSize="12sp"
    tools:text="Supporting information that can span two lines maximum"/>
```

**Optimized Typography:**
```xml
<!-- res/values/dimens.xml -->
<resources>
    <!-- Text Sizes -->
    <dimen name="text_title">15sp</dimen>           <!-- Maximum for titles -->
    <dimen name="text_subtitle">11sp</dimen>        <!-- Secondary information -->
    <dimen name="text_body">13sp</dimen>            <!-- Body text readability -->
    <dimen name="text_caption">12sp</dimen>         <!-- Captions and labels -->

    <!-- Spacing -->
    <dimen name="spacing_xs">4dp</dimen>
    <dimen name="spacing_sm">8dp</dimen>
    <dimen name="spacing_md">12dp</dimen>
    <dimen name="spacing_lg">16dp</dimen>
    <dimen name="spacing_xl">24dp</dimen>
    <dimen name="spacing_xxl">32dp</dimen>

    <!-- Element Sizes -->
    <dimen name="touch_target_min">48dp</dimen>
    <dimen name="button_height_standard">72dp</dimen>
    <dimen name="button_height_max">80dp</dimen>
</resources>
```

**ScrollView Best Practices:**
```xml
<!-- Use NestedScrollView for complex layouts -->
<androidx.core.widget.NestedScrollView
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:fillViewport="true">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical">
        <!-- Content that might exceed screen height -->
    </LinearLayout>
</androidx.core.widget.NestedScrollView>
```

---

### 5. Clean Layout Structure

A well-organized layout enhances usability and maintains visual clarity on tablets.

#### Layout Components:

**Simplified Logo/Branding:**
```xml
<!-- Logo container with consistent sizing -->
<androidx.cardview.widget.CardView
    android:layout_width="80dp"
    android:layout_height="80dp"
    android:layout_gravity="center"
    app:cardBackgroundColor="@color/surface_white"
    app:cardCornerRadius="16dp"
    app:cardElevation="2dp">

    <ImageView
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:padding="16dp"
        android:src="@drawable/ic_seacliff_logo"
        android:contentDescription="@string/app_name"/>
</androidx.cardview.widget.CardView>
```

**Typography Hierarchy:**
```xml
<!-- res/values/styles.xml -->
<resources>
    <!-- Text Appearance Styles -->
    <style name="TextAppearance.SeaCliff.Heading" parent="TextAppearance.MaterialComponents.Headline6">
        <item name="android:textColor">@color/text_primary</item>
        <item name="android:textSize">20sp</item>
        <item name="android:fontFamily">sans-serif-medium</item>
    </style>

    <style name="TextAppearance.SeaCliff.Title" parent="TextAppearance.MaterialComponents.Subtitle1">
        <item name="android:textColor">@color/text_primary</item>
        <item name="android:textSize">15sp</item>
        <item name="android:fontFamily">sans-serif-medium</item>
    </style>

    <style name="TextAppearance.SeaCliff.Subtitle" parent="TextAppearance.MaterialComponents.Subtitle2">
        <item name="android:textColor">@color/text_secondary</item>
        <item name="android:textSize">11sp</item>
        <item name="android:fontFamily">sans-serif</item>
    </style>

    <style name="TextAppearance.SeaCliff.Body" parent="TextAppearance.MaterialComponents.Body1">
        <item name="android:textColor">@color/text_primary</item>
        <item name="android:textSize">13sp</item>
        <item name="android:fontFamily">sans-serif</item>
    </style>

    <style name="TextAppearance.SeaCliff.Caption" parent="TextAppearance.MaterialComponents.Caption">
        <item name="android:textColor">@color/text_secondary</item>
        <item name="android:textSize">12sp</item>
        <item name="android:fontFamily">sans-serif</item>
    </style>
</resources>
```

**Consistent Spacing:**
```xml
<!-- Standard container with consistent padding -->
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:padding="@dimen/spacing_lg">

    <!-- Button with standard gap -->
    <Button />

    <Space
        android:layout_width="match_parent"
        android:layout_height="@dimen/spacing_md"/>

    <Button />
</LinearLayout>
```

**Material Design Elements:**
```xml
<!-- res/values/styles.xml -->
<resources>
    <!-- Card Elevation -->
    <dimen name="elevation_card">2dp</dimen>
    <dimen name="elevation_button">4dp</dimen>
    <dimen name="elevation_dialog">8dp</dimen>

    <!-- Corner Radius -->
    <dimen name="corner_radius_large">16dp</dimen>
    <dimen name="corner_radius_medium">12dp</dimen>
    <dimen name="corner_radius_small">8dp</dimen>
</resources>
```

---

### 6. Professional Button Design

Buttons are primary interaction points and must be both functional and aesthetically consistent.

#### Button Specifications:

**Standard Button Style:**
```xml
<!-- res/values/styles.xml -->
<style name="Widget.SeaCliff.Button" parent="Widget.MaterialComponents.Button.UnelevatedButton">
    <item name="android:background">@drawable/bg_button_white</item>
    <item name="android:textColor">@color/text_primary</item>
    <item name="android:textSize">15sp</item>
    <item name="android:fontFamily">sans-serif-medium</item>
    <item name="android:paddingStart">16dp</item>
    <item name="android:paddingEnd">16dp</item>
    <item name="android:minHeight">@dimen/button_height_standard</item>
    <item name="android:stateListAnimator">@animator/button_elevation</item>
</style>

<!-- Button background with shadow -->
<!-- res/drawable/bg_button_white.xml -->
<layer-list xmlns:android="http://schemas.android.com/apk/res/android">
    <!-- Shadow layer -->
    <item>
        <shape android:shape="rectangle">
            <solid android:color="@color/shadow_light"/>
            <corners android:radius="16dp"/>
        </shape>
    </item>
    <!-- White background -->
    <item android:bottom="2dp">
        <shape android:shape="rectangle">
            <solid android:color="@color/surface_white"/>
            <corners android:radius="16dp"/>
        </shape>
    </item>
</layer-list>

<!-- Button ripple effect -->
<!-- res/drawable/button_ripple.xml -->
<ripple xmlns:android="http://schemas.android.com/apk/res/android"
    android:color="@color/text_tertiary">
    <item android:drawable="@drawable/bg_button_white"/>
</ripple>
```

**Button with Icon Layout:**
```xml
<!-- Custom button layout -->
<!-- res/layout/item_menu_button.xml -->
<androidx.cardview.widget.CardView
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginBottom="@dimen/spacing_md"
    android:minHeight="@dimen/button_height_standard"
    android:foreground="?attr/selectableItemBackground"
    app:cardBackgroundColor="@color/surface_white"
    app:cardCornerRadius="@dimen/corner_radius_large"
    app:cardElevation="@dimen/elevation_button">

    <androidx.constraintlayout.widget.ConstraintLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:padding="@dimen/spacing_lg">

        <!-- Dark icon container -->
        <androidx.cardview.widget.CardView
            android:id="@+id/iconContainer"
            android:layout_width="48dp"
            android:layout_height="48dp"
            app:cardBackgroundColor="@color/icon_background"
            app:cardCornerRadius="12dp"
            app:cardElevation="0dp"
            app:layout_constraintStart_toStartOf="parent"
            app:layout_constraintTop_toTopOf="parent"
            app:layout_constraintBottom_toBottomOf="parent">

            <ImageView
                android:id="@+id/icon"
                android:layout_width="24dp"
                android:layout_height="24dp"
                android:layout_gravity="center"
                android:tint="@color/icon_tint_white"
                android:src="@drawable/ic_restaurant_rounded"
                android:contentDescription="@null"/>
        </androidx.cardview.widget.CardView>

        <!-- Text content -->
        <TextView
            android:id="@+id/tvTitle"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_marginStart="@dimen/spacing_lg"
            android:ellipsize="end"
            android:maxLines="1"
            android:textAppearance="@style/TextAppearance.SeaCliff.Title"
            app:layout_constraintStart_toEndOf="@id/iconContainer"
            app:layout_constraintEnd_toEndOf="parent"
            app:layout_constraintTop_toTopOf="@id/iconContainer"
            android:text="@string/tables"/>

        <TextView
            android:id="@+id/tvSubtitle"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_marginStart="@dimen/spacing_lg"
            android:ellipsize="end"
            android:maxLines="1"
            android:textAppearance="@style/TextAppearance.SeaCliff.Subtitle"
            app:layout_constraintStart_toEndOf="@id/iconContainer"
            app:layout_constraintEnd_toEndOf="parent"
            app:layout_constraintTop_toBottomOf="@id/tvTitle"
            app:layout_constraintBottom_toBottomOf="@id/iconContainer"
            android:text="@string/manage_tables"/>
    </androidx.constraintlayout.widget.ConstraintLayout>
</androidx.cardview.widget.CardView>
```

**Icon Guidelines:**
```kotlin
// Using Material Icons (rounded variant)
// In build.gradle
dependencies {
    implementation 'androidx.compose.material:material-icons-extended:1.5.4'
    // Or use vector drawables from Material Design Icons
}

// Icon naming convention for rounded variants
// ic_restaurant_rounded.xml
// ic_receipt_rounded.xml
// ic_payment_rounded.xml
```

**Touch Target Optimization:**
```xml
<!-- Ensure minimum touch target (48dp) -->
<ImageButton
    android:layout_width="48dp"
    android:layout_height="48dp"
    android:padding="12dp"
    android:background="?attr/selectableItemBackgroundBorderless"
    android:src="@drawable/ic_more_vert"
    android:contentDescription="@string/more_options"/>
```

**Button Specifications:**

| Type | Min Height | Width | Border Radius |
|------|------------|-------|---------------|
| Primary Action | 72–80dp | Full width | 16dp |
| Secondary Button | 48dp | Fit content | 12dp |
| Icon Button | 48×48dp | 48dp | Circle |
| Chip/Badge | 28dp | Fit content | 12dp |

---

## Component Patterns

Use these patterns for consistent UI across the app.

### 4.1 Action Card (Grid Selection)

Use for main menu buttons (e.g. Tables, Orders, Menu). Min height 72dp, white card, 16dp radius, elevation 2. Icon: 48×48dp dark circle, 24dp icon. Title 15sp w600, subtitle 11sp; 12dp gap between icon and text, 4dp between title and subtitle.

*(See "Menu Button Template" in Layout Templates section.)*

**Grid layout:** 2-column grid with 12dp gap — use `RecyclerView` with `GridLayoutManager(2)` or two `Expanded`-equivalent columns.

### 4.2 Info Card (Tips, Notices)

Informational section with icon + content. White background, 16dp radius, border `#999999` at 0.3 opacity, shadow blur 8dp offset (0,2) at 6% opacity.

```xml
<androidx.cardview.widget.CardView
    android:layout_marginStart="16dp" android:layout_marginEnd="16dp" android:layout_marginTop="24dp"
    app:cardBackgroundColor="@color/surface_white"
    app:cardCornerRadius="16dp"
    app:cardElevation="2dp">
    <LinearLayout android:orientation="vertical" android:padding="16dp">
        <LinearLayout android:orientation="horizontal">
            <!-- Icon container 48×48dp rounded square (#1A1A1A) -->
            <FrameLayout android:layout_width="48dp" android:layout_height="48dp" android:background="@drawable/bg_icon_rounded_square">
                <ImageView android:layout_width="24dp" android:layout_height="24dp" android:src="@drawable/ic_tips" android:tint="@color/icon_tint_white"/>
            </FrameLayout>
            <TextView android:layout_marginStart="12dp" android:textAppearance="@style/TextAppearance.SeaCliff.TitleMedium" android:text="Section Title"/>
        </LinearLayout>
        <View android:layout_height="12dp"/>
        <!-- Content items -->
    </LinearLayout>
</androidx.cardview.widget.CardView>
```

### 4.3 Tip Item (List Row with Icon)

Row: icon 16dp, 8dp gap, expanded text 12sp #666666, maxLines 2, ellipsize end.

```xml
<LinearLayout android:orientation="horizontal" android:paddingTop="8dp">
    <ImageView android:layout_width="16dp" android:layout_height="16dp" android:src="@drawable/ic_info" android:tint="@color/text_tertiary"/>
    <TextView android:layout_width="0dp" android:layout_weight="1" android:layout_marginStart="8dp"
        android:textSize="12sp" android:textColor="@color/text_secondary" android:maxLines="2" android:ellipsize="end"/>
</LinearLayout>
```

### 4.4 Horizontal Scroll Card

Card width 160dp, marginEnd 12dp. Background #FAFAFA, radius 12dp, padding 12dp. Use inside `RecyclerView` with `LinearLayoutManager(HORIZONTAL)` and horizontal padding 16dp.

### 4.5 Navigation Card (Clickable with Chevron)

White card, 12dp radius, border + shadow. Row: leading 48×48dp container with bg primary 8% opacity, 12dp radius; 12dp gap; title 15sp w600 + subtitle 11sp; trailing `chevron_right` #999999. Full row has `?attr/selectableItemBackground`.

```xml
<androidx.cardview.widget.CardView
    android:layout_marginHorizontal="16dp" android:layout_marginTop="16dp"
    app:cardBackgroundColor="@color/surface_white" app:cardCornerRadius="12dp">
    <LinearLayout android:orientation="horizontal" android:padding="16dp" android:background="?attr/selectableItemBackground">
        <FrameLayout android:layout_width="48dp" android:layout_height="48dp" android:background="@drawable/bg_icon_light_rounded"/>
        <LinearLayout android:layout_width="0dp" android:layout_weight="1" android:layout_marginStart="12dp" android:orientation="vertical">
            <TextView android:textAppearance="@style/TextAppearance.SeaCliff.Title" android:text="Title"/>
            <TextView android:layout_marginTop="2dp" android:textAppearance="@style/TextAppearance.SeaCliff.Subtitle" android:text="Subtitle"/>
        </LinearLayout>
        <ImageView android:layout_width="24dp" android:layout_height="24dp" android:src="@drawable/ic_chevron_right" android:tint="@color/text_tertiary"/>
    </LinearLayout>
</androidx.cardview.widget.CardView>
```

### 4.6 Badge/Chip

Padding 8dp horizontal, 4dp vertical. Background primary at 8% opacity (`@color/overlay_chip`), radius 12dp. Content: optional icon 14dp, 4dp gap, label 12sp w500 #666666.

```xml
<com.google.android.material.chip.Chip
    android:paddingStart="8dp" android:paddingEnd="8dp"
    app:chipBackgroundColor="@color/overlay_chip"
    app:chipCornerRadius="12dp"
    app:chipMinHeight="28dp"
    android:textAppearance="@style/TextAppearance.SeaCliff.LabelLarge"/>
```

### 4.7 List Tile Card

Card with bottom margin 8dp. Leading: 48×48dp container, radius 8dp, bg #666666 10%, icon #666666. Title (maxLines 1, ellipsize), subtitle (label • timestamp). Trailing: IconButton delete (48dp touch target).

---

## AppBar & Navigation

### Standard AppBar

- **Title:** Centered
- **Elevation:** 0
- **Background:** White (`#FFFFFF`)
- **Foreground:** Black87 / `@color/text_primary`
- **Actions:** Optional; icon size 20dp

```xml
<com.google.android.material.appbar.MaterialToolbar
    android:background="@color/surface_white"
    app:elevation="0dp"
    app:titleCentered="true"
    app:titleTextColor="@color/text_primary"/>
```

---

### 7. Performance & Accessibility

Design decisions must prioritize both performance and accessibility for all users.

#### Performance Optimizations:

**Simplified Animations:**
```xml
<!-- res/animator/fade_in.xml -->
<alpha xmlns:android="http://schemas.android.com/apk/res/android"
    android:duration="600"
    android:fromAlpha="0.0"
    android:toAlpha="1.0"
    android:interpolator="@android:interpolator/decelerate_cubic"/>

<!-- res/animator/button_elevation.xml -->
<selector xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:state_pressed="true">
        <objectAnimator
            android:propertyName="translationZ"
            android:duration="100"
            android:valueTo="6dp"
            android:valueType="floatType"/>
    </item>
    <item>
        <objectAnimator
            android:propertyName="translationZ"
            android:duration="100"
            android:valueTo="2dp"
            android:valueType="floatType"/>
    </item>
</selector>
```

```kotlin
// Kotlin code for animations
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Simple fade-in animation
        binding.root.apply {
            alpha = 0f
            animate()
                .alpha(1f)
                .setDuration(600)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }
    }
}
```

**Responsive Design:**
```xml
<!-- Use dimension qualifiers for different screen sizes -->
<!-- res/values/dimens.xml (phones) -->
<dimen name="grid_columns">2</dimen>
<dimen name="card_width">match_parent</dimen>

<!-- res/values-sw600dp/dimens.xml (tablets) -->
<dimen name="grid_columns">3</dimen>
<dimen name="card_width">300dp</dimen>

<!-- res/values-sw720dp/dimens.xml (large tablets) -->
<dimen name="grid_columns">4</dimen>
<dimen name="card_width">350dp</dimen>
```

```kotlin
// Programmatic responsive design
val screenWidth = resources.displayMetrics.widthPixels
val isTablet = resources.getBoolean(R.bool.isTablet)

val layoutManager = GridLayoutManager(
    this,
    if (isTablet) 3 else 2
)
```

**Memory Management:**
```kotlin
// Proper lifecycle management
class TablesActivity : AppCompatActivity() {

    private var _binding: ActivityTablesBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityTablesBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null  // Prevent memory leaks
    }
}

// Use ViewBinding or DataBinding (never findViewById in loops)
// Use DiffUtil for RecyclerView updates
class TableAdapter : ListAdapter<Table, TableViewHolder>(TableDiffCallback()) {

    class TableDiffCallback : DiffUtil.ItemCallback<Table>() {
        override fun areItemsTheSame(oldItem: Table, newItem: Table) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Table, newItem: Table) =
            oldItem == newItem
    }
}
```

#### Accessibility Features:

**Content Descriptions:**
```xml
<!-- Always provide content descriptions for images -->
<ImageView
    android:layout_width="24dp"
    android:layout_height="24dp"
    android:src="@drawable/ic_table"
    android:contentDescription="@string/table_icon"/>

<!-- Use null for decorative images -->
<ImageView
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:src="@drawable/decorative_line"
    android:contentDescription="@null"/>
```

**Semantic Structure:**
```xml
<!-- Use proper heading hierarchy -->
<TextView
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="@string/dashboard"
    android:textAppearance="@style/TextAppearance.SeaCliff.Heading"
    android:accessibilityHeading="true"
    android:importantForAccessibility="yes"/>

<!-- Group related elements -->
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:importantForAccessibility="yes"
    android:contentDescription="@string/order_summary">
    <!-- Order items -->
</LinearLayout>
```

**High Contrast Support:**
```xml
<!-- res/values/colors.xml -->
<!-- Ensure 4.5:1 contrast ratio minimum -->
<color name="text_primary">#1A1A1A</color>      <!-- 13.8:1 on white -->
<color name="text_secondary">#666666</color>    <!-- 5.7:1 on white -->

<!-- Test with Android Accessibility Scanner -->
```

**Touch Target Sizes:**
```xml
<!-- Minimum 48dp touch targets -->
<resources>
    <dimen name="touch_target_min">48dp</dimen>
</resources>

<!-- Apply to all interactive elements -->
<Button
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:minWidth="@dimen/touch_target_min"
    android:minHeight="@dimen/touch_target_min"/>
```

**Haptic Feedback:**
```kotlin
// Provide tactile feedback for important actions
fun onOrderPlaced() {
    // Haptic feedback
    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)

    // Visual feedback
    Snackbar.make(binding.root, "Order placed", Snackbar.LENGTH_SHORT).show()
}
```

---

## Shadows & Elevation

### Shadow Presets

| Component | Elevation | Shadow |
|-----------|-----------|--------|
| Action Cards, Buttons | 2dp | color black 10%, blur 4, offset (0, 2) |
| Info Cards, Modals | 4dp | color black 6%, blur 8, offset (0, 2) |
| Bottom Sheets, Dialogs | 8dp | color black 12%, blur 16, offset (0, 4) |

```xml
<!-- res/values/dimens.xml -->
<dimen name="elevation_card">2dp</dimen>
<dimen name="elevation_elevated">4dp</dimen>
<dimen name="elevation_modal">8dp</dimen>
```

---

## Icon System

### Icon Sizes

| Context | Size |
|---------|------|
| In 48×48dp container | 24dp |
| Inline with text | 16dp |
| Small badges | 14dp |
| List trailing / AppBar actions | 20dp |

### Icon Container Styles

**Dark circle (primary actions):** 48×48dp, bg `#1A1A1A`, shape circle, icon 24dp white.

**Dark rounded square (section headers):** 48×48dp, bg `#1A1A1A`, radius 12dp, icon 24dp white.

**Light rounded square (list items, navigation):** 48×48dp, bg primary at 8% opacity, radius 12dp, icon 24dp `#1A1A1A`.

Use **rounded** icon variants where available (e.g. `ic_restaurant_rounded`, `ic_receipt_rounded`).

---

## Dialogs & Sheets

### Alert Dialog

- **Shape:** `RoundedRectangleBorder(borderRadius: 16dp)`
- **Destructive action:** Red `TextButton` for "Yes" / "Remove"; or `FilledButton` with red background for "Yes, log out"

```kotlin
AlertDialog.Builder(this)
    .setTitle("Title")
    .setMessage("Description text")
    .setPositiveButton("Confirm") { _, _ -> }
    .setNegativeButton("Cancel") { _, _ -> }
    .create()
    .apply { window?.setBackgroundDrawableResource(R.drawable.bg_dialog_rounded) }
```

---

## Empty States

Center content: icon 64dp grey (e.g. `Colors.grey.shade400`), 16dp gap, title 18sp grey.shade600, 8dp gap, description grey.shade500.

```xml
<LinearLayout android:gravity="center" android:orientation="vertical" android:padding="24dp">
    <ImageView android:layout_width="64dp" android:layout_height="64dp" android:src="@drawable/ic_empty" android:tint="@color/text_tertiary"/>
    <TextView android:layout_marginTop="16dp" android:textSize="18sp" android:textColor="@color/text_secondary" android:text="No items yet"/>
    <TextView android:layout_marginTop="8dp" android:textSize="14sp" android:textColor="@color/text_tertiary" android:text="Helpful description here"/>
</LinearLayout>
```

---

## Loading States

**Centered:** `ProgressBar` in center of content area.

**Section loading:** Padding 20dp, then centered `ProgressBar`.

**Loading flag pattern:** Set loading true before async; in callback check `isDestroyed` (Activity) or `view.findViewTreeLifecycleOwner()?.lifecycle?.currentState` before updating UI. Clear loading and set data in one update.

```kotlin
fun loadData() {
    _uiState.value = UiState.Loading
    viewModelScope.launch {
        val result = repository.getData()
        _uiState.value = UiState.Success(result)  // Only update if still needed
    }
}
```

---

## Snackbars & Feedback

Use `Snackbar` for transient confirmation.

```kotlin
com.google.android.material.snackbar.Snackbar.make(
    binding.root,
    getString(R.string.action_completed),
    Snackbar.LENGTH_SHORT
).show()
```

---

## Animations & Motion

- **Durations:** 200–400 ms for UI feedback; 300–500 ms for page/activity transitions.
- **Curves:** Use `easeOutBack`, `bounceOut`, or `easeInOutCubic` for a modern feel; `DecelerateInterpolator()` for simple fade.
- **Tap feedback:** Slight scale (e.g. 0.97) with 100–150 ms duration, or use `?attr/selectableItemBackground`.
- **Lists:** Consider staggered item animation (e.g. 30–50 ms delay per item) with fade + slight slide.
- **Dispose:** Always dispose animation controllers in `onDestroy` / `onCleared`.

```kotlin
// Simple entrance fade (existing pattern)
binding.root.alpha = 0f
binding.root.animate().alpha(1f).setDuration(400).setInterpolator(DecelerateInterpolator()).start()
```

---

## 📱 Technical Implementation

### Layout Best Practices

#### 1. Zero Overflow Tolerance

**Use ConstraintLayout for complex layouts:**
```xml
<!-- activity_main.xml -->
<androidx.constraintlayout.widget.ConstraintLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/background_primary">

    <!-- Header (40% of screen) -->
    <LinearLayout
        android:id="@+id/layoutHeader"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:orientation="vertical"
        android:gravity="center"
        android:padding="@dimen/spacing_xl"
        app:layout_constraintHeight_percent="0.4"
        app:layout_constraintTop_toTopOf="parent">

        <!-- Logo and branding -->
    </LinearLayout>

    <!-- Content (60% of screen) -->
    <androidx.core.widget.NestedScrollView
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:fillViewport="true"
        app:layout_constraintTop_toBottomOf="@id/layoutHeader"
        app:layout_constraintBottom_toBottomOf="parent">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:padding="@dimen/spacing_xl">
            <!-- Scrollable content -->
        </LinearLayout>
    </androidx.core.widget.NestedScrollView>
</androidx.constraintlayout.widget.ConstraintLayout>
```

**RecyclerView with proper constraints:**
```xml
<androidx.recyclerview.widget.RecyclerView
    android:id="@+id/rvTables"
    android:layout_width="match_parent"
    android:layout_height="0dp"
    android:clipToPadding="false"
    android:paddingTop="@dimen/spacing_md"
    android:paddingBottom="@dimen/spacing_md"
    app:layout_constraintTop_toBottomOf="@id/toolbar"
    app:layout_constraintBottom_toBottomOf="parent"/>
```

---

#### 2. Optimized View Hierarchy

**Flatten view hierarchies with ConstraintLayout:**
```xml
<!-- AVOID: Nested LinearLayouts -->
<LinearLayout orientation="vertical">
    <LinearLayout orientation="horizontal">
        <LinearLayout orientation="vertical">
            <!-- Deep nesting = poor performance -->
        </LinearLayout>
    </LinearLayout>
</LinearLayout>

<!-- PREFER: Flat ConstraintLayout -->
<androidx.constraintlayout.widget.ConstraintLayout>
    <ImageView
        android:id="@+id/icon"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent"/>

    <TextView
        android:id="@+id/tvTitle"
        app:layout_constraintStart_toEndOf="@id/icon"
        app:layout_constraintTop_toTopOf="@id/icon"/>

    <TextView
        android:id="@+id/tvSubtitle"
        app:layout_constraintStart_toEndOf="@id/icon"
        app:layout_constraintTop_toBottomOf="@id/tvTitle"/>
</androidx.constraintlayout.widget.ConstraintLayout>
```

**Use merge tags to reduce hierarchy:**
```xml
<!-- res/layout/include_button_group.xml -->
<merge xmlns:android="http://schemas.android.com/apk/res/android">
    <Button
        android:id="@+id/btnTables"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"/>

    <Space
        android:layout_width="match_parent"
        android:layout_height="@dimen/spacing_md"/>

    <Button
        android:id="@+id/btnOrders"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"/>
</merge>

<!-- Usage -->
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical">

    <include layout="@layout/include_button_group"/>
</LinearLayout>
```

---

#### 3. State Management

**Loading states:**
```kotlin
sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

// In ViewModel
class TablesViewModel @Inject constructor(
    private val tableRepository: TableRepository
) : ViewModel() {

    private val _uiState = MutableLiveData<UiState<List<Table>>>()
    val uiState: LiveData<UiState<List<Table>>> = _uiState

    fun loadTables() {
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val tables = tableRepository.getTables()
                _uiState.value = UiState.Success(tables)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

// In Activity
viewModel.uiState.observe(this) { state ->
    when (state) {
        is UiState.Loading -> {
            binding.progressBar.visibility = View.VISIBLE
            binding.rvTables.visibility = View.GONE
        }
        is UiState.Success -> {
            binding.progressBar.visibility = View.GONE
            binding.rvTables.visibility = View.VISIBLE
            adapter.submitList(state.data)
        }
        is UiState.Error -> {
            binding.progressBar.visibility = View.GONE
            Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
        }
    }
}
```

---

#### 4. Code Quality Standards

**Extract reusable components:**
```kotlin
// Custom View for menu buttons
class MenuButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding: ViewMenuButtonBinding

    init {
        binding = ViewMenuButtonBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )

        // Read custom attributes
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.MenuButton,
            0, 0
        ).apply {
            try {
                val title = getString(R.styleable.MenuButton_title)
                val subtitle = getString(R.styleable.MenuButton_subtitle)
                val icon = getResourceId(R.styleable.MenuButton_icon, 0)

                binding.tvTitle.text = title
                binding.tvSubtitle.text = subtitle
                if (icon != 0) {
                    binding.icon.setImageResource(icon)
                }
            } finally {
                recycle()
            }
        }
    }

    fun setOnClickListener(listener: OnClickListener?) {
        binding.root.setOnClickListener(listener)
    }
}

// Usage in XML
<com.seacliff.pos.ui.views.MenuButton
    android:id="@+id/btnTables"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:title="@string/tables"
    app:subtitle="@string/manage_tables"
    app:icon="@drawable/ic_table_rounded"/>
```

**Consistent naming conventions:**
```kotlin
// Activity naming: [Feature]Activity
class TablesActivity : AppCompatActivity()
class OrderActivity : AppCompatActivity()

// Fragment naming: [Feature]Fragment
class MenuFragment : Fragment()
class CartFragment : Fragment()

// ViewModel naming: [Feature]ViewModel
class OrderViewModel : ViewModel()

// Adapter naming: [Item]Adapter
class TableAdapter : ListAdapter<Table, TableViewHolder>()

// ViewHolder naming: [Item]ViewHolder
class TableViewHolder(private val binding: ItemTableBinding) :
    RecyclerView.ViewHolder(binding.root)

// Layout naming:
// - activity_[name].xml
// - fragment_[name].xml
// - item_[name].xml
// - view_[name].xml
// - dialog_[name].xml
```

---

#### 5. Resource Organization

**Proper resource structure:**
```
res/
├── drawable/
│   ├── bg_button_white.xml
│   ├── bg_card_elevated.xml
│   └── ic_*.xml (vector icons)
├── drawable-night/
│   └── (dark mode variants if needed)
├── layout/
│   ├── activity_*.xml
│   ├── fragment_*.xml
│   ├── item_*.xml
│   └── view_*.xml
├── values/
│   ├── attrs.xml (custom attributes)
│   ├── colors.xml
│   ├── dimens.xml
│   ├── strings.xml
│   ├── styles.xml
│   └── themes.xml
├── values-sw600dp/
│   └── dimens.xml (tablet dimensions)
└── values-sw720dp/
    └── dimens.xml (large tablet dimensions)
```

---

## 🎯 Design Goals & Outcomes

### Primary Objectives

The minimalist design approach achieves several critical goals for POS operations:

1. **Visual Clarity**: Eliminates visual clutter through monochrome color scheme
2. **Technical Stability**: Prevents all overflow issues through proper constraint management
3. **Usability**: Maintains excellent usability with clear visual hierarchy for fast service
4. **Modern Aesthetics**: Follows Material Design 3 principles for Android applications
5. **Performance**: Provides smooth 60fps user experience without distracting animations
6. **Tablet Optimization**: Designed specifically for 7-10 inch tablets used by waiters

### Expected Benefits

- **Reduced Cognitive Load**: Waiters can focus on tasks without visual distractions
- **Professional Appearance**: Builds trust through sophisticated design
- **Consistent Experience**: Uniform design language across all screens
- **Improved Performance**: Simplified animations and layouts reduce processing overhead
- **Better Accessibility**: High contrast and clear hierarchy benefit all users
- **Fast Service**: Optimized for quick interactions in busy restaurant environments

---

## 📐 Layout Templates

### Section Header

**Simple section title:** Padding from LTRB 20, 24, 20, 16. Use `TextAppearance.SeaCliff.Heading` or titleLarge (20sp bold).

**Section with header row (title + action):** Padding 20/16/16/12. Row: icon 20dp, 8dp gap, title (titleMedium), Spacer, "See All" TextButton.

```xml
<LinearLayout android:orientation="horizontal" android:paddingStart="20dp" android:paddingTop="24dp" android:paddingEnd="20dp" android:paddingBottom="16dp">
    <TextView android:textAppearance="@style/TextAppearance.SeaCliff.Heading" android:text="Section Title"/>
</LinearLayout>
```

### Standard Activity Layout

```xml
<!-- activity_template.xml -->
<androidx.constraintlayout.widget.ConstraintLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/background_primary"
    tools:context=".ui.activities.MainActivity">

    <!-- Toolbar -->
    <com.google.android.material.appbar.MaterialToolbar
        android:id="@+id/toolbar"
        android:layout_width="match_parent"
        android:layout_height="?attr/actionBarSize"
        android:background="@color/surface_white"
        android:elevation="4dp"
        app:title="@string/app_name"
        app:titleTextColor="@color/text_primary"
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

        <!-- Logo -->
        <androidx.cardview.widget.CardView
            android:layout_width="80dp"
            android:layout_height="80dp"
            app:cardBackgroundColor="@color/surface_white"
            app:cardCornerRadius="16dp"
            app:cardElevation="2dp">

            <ImageView
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:padding="16dp"
                android:src="@drawable/ic_seacliff_logo"
                android:contentDescription="@string/app_name"/>
        </androidx.cardview.widget.CardView>

        <!-- Title -->
        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="@dimen/spacing_lg"
            android:text="@string/welcome"
            android:textAppearance="@style/TextAppearance.SeaCliff.Heading"/>

        <!-- Subtitle -->
        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="@dimen/spacing_sm"
            android:text="@string/select_action"
            android:textAppearance="@style/TextAppearance.SeaCliff.Subtitle"/>
    </LinearLayout>

    <!-- Content Section (Scrollable) -->
    <androidx.core.widget.NestedScrollView
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:fillViewport="true"
        android:clipToPadding="false"
        app:layout_constraintTop_toBottomOf="@id/layoutHeader"
        app:layout_constraintBottom_toBottomOf="parent">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:paddingStart="@dimen/spacing_xl"
            android:paddingEnd="@dimen/spacing_xl"
            android:paddingBottom="@dimen/spacing_xl">

            <!-- Content buttons/cards -->
        </LinearLayout>
    </androidx.core.widget.NestedScrollView>
</androidx.constraintlayout.widget.ConstraintLayout>
```

---

### Menu Button Template

```xml
<!-- view_menu_button.xml -->
<androidx.cardview.widget.CardView
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginBottom="@dimen/spacing_md"
    android:foreground="?attr/selectableItemBackground"
    app:cardBackgroundColor="@color/surface_white"
    app:cardCornerRadius="@dimen/corner_radius_large"
    app:cardElevation="@dimen/elevation_button">

    <androidx.constraintlayout.widget.ConstraintLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:minHeight="@dimen/button_height_standard"
        android:padding="@dimen/spacing_lg">

        <!-- Icon Container -->
        <androidx.cardview.widget.CardView
            android:id="@+id/iconContainer"
            android:layout_width="48dp"
            android:layout_height="48dp"
            app:cardBackgroundColor="@color/icon_background"
            app:cardCornerRadius="12dp"
            app:cardElevation="0dp"
            app:layout_constraintStart_toStartOf="parent"
            app:layout_constraintTop_toTopOf="parent"
            app:layout_constraintBottom_toBottomOf="parent">

            <ImageView
                android:id="@+id/icon"
                android:layout_width="24dp"
                android:layout_height="24dp"
                android:layout_gravity="center"
                android:tint="@color/icon_tint_white"
                tools:src="@drawable/ic_restaurant_rounded"
                android:contentDescription="@null"/>
        </androidx.cardview.widget.CardView>

        <!-- Title -->
        <TextView
            android:id="@+id/tvTitle"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_marginStart="@dimen/spacing_lg"
            android:ellipsize="end"
            android:maxLines="1"
            android:textAppearance="@style/TextAppearance.SeaCliff.Title"
            app:layout_constraintStart_toEndOf="@id/iconContainer"
            app:layout_constraintEnd_toEndOf="parent"
            app:layout_constraintTop_toTopOf="@id/iconContainer"
            tools:text="Tables"/>

        <!-- Subtitle -->
        <TextView
            android:id="@+id/tvSubtitle"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_marginStart="@dimen/spacing_lg"
            android:ellipsize="end"
            android:maxLines="1"
            android:textAppearance="@style/TextAppearance.SeaCliff.Subtitle"
            app:layout_constraintStart_toEndOf="@id/iconContainer"
            app:layout_constraintEnd_toEndOf="parent"
            app:layout_constraintTop_toBottomOf="@id/tvTitle"
            app:layout_constraintBottom_toBottomOf="@id/iconContainer"
            tools:text="Manage restaurant tables"/>
    </androidx.constraintlayout.widget.ConstraintLayout>
</androidx.cardview.widget.CardView>
```

---

### Table Card Template

```xml
<!-- item_table.xml -->
<androidx.cardview.widget.CardView
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_margin="@dimen/spacing_sm"
    android:foreground="?attr/selectableItemBackground"
    app:cardBackgroundColor="@color/surface_white"
    app:cardCornerRadius="@dimen/corner_radius_medium"
    app:cardElevation="@dimen/elevation_card">

    <androidx.constraintlayout.widget.ConstraintLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:padding="@dimen/spacing_lg">

        <!-- Table Icon -->
        <ImageView
            android:id="@+id/ivTableIcon"
            android:layout_width="40dp"
            android:layout_height="40dp"
            android:src="@drawable/ic_table_rounded"
            android:tint="@color/icon_background"
            android:contentDescription="@string/table_icon"
            app:layout_constraintStart_toStartOf="parent"
            app:layout_constraintTop_toTopOf="parent"/>

        <!-- Table Name -->
        <TextView
            android:id="@+id/tvTableName"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_marginStart="@dimen/spacing_md"
            android:ellipsize="end"
            android:maxLines="1"
            android:textAppearance="@style/TextAppearance.SeaCliff.Title"
            app:layout_constraintStart_toEndOf="@id/ivTableIcon"
            app:layout_constraintEnd_toEndOf="parent"
            app:layout_constraintTop_toTopOf="@id/ivTableIcon"
            tools:text="Table 5"/>

        <!-- Table Info -->
        <TextView
            android:id="@+id/tvTableInfo"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_marginStart="@dimen/spacing_md"
            android:ellipsize="end"
            android:maxLines="1"
            android:textAppearance="@style/TextAppearance.SeaCliff.Caption"
            app:layout_constraintStart_toEndOf="@id/ivTableIcon"
            app:layout_constraintEnd_toEndOf="parent"
            app:layout_constraintTop_toBottomOf="@id/tvTableName"
            tools:text="Indoor • 4 seats"/>

        <!-- Status Badge -->
        <com.google.android.material.chip.Chip
            android:id="@+id/chipStatus"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="@dimen/spacing_sm"
            android:textAppearance="@style/TextAppearance.SeaCliff.Caption"
            app:chipBackgroundColor="@color/text_tertiary"
            app:chipStrokeWidth="0dp"
            app:layout_constraintStart_toStartOf="parent"
            app:layout_constraintTop_toBottomOf="@id/ivTableIcon"
            tools:text="Available"/>
    </androidx.constraintlayout.widget.ConstraintLayout>
</androidx.cardview.widget.CardView>
```

---

## Design Checklist

Before submitting any new screen or component, verify:

### Colors
- [ ] Background is `#FAFAFA`
- [ ] Cards/surfaces are `#FFFFFF`
- [ ] Primary text is `#1A1A1A`
- [ ] Secondary text is `#666666`
- [ ] No colorful buttons (orange, green, blue)

### Typography
- [ ] Title: 15–16sp, weight 600
- [ ] Subtitle: 11–12sp, weight 400
- [ ] All dynamic text has `maxLines` + `ellipsize="end"`

### Spacing
- [ ] Container padding: 16dp
- [ ] Section margins: 24dp
- [ ] Card gaps: 12dp
- [ ] Border radius: 16dp (cards), 12dp (buttons)

### Touch Targets
- [ ] All tappable elements: minimum 48×48dp
- [ ] Primary buttons: 72–80dp height
- [ ] IconButtons have explicit min width/height 48dp

### Animations & Motion
- [ ] UI feedback: 200–400 ms; use interpolator where appropriate
- [ ] Tap feedback: ripple or slight scale
- [ ] Dispose animation controllers in `onDestroy` / `onCleared`

### Structure
- [ ] Content in `ScrollView` / `NestedScrollView` where needed
- [ ] Use `RefreshIndicator` (or pull-to-refresh) for refreshable lists
- [ ] Proper lifecycle management and binding cleanup

### Icons
- [ ] Prefer rounded variants
- [ ] 24dp in 48dp containers, 16dp inline
- [ ] Dark containers (`#1A1A1A`) with white icons

---

## Developer Notes

1. **SafeArea / fitSystemWindows:** Use appropriate insets for status/navigation bars.
2. **Test on multiple devices** (phone, 7" tablet, 10" tablet) before deployment.
3. **Follow monochrome palette** — no colorful elements.
4. **48dp minimum touch targets** — no exceptions.
5. **Always use `maxLines` + `ellipsize`** on dynamic text.
6. **Dispose resources** in `onDestroy()` / `onCleared()` (animations, listeners).
7. **Check `isDestroyed` / lifecycle** before updating UI from async callbacks.
8. **Use `const` / `@JvmStatic`** and reuse styles/dimens where possible.

---

## 🔄 Version History

- **v2.0.0** (2026-02-18): Design system update
    - Added Quick Reference table; aligned colors, typography scale, and spacing
    - Added Component Patterns: Action Card, Info Card, Tip Item, Horizontal Scroll Card, Navigation Card, Badge/Chip, List Tile Card
    - Added AppBar standard, Shadows & Elevation presets, Icon System (sizes + container styles)
    - Added Dialogs, Empty States, Loading States, Snackbars, Animations & Motion
    - Added Design Checklist and Developer Notes
- **v1.0.0** (2026-01-30): Initial Android design guidelines established
    - Converted from Flutter design system to Android native
    - Implemented monochrome color palette for Material Design
    - Established overflow prevention strategies using ConstraintLayout
    - Created standard component templates for Activities and Views
    - Added tablet-optimized layouts and responsive design
    - Integrated accessibility best practices

---

## 📝 Notes for Android Developers (Detailed)

### Development Guidelines

1. **Always test layouts on multiple device sizes:**
   - Phone (480dp, 600dp)
   - 7" Tablet (600dp-720dp)
   - 10" Tablet (720dp+)
   - Both portrait and landscape

2. **Use Android Studio Layout Inspector:**
   - Check view hierarchy depth (keep under 10 levels)
   - Measure layout performance
   - Verify no overdraw issues

3. **Maintain consistency:**
   - Use defined styles and themes
   - Follow naming conventions
   - Reuse custom views and components

4. **Document deviations:**
   - Any changes to color palette must be documented
   - Custom view implementations should be commented
   - Accessibility decisions should be justified

5. **Performance first:**
   - Use ViewBinding (never findViewById in loops)
   - Implement DiffUtil for RecyclerViews
   - Optimize images and drawables
   - Use vector drawables for icons

6. **Accessibility checklist:**
   - Run Accessibility Scanner on all screens
   - Test with TalkBack enabled
   - Verify touch targets (48dp minimum)
   - Ensure 4.5:1 contrast ratio

### Code Review Checklist

- [ ] Layout uses ConstraintLayout or proper ViewGroups
- [ ] No hardcoded strings (all in strings.xml)
- [ ] Colors defined in colors.xml
- [ ] Dimensions defined in dimens.xml
- [ ] Proper content descriptions for images
- [ ] Touch targets minimum 48dp
- [ ] ViewBinding used (no findViewById)
- [ ] Proper lifecycle management
- [ ] No memory leaks (binding cleanup)
- [ ] Animations are smooth and simple
- [ ] Tested on tablet and phone
- [ ] Accessibility Scanner passed

---

## 🛠️ Implementation Examples

### Complete MainActivity Example

```kotlin
// MainActivity.kt
package com.seacliff.pos.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.animation.DecelerateInterpolator
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.seacliff.pos.R
import com.seacliff.pos.databinding.ActivityMainBinding
import com.seacliff.pos.ui.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupUI()
        animateEntrance()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = getString(R.string.app_name)
            subtitle = getString(
                R.string.welcome_user,
                authViewModel.getStaffName() ?: getString(R.string.user)
            )
        }
    }

    private fun setupUI() {
        // Tables button
        binding.btnTables.setOnClickListener {
            startActivity(Intent(this, TablesActivity::class.java))
        }

        // Orders button
        binding.btnOrders.setOnClickListener {
            startActivity(Intent(this, OrdersActivity::class.java))
        }

        // Menu button
        binding.btnMenu.setOnClickListener {
            startActivity(Intent(this, MenuActivity::class.java))
        }

        // Role-based visibility
        val role = authViewModel.getStaffRole()
        if (role == "manager" || role == "admin") {
            binding.btnPayments.visibility = android.view.View.VISIBLE
            binding.btnPayments.setOnClickListener {
                // Navigate to payments
            }
        }
    }

    private fun animateEntrance() {
        binding.root.apply {
            alpha = 0f
            animate()
                .alpha(1f)
                .setDuration(600)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sync -> {
                // Trigger sync
                true
            }
            R.id.action_logout -> {
                showLogoutDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.logout)
            .setMessage(R.string.logout_confirmation)
            .setPositiveButton(R.string.yes) { _, _ ->
                authViewModel.logout()
                navigateToLogin()
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
```

---

*These guidelines are living documentation and should be updated as the application evolves while maintaining the core minimalist philosophy and Material Design principles for Android.*

---

## 📚 Additional Resources

- [Material Design 3 Guidelines](https://m3.material.io/)
- [Android Accessibility](https://developer.android.com/guide/topics/ui/accessibility)
- [ConstraintLayout Guide](https://developer.android.com/develop/ui/views/layout/constraint-layout)
- [Android Performance Best Practices](https://developer.android.com/topic/performance)
- [ViewBinding Documentation](https://developer.android.com/topic/libraries/view-binding)
- [Material Components for Android](https://github.com/material-components/material-components-android)

---

**END OF DOCUMENT**
