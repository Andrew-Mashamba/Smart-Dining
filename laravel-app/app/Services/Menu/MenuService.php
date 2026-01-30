<?php

namespace App\Services\Menu;

use App\Models\MenuItem;
use Illuminate\Support\Collection;

class MenuService
{
    /**
     * Get available menu items with optional filters
     *
     * @param array $filters
     * @return Collection
     */
    public function getAvailableMenu(array $filters = []): Collection
    {
        $query = MenuItem::where('is_available', true);

        if (isset($filters['category'])) {
            $query->where('category', $filters['category']);
        }

        if (isset($filters['prep_area'])) {
            $query->where('preparation_area', $filters['prep_area']);
        }

        if (isset($filters['max_price'])) {
            $query->where('price', '<=', $filters['max_price']);
        }

        if (isset($filters['min_price'])) {
            $query->where('price', '>=', $filters['min_price']);
        }

        return $query->orderBy('category')
            ->orderBy('name')
            ->get();
    }

    /**
     * Get menu items by category
     *
     * @param string $category
     * @return Collection
     */
    public function getItemsByCategory(string $category): Collection
    {
        return MenuItem::where('category', $category)
            ->where('is_available', true)
            ->orderBy('name')
            ->get();
    }

    /**
     * Get menu items by preparation area
     *
     * @param string $prepArea
     * @return Collection
     */
    public function getItemsByPrepArea(string $prepArea): Collection
    {
        return MenuItem::where('preparation_area', $prepArea)
            ->where('is_available', true)
            ->orderBy('category')
            ->orderBy('name')
            ->get();
    }

    /**
     * Update menu item availability
     *
     * @param MenuItem $item
     * @param bool $available
     * @return void
     */
    public function updateAvailability(MenuItem $item, bool $available): void
    {
        $item->update(['is_available' => $available]);

        \Log::info('Menu item availability updated', [
            'item_id' => $item->id,
            'name' => $item->name,
            'available' => $available,
        ]);
    }

    /**
     * Get menu grouped by category
     *
     * @return array
     */
    public function getMenuByCategory(): array
    {
        $items = MenuItem::where('is_available', true)
            ->orderBy('category')
            ->orderBy('name')
            ->get();

        return $items->groupBy('category')
            ->map(function ($categoryItems, $category) {
                return [
                    'category' => $category,
                    'items' => $categoryItems->map(function ($item) {
                        return [
                            'id' => $item->id,
                            'name' => $item->name,
                            'description' => $item->description,
                            'price' => $item->price,
                            'preparation_time' => $item->preparation_time,
                            'preparation_area' => $item->preparation_area,
                        ];
                    }),
                ];
            })
            ->values()
            ->toArray();
    }

    /**
     * Search menu items by name or description
     *
     * @param string $searchTerm
     * @return Collection
     */
    public function searchMenu(string $searchTerm): Collection
    {
        return MenuItem::where('is_available', true)
            ->where(function ($query) use ($searchTerm) {
                $query->where('name', 'ILIKE', "%{$searchTerm}%")
                    ->orWhere('description', 'ILIKE', "%{$searchTerm}%");
            })
            ->orderBy('name')
            ->get();
    }

    /**
     * Get menu item with details
     *
     * @param int $itemId
     * @return MenuItem
     */
    public function getMenuItem(int $itemId): MenuItem
    {
        return MenuItem::findOrFail($itemId);
    }

    /**
     * Get popular menu items based on order count
     *
     * @param int $limit
     * @return Collection
     */
    public function getPopularItems(int $limit = 10): Collection
    {
        return MenuItem::where('is_available', true)
            ->withCount('orderItems')
            ->orderBy('order_items_count', 'desc')
            ->limit($limit)
            ->get();
    }

    /**
     * Bulk update menu item availability
     *
     * @param array $itemIds
     * @param bool $available
     * @return int
     */
    public function bulkUpdateAvailability(array $itemIds, bool $available): int
    {
        return MenuItem::whereIn('id', $itemIds)
            ->update(['is_available' => $available]);
    }

    /**
     * Get menu statistics
     *
     * @return array
     */
    public function getMenuStats(): array
    {
        $total = MenuItem::count();
        $available = MenuItem::where('is_available', true)->count();
        $byCategory = MenuItem::select('category')
            ->selectRaw('count(*) as count')
            ->groupBy('category')
            ->pluck('count', 'category')
            ->toArray();

        return [
            'total_items' => $total,
            'available_items' => $available,
            'unavailable_items' => $total - $available,
            'by_category' => $byCategory,
        ];
    }
}
