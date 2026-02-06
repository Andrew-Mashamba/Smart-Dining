<?php

use Illuminate\Support\Facades\Route;
use App\Http\Controllers\Web\AuthController;
use App\Http\Controllers\Web\ManagerController;
use App\Http\Controllers\Web\KitchenController;
use App\Http\Controllers\Web\BarController;
use App\Livewire\Dashboard;
use App\Livewire\Reports;
use App\Livewire\Users;
use App\Livewire\MenuManagement;
use App\Livewire\TableManagement;
use App\Livewire\CreateOrder;
use App\Livewire\OrdersList;

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

    // Reports Livewire component route (admin and manager only)
    Route::get('/reports', Reports::class)->middleware(['role:admin,manager'])->name('reports');

    // Menu Management Livewire component route
    Route::get('/menu', MenuManagement::class)->middleware(['auth', 'role:admin,manager'])->name('menu');

    // Table Management Livewire component route (admin and manager only)
    Route::get('/tables', TableManagement::class)->middleware(['auth', 'role:admin,manager'])->name('tables');

    // Orders List Livewire component route (authenticated users)
    Route::get('/orders', OrdersList::class)->middleware(['auth'])->name('orders');

    // Create Order Livewire component route (waiter, manager, and admin access)
    Route::get('/orders/create', CreateOrder::class)->middleware(['auth', 'role:waiter,manager,admin'])->name('orders.create');

    // Manager Portal (admin and manager access)
    Route::middleware(['role:admin,manager'])->prefix('manager')->name('manager.')->group(function () {
        Route::get('/dashboard', [ManagerController::class, 'dashboard'])->name('dashboard');
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
