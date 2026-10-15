package app.murmel.speech

import kotlinx.coroutines.flow.Flow

/** What the recognizer reports while the user speaks. */
sealed interface SpeechEvent {
    /** Text recognised so far; may still change. */
    data class Partial(val text: String) : SpeechEvent

    /** Final text for this recording. Ends the flow. */
    data class Final(val text: String) : SpeechEvent

    /** Recognition stopped without a result. Ends the flow. */
    data class Failed(val error: SpeechError) : SpeechEvent
}

enum class SpeechError {
    /** Nothing was said, or nothing could be understood. */
    NothingHeard,

    /** The on-device model for the requested language is not installed. */
    LanguageUnavailable,

    /** Anything else (busy recognizer, audio problem, …). */
    Other,
}

/** Progress of fetching the on-device language pack. */
sealed interface LanguageDownload {
    data class Running(val percent: Int) : LanguageDownload

    /** The system will download later (e.g. once on Wi-Fi). Ends the flow. */
    data object Scheduled : LanguageDownload

    /** The pack is installed. Ends the flow. */
    data object Done : LanguageDownload

    /** Ends the flow. */
    data object Failed : LanguageDownload
}

/**
 * Turns speech into text, on the device only.
 *
 * Collecting [listen] starts recording; the flow ends after a [SpeechEvent.Final]
 * or [SpeechEvent.Failed]. Cancelling the collection aborts without a result.
 */
interface SpeechToText {
    fun isAvailable(): Boolean

    fun listen(): Flow<SpeechEvent>

    /** Stop recording and deliver the final result of the current [listen]. */
    fun stop()

    /** Whether this device lets apps ask for a missing language pack (Android 13+). */
    fun canDownloadLanguage(): Boolean

    /**
     * Ask the system's on-device recognizer to fetch the language pack.
     * The system downloads it; this app needs no network access.
     */
    fun downloadLanguage(): Flow<LanguageDownload>
}
