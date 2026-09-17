package com.android.tweaker.data

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager

object BatteryInfoManager {
    fun getBatteryStatus(context: Context): String {
        return try {
            val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val batteryStatus: Intent? = context.registerReceiver(null, intentFilter)

            if (batteryStatus == null) return "Unable to read battery status via system APIs."

            val level: Int = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale: Int = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val batteryPct: Float = if (level != -1 && scale != -1) (level / scale.toFloat()) * 100 else -1f

            val status: Int = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val isCharging: Boolean = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL

            val chargePlug: Int = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
            val usbCharge: Boolean = chargePlug == BatteryManager.BATTERY_PLUGGED_USB
            val acCharge: Boolean = chargePlug == BatteryManager.BATTERY_PLUGGED_AC
            val wirelessCharge: Boolean = chargePlug == BatteryManager.BATTERY_PLUGGED_WIRELESS

            val health: Int = batteryStatus.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)
            val healthStr = when (health) {
                BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
                BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
                BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
                BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
                BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Unspecified Failure"
                BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
                else -> "Unknown"
            }

            val technology: String = batteryStatus.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Unknown"
            val temperature: Int = batteryStatus.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1)
            val tempCelsius: Float = if (temperature != -1) temperature / 10f else 0f

            val voltage: Int = batteryStatus.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)

            val chargeTypeStr = when {
                acCharge -> "AC Charger"
                usbCharge -> "USB Port"
                wirelessCharge -> "Wireless Charging"
                else -> "Not Charging"
            }

            """
            === DETAILED BATTERY STATUS (Native Android API) ===
            Level: ${batteryPct.toInt()}% ($level / $scale)
            Status: ${if (isCharging) "Charging" else "Discharging / Full"}
            Power Source: $chargeTypeStr
            Health: $healthStr
            Technology: $technology
            Temperature: ${"%.1f".format(tempCelsius)} °C
            Voltage: ${voltage} mV
            """.trimIndent()
        } catch (e: Exception) {
            "Battery Status Error: ${e.localizedMessage}"
        }
    }
}
