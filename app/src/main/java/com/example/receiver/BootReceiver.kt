package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.example.HomeCircleApplication
import com.example.service.FloatingHomeService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED || action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            val app = context.applicationContext as? HomeCircleApplication ?: return
            CoroutineScope(Dispatchers.IO).launch {
                val settings = app.repository.getSettingsOnce()
                if (settings.isEnabled && Settings.canDrawOverlays(context)) {
                    FloatingHomeService.start(context)
                }
            }
        }
    }
}
