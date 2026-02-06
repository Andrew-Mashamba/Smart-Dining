<?php

namespace App\Services;

use Netflie\WhatsAppCloudApi\WhatsAppCloudApi;
use Netflie\WhatsAppCloudApi\Message\TextMessage;
use Illuminate\Support\Facades\Log;

class WhatsAppService
{
    protected WhatsAppCloudApi $whatsapp;
    protected string $phoneNumberId;

    public function __construct()
    {
        $this->phoneNumberId = config('services.whatsapp.phone_number_id');
        $this->whatsapp = new WhatsAppCloudApi([
            'from_phone_number_id' => $this->phoneNumberId,
            'access_token' => config('services.whatsapp.api_token'),
        ]);
    }

    /**
     * Send a text message to a WhatsApp number
     */
    public function sendMessage(string $to, string $message): bool
    {
        try {
            $textMessage = new TextMessage($to, $message);
            $response = $this->whatsapp->sendTextMessage($textMessage);

            Log::info('WhatsApp message sent', [
                'to' => $to,
                'message' => $message,
                'response' => $response,
            ]);

            return true;
        } catch (\Exception $e) {
            Log::error('Failed to send WhatsApp message', [
                'to' => $to,
                'message' => $message,
                'error' => $e->getMessage(),
            ]);

            return false;
        }
    }

    /**
     * Send formatted menu to customer
     */
    public function sendMenu(string $to, $categories): bool
    {
        $menuText = "🍽️ *Our Menu*\n\n";

        foreach ($categories as $category) {
            $menuText .= "*{$category->name}*\n";
            if ($category->description) {
                $menuText .= "_{$category->description}_\n";
            }
            $menuText .= "\n";

            foreach ($category->menuItems as $item) {
                if ($item->status === 'available') {
                    $menuText .= "• {$item->name} - ${$item->price}\n";
                    if ($item->description) {
                        $menuText .= "  _{$item->description}_\n";
                    }
                }
            }
            $menuText .= "\n";
        }

        $menuText .= "To order, reply with:\n";
        $menuText .= "*order* followed by item names and quantities\n";
        $menuText .= "Example: order 2x Burger, 1x Fries";

        return $this->sendMessage($to, $menuText);
    }

    /**
     * Send order confirmation
     */
    public function sendOrderConfirmation(string $to, string $orderNumber, int $estimatedMinutes = 30): bool
    {
        $message = "✅ *Order Confirmed!*\n\n";
        $message .= "Order Number: *{$orderNumber}*\n";
        $message .= "Estimated Time: *{$estimatedMinutes} minutes*\n\n";
        $message .= "We'll notify you when your order is ready!\n";
        $message .= "Thank you for your order! 🙏";

        return $this->sendMessage($to, $message);
    }

    /**
     * Send order status update
     */
    public function sendStatusUpdate(string $to, string $orderNumber, string $status): bool
    {
        $statusMessages = [
            'preparing' => "👨‍🍳 Your order *{$orderNumber}* is being prepared!",
            'ready' => "✨ Your order *{$orderNumber}* is ready for pickup/delivery!",
            'completed' => "✅ Order *{$orderNumber}* completed. Thank you!",
            'cancelled' => "❌ Order *{$orderNumber}* has been cancelled.",
        ];

        $message = $statusMessages[$status] ?? "Order *{$orderNumber}* status: {$status}";

        return $this->sendMessage($to, $message);
    }

    /**
     * Send help message
     */
    public function sendHelpMessage(string $to): bool
    {
        $message = "🤖 *How to use our WhatsApp ordering*\n\n";
        $message .= "Available commands:\n\n";
        $message .= "*menu* - View our full menu\n";
        $message .= "*order* - Place an order\n";
        $message .= "  Example: order 2x Burger, 1x Fries\n\n";
        $message .= "*help* - Show this message\n\n";
        $message .= "Need assistance? Just ask! 😊";

        return $this->sendMessage($to, $message);
    }

    /**
     * Send error message
     */
    public function sendErrorMessage(string $to, string $error): bool
    {
        $message = "❌ *Error*\n\n{$error}\n\nType *help* for assistance.";

        return $this->sendMessage($to, $message);
    }
}
