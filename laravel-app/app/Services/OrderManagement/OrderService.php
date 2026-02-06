<?php

namespace App\Services\OrderManagement;

use App\Models\Order;
use App\Models\Guest;
use App\Models\Table;
use App\Models\Staff;
use App\Models\OrderItem;
use App\Models\MenuItem;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Collection;

class OrderService
{
    /**
     * Create a new order
     *
     * @param array $data
     * @return Order
     */
    public function createOrder(array $data): Order
    {
        return DB::transaction(function () use ($data) {
            $order = Order::create([
                'guest_id' => $data['guest_id'],
                'table_id' => $data['table_id'],
                'waiter_id' => $data['waiter_id'],
                'session_id' => $data['session_id'] ?? null,
                'order_source' => $data['order_source'] ?? 'pos',
                'status' => 'pending',
                'notes' => $data['notes'] ?? null,
            ]);

            if (isset($data['items']) && is_array($data['items'])) {
                $this->addItems($order, $data['items']);
            }

            return $order->fresh();
        });
    }

    /**
     * Add items to an order
     *
     * @param Order $order
     * @param array $items
     * @return void
     */
    public function addItems(Order $order, array $items): void
    {
        // Validate stock availability for all items before creating order items
        foreach ($items as $item) {
            $menuItem = MenuItem::findOrFail($item['menu_item_id']);

            if ($menuItem->stock_quantity < $item['quantity']) {
                throw new \Exception("Insufficient stock for {$menuItem->name}. Only {$menuItem->stock_quantity} {$menuItem->unit} available.");
            }
        }

        // Create order items after validation passes
        foreach ($items as $item) {
            $menuItem = MenuItem::findOrFail($item['menu_item_id']);

            $orderItem = OrderItem::create([
                'order_id' => $order->id,
                'menu_item_id' => $menuItem->id,
                'quantity' => $item['quantity'],
                'unit_price' => $menuItem->price,
                'subtotal' => $menuItem->price * $item['quantity'],
                'status' => 'pending',
                'special_instructions' => $item['special_instructions'] ?? null,
            ]);
        }

        $this->calculateTotals($order);
    }

    /**
     * Remove an item from an order
     *
     * @param OrderItem $orderItem
     * @return void
     */
    public function removeItem(OrderItem $orderItem): void
    {
        $order = $orderItem->order;

        if (in_array($orderItem->status, ['preparing', 'ready', 'served'])) {
            throw new \Exception('Cannot remove item that is already being prepared or served');
        }

        $orderItem->delete();
        $this->calculateTotals($order);
    }

    /**
     * Update order status
     *
     * @param Order $order
     * @param string $status
     * @return void
     */
    public function updateOrderStatus(Order $order, string $status): void
    {
        $validTransitions = $this->getValidStatusTransitions($order->status);

        if (!in_array($status, $validTransitions)) {
            throw new \Exception("Cannot transition from {$order->status} to {$status}");
        }

        $order->update(['status' => $status]);
    }

    /**
     * Calculate order totals
     *
     * @param Order $order
     * @return array
     */
    public function calculateTotals(Order $order): array
    {
        $subtotal = $order->items()->sum('subtotal');
        $tax = $subtotal * 0.18; // 18% VAT
        $serviceCharge = $subtotal * 0.05; // 5% service charge
        $total = $subtotal + $tax + $serviceCharge;

        $order->update([
            'subtotal' => $subtotal,
            'tax' => $tax,
            'service_charge' => $serviceCharge,
            'total_amount' => $total,
        ]);

        return [
            'subtotal' => $subtotal,
            'tax' => $tax,
            'service_charge' => $serviceCharge,
            'total_amount' => $total,
        ];
    }

    /**
     * Cancel an order
     *
     * @param Order $order
     * @param string $reason
     * @return void
     */
    public function cancelOrder(Order $order, string $reason): void
    {
        if (in_array($order->status, ['completed', 'cancelled'])) {
            throw new \Exception('Cannot cancel a completed or already cancelled order');
        }

        $order->update([
            'status' => 'cancelled',
            'notes' => ($order->notes ?? '') . "\nCancellation reason: " . $reason,
        ]);

        // Cancel all pending items
        $order->items()
            ->whereIn('status', ['pending', 'confirmed'])
            ->update(['status' => 'cancelled']);
    }

    /**
     * Get valid status transitions for current status
     *
     * @param string $currentStatus
     * @return array
     */
    protected function getValidStatusTransitions(string $currentStatus): array
    {
        $transitions = [
            'pending' => ['confirmed', 'cancelled'],
            'confirmed' => ['preparing', 'cancelled'],
            'preparing' => ['ready', 'cancelled'],
            'ready' => ['served'],
            'served' => ['completed'],
            'completed' => [],
            'cancelled' => [],
        ];

        return $transitions[$currentStatus] ?? [];
    }

    /**
     * Get order summary with items
     *
     * @param Order $order
     * @return array
     */
    public function getOrderSummary(Order $order): array
    {
        $order->load(['items.menuItem', 'guest', 'table', 'waiter']);

        return [
            'order_id' => $order->id,
            'guest' => [
                'name' => $order->guest->name,
                'phone' => $order->guest->phone_number,
            ],
            'table' => $order->table->name,
            'waiter' => $order->waiter->name,
            'status' => $order->status,
            'items' => $order->items->map(function ($item) {
                return [
                    'name' => $item->menuItem->name,
                    'quantity' => $item->quantity,
                    'unit_price' => $item->unit_price,
                    'subtotal' => $item->subtotal,
                    'status' => $item->status,
                    'special_instructions' => $item->special_instructions,
                ];
            }),
            'totals' => [
                'subtotal' => $order->subtotal,
                'tax' => $order->tax,
                'service_charge' => $order->service_charge,
                'total_amount' => $order->total_amount,
            ],
            'created_at' => $order->created_at,
        ];
    }

    /**
     * Get orders by status
     *
     * @param string $status
     * @return Collection
     */
    public function getOrdersByStatus(string $status): Collection
    {
        return Order::where('status', $status)
            ->with(['items.menuItem', 'guest', 'table', 'waiter'])
            ->orderBy('created_at', 'desc')
            ->get();
    }
}
