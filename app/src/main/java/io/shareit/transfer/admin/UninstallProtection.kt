package io.shareit.transfer.admin

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent

object UninstallProtection {

    fun componentName(context: Context): ComponentName =
        ComponentName(context, ShareItDeviceAdminReceiver::class.java)

    fun isActive(context: Context): Boolean {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        return dpm.isAdminActive(componentName(context))
    }

    fun requestEnableIntent(context: Context): Intent =
        Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName(context))
            putExtra(
                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "Protects SHAREit transfer services. You will need to turn this off before uninstalling the app."
            )
        }

    fun removeActiveAdmin(context: Context) {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val comp = componentName(context)
        if (dpm.isAdminActive(comp)) {
            dpm.removeActiveAdmin(comp)
        }
    }
}
