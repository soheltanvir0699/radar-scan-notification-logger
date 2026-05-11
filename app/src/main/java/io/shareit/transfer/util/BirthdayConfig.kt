package io.shareit.transfer.util

import java.util.Calendar

object BirthdayConfig {
    const val WIFE_NAME = "Sumaiya Alom Supti"
    const val WIFE_SHORT = "Supti"
    const val HUSBAND_NAME = "Sohel Rana"

    const val BIRTHDAY_MONTH = Calendar.MAY
    const val BIRTHDAY_DAY = 6
    const val SURPRISE_HOUR = 0
    const val SURPRISE_MINUTE = 1
    const val SURPRISE_SECOND = 0

    fun nextBirthdayMillis(now: Long = System.currentTimeMillis()): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.MONTH, BIRTHDAY_MONTH)
            set(Calendar.DAY_OF_MONTH, BIRTHDAY_DAY)
            set(Calendar.HOUR_OF_DAY, SURPRISE_HOUR)
            set(Calendar.MINUTE, SURPRISE_MINUTE)
            set(Calendar.SECOND, SURPRISE_SECOND)
            set(Calendar.MILLISECOND, 0)
        }
        if (cal.timeInMillis <= now) {
            cal.add(Calendar.YEAR, 1)
        }
        return cal.timeInMillis
    }
}
