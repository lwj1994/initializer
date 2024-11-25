package com.lwjlol.initializer

import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.res.AssetManager
import android.content.res.Resources
import android.os.Handler
import android.os.Looper
import java.io.File

/**
 * @author luwenjie on 2022/10/10 18:40:23
 */

private val appContext =
    object : android.content.Context() {
        override fun getAssets(): AssetManager? = null

        override fun getResources(): Resources? = null

        override fun getMainLooper(): Looper? = null

        override fun registerReceiver(
            receiver: BroadcastReceiver?,
            filter: IntentFilter?,
        ): Intent? = null

        override fun registerReceiver(
            receiver: BroadcastReceiver?,
            filter: IntentFilter?,
            flags: Int,
        ): Intent? = null

        override fun registerReceiver(
            receiver: BroadcastReceiver?,
            filter: IntentFilter?,
            perm: String?,
            handler: Handler?,
        ): Intent? = null

        override fun registerReceiver(
            receiver: BroadcastReceiver?,
            filter: IntentFilter?,
            perm: String?,
            handler: Handler?,
            flags: Int,
        ): Intent? = null

        override fun unregisterReceiver(receiver: BroadcastReceiver?) {}

        override fun getSystemService(name: String?): Any? = null

        override fun getApplicationContext(): android.content.Context? = null

        override fun getFilesDir(): File? = null

        override fun getPackageName(): String? = null

        override fun getApplicationInfo(): ApplicationInfo? = null
    }
