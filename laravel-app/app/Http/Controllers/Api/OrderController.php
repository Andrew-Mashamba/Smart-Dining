<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Http\Requests\StoreOrderRequest;
use App\Http\Requests\UpdateOrderStatusRequest;
use App\Models\Order;
use App\Services\OrderManagement\OrderService;
use App\Services\OrderManagement\OrderDistributionService;
use App\Events\OrderCreated;
use App\Events\OrderStatusChanged;
use Illuminate\Http\Request;
use Barryvdh\DomPDF\Facade\Pdf;

class OrderController extends Controller
{
    protected OrderService $orderService;
    protected OrderDistributionService $distributionService;

    public function __construct(OrderService $orderService, OrderDistributionService $distributionService)
    {
        $this->orderService = $orderService;
        $this->distributionService = $distributionService;
    }

    /**
     * Get all orders with filters
     */
    public function index(Request $request)
    {
        $query = Order::with(['items.menuItem', 'guest', 'table', 'waiter']);

        if ($request->has('status')) {
            $query->where('status', $request->status);
        }

        if ($request->has('table_id')) {
            $query->where('table_id', $request->table_id);
        }

        if ($request->has('waiter_id')) {
            $query->where('waiter_id', $request->waiter_id);
        }

        if ($request->has('date')) {
            $query->whereDate('created_at', $request->date);
        }

        $orders = $query->orderBy('created_at', 'desc')->paginate(20);

        return response()->json($orders);
    }

    /**
     * Create a new order
     */
    public function store(StoreOrderRequest $request)
    {
        $validated = $request->validated();

        $order = $this->orderService->createOrder($validated);

        $this->distributionService->distributeOrder($order);

        event(new OrderCreated($order));

        return response()->json([
            'message' => 'Order created successfully',
            'order' => $this->orderService->getOrderSummary($order),
        ], 201);
    }

    /**
     * Get a specific order
     */
    public function show($id)
    {
        $order = Order::with(['items.menuItem', 'guest', 'table', 'waiter', 'payments'])->findOrFail($id);

        return response()->json($this->orderService->getOrderSummary($order));
    }

    /**
     * Update order status
     */
    public function updateStatus(UpdateOrderStatusRequest $request, $id)
    {
        $validated = $request->validated();

        $order = Order::findOrFail($id);
        $previousStatus = $order->status;

        $this->orderService->updateOrderStatus($order, $validated['status']);

        event(new OrderStatusChanged($order, $previousStatus));

        return response()->json([
            'message' => 'Order status updated successfully',
            'order' => $this->orderService->getOrderSummary($order),
        ]);
    }

    /**
     * Add items to an existing order
     */
    public function addItems(Request $request, $id)
    {
        $request->validate([
            'items' => 'required|array|min:1',
            'items.*.menu_item_id' => 'required|exists:menu_items,id',
            'items.*.quantity' => 'required|integer|min:1',
            'items.*.special_instructions' => 'nullable|string',
        ]);

        $order = Order::findOrFail($id);

        if (!in_array($order->status, ['pending', 'confirmed'])) {
            return response()->json([
                'message' => 'Cannot add items to an order that is already being prepared',
            ], 422);
        }

        $this->orderService->addItems($order, $request->items);

        return response()->json([
            'message' => 'Items added successfully',
            'order' => $this->orderService->getOrderSummary($order),
        ]);
    }

    /**
     * Mark order as served
     */
    public function markAsServed($id)
    {
        $order = Order::findOrFail($id);

        if ($order->status !== 'ready') {
            return response()->json([
                'message' => 'Order must be ready before marking as served',
            ], 422);
        }

        $previousStatus = $order->status;
        $this->orderService->updateOrderStatus($order, 'served');

        event(new OrderStatusChanged($order, $previousStatus));

        return response()->json([
            'message' => 'Order marked as served',
            'order' => $this->orderService->getOrderSummary($order),
        ]);
    }

    /**
     * Cancel an order
     */
    public function cancel(Request $request, $id)
    {
        $request->validate([
            'reason' => 'required|string',
        ]);

        $order = Order::findOrFail($id);

        $this->orderService->cancelOrder($order, $request->reason);

        return response()->json([
            'message' => 'Order cancelled successfully',
        ]);
    }

    /**
     * Generate and download receipt PDF for an order
     */
    public function generateReceipt($orderId)
    {
        // Load order with all necessary relationships
        $order = Order::with([
            'orderItems.menuItem',
            'table',
            'waiter',
            'payments',
            'tip'
        ])->findOrFail($orderId);

        // Generate PDF from the receipt blade template
        $pdf = Pdf::loadView('receipts.order-receipt', compact('order'));

        // Set PDF options for thermal printer compatibility
        $pdf->setPaper([0, 0, 226.77, 841.89], 'portrait'); // 80mm width
        $pdf->setOption('isHtml5ParserEnabled', true);
        $pdf->setOption('isRemoteEnabled', true);

        // Return PDF download with filename based on order number
        return $pdf->download('receipt-' . $order->order_number . '.pdf');
    }
}
