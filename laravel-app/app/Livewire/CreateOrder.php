<?php

namespace App\Livewire;

use App\Models\MenuItem;
use App\Models\MenuCategory;
use App\Models\Table;
use App\Models\Order;
use App\Models\OrderItem;
use Livewire\Component;
use Illuminate\Support\Facades\DB;

class CreateOrder extends Component
{
    // Order properties
    public $selectedTableId = '';
    public $cart = [];
    public $searchTerm = '';
    public $selectedCategoryId = '';

    // Totals
    public $subtotal = 0;
    public $tax = 0;
    public $total = 0;
    public $taxRate = 0.18; // 18% VAT - configurable

    /**
     * Validation rules
     */
    protected function rules()
    {
        return [
            'selectedTableId' => 'required|exists:tables,id',
            'cart' => 'required|array|min:1',
        ];
    }

    /**
     * Custom validation messages
     */
    protected function messages()
    {
        return [
            'selectedTableId.required' => 'Please select a table for this order.',
            'cart.required' => 'Please add at least one item to the cart.',
            'cart.min' => 'Please add at least one item to the cart.',
        ];
    }

    /**
     * Add item to cart
     */
    public function addItem($menuItemId)
    {
        $menuItem = MenuItem::findOrFail($menuItemId);

        // Check if item already exists in cart
        $existingIndex = $this->findCartItemIndex($menuItemId);

        if ($existingIndex !== null) {
            // Increment quantity if already in cart
            $this->cart[$existingIndex]['quantity']++;
        } else {
            // Add new item to cart
            $this->cart[] = [
                'menu_item_id' => $menuItem->id,
                'name' => $menuItem->name,
                'unit_price' => $menuItem->price,
                'quantity' => 1,
                'special_instructions' => '',
            ];
        }

        $this->calculateTotals();
    }

    /**
     * Remove item from cart
     */
    public function removeItem($index)
    {
        if (isset($this->cart[$index])) {
            unset($this->cart[$index]);
            $this->cart = array_values($this->cart); // Re-index array
            $this->calculateTotals();
        }
    }

    /**
     * Update item quantity in cart
     */
    public function updateQuantity($index, $quantity)
    {
        if (isset($this->cart[$index])) {
            $quantity = max(1, intval($quantity)); // Ensure minimum quantity of 1
            $this->cart[$index]['quantity'] = $quantity;
            $this->calculateTotals();
        }
    }

    /**
     * Update special instructions for cart item
     */
    public function updateInstructions($index, $instructions)
    {
        if (isset($this->cart[$index])) {
            $this->cart[$index]['special_instructions'] = $instructions;
        }
    }

    /**
     * Calculate order totals (subtotal, tax, total)
     */
    public function calculateTotals()
    {
        $this->subtotal = 0;

        foreach ($this->cart as $item) {
            $this->subtotal += $item['unit_price'] * $item['quantity'];
        }

        $this->tax = $this->subtotal * $this->taxRate;
        $this->total = $this->subtotal + $this->tax;
    }

    /**
     * Find cart item index by menu item ID
     */
    private function findCartItemIndex($menuItemId)
    {
        foreach ($this->cart as $index => $item) {
            if ($item['menu_item_id'] == $menuItemId) {
                return $index;
            }
        }
        return null;
    }

    /**
     * Clear cart
     */
    public function clearCart()
    {
        $this->cart = [];
        $this->calculateTotals();
    }

    /**
     * Place order - create Order and OrderItems, update table status
     */
    public function placeOrder()
    {
        // Validate
        $this->validate();

        try {
            DB::beginTransaction();

            // Create order
            $order = Order::create([
                'table_id' => $this->selectedTableId,
                'waiter_id' => auth()->id(),
                'order_source' => 'pos',
                'status' => 'pending',
                'subtotal' => $this->subtotal,
                'tax' => $this->tax,
                'total' => $this->total,
            ]);

            // Create order items
            foreach ($this->cart as $cartItem) {
                OrderItem::create([
                    'order_id' => $order->id,
                    'menu_item_id' => $cartItem['menu_item_id'],
                    'quantity' => $cartItem['quantity'],
                    'unit_price' => $cartItem['unit_price'],
                    'subtotal' => $cartItem['unit_price'] * $cartItem['quantity'],
                    'special_instructions' => $cartItem['special_instructions'],
                    'prep_status' => 'pending',
                ]);
            }

            // Update table status to occupied
            $table = Table::findOrFail($this->selectedTableId);
            $table->markAsOccupied();

            DB::commit();

            // Clear cart and reset form
            $this->clearCart();
            $this->selectedTableId = '';

            // Redirect to orders list with success message
            session()->flash('success', 'Order placed successfully! Order #' . $order->order_number);

            return redirect()->route('dashboard');

        } catch (\Exception $e) {
            DB::rollBack();
            session()->flash('error', 'Failed to place order: ' . $e->getMessage());
        }
    }

    /**
     * Render the component
     */
    public function render()
    {
        // Get all categories
        $categories = MenuCategory::where('status', 'active')->get();

        // Get available menu items with filtering
        $menuItems = MenuItem::with('menuCategory')
            ->available()
            ->when($this->selectedCategoryId, function ($query) {
                $query->where('category_id', $this->selectedCategoryId);
            })
            ->when($this->searchTerm, function ($query) {
                $query->where('name', 'like', '%' . $this->searchTerm . '%');
            })
            ->orderBy('name')
            ->get();

        // Get all tables
        $tables = Table::orderBy('name')->get();

        return view('livewire.create-order', [
            'categories' => $categories,
            'menuItems' => $menuItems,
            'tables' => $tables,
        ])->layout('layouts.app-layout');
    }
}
