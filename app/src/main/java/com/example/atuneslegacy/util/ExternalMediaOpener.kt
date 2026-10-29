
package com.example.atuneslegacy.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

fun openMediaExternally(
    context: Context,
    uriString: String,
    mimeType: String? = null
) {
    if (uriString.isBlank()) {
        Toast.makeText(
            context,
            "No media file is available.",
            Toast.LENGTH_SHORT
        ).show()
        return
    }

    try {
        val uri = Uri.parse(uriString)

        val resolvedType = mimeType
            ?: context.contentResolver.getType(uri)
            ?: "*/*"

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, resolvedType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(
            Intent.createChooser(intent, "Open media with")
        )
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(
            context,
            "No compatible app was found.",
            Toast.LENGTH_LONG
        ).show()
    } catch (e: SecurityException) {
        Toast.makeText(
            context,
            "Permission to open this file was denied.",
            Toast.LENGTH_LONG
        ).show()
    }
}
