package com.example.atuneslegacy.data.importing.spotify

object DownloadPermissionChecker {
    fun verify(candidate: CandidateRecording): Boolean {
        return candidate.downloadUrl.isNotBlank() &&
                (candidate.downloadUrl.startsWith("http://") || candidate.downloadUrl.startsWith("https://"))
    }
}
