<div>
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        {{-- Reports Heading --}}
        <div class="mb-6">
            <h1 class="text-lg font-bold text-gray-900">Reports</h1>
        </div>

        {{-- Success Message --}}
        @if (session()->has('message'))
            <div class="mb-6 bg-white border border-gray-200 rounded-lg p-4">
                <p class="text-gray-600">{{ session('message') }}</p>
            </div>
        @endif

        {{-- Report Filters --}}
        <div class="bg-white shadow-sm rounded-lg p-6 mb-6">
            <h2 class="text-gray-900 font-semibold mb-4">Report Parameters</h2>

            <div class="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
                {{-- Date Range Selector --}}
                <div>
                    <label for="start_date" class="block text-gray-600 text-sm mb-2">Start Date</label>
                    <input
                        type="date"
                        id="start_date"
                        wire:model="start_date"
                        class="w-full px-4 py-2 border border-gray-200 rounded-lg text-gray-900 focus:outline-none focus:ring-2 focus:ring-gray-900 focus:border-transparent"
                    >
                    @error('start_date')
                        <span class="text-red-500 text-xs mt-1">{{ $message }}</span>
                    @enderror
                </div>

                <div>
                    <label for="end_date" class="block text-gray-600 text-sm mb-2">End Date</label>
                    <input
                        type="date"
                        id="end_date"
                        wire:model="end_date"
                        class="w-full px-4 py-2 border border-gray-200 rounded-lg text-gray-900 focus:outline-none focus:ring-2 focus:ring-gray-900 focus:border-transparent"
                    >
                    @error('end_date')
                        <span class="text-red-500 text-xs mt-1">{{ $message }}</span>
                    @enderror
                </div>

                {{-- Report Type Dropdown --}}
                <div>
                    <label for="report_type" class="block text-gray-600 text-sm mb-2">Report Type</label>
                    <select
                        id="report_type"
                        wire:model="report_type"
                        class="w-full px-4 py-2 border border-gray-200 rounded-lg text-gray-900 focus:outline-none focus:ring-2 focus:ring-gray-900 focus:border-transparent"
                    >
                        <option value="sales">Sales</option>
                        <option value="inventory">Inventory</option>
                        <option value="staff_performance">Staff Performance</option>
                    </select>
                    @error('report_type')
                        <span class="text-red-500 text-xs mt-1">{{ $message }}</span>
                    @enderror
                </div>
            </div>

            {{-- Generate Button --}}
            <div>
                <button
                    wire:click="generateReport"
                    class="bg-white text-gray-900 border border-gray-200 px-6 py-2 rounded-lg hover:bg-gray-50 transition-colors font-medium"
                >
                    Generate Report
                </button>
            </div>
        </div>

        {{-- Placeholder Chart Area --}}
        <div class="bg-gray-50 rounded-xl p-6">
            <div class="flex items-center justify-center h-64">
                <p class="text-gray-400 text-lg">Chart will appear here</p>
            </div>
        </div>
    </div>
</div>
