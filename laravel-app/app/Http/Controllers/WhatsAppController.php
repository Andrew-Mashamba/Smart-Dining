<?php

namespace App\Http\Controllers;

use App\Models\Guest;
use App\Models\MenuItem;
use App\Models\MenuCategory;
use App\Models\Order;
use App\Models\OrderItem;
use App\Services\WhatsAppService;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Log;
use Illuminate\Support\Facades\DB;

class WhatsAppController extends Controller
{
    protected WhatsAppService $whatsappService;

    public function __construct(WhatsAppService $whatsappService)
    {
        $this->whatsappService = $whatsappService;
    }

    /**
     * Handle WhatsApp webhook verification (GET request)
     */
    public function verify(Request $request)
    {
        $mode = $request->query('hub_mode');
        $token = $request->query('hub_verify_token');
        $challenge = $request->query('hub_challenge');

        $verifyToken = config('services.whatsapp.verify_token');

        if ($mode === 'subscribe' && $token === $verifyToken) {
            Log::info('WhatsApp webhook verified successfully');
            return response($challenge, 200)->header('Content-Type', 'text/plain');
        }

        Log::warning('WhatsApp webhook verification failed', [
            'mode' => $mode,
            'token' => $token,
        ]);

        return response()->json(['error' => 'Verification failed'], 403);
    }

    /**
     * Handle incoming WhatsApp messages (POST request)
     */
    public function webhook(Request $request)
    {
        Log::info('WhatsApp webhook received', ['payload' => $request->all()]);

        try {
            $data = $request->all();

            // Check if this is a message event
            if (!isset($data['entry'][0]['changes'][0]['value']['messages'][0])) {
                return response()->json(['status' => 'ok']);
            }

            $message = $data['entry'][0]['changes'][0]['value']['messages'][0];
            $from = $message['from'];
            $messageType = $message['type'];

            // Only process text messages
            if ($messageType !== 'text') {
                return response()->json(['status' => 'ok']);
            }

            $messageText = strtolower(trim($message['text']['body']));

            // Process the message based on command
            $this->processMessage($from, $messageText);

            return response()->json(['status' => 'ok']);
        } catch (\Exception $e) {
            Log::error('WhatsApp webhook error', [
                'error' => $e->getMessage(),
                'trace' => $e->getTraceAsString(),
            ]);

            return response()->json(['status' => 'error'], 500);
        }
    }

    /**
     * Process incoming message and execute appropriate action
     */
    protected function processMessage(string $from, string $message): void
    {
        Log::info('Processing WhatsApp message', [
            'from' => $from,
            'message' => $message,
        ]);

        // Parse command from message
        $command = $this->extractCommand($message);

        switch ($command) {
            case 'menu':
                $this->handleMenuCommand($from);
                break;

            case 'order':
                $this->handleOrderCommand($from, $message);
                break;

            case 'help':
                $this->whatsappService->sendHelpMessage($from);
                break;

            default:
                $this->whatsappService->sendHelpMessage($from);
                break;
        }
    }

    /**
     * Extract command from message text
     */
    protected function extractCommand(string $message): string
    {
        $words = explode(' ', $message);
        return $words[0] ?? 'help';
    }

    /**
     * Handle menu command - send menu to customer
     */
    protected function handleMenuCommand(string $from): void
    {
        $categories = MenuCategory::with(['menuItems' => function ($query) {
            $query->where('status', 'available')->orderBy('name');
        }])
            ->where('status', 'active')
            ->orderBy('display_order')
            ->get();

        if ($categories->isEmpty()) {
            $this->whatsappService->sendErrorMessage($from, 'Menu is currently unavailable. Please try again later.');
            return;
        }

        $this->whatsappService->sendMenu($from, $categories);
    }

    /**
     * Handle order command - process customer order
     */
    protected function handleOrderCommand(string $from, string $message): void
    {
        try {
            // Remove 'order' command from message
            $orderText = trim(str_replace('order', '', $message));

            if (empty($orderText)) {
                $this->whatsappService->sendErrorMessage($from, 'Please specify items to order. Example: order 2x Burger, 1x Fries');
                return;
            }

            // Parse order items from text
            $parsedItems = $this->parseOrderItems($orderText);

            if (empty($parsedItems)) {
                $this->whatsappService->sendErrorMessage($from, 'Could not understand your order. Please use format: order 2x Burger, 1x Fries');
                return;
            }

            // Find or create guest by phone number
            $guest = $this->findOrCreateGuest($from);

            // Create order
            $order = $this->createOrder($guest, $parsedItems);

            if ($order) {
                // Calculate estimated time based on items
                $estimatedMinutes = $this->calculateEstimatedTime($order);

                // Send confirmation
                $this->whatsappService->sendOrderConfirmation($from, $order->order_number, $estimatedMinutes);
            } else {
                $this->whatsappService->sendErrorMessage($from, 'Failed to create order. Please try again.');
            }
        } catch (\Exception $e) {
            Log::error('Order processing error', [
                'from' => $from,
                'message' => $message,
                'error' => $e->getMessage(),
            ]);

            $this->whatsappService->sendErrorMessage($from, 'An error occurred while processing your order. Please try again.');
        }
    }

    /**
     * Parse order items from text
     * Expected format: "2x Burger, 1x Fries" or "Burger, 2 Fries"
     */
    protected function parseOrderItems(string $orderText): array
    {
        $items = [];
        $parts = explode(',', $orderText);

        foreach ($parts as $part) {
            $part = trim($part);

            // Try to extract quantity and item name
            // Patterns: "2x Burger", "2 Burger", "Burger"
            if (preg_match('/^(\d+)\s*x?\s*(.+)$/i', $part, $matches)) {
                $quantity = (int)$matches[1];
                $itemName = trim($matches[2]);
            } else {
                $quantity = 1;
                $itemName = $part;
            }

            if (!empty($itemName)) {
                // Find menu item by name (case-insensitive partial match)
                $menuItem = MenuItem::where('status', 'available')
                    ->whereRaw('LOWER(name) LIKE ?', ['%' . strtolower($itemName) . '%'])
                    ->first();

                if ($menuItem) {
                    $items[] = [
                        'menu_item' => $menuItem,
                        'quantity' => $quantity,
                    ];
                } else {
                    Log::warning('Menu item not found', ['item_name' => $itemName]);
                }
            }
        }

        return $items;
    }

    /**
     * Find existing guest by phone number or create new one
     */
    protected function findOrCreateGuest(string $phoneNumber): Guest
    {
        $guest = Guest::where('phone_number', $phoneNumber)->first();

        if (!$guest) {
            $guest = Guest::create([
                'phone_number' => $phoneNumber,
                'name' => 'WhatsApp Customer',
                'loyalty_points' => 0,
            ]);

            Log::info('New guest created from WhatsApp', [
                'guest_id' => $guest->id,
                'phone_number' => $phoneNumber,
            ]);
        }

        return $guest;
    }

    /**
     * Create order with parsed items
     */
    protected function createOrder(Guest $guest, array $parsedItems): ?Order
    {
        return DB::transaction(function () use ($guest, $parsedItems) {
            // Create order
            $order = Order::create([
                'guest_id' => $guest->id,
                'order_source' => 'whatsapp',
                'status' => 'pending',
                'subtotal' => 0,
                'tax' => 0,
                'total' => 0,
            ]);

            // Add order items
            foreach ($parsedItems as $item) {
                OrderItem::create([
                    'order_id' => $order->id,
                    'menu_item_id' => $item['menu_item']->id,
                    'quantity' => $item['quantity'],
                    'unit_price' => $item['menu_item']->price,
                    'prep_status' => 'pending',
                ]);
            }

            // Calculate totals
            $order->calculateTotals();

            Log::info('Order created from WhatsApp', [
                'order_id' => $order->id,
                'order_number' => $order->order_number,
                'guest_id' => $guest->id,
                'total' => $order->total,
            ]);

            return $order->fresh();
        });
    }

    /**
     * Calculate estimated preparation time
     */
    protected function calculateEstimatedTime(Order $order): int
    {
        $maxPrepTime = $order->orderItems()
            ->join('menu_items', 'order_items.menu_item_id', '=', 'menu_items.id')
            ->max('menu_items.prep_time_minutes');

        return $maxPrepTime ?? 30;
    }

    /**
     * Send status update notification (called when order status changes)
     */
    public function notifyStatusChange(Order $order, string $newStatus): void
    {
        if ($order->order_source !== 'whatsapp') {
            return;
        }

        if (!$order->guest || !$order->guest->phone_number) {
            return;
        }

        // Only notify for specific status changes
        if (in_array($newStatus, ['preparing', 'ready', 'completed', 'cancelled'])) {
            $this->whatsappService->sendStatusUpdate(
                $order->guest->phone_number,
                $order->order_number,
                $newStatus
            );
        }
    }
}
