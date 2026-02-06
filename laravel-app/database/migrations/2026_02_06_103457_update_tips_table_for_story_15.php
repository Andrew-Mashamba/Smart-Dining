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
        Schema::table('tips', function (Blueprint $table) {
            // Drop the old columns and indexes
            $table->dropForeign(['payment_id']);
            $table->dropIndex(['payment_id']);
            $table->dropIndex(['order_id']);
            $table->dropColumn(['payment_id']);

            // Rename method to tip_method and update enum values
            $table->dropColumn('method');
        });

        Schema::table('tips', function (Blueprint $table) {
            // Add tip_method column with correct enum values
            $table->enum('tip_method', ['cash', 'card'])->after('amount');

            // Update amount precision from 10,2 to 8,2
            $table->decimal('amount', 8, 2)->change();
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::table('tips', function (Blueprint $table) {
            // Reverse the changes
            $table->dropColumn('tip_method');
        });

        Schema::table('tips', function (Blueprint $table) {
            $table->enum('method', ['cash', 'digital'])->default('cash')->after('amount');
            $table->decimal('amount', 10, 2)->change();
            $table->foreignId('payment_id')->nullable()->constrained('payments')->onDelete('set null');
            $table->index('order_id');
            $table->index('payment_id');
        });
    }
};
