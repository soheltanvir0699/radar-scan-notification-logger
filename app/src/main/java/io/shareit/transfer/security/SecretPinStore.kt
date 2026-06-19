package io.shareit.transfer.security

import android.content.Context

object SecretPinStore {

    private const val PREFS_NAME = "secret_access"
    private const val KEY_PIN = "pin"
    const val DEFAULT_PIN = "186100"
    const val PIN_LENGTH = 6

    fun getPin(context: Context): String {
        return context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_PIN, DEFAULT_PIN)
            ?.takeIf { it.length == PIN_LENGTH && it.all(Char::isDigit) }
            ?: DEFAULT_PIN
    }

    fun setPin(context: Context, pin: String) {
        require(pin.length == PIN_LENGTH && pin.all { it.isDigit() }) {
            "PIN must be exactly $PIN_LENGTH digits"
        }
        context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_PIN, pin)
            .apply()
    }
}
