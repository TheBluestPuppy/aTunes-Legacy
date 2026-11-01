package com.example.atuneslegacy.ui.components

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.URLUtil
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.atuneslegacy.ui.theme.LocalThemeColors
import com.example.atuneslegacy.ui.viewmodel.MainViewModel
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipInputStream

class BlobDownloadInterface(
    private val context: Context,
    private val viewModel: MainViewModel
) {
    @JavascriptInterface
    fun processBlobData(base64Data: String, fileName: String, mimeType: String) {
        try {
            val cleanBase64 = if (base64Data.contains(",")) base64Data.substringAfter(",") else base64Data
            val bytes = android.util.Base64.decode(cleanBase64, android.util.Base64.DEFAULT)

            val cleanName = if (fileName.isBlank() || fileName == "download") "track_${System.currentTimeMillis()}.mp3" else fileName
            val musicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
            val aTunesDir = File(musicDir, "aTunes Directory")

            var savedSuccessfully = false

            // Try MediaStore insertion for Scoped Storage (Android 10+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                try {
                    val values = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, cleanName)
                        put(MediaStore.MediaColumns.MIME_TYPE, if (cleanName.endsWith(".zip", ignoreCase = true)) "application/zip" else "audio/mpeg")
                        put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_MUSIC}/aTunes Directory")
                    }
                    val collection = if (cleanName.endsWith(".zip", ignoreCase = true)) {
                        MediaStore.Downloads.EXTERNAL_CONTENT_URI
                    } else {
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    }
                    val uri = context.contentResolver.insert(collection, values)
                    if (uri != null) {
                        context.contentResolver.openOutputStream(uri)?.use { out ->
                            out.write(bytes)
                        }
                        savedSuccessfully = true
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            if (!savedSuccessfully) {
                try {
                    if (!aTunesDir.exists()) aTunesDir.mkdirs()
                    val targetFile = File(aTunesDir, cleanName)
                    FileOutputStream(targetFile).use { out ->
                        out.write(bytes)
                    }
                    savedSuccessfully = true
                } catch (e: Exception) {
                    e.printStackTrace()
                    val fallbackDir = File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "aTunes Directory")
                    if (!fallbackDir.exists()) fallbackDir.mkdirs()
                    val fallbackFile = File(fallbackDir, cleanName)
                    FileOutputStream(fallbackFile).use { out ->
                        out.write(bytes)
                    }
                    savedSuccessfully = true
                }
            }

            if (cleanName.endsWith(".zip", ignoreCase = true)) {
                val folderName = cleanName.substringBeforeLast(".")
                val targetDir = File(aTunesDir, folderName)
                if (!targetDir.exists()) targetDir.mkdirs()

                try {
                    ZipInputStream(bytes.inputStream()).use { zis ->
                        var entry = zis.nextEntry
                        while (entry != null) {
                            val newFile = File(targetDir, entry.name)
                            if (entry.isDirectory) {
                                newFile.mkdirs()
                            } else {
                                newFile.parentFile?.mkdirs()
                                FileOutputStream(newFile).use { fos ->
                                    zis.copyTo(fos)
                                }
                            }
                            zis.closeEntry()
                            entry = zis.nextEntry
                        }
                    }
                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                        Toast.makeText(context, "ZIP extracted to Music/aTunes Directory/$folderName/", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    Toast.makeText(context, "Saved to Music/aTunes Directory/$cleanName", Toast.LENGTH_LONG).show()
                }
            }

            // Auto-scan directory to add new music to library
            viewModel.scanATunesDirectory()

        } catch (e: Exception) {
            e.printStackTrace()
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                Toast.makeText(context, "Download failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@Composable
fun SpotifyDownloaderBrowser(
    viewModel: MainViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val themeColors = LocalThemeColors.current

    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                if (intent?.action == DownloadManager.ACTION_DOWNLOAD_COMPLETE && ctx != null) {
                    val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
                    if (downloadId != -1L) {
                        try {
                            val dm = ctx.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                            val query = DownloadManager.Query().setFilterById(downloadId)
                            dm.query(query)?.use { cursor ->
                                if (cursor.moveToFirst()) {
                                    val statusIdx = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                                    val titleIdx = cursor.getColumnIndex(DownloadManager.COLUMN_TITLE)
                                    if (statusIdx != -1 && cursor.getInt(statusIdx) == DownloadManager.STATUS_SUCCESSFUL) {
                                        val title = if (titleIdx != -1) cursor.getString(titleIdx) else ""
                                        val musicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
                                        val aTunesDir = File(musicDir, "aTunes Directory")

                                        if (title.endsWith(".zip", ignoreCase = true)) {
                                            val zipFile = File(aTunesDir, title)
                                            if (zipFile.exists()) {
                                                val folderName = title.substringBeforeLast(".")
                                                val targetDir = File(aTunesDir, folderName)
                                                if (!targetDir.exists()) targetDir.mkdirs()

                                                try {
                                                    ZipInputStream(zipFile.inputStream()).use { zis ->
                                                        var entry = zis.nextEntry
                                                        while (entry != null) {
                                                            val newFile = File(targetDir, entry.name)
                                                            if (entry.isDirectory) {
                                                                newFile.mkdirs()
                                                            } else {
                                                                newFile.parentFile?.mkdirs()
                                                                FileOutputStream(newFile).use { fos ->
                                                                    zis.copyTo(fos)
                                                                }
                                                            }
                                                            zis.closeEntry()
                                                            entry = zis.nextEntry
                                                        }
                                                    }
                                                    Toast.makeText(ctx, "ZIP extracted to Music/aTunes Directory/$folderName/", Toast.LENGTH_LONG).show()
                                                } catch (e: Exception) {
                                                    e.printStackTrace()
                                                }
                                            }
                                        }

                                        // Auto-scan aTunes Directory when any download completes
                                        viewModel.scanATunesDirectory()
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }

        val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        try {
            ContextCompat.registerReceiver(
                context,
                receiver,
                filter,
                ContextCompat.RECEIVER_EXPORTED
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

        onDispose {
            try {
                context.unregisterReceiver(receiver)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(themeColors.background)
    ) {
        // Top Toolbar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(themeColors.surface)
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(onClick = onDismiss) {
                    Text("← Back to Settings", fontSize = 12.sp)
                }
                Text(
                    text = "SpotiDownloader",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.textPrimary
                )
                Spacer(modifier = Modifier.width(60.dp))
            }
        }

        // Embedded WebView
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.loadWithOverviewMode = true
                    settings.useWideViewPort = true

                    addJavascriptInterface(BlobDownloadInterface(ctx, viewModel), "BlobHandler")

                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                            val url = request?.url?.toString() ?: return false
                            val isAllowed = url.startsWith("blob:https://spotidownloader.com/", ignoreCase = true) ||
                                    url.startsWith("https://spotidownloader.com", ignoreCase = true) ||
                                    url.contains("spotidownloader.com", ignoreCase = true)

                            return if (isAllowed) {
                                false
                            } else {
                                Toast.makeText(ctx, "External link blocked.", Toast.LENGTH_SHORT).show()
                                true
                            }
                        }

                        @Deprecated("Deprecated in Java")
                        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                            val currentUrl = url ?: return false
                            val isAllowed = currentUrl.startsWith("blob:https://spotidownloader.com/", ignoreCase = true) ||
                                    currentUrl.startsWith("https://spotidownloader.com", ignoreCase = true) ||
                                    currentUrl.contains("spotidownloader.com", ignoreCase = true)

                            return if (isAllowed) {
                                false
                            } else {
                                Toast.makeText(ctx, "External link blocked.", Toast.LENGTH_SHORT).show()
                                true
                            }
                        }
                    }

                    setDownloadListener { url, userAgent, contentDisposition, mimeType, contentLength ->
                        try {
                            val fileName = URLUtil.guessFileName(url, contentDisposition, mimeType)
                            if (url.startsWith("blob:", ignoreCase = true)) {
                                val js = """
                                    (function() {
                                        var xhr = new XMLHttpRequest();
                                        xhr.open('GET', '$url', true);
                                        xhr.responseType = 'blob';
                                        xhr.onload = function(e) {
                                            if (this.status == 200) {
                                                var blob = this.response;
                                                var reader = new FileReader();
                                                reader.readAsDataURL(blob);
                                                reader.onloadend = function() {
                                                    var base64data = reader.result;
                                                    BlobHandler.processBlobData(base64data, '$fileName', '$mimeType');
                                                }
                                            }
                                        };
                                        xhr.send();
                                    })();
                                """.trimIndent()
                                evaluateJavascript(js, null)
                                Toast.makeText(ctx, "Processing Blob download...", Toast.LENGTH_SHORT).show()
                            } else {
                                val musicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
                                val aTunesDir = File(musicDir, "aTunes Directory")
                                if (!aTunesDir.exists()) {
                                    aTunesDir.mkdirs()
                                }

                                val request = DownloadManager.Request(Uri.parse(url)).apply {
                                    setMimeType(mimeType)
                                    val cookies = CookieManager.getInstance().getCookie(url)
                                    addRequestHeader("cookie", cookies)
                                    addRequestHeader("User-Agent", userAgent)
                                    setDescription("Downloading music from SpotiDownloader...")
                                    setTitle(fileName)
                                    setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                                    setDestinationInExternalPublicDir(Environment.DIRECTORY_MUSIC, "aTunes Directory/$fileName")
                                }
                                val dm = ctx.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                                dm.enqueue(request)
                                Toast.makeText(ctx, "Download started! Saving to Music/aTunes Directory/$fileName", Toast.LENGTH_LONG).show()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            Toast.makeText(ctx, "Download failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                        }
                    }
                    loadUrl("https://spotidownloader.com/en20")
                }
            },
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )
    }
}
