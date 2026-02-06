<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Http\Requests\LoginRequest;
use App\Models\Staff;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Hash;
use Illuminate\Validation\ValidationException;

class AuthController extends Controller
{
    /**
     * Login staff member and generate API token
     */
    public function login(LoginRequest $request)
    {
        $validated = $request->validated();

        $staff = Staff::where('email', $validated['email'])->first();

        if (!$staff || !Hash::check($validated['password'], $staff->password)) {
            throw ValidationException::withMessages([
                'email' => ['The provided credentials are incorrect.'],
            ]);
        }

        if ($staff->status !== 'active') {
            return response()->json([
                'message' => 'Your account is inactive. Please contact the administrator.',
            ], 403);
        }

        $staff->tokens()->delete();

        $abilities = $this->getAbilitiesByRole($staff->role);
        $token = $staff->createToken(
            $validated['device_name'] ?? 'default',
            $abilities
        )->plainTextToken;

        return response()->json([
            'message' => 'Login successful',
            'token' => $token,
            'user' => [
                'id' => $staff->id,
                'name' => $staff->name,
                'email' => $staff->email,
                'role' => $staff->role,
                'phone_number' => $staff->phone_number,
            ],
        ]);
    }

    /**
     * Logout staff member and revoke token
     */
    public function logout(Request $request)
    {
        $request->user()->currentAccessToken()->delete();

        return response()->json([
            'message' => 'Logged out successfully',
        ]);
    }

    /**
     * Refresh token by generating a new one
     */
    public function refresh(Request $request)
    {
        $staff = $request->user();
        $request->user()->currentAccessToken()->delete();

        $abilities = $this->getAbilitiesByRole($staff->role);
        $token = $staff->createToken('refreshed-token', $abilities)->plainTextToken;

        return response()->json([
            'message' => 'Token refreshed successfully',
            'token' => $token,
        ]);
    }

    /**
     * Get current authenticated user
     */
    public function me(Request $request)
    {
        return response()->json([
            'user' => [
                'id' => $request->user()->id,
                'name' => $request->user()->name,
                'email' => $request->user()->email,
                'role' => $request->user()->role,
                'phone_number' => $request->user()->phone_number,
            ],
        ]);
    }

    /**
     * Get token abilities based on staff role
     */
    protected function getAbilitiesByRole(string $role): array
    {
        $abilities = [
            'admin' => ['*'],
            'manager' => ['*'],
            'waiter' => ['orders:create', 'orders:view', 'orders:update', 'tables:view', 'payments:create', 'tips:create'],
            'chef' => ['orders:view', 'order-items:update'],
            'bartender' => ['orders:view', 'order-items:update'],
        ];

        return $abilities[$role] ?? [];
    }
}
