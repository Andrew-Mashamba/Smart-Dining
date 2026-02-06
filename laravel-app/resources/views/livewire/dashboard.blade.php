<div>
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        {{-- Dashboard Heading --}}
        <div class="mb-6">
            <h1 class="text-lg font-bold text-gray-900">Dashboard Overview</h1>
        </div>

        {{-- Metrics Grid --}}
        <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
            {{-- Total Orders Card --}}
            <div class="bg-white shadow-sm rounded-lg p-6">
                <div class="text-gray-600 text-sm mb-2">Total Orders</div>
                <div class="text-gray-900 text-2xl font-semibold">{{ $total_orders }}</div>
            </div>

            {{-- Revenue Card --}}
            <div class="bg-white shadow-sm rounded-lg p-6">
                <div class="text-gray-600 text-sm mb-2">Revenue</div>
                <div class="text-gray-900 text-2xl font-semibold">TZS {{ number_format($revenue, 0) }}</div>
            </div>

            {{-- Active Tables Card --}}
            <div class="bg-white shadow-sm rounded-lg p-6">
                <div class="text-gray-600 text-sm mb-2">Active Tables</div>
                <div class="text-gray-900 text-2xl font-semibold">{{ $active_tables }}</div>
            </div>

            {{-- Staff Count Card --}}
            <div class="bg-white shadow-sm rounded-lg p-6">
                <div class="text-gray-600 text-sm mb-2">Staff Count</div>
                <div class="text-gray-900 text-2xl font-semibold">{{ $staff_count }}</div>
            </div>
        </div>
    </div>
</div>
