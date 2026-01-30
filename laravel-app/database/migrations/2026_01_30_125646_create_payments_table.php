<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    /**
     * Run the migrations.
     */
    public function up(): void
    {
        Schema::create('payments', function (Blueprint $table) {
            $table->id();
            $table->foreignId('order_id')->constrained('orders')->onDelete('cascade');
            $table->decimal('amount', 10, 2);
            $table->enum('method', ['cash', 'card', 'mpesa', 'pesapal', 'bank_transfer'])->default('cash');
            $table->enum('status', [
                'pending',
                'processing',
                'completed',
                'failed',
                'cancelled',
                'refunded'
            ])->default('pending');
            $table->string('transaction_id')->nullable()->comment('External payment gateway transaction ID');
            $table->json('gateway_response')->nullable()->comment('Full response from payment gateway');
            $table->timestamp('paid_at')->nullable();
            $table->timestamps();

            // Indexes
            $table->index('order_id');
            $table->index('status');
            $table->index('transaction_id');
            $table->index('method');
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('payments');
    }
};
