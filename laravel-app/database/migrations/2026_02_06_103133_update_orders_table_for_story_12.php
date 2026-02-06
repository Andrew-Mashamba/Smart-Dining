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
        Schema::table('orders', function (Blueprint $table) {
            // Drop old columns
            $table->dropColumn(['session_id', 'service_charge', 'total_amount', 'notes']);

            // Add new columns
            $table->string('order_number')->unique()->after('id');
            $table->decimal('total', 10, 2)->after('tax');
            $table->text('special_instructions')->nullable()->after('total');

            // Modify existing columns to be nullable
            $table->foreignId('table_id')->nullable()->change();
            $table->foreignId('guest_id')->nullable()->change();

            // Update status enum values
            $table->dropColumn('status');
        });

        Schema::table('orders', function (Blueprint $table) {
            $table->enum('status', ['pending', 'preparing', 'ready', 'delivered', 'paid', 'cancelled'])->default('pending')->after('order_source');
        });

        // Drop old indexes
        Schema::table('orders', function (Blueprint $table) {
            $table->dropIndex(['guest_id']);
            $table->dropIndex(['table_id']);
            $table->dropIndex(['waiter_id']);
            $table->dropIndex(['order_source']);

            // Add new index
            $table->index('order_number');
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::table('orders', function (Blueprint $table) {
            // Restore old columns
            $table->dropColumn(['order_number', 'total', 'special_instructions']);
            $table->foreignId('session_id')->nullable()->after('waiter_id');
            $table->decimal('service_charge', 10, 2)->default(0)->after('tax');
            $table->decimal('total_amount', 10, 2)->default(0)->after('service_charge');
            $table->text('notes')->nullable()->after('total_amount');

            // Restore status enum
            $table->dropColumn('status');
        });

        Schema::table('orders', function (Blueprint $table) {
            $table->enum('status', ['pending', 'confirmed', 'preparing', 'ready', 'served', 'completed', 'cancelled'])->default('pending')->after('session_id');

            // Restore indexes
            $table->index('guest_id');
            $table->index('table_id');
            $table->index('waiter_id');
            $table->index('order_source');
            $table->dropIndex(['order_number']);
        });
    }
};
