<?php

require __DIR__ . '/vendor/autoload.php';

$app = require_once __DIR__ . '/bootstrap/app.php';
$app->make(Illuminate\Contracts\Console\Kernel::class)->bootstrap();

use App\Events\OrderCreated;
use App\Events\OrderStatusUpdated;
use App\Models\Order;
use App\Models\Table;
use App\Models\User;

echo "Testing Laravel Reverb Broadcasting\n";
echo "====================================\n\n";

// Check if we have any orders
$order = Order::with(['table', 'items'])->first();

if (!$order) {
    echo "No orders found. Creating test data...\n\n";

    // Create a test table
    $table = Table::firstOrCreate(
        ['name' => 'Test Table 1'],
        [
            'capacity' => 4,
            'status' => 'available',
            'qr_code' => 'TEST123',
            'location' => 'Test Area'
        ]
    );

    // Create a test waiter
    $waiter = User::where('role', 'waiter')->first();
    if (!$waiter) {
        echo "Creating test waiter...\n";
        $waiter = User::create([
            'name' => 'Test Waiter',
            'email' => 'waiter@test.com',
            'password' => bcrypt('password'),
            'role' => 'waiter',
        ]);
    }

    // Create a test order
    $order = Order::create([
        'table_id' => $table->id,
        'waiter_id' => $waiter->id,
        'status' => 'pending',
        'order_number' => 'ORD-' . time(),
        'order_source' => 'web',
        'subtotal' => 25.50,
        'tax' => 2.55,
        'total' => 28.05,
    ]);

    echo "✓ Test order created (ID: {$order->id})\n\n";
}

// Reload order with relationships
$order = Order::with(['table', 'items'])->find($order->id);

echo "Order Details:\n";
echo "  ID: {$order->id}\n";
echo "  Table: {$order->table->name}\n";
echo "  Status: {$order->status}\n";
echo "  Items Count: {$order->items->count()}\n\n";

echo "Dispatching OrderCreated event...\n";
event(new OrderCreated($order));
echo "✓ OrderCreated event dispatched!\n\n";

echo "Dispatching OrderStatusUpdated event...\n";
event(new OrderStatusUpdated($order, 'pending', 'confirmed'));
echo "✓ OrderStatusUpdated event dispatched!\n\n";

echo "Broadcasting Test Complete!\n";
echo "====================================\n";
echo "Check your Reverb server logs to verify the broadcast.\n";
echo "Reverb server should show connections and message activity.\n";
