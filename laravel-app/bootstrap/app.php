<?php

use Illuminate\Foundation\Application;
use Illuminate\Foundation\Configuration\Exceptions;
use Illuminate\Foundation\Configuration\Middleware;

return Application::configure(basePath: dirname(__DIR__))
    ->withRouting(
        web: __DIR__.'/../routes/web.php',
        api: __DIR__.'/../routes/api.php',
        commands: __DIR__.'/../routes/console.php',
        channels: __DIR__.'/../routes/channels.php',
        health: '/up',
        // Set HOME route for authenticated users
        then: function () {
            // This sets the default home route after login
            config(['app.home' => '/dashboard']);
        }
    )
    ->withMiddleware(function (Middleware $middleware): void {
        $middleware->alias([
            'role' => \App\Http\Middleware\CheckRole::class,
            'api.role' => \App\Http\Middleware\ApiCheckRole::class,
        ]);

        // Redirect authenticated users trying to access guest routes to /dashboard
        $middleware->redirectGuestsTo('/login');
        $middleware->redirectUsersTo('/dashboard');

        // Exempt WhatsApp and Stripe webhooks from CSRF protection
        $middleware->validateCsrfTokens(except: [
            'webhooks/whatsapp',
            'webhooks/stripe',
            'api/webhooks/stripe',
        ]);

        // API rate limiting: 60 requests per minute
        $middleware->throttleApi('60,1');
    })
    ->withExceptions(function (Exceptions $exceptions): void {
        //
    })->create();
