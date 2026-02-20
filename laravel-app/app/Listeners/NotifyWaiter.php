<?php

namespace App\Listeners;

use App\Events\OrderItemReady;
use App\Jobs\SendFcmNotification;
use Illuminate\Support\Facades\Log;

class NotifyWaiter
{
    public function handle(OrderItemReady $event): void
    {
        $orderItem = $event->orderItem;
        $order = $orderItem->order;

        Log::info('FCM Listener [NotifyWaiter]: OrderItemReady event received', [
            'order_item_id' => $orderItem->id,
            'order_id' => $order?->id,
            'waiter_id' => $order?->waiter_id,
        ]);

        if (! $order || ! $order->waiter_id) {
            Log::info('FCM Listener [NotifyWaiter]: Skipped — no order or no waiter assigned');

            return;
        }

        $data = [
            'type' => 'order_item_ready',
            'order_id' => (string) $order->id,
            'order_number' => $order->order_number ?? '',
            'order_item_id' => (string) $orderItem->id,
            'menu_item_name' => $orderItem->menuItem->name ?? 'Unknown',
            'prep_status' => 'ready',
            'table_name' => $order->table->name ?? 'N/A',
            'timestamp' => now()->toISOString(),
        ];

        Log::info('FCM Listener [NotifyWaiter]: Dispatching FCM job', [
            'waiter_id' => $order->waiter_id,
            'menu_item' => $data['menu_item_name'],
        ]);

        SendFcmNotification::dispatch('staff', [$order->waiter_id], $data)
            ->onQueue('notifications');
    }
}
