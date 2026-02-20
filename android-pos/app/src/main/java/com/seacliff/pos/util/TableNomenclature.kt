package com.seacliff.pos.util

/**
 * Table naming convention:
 * - **T0021** = indoor/main table number 21 (T + 4-digit zero-padded)
 * - **BT03**  = bar table number 3 (BT + 2-digit zero-padded)
 * - **OT008** = outside table number 8 (OT + 3-digit zero-padded)
 */
object TableNomenclature {

    private const val PREFIX_INDOOR = "T"
    private const val PREFIX_BAR = "BT"
    private const val PREFIX_OUTDOOR = "OT"

    /**
     * Format table name from location and table number.
     * @param location "indoor" | "outdoor" | "bar"
     * @param tableNumber 1-based table number within that location
     */
    @JvmStatic
    fun format(location: String, tableNumber: Int): String {
        return when (location.lowercase()) {
            "bar" -> "$PREFIX_BAR${tableNumber.toString().padStart(2, '0')}"
            "outdoor" -> "$PREFIX_OUTDOOR${tableNumber.toString().padStart(3, '0')}"
            else -> "$PREFIX_INDOOR${tableNumber.toString().padStart(4, '0')}" // indoor or default
        }
    }
}
