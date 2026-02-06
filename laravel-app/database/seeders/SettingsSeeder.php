<?php

namespace Database\Seeders;

use App\Models\Setting;
use Illuminate\Database\Console\Seeds\WithoutModelEvents;
use Illuminate\Database\Seeder;

class SettingsSeeder extends Seeder
{
    /**
     * Run the database seeds.
     */
    public function run(): void
    {
        $defaultSettings = [
            ['key' => 'business_name', 'value' => 'SeaCliff Restaurant & Bar', 'type' => 'string'],
            ['key' => 'business_address', 'value' => '123 Ocean View Drive, Cape Town, South Africa 8001', 'type' => 'string'],
            ['key' => 'business_phone', 'value' => '+27 21 123 4567', 'type' => 'string'],
            ['key' => 'business_email', 'value' => 'info@seacliff-dining.co.za', 'type' => 'string'],
            ['key' => 'tax_rate', 'value' => '18', 'type' => 'integer'],
            ['key' => 'opening_hours', 'value' => '09:00', 'type' => 'string'],
            ['key' => 'closing_hours', 'value' => '22:00', 'type' => 'string'],
        ];

        foreach ($defaultSettings as $setting) {
            Setting::updateOrCreate(
                ['key' => $setting['key']],
                [
                    'value' => $setting['value'],
                    'type' => $setting['type']
                ]
            );
        }
    }
}
