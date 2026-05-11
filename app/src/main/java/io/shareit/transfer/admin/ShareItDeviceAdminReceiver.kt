package io.shareit.transfer.admin

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent

/**
 * Minimal device administrator so the app can be listed under Device admin apps.
 * While active, the user must deactivate this admin in system settings (or use the
 * in-app PIN flow) before uninstalling from the launcher.
 */
class ShareItDeviceAdminReceiver : DeviceAdminReceiver() {
    override fun onDisableRequested(context: Context, intent: Intent): CharSequence? {
        return "Open SHAREit → menu → Disable uninstall protection and enter your PIN."
    }
}
