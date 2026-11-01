package com.example.atuneslegacy.data.importing.spotify

data class MatchResult(
    val candidate: CandidateRecording,
    val confidenceScore: Int // 0 to 100
)

object TrackMatchingEngine {
    fun match(original: UnifiedTrackMetadata, candidates: List<CandidateRecording>): MatchResult? {
        if (candidates.isEmpty()) return null

        var bestCandidate = candidates.first()
        var bestScore = 0

        for (candidate in candidates) {
            var score = 0
            val titleMatch = normalize(candidate.title).contains(normalize(original.title), ignoreCase = true) ||
                    normalize(original.title).contains(normalize(candidate.title), ignoreCase = true)
            if (titleMatch) score += 30

            val artistMatch = normalize(candidate.artist).contains(normalize(original.artist), ignoreCase = true) ||
                    normalize(original.artist).contains(normalize(candidate.artist), ignoreCase = true)
            if (artistMatch) score += 25

            score += 20
            score += 10

            if (candidate.downloadUrl.isNotBlank()) score += 15

            if (score > bestScore) {
                bestScore = score
                bestCandidate = candidate
            }
        }

        return MatchResult(bestCandidate, bestScore.coerceIn(0, 100))
    }

    private fun normalize(input: String): String {
        return input.lowercase().replace(Regex("[^a-z0-9]"), "")
    }
}
