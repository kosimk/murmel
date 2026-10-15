package app.murmel.speech

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.ModelDownloadListener
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.annotation.RequiresApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import java.util.Locale

/**
 * German speech recognition via Android's on-device recognizer (Android 12+).
 *
 * Deliberately uses [SpeechRecognizer.createOnDeviceSpeechRecognizer] instead of the
 * default recognizer, which may send audio to a server. If on-device recognition is
 * missing we report it as unavailable rather than fall back to the cloud.
 *
 * Many phones ship only an English pack (seen on the Fairphone 6); [downloadLanguage]
 * asks the system recognizer to fetch German. The system does the download.
 *
 * The recognizer runs in a system service; its API must be called on the main thread.
 */
class OnDeviceSpeechToText(private val context: Context) : SpeechToText {

    private var recognizer: SpeechRecognizer? = null

    override fun isAvailable(): Boolean = SpeechRecognizer.isOnDeviceRecognitionAvailable(context)

    override fun listen(): Flow<SpeechEvent> = callbackFlow {
        val recognizer = SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
        this@OnDeviceSpeechToText.recognizer = recognizer

        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onPartialResults(partialResults: Bundle) {
                partialResults.firstResult()?.let { trySend(SpeechEvent.Partial(it)) }
            }

            override fun onResults(results: Bundle) {
                trySend(SpeechEvent.Final(results.firstResult().orEmpty()))
                close()
            }

            override fun onError(error: Int) {
                trySend(SpeechEvent.Failed(error.toSpeechError()))
                close()
            }

            override fun onReadyForSpeech(params: Bundle?) = Unit
            override fun onBeginningOfSpeech() = Unit
            override fun onRmsChanged(rmsdB: Float) = Unit
            override fun onBufferReceived(buffer: ByteArray?) = Unit
            override fun onEndOfSpeech() = Unit
            override fun onEvent(eventType: Int, params: Bundle?) = Unit
        })

        recognizer.startListening(germanRecognizerIntent())

        awaitClose {
            recognizer.destroy()
            this@OnDeviceSpeechToText.recognizer = null
        }
    }.flowOn(Dispatchers.Main)

    override fun stop() {
        recognizer?.stopListening()
    }

    override fun canDownloadLanguage(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    override fun downloadLanguage(): Flow<LanguageDownload> =
        if (canDownloadLanguage()) systemDownload() else flowOf(LanguageDownload.Failed)

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun systemDownload(): Flow<LanguageDownload> = callbackFlow {
        val recognizer = SpeechRecognizer.createOnDeviceSpeechRecognizer(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            recognizer.triggerModelDownload(
                germanRecognizerIntent(),
                context.mainExecutor,
                object : ModelDownloadListener {
                    override fun onProgress(completedPercent: Int) {
                        trySend(LanguageDownload.Running(completedPercent))
                    }

                    override fun onSuccess() {
                        trySend(LanguageDownload.Done)
                        close()
                    }

                    override fun onScheduled() {
                        trySend(LanguageDownload.Scheduled)
                        close()
                    }

                    override fun onError(error: Int) {
                        trySend(LanguageDownload.Failed)
                        close()
                    }
                },
            )
        } else {
            // Android 13 can only start the download, without progress or result.
            recognizer.triggerModelDownload(germanRecognizerIntent())
            trySend(LanguageDownload.Scheduled)
            close()
        }

        awaitClose { recognizer.destroy() }
    }.flowOn(Dispatchers.Main)
}

private fun germanRecognizerIntent(): Intent =
    Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        .putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.GERMANY.toLanguageTag())
        .putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)

private fun Bundle.firstResult(): String? =
    getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()

private fun Int.toSpeechError(): SpeechError = when (this) {
    SpeechRecognizer.ERROR_NO_MATCH,
    SpeechRecognizer.ERROR_SPEECH_TIMEOUT,
    -> SpeechError.NothingHeard

    SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED,
    SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE,
    -> SpeechError.LanguageUnavailable

    else -> SpeechError.Other
}
