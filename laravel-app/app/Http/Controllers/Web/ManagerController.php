<?php

namespace App\Http\Controllers\Web;

use App\Http\Controllers\Controller;
use App\Models\Order;
use App\Models\Table;
use App\Models\Staff;
use App\Models\Payment;
use Carbon\Carbon;

class ManagerController extends Controller
{
    public function dashboard()
    {
        $activeOrders = Order::whereNotIn('status', ['completed', 'cancelled'])->count();

        $todayRevenue = Payment::whereDate('created_at', Carbon::today())
            ->where('status', 'completed')
            ->sum('amount');

        $occupiedTables = Table::where('status', 'occupied')->count();
        $totalTables = Table::count();

        $staffOnDuty = Staff::where('status', 'active')->count();

        $recentOrders = Order::with(['guest', 'table', 'waiter'])
            ->orderBy('created_at', 'desc')
            ->limit(10)
            ->get();

        return view('manager.dashboard', compact(
            'activeOrders',
            'todayRevenue',
            'occupiedTables',
            'totalTables',
            'staffOnDuty',
            'recentOrders'
        ));
    }
}
