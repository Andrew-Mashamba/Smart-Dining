<?php

namespace App\Console\Commands;

use App\Events\OrderCreated;
use App\Models\Order;
use Illuminate\Console\Command;

class TestBroadcasting extends Command
{
    /**
     * The name and signature of the console command.
     *
     * @var string
     */
    protected $signature = 'test:broadcasting';

    /**
     * The console command description.
     *
     * @var string
     */
    protected $description = 'Test broadcasting by dispatching an OrderCreated event';

    /**
     * Execute the console command.
     */
    public function handle()
    {
        $this->info('Testing broadcasting...');

        // Get the first order from the database
        $order = Order::with(['table', 'items'])->first();

        if (!$order) {
            $this->error('No orders found in the database. Please create an order first.');
            return 1;
        }

        $this->info("Dispatching OrderCreated event for Order #{$order->id}");

        // Dispatch the OrderCreated event
        event(new OrderCreated($order));

        $this->info('Event dispatched successfully!');
        $this->info('Check your WebSocket connections for the broadcast.');
        $this->info("Channels: orders, kitchen, bar, waiter.{$order->waiter_id}");

        return 0;
    }
}
