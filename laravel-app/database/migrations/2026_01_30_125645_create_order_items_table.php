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
        Schema::create('order_items', function (Blueprint $table) {
            $table->id();
            $table->foreignId('order_id')->constrained('orders')->onDelete('cascade');
            $table->foreignId('menu_item_id')->constrained('menu_items')->onDelete('restrict');
            $table->integer('quantity')->default(1);
            $table->decimal('unit_price', 10, 2);
            $table->decimal('subtotal', 10, 2);
            $table->enum('status', ['pending', 'received', 'preparing', 'done'])->default('pending');
            $table->text('notes')->nullable()->comment('Special instructions (e.g., no chili)');
            $table->foreignId('prepared_by')->nullable()->constrained('staff')->onDelete('set null');
            $table->timestamp('prepared_at')->nullable();
            $table->timestamps();

            // Indexes
            $table->index('order_id');
            $table->index('menu_item_id');
            $table->index('status');
            $table->index('prepared_by');
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('order_items');
    }
};
