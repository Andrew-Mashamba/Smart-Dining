<?php

use Illuminate\Support\Facades\Route;
use App\Http\Controllers\Web\AuthController;
use App\Http\Controllers\Web\ManagerController;
use App\Http\Controllers\Web\KitchenController;
use App\Http\Controllers\Web\BarController;

// Redirect root to login
Route::get('/', function () {
    return redirect()->route('login');
});

// Authentication routes
Route::get('/login', [AuthController::class, 'showLogin'])->name('login');
Route::post('/login', [AuthController::class, 'login']);
Route::post('/logout', [AuthController::class, 'logout'])->name('logout');

// Protected routes
Route::middleware(['auth:web'])->group(function () {

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
