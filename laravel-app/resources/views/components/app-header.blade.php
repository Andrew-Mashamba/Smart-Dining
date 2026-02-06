<header class="bg-white shadow-sm sticky top-0 z-50">
    <div class="px-4 sm:px-6 lg:px-8">
        <div class="flex items-center justify-between h-16">
            <!-- Mobile Menu Toggle & Logo -->
            <div class="flex items-center gap-4">
                <!-- Mobile Menu Button -->
                <button
                    @click="sidebarOpen = !sidebarOpen"
                    class="lg:hidden p-2 rounded-lg hover:bg-gray-50 transition-colors"
                    aria-label="Toggle menu"
                >
                    <svg class="w-6 h-6 text-gray-900" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16"></path>
                    </svg>
                </button>

                <!-- Logo -->
                <div class="flex items-center gap-3">
                    <div class="w-8 h-8 bg-gray-900 rounded-lg flex items-center justify-center">
                        <span class="text-white font-bold text-sm">SC</span>
                    </div>
                    <h1 class="text-xl font-bold text-gray-900">Sea Cliff Smart Dining</h1>
                </div>
            </div>

            <!-- User Menu -->
            @auth
            <div class="relative" x-data="{ dropdownOpen: false }">
                <button
                    @click="dropdownOpen = !dropdownOpen"
                    @click.away="dropdownOpen = false"
                    class="flex items-center gap-3 px-3 py-2 rounded-lg hover:bg-gray-50 transition-colors"
                >
                    <div class="text-right hidden sm:block">
                        <div class="text-sm font-medium text-gray-900">{{ auth()->user()->name }}</div>
                        <div class="text-xs text-gray-600">{{ ucfirst(auth()->user()->role) }}</div>
                    </div>
                    <div class="w-8 h-8 bg-gray-900 rounded-lg flex items-center justify-center">
                        <span class="text-white text-sm font-medium">{{ substr(auth()->user()->name, 0, 1) }}</span>
                    </div>
                    <svg class="w-4 h-4 text-gray-600" :class="{ 'rotate-180': dropdownOpen }" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"></path>
                    </svg>
                </button>

                <!-- Dropdown Menu -->
                <div
                    x-show="dropdownOpen"
                    x-transition:enter="transition ease-out duration-200"
                    x-transition:enter-start="opacity-0 transform scale-95"
                    x-transition:enter-end="opacity-100 transform scale-100"
                    x-transition:leave="transition ease-in duration-150"
                    x-transition:leave-start="opacity-100 transform scale-100"
                    x-transition:leave-end="opacity-0 transform scale-95"
                    class="absolute right-0 mt-2 w-48 bg-white rounded-xl shadow-sm border border-gray-200 py-2"
                    style="display: none;"
                >
                    <div class="px-4 py-2 border-b border-gray-100">
                        <div class="text-sm font-medium text-gray-900">{{ auth()->user()->name }}</div>
                        <div class="text-xs text-gray-600">{{ auth()->user()->email }}</div>
                    </div>
                    <form method="POST" action="{{ route('logout') }}" class="mt-1">
                        @csrf
                        <button type="submit" class="w-full text-left px-4 py-2 text-sm text-gray-900 hover:bg-gray-50 transition-colors">
                            Logout
                        </button>
                    </form>
                </div>
            </div>
            @endauth

            @guest
            <div class="flex items-center gap-4">
                <a href="{{ route('login') }}" class="text-sm font-medium text-gray-900 hover:text-gray-600 transition-colors">
                    Login
                </a>
            </div>
            @endguest
        </div>
    </div>
</header>
