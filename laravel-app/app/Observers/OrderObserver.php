<?php

namespace App\Observers;

use App\Models\Order;
use App\Http\Controllers\WhatsAppController;
use Illuminate\Support\Facades\Log;

class OrderObserver
{
    /**
     * Handle the Order "updated" event.
     */
    public function updated(Order $order): void
    {
        // Check if status was changed
        if ($order->isDirty('status')) {
            $newStatus = $order->status;
            $oldStatus = $order->getOriginal('status');

            Log::info('Order status changed', [
                'order_id' => $order->id,
                'order_number' => $order->order_number,
                'old_status' => $oldStatus,
                'new_status' => $newStatus,
                'order_source' => $order->order_source,
            ]);

            // Send WhatsApp notification if order was placed via WhatsApp
            if ($order->order_source === 'whatsapp' && $order->guest) {
                try {
                    $whatsappController = app(WhatsAppController::class);
                    $whatsappController->notifyStatusChange($order, $newStatus);
                } catch (\Exception $e) {
                    Log::error('Failed to send WhatsApp status notification', [
                        'order_id' => $order->id,
                        'error' => $e->getMessage(),
                    ]);
                }
            }
        }
    }
}
