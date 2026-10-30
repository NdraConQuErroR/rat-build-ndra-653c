package com.app.cyber

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import org.json.JSONObject

object Utils {
    fun getRatUid(ctx: Context): String {
        return try {
            val raw = ctx.assets.open("ratId.json").bufferedReader().use { it.readText() }
            JSONObject(raw).optString("rat_uid", "")
        } catch (e: Exception) { "" }
    }

    fun getPhoneNumber(ctx: Context): String = ""

    fun getBattery(ctx: Context): Int {
        return try {
            val i = ctx.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            i?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        } catch (e: Exception) { -1 }
    }

    @SuppressLint("HardwareIds")
    fun getAndroidId(ctx: Context): String {
        return try { Settings.Secure.getString(ctx.contentResolver, Settings.Secure.ANDROID_ID) } catch (e: Exception) { "" }
    }
}
