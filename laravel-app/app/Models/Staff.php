<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Foundation\Auth\User as Authenticatable;
use Illuminate\Database\Eloquent\Relations\HasMany;
use Illuminate\Notifications\Notifiable;
use Laravel\Sanctum\HasApiTokens;

class Staff extends Authenticatable
{
    use HasFactory, Notifiable, HasApiTokens;

    /**
     * The attributes that are mass assignable.
     *
     * @var array<int, string>
     */
    protected $fillable = [
        'name',
        'email',
        'password',
        'role',
        'phone_number',
        'status',
    ];

    /**
     * The attributes that should be hidden for serialization.
     *
     * @var array<int, string>
     */
    protected $hidden = [
        'password',
        'remember_token',
    ];

    /**
     * The attributes that should be cast.
     *
     * @var array<string, string>
     */
    protected $casts = [
        'password' => 'hashed',
        'role' => 'string',
        'status' => 'string',
    ];

    /**
     * Get orders assigned to this waiter.
     */
    public function orders(): HasMany
    {
        return $this->hasMany(Order::class, 'waiter_id');
    }

    /**
     * Get order items prepared by this staff.
     */
    public function preparedItems(): HasMany
    {
        return $this->hasMany(OrderItem::class, 'prepared_by');
    }

    /**
     * Get tips received by this waiter.
     */
    public function tips(): HasMany
    {
        return $this->hasMany(Tip::class, 'waiter_id');
    }

    /**
     * Get all inventory transactions created by this staff member.
     */
    public function inventoryTransactions(): HasMany
    {
        return $this->hasMany(InventoryTransaction::class, 'created_by');
    }

    /**
     * Check if staff is a waiter.
     */
    public function isWaiter(): bool
    {
        return $this->role === 'waiter';
    }

    /**
     * Check if staff is a chef.
     */
    public function isChef(): bool
    {
        return $this->role === 'chef';
    }

    /**
     * Check if staff is a bartender.
     */
    public function isBartender(): bool
    {
        return $this->role === 'bartender';
    }

    /**
     * Check if staff is a manager.
     */
    public function isManager(): bool
    {
        return $this->role === 'manager';
    }

    /**
     * Check if staff is an admin.
     */
    public function isAdmin(): bool
    {
        return $this->role === 'admin';
    }

    /**
     * Check if staff has a specific role.
     *
     * @param string $role
     * @return bool
     */
    public function hasRole(string $role): bool
    {
        return $this->role === $role;
    }
}
