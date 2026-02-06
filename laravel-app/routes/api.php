<?php

use Illuminate\Http\Request;
use Illuminate\Support\Facades\Route;
use App\Http\Controllers\Api\AuthController;
use App\Http\Controllers\Api\OrderController;
use App\Http\Controllers\Api\MenuController;
use App\Http\Controllers\Api\TableController;
use App\Http\Controllers\Api\OrderItemController;
use App\Http\Controllers\Api\PaymentController;
use App\Http\Controllers\Api\TipController;
use App\Http\Controllers\Api\GuestController;

// Authentication routes
Route::post('auth/login', [AuthController::class, 'login']);
Route::middleware('auth:sanctum')->group(function () {
    Route::post('auth/logout', [AuthController::class, 'logout']);
    Route::post('auth/refresh', [AuthController::class, 'refresh']);
    Route::get('auth/me', [AuthController::class, 'me']);
});

// Public menu routes
Route::get('menu', [MenuController::class, 'index']);
Route::get('menu/items', [MenuController::class, 'items']);
Route::get('menu/categories', [MenuController::class, 'categories']);
Route::get('menu/popular', [MenuController::class, 'popular']);
Route::get('menu/search', [MenuController::class, 'search']);
Route::get('menu/{id}', [MenuController::class, 'show']);

// Protected routes (require authentication)
Route::middleware('auth:sanctum')->group(function () {

    // Orders
    Route::get('orders', [OrderController::class, 'index']);
    Route::post('orders', [OrderController::class, 'store']);
    Route::get('orders/{id}', [OrderController::class, 'show']);
    Route::patch('orders/{id}/status', [OrderController::class, 'updateStatus']);
    Route::post('orders/{id}/items', [OrderController::class, 'addItems']);
    Route::post('orders/{id}/serve', [OrderController::class, 'markAsServed']);
    Route::post('orders/{id}/cancel', [OrderController::class, 'cancel']);
    Route::get('orders/{id}/receipt', [OrderController::class, 'generateReceipt']);

    // Tables
    Route::get('tables', [TableController::class, 'index']);
    Route::get('tables/{id}', [TableController::class, 'show']);
    Route::patch('tables/{id}/status', [TableController::class, 'updateStatus']);

    // Order Items (for kitchen/bar)
    Route::get('order-items/pending', [OrderItemController::class, 'pending']);
    Route::post('order-items/{id}/received', [OrderItemController::class, 'markReceived']);
    Route::post('order-items/{id}/done', [OrderItemController::class, 'markDone']);

    // Payments
    Route::get('payments', [PaymentController::class, 'index']);
    Route::post('payments', [PaymentController::class, 'store']);
    Route::get('payments/{id}', [PaymentController::class, 'show']);
    Route::post('payments/{id}/confirm', [PaymentController::class, 'confirm']);
    Route::get('orders/{orderId}/bill', [PaymentController::class, 'getBill']);

    // Stripe payment
    Route::post('payments/stripe/create-intent', [App\Http\Controllers\StripePaymentController::class, 'createIntent']);
    Route::post('payments/stripe/confirm', [App\Http\Controllers\StripePaymentController::class, 'confirm']);

    // Tips
    Route::post('tips', [TipController::class, 'store']);
    Route::get('orders/{orderId}/tip-suggestions', [TipController::class, 'suggestions']);

    // Guests
    Route::get('guests/phone/{phone}', [GuestController::class, 'findByPhone']);
    Route::post('guests', [GuestController::class, 'store']);

    // Menu management (admin/manager only)
    Route::put('menu/{id}/availability', [MenuController::class, 'updateAvailability']);
    Route::get('menu/stats', [MenuController::class, 'stats']);

    // QR Codes (admin/manager only)
    Route::get('qr-codes/tables/{tableId}', [App\Http\Controllers\WhatsApp\QRCodeController::class, 'show']);
    Route::post('qr-codes/tables/{tableId}/generate', [App\Http\Controllers\WhatsApp\QRCodeController::class, 'generate']);
    Route::post('qr-codes/generate-all', [App\Http\Controllers\WhatsApp\QRCodeController::class, 'generateAll']);
});

// WhatsApp webhook routes (no authentication required)
Route::prefix('webhooks')->group(function () {
    Route::get('whatsapp', [App\Http\Controllers\WhatsApp\WebhookController::class, 'verify']);
    Route::post('whatsapp', [App\Http\Controllers\WhatsApp\WebhookController::class, 'handle']);

    // Stripe webhook (signature verification handled in controller)
    Route::post('stripe', [App\Http\Controllers\Api\StripeWebhookController::class, 'handle']);
});
