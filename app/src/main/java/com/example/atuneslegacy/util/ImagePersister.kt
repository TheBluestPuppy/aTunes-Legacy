package com.example.atuneslegacy.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object ImagePersister {
    fun persistImageLocally(context: Context, uriString: String, prefix: String): String {
        if (uriString.isBlank()) return ""
        try {
            val uri = Uri.parse(uriString)
            if (uri.scheme == "file") {
                return uriString
            }
            val inputStream = context.contentResolver.openInputStream(uri) ?: return uriString
            val persistentDir = File(context.filesDir, "media_artwork").apply { mkdirs() }
            val targetFile = File(persistentDir, "${prefix}_${System.currentTimeMillis()}.jpg")
            FileOutputStream(targetFile).use { output ->
                inputStream.copyTo(output)
            }
            return Uri.fromFile(targetFile).toString()
        } catch (e: Exception) {
            e.printStackTrace()
            return uriString
        }
    }
}
