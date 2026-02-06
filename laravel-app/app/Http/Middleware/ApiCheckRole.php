<?php

namespace App\Http\Middleware;

use Closure;
use Illuminate\Http\Request;
use Symfony\Component\HttpFoundation\Response;

/**
 * API-specific role checking middleware
 *
 * This middleware extends CheckRole functionality for API routes,
 * returning JSON responses instead of redirects.
 */
class ApiCheckRole extends CheckRole
{
    /**
     * Handle an incoming API request with role-based authorization.
     *
     * @param  \Closure(\Illuminate\Http\Request): (\Symfony\Component\HttpFoundation\Response)  $next
     * @param  string  ...$roles
     */
    public function handle(Request $request, Closure $next, ...$roles): Response
    {
        // Check if user is authenticated
        if (!$request->user()) {
            return response()->json([
                'message' => 'Unauthenticated.',
            ], 401);
        }

        // Check if user has one of the required roles
        if (!in_array($request->user()->role, $roles)) {
            return response()->json([
                'message' => 'Insufficient permissions',
            ], 403);
        }

        return $next($request);
    }
}
