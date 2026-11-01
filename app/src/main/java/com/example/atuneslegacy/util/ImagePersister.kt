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
            val file = File(context.filesDir, "${prefix}_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { output ->
                inputStream.copyTo(output)
            }
            return Uri.fromFile(file).toString()
        } catch (e: Exception) {
            e.printStackTrace()
            return uriString
        }
    }
}
