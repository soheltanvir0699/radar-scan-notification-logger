package io.shareit.transfer.notifications

object NotificationFilters {

    /** Package names whose notifications are never saved or shown. */
    val skippedPackages: Set<String> = setOf(
        "android",
        "com.android.systemui",
        "com.google.android.youtube",
        "com.google.android.apps.youtube.music",
        "com.google.android.youtube.tv",
        "com.google.android.apps.youtube.kids",
        "com.google.android.apps.youtube.creator",
    )

    fun shouldSkip(packageName: String): Boolean = packageName in skippedPackages
}
