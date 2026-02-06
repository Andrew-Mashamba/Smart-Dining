<?php

use Illuminate\Support\Facades\Route;
use App\Http\Controllers\Web\AuthController;
use App\Http\Controllers\Web\ManagerController;
use App\Http\Controllers\Web\KitchenController;
use App\Http\Controllers\Web\BarController;
use App\Livewire\Dashboard;
use App\Livewire\Reports;
use App\Livewire\Users;
use App\Livewire\StaffManagement;
use App\Livewire\MenuManagement;
use App\Livewire\TableManagement;
use App\Livewire\CreateOrder;
use App\Livewire\OrdersList;
use App\Livewire\OrderDetails;
use App\Livewire\ProcessPayment;
use App\Livewire\KitchenDisplay;
use App\Livewire\BarDisplay;
use App\Livewire\GuestManagement;
use App\Livewire\InventoryManagement;
use App\Http\Controllers\GuestOrderController;

// Guest ordering route (public access via QR code)
Route::get('/guest/order', [GuestOrderController::class, 'index'])->name('guest.order');

// Root route: Redirect guests to login, authenticated users to dashboard
Route::get('/', function () {
    if (auth()->check()) {
        return redirect('/dashboard');
    }
    return redirect()->route('login');
})->name('home');

// Authentication routes
Route::get('/login', [AuthController::class, 'showLogin'])->name('login');
Route::post('/login', [AuthController::class, 'login']);
Route::post('/logout', [AuthController::class, 'logout'])->name('logout');

// Protected routes
Route::middleware(['auth:web'])->group(function () {

    // Dashboard Livewire component route
    Route::get('/dashboard', Dashboard::class)->name('dashboard');

    // Users Livewire component route (admin and manager only)
    Route::get('/users', Users::class)->middleware(['role:admin,manager'])->name('users');

    // Staff Management Livewire component route (admin and manager only)
    Route::get('/staff', StaffManagement::class)->middleware(['auth', 'role:admin,manager'])->name('staff');

    // Reports Livewire component route (admin and manager only)
    Route::get('/reports', Reports::class)->middleware(['role:admin,manager'])->name('reports');

    // Menu Management Livewire component route
    Route::get('/menu', MenuManagement::class)->middleware(['auth', 'role:admin,manager'])->name('menu');

    // Inventory Management Livewire component route (admin and manager only)
    Route::get('/inventory', InventoryManagement::class)->middleware(['auth', 'role:manager,admin'])->name('inventory');

    // Table Management Livewire component route (admin and manager only)
    Route::get('/tables', TableManagement::class)->middleware(['auth', 'role:admin,manager'])->name('tables');

    // Guest Management Livewire component route (admin and manager only)
    Route::get('/guests', GuestManagement::class)->middleware(['auth', 'role:manager,admin'])->name('guests');

    // Orders List Livewire component route (authenticated users)
    Route::get('/orders', OrdersList::class)->middleware(['auth'])->name('orders');

    // Create Order Livewire component route (waiter, manager, and admin access)
    Route::get('/orders/create', CreateOrder::class)->middleware(['auth', 'role:waiter,manager,admin'])->name('orders.create');

    // Order Details Livewire component route (authenticated users)
    Route::get('/orders/{order}', OrderDetails::class)->middleware(['auth'])->name('orders.show');

    // Process Payment Livewire component route (authenticated users)
    Route::get('/orders/{order}/payment', ProcessPayment::class)->middleware(['auth'])->name('orders.payment');

    // Kitchen Display System Livewire component route (chef, manager, and admin access)
    Route::get('/kitchen', KitchenDisplay::class)->middleware(['auth', 'role:chef,manager,admin'])->name('kitchen');

    // Bar Display System Livewire component route (bartender, manager, and admin access)
    Route::get('/bar', BarDisplay::class)->middleware(['auth', 'role:bartender,manager,admin'])->name('bar');

    // Manager Portal (admin and manager access)
    Route::middleware(['role:admin,manager'])->prefix('manager')->name('manager.')->group(function () {
        Route::get('/dashboard', [ManagerController::class, 'dashboard'])->name('dashboard');
        Route::get('/orders/{orderId}/receipt', [ManagerController::class, 'generateReceipt'])->name('orders.receipt');
    });

    // Kitchen Display System (chef access)
    Route::middleware(['role:chef'])->prefix('kitchen')->name('kitchen.')->group(function () {
        Route::get('/display', [KitchenController::class, 'display'])->name('display');
        Route::post('/items/{id}/received', [KitchenController::class, 'markReceived'])->name('mark-received');
        Route::post('/items/{id}/done', [KitchenController::class, 'markDone'])->name('mark-done');
    });

    // Bar Display System (bartender access)
    Route::middleware(['role:bartender'])->prefix('bar')->name('bar.')->group(function () {
        Route::get('/display', [BarController::class, 'display'])->name('display');
        Route::post('/items/{id}/received', [BarController::class, 'markReceived'])->name('mark-received');
        Route::post('/items/{id}/done', [BarController::class, 'markDone'])->name('mark-done');
    });
});
