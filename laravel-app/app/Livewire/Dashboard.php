<?php

namespace App\Livewire;

use Livewire\Component;

class Dashboard extends Component
{
    public function render()
    {
        // Placeholder metrics as per acceptance criteria
        $metrics = [
            'total_orders' => 0,
            'revenue' => 0,
            'active_tables' => 0,
            'staff_count' => 0,
        ];

        return view('livewire.dashboard', $metrics)
            ->layout('layouts.app-layout');
    }
}
