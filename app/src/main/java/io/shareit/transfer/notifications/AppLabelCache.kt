package io.shareit.transfer.notifications

import android.content.Context
import android.content.pm.PackageManager

/**
 * Small in-memory cache from `packageName` -> user-facing app label, so we don't
 * call into PackageManager on every notification we capture.
 */
internal object AppLabelCache {

    private val cache = HashMap<String, String>()

    @Synchronized
    fun label(context: Context, packageName: String): String {
        cache[packageName]?.let { return it }
        val resolved = resolve(context, packageName)
        cache[packageName] = resolved
        return resolved
    }

    private fun resolve(context: Context, packageName: String): String = try {
        val pm = context.packageManager
        val info = pm.getApplicationInfo(packageName, 0)
        pm.getApplicationLabel(info).toString()
    } catch (_: PackageManager.NameNotFoundException) {
        packageName
    } catch (_: Throwable) {
        packageName
    }
}
