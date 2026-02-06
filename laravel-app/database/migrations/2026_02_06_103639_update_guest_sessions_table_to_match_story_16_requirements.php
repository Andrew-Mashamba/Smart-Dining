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
        Schema::table('guest_sessions', function (Blueprint $table) {
            // Drop existing foreign keys
            $table->dropForeign(['guest_id']);
            $table->dropForeign(['table_id']);

            // Drop status index first before dropping the column
            $table->dropIndex(['status']);

            // Drop status column as it's not in acceptance criteria
            $table->dropColumn('status');
        });

        Schema::table('guest_sessions', function (Blueprint $table) {
            // Make guest_id nullable and table_id not nullable
            $table->foreignId('guest_id')->nullable()->change()->constrained('guests')->onDelete('set null');
            $table->foreignId('table_id')->nullable(false)->change()->constrained('tables')->onDelete('cascade');

            // Update session_token to have length of 32
            $table->string('session_token', 32)->change();
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::table('guest_sessions', function (Blueprint $table) {
            // Reverse the changes
            $table->dropForeign(['guest_id']);
            $table->dropForeign(['table_id']);

            $table->foreignId('guest_id')->nullable(false)->change()->constrained('guests')->onDelete('cascade');
            $table->foreignId('table_id')->nullable()->change()->constrained('tables')->onDelete('set null');

            $table->string('session_token')->change();

            $table->enum('status', ['active', 'closed'])->default('active')->after('session_token');
        });
    }
};
