package com.app.cyber

import android.content.Context
import android.hardware.camera2.CameraManager
import android.os.Handler
import android.os.Looper

object FlashlightController {
    private var camManager: CameraManager? = null
    private var cameraId: String? = null
    private var blinkHandler: Handler? = null
    private var blinking = false

    private fun init(ctx: Context) {
        if (camManager == null) {
            camManager = ctx.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            cameraId = camManager?.cameraIdList?.firstOrNull()
        }
    }

    fun set(ctx: Context, on: Boolean) {
        init(ctx)
        try { camManager?.setTorchMode(cameraId ?: return, on) } catch (e: Exception) { }
    }

    fun blink(ctx: Context, ms: Int) {
        init(ctx)
        blinking = true
        blinkHandler = Handler(Looper.getMainLooper())
        var state = false
        val runnable = object : Runnable {
            override fun run() {
                if (!blinking) return
                state = !state
                set(ctx, state)
                blinkHandler?.postDelayed(this, ms.toLong())
            }
        }
        blinkHandler?.post(runnable)
    }

    fun stopBlink(ctx: Context) {
        blinking = false
        blinkHandler?.removeCallbacksAndMessages(null)
        set(ctx, false)
    }
}
