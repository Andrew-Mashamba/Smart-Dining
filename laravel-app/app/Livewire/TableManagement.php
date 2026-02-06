<?php

namespace App\Livewire;

use App\Models\Table;
use Livewire\Component;
use SimpleSoftwareIO\QrCode\Facades\QrCode;
use Illuminate\Support\Str;

class TableManagement extends Component
{
    public $name;
    public $location;
    public $capacity;
    public $editingTableId;
    public $showDeleteConfirmation = false;
    public $tableToDelete;

    protected $rules = [
        'name' => 'required|string|max:255',
        'location' => 'required|string|max:255',
        'capacity' => 'required|integer|min:1|max:20',
    ];

    protected $messages = [
        'name.required' => 'Table name is required.',
        'location.required' => 'Location is required.',
        'capacity.required' => 'Capacity is required.',
        'capacity.min' => 'Capacity must be at least 1.',
        'capacity.max' => 'Capacity cannot exceed 20.',
    ];

    public function mount()
    {
        // Initialize component
    }

    /**
     * Add a new table
     */
    public function addTable()
    {
        $this->validate();

        Table::create([
            'name' => $this->name,
            'location' => $this->location,
            'capacity' => $this->capacity,
            'status' => 'available',
        ]);

        // Reset form
        $this->reset(['name', 'location', 'capacity']);

        session()->flash('message', 'Table created successfully.');
    }

    /**
     * Edit an existing table
     */
    public function editTable($tableId)
    {
        $table = Table::findOrFail($tableId);

        $this->editingTableId = $tableId;
        $this->name = $table->name;
        $this->location = $table->location;
        $this->capacity = $table->capacity;
    }

    /**
     * Update the table being edited
     */
    public function updateTable()
    {
        $this->validate();

        $table = Table::findOrFail($this->editingTableId);
        $table->update([
            'name' => $this->name,
            'location' => $this->location,
            'capacity' => $this->capacity,
        ]);

        // Reset form
        $this->reset(['name', 'location', 'capacity', 'editingTableId']);

        session()->flash('message', 'Table updated successfully.');
    }

    /**
     * Cancel editing
     */
    public function cancelEdit()
    {
        $this->reset(['name', 'location', 'capacity', 'editingTableId']);
    }

    /**
     * Show delete confirmation modal
     */
    public function confirmDelete($tableId)
    {
        $this->tableToDelete = $tableId;
        $this->showDeleteConfirmation = true;
    }

    /**
     * Delete a table
     */
    public function deleteTable()
    {
        if ($this->tableToDelete) {
            Table::findOrFail($this->tableToDelete)->delete();
            $this->showDeleteConfirmation = false;
            $this->tableToDelete = null;
            session()->flash('message', 'Table deleted successfully.');
        }
    }

    /**
     * Cancel delete operation
     */
    public function cancelDelete()
    {
        $this->showDeleteConfirmation = false;
        $this->tableToDelete = null;
    }

    /**
     * Generate QR code for a table
     */
    public function generateQrCode($tableId)
    {
        $table = Table::findOrFail($tableId);

        // Generate unique session token
        $sessionToken = Str::random(32);

        // Generate URL with session token pointing to guest ordering page
        $url = url('/guest/order?table=' . $table->id . '&token=' . $sessionToken);

        // Generate QR code as SVG and store it
        $qrCode = QrCode::size(300)->generate($url);

        // Update table with QR code
        $table->update([
            'qr_code' => $qrCode,
        ]);

        session()->flash('message', 'QR Code generated successfully for ' . $table->name);
    }

    /**
     * Update table status
     */
    public function updateStatus($tableId, $status)
    {
        $table = Table::findOrFail($tableId);
        $table->update(['status' => $status]);

        session()->flash('message', 'Table status updated.');
    }

    /**
     * Render the component with real-time polling
     */
    public function render()
    {
        $tables = Table::orderBy('created_at', 'desc')->get();

        return view('livewire.table-management', [
            'tables' => $tables,
        ])->layout('layouts.app-layout');
    }
}
