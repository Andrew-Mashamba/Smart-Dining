<?php

namespace App\Services;

use App\Models\Table;
use App\Models\GuestSession;
use Illuminate\Support\Facades\Storage;
use SimpleSoftwareIO\QrCode\Facades\QrCode;

class QRCodeService
{
    /**
     * Generate QR code for a specific table.
     *
     * @param int $tableId
     * @return string Path to the generated QR code file
     */
    public function generateTableQR(int $tableId): string
    {
        // Find the table
        $table = Table::findOrFail($tableId);

        // Create a new guest session with a unique token
        $guestSession = GuestSession::create([
            'table_id' => $tableId,
            'guest_id' => null, // Guest not assigned yet
            'started_at' => now(),
        ]);

        // Generate the URL for the guest ordering page with session token
        $url = url("/guest/order?token={$guestSession->session_token}");

        // Generate QR code as PNG
        $qrCode = QrCode::format('png')
            ->size(400)
            ->errorCorrection('H')
            ->generate($url);

        // Define the file path
        $fileName = "{$tableId}.png";
        $filePath = "qrcodes/{$fileName}";

        // Ensure the qrcodes directory exists
        Storage::disk('public')->makeDirectory('qrcodes');

        // Save the QR code to storage
        Storage::disk('public')->put($filePath, $qrCode);

        // Update the table with the QR code path
        $table->update([
            'qr_code' => $filePath,
        ]);

        return $filePath;
    }

    /**
     * Regenerate QR code for a specific table.
     * This will create a new session and QR code.
     *
     * @param int $tableId
     * @return string Path to the regenerated QR code file
     */
    public function regenerateTableQR(int $tableId): string
    {
        // Close any active sessions for this table
        GuestSession::where('table_id', $tableId)
            ->active()
            ->each(function ($session) {
                $session->close();
            });

        // Generate a new QR code
        return $this->generateTableQR($tableId);
    }

    /**
     * Get the full URL path to a table's QR code.
     *
     * @param string $filePath
     * @return string
     */
    public function getQRCodeUrl(string $filePath): string
    {
        return Storage::disk('public')->url($filePath);
    }

    /**
     * Get the full storage path to a table's QR code.
     *
     * @param string $filePath
     * @return string
     */
    public function getQRCodePath(string $filePath): string
    {
        return Storage::disk('public')->path($filePath);
    }

    /**
     * Delete QR code for a specific table.
     *
     * @param int $tableId
     * @return bool
     */
    public function deleteTableQR(int $tableId): bool
    {
        $table = Table::find($tableId);

        if ($table && $table->qr_code) {
            // Delete the file
            Storage::disk('public')->delete($table->qr_code);

            // Update the table
            $table->update(['qr_code' => null]);

            return true;
        }

        return false;
    }
}
