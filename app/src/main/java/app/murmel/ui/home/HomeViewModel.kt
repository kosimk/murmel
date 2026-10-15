package app.murmel.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.murmel.speech.OnDeviceSpeechToText
import app.murmel.speech.SpeechError
import app.murmel.speech.SpeechEvent
import app.murmel.speech.SpeechToText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val isListening: Boolean = false,
    val transcript: String = "",
    val problem: HomeProblem? = null,
)

/** Why the last recording produced no transcript. Shown as a short message. */
enum class HomeProblem {
    MicrophoneDenied,
    RecognitionUnavailable,
    LanguageUnavailable,
    NothingHeard,
    Failed,
}

class HomeViewModel(private val speech: SpeechToText) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    /** Record button tapped (microphone permission already granted). */
    fun onRecordTapped() {
        if (_state.value.isListening) speech.stop() else startListening()
    }

    fun onMicrophoneDenied() {
        _state.update { it.copy(problem = HomeProblem.MicrophoneDenied) }
    }

    private fun startListening() {
        if (!speech.isAvailable()) {
            _state.update { it.copy(problem = HomeProblem.RecognitionUnavailable) }
            return
        }
        _state.value = HomeUiState(isListening = true)
        viewModelScope.launch {
            speech.listen()
                .onCompletion { _state.update { it.copy(isListening = false) } }
                .collect { event ->
                    when (event) {
                        is SpeechEvent.Partial -> _state.update { it.copy(transcript = event.text) }
                        is SpeechEvent.Final -> _state.update {
                            it.copy(transcript = event.text.ifEmpty { it.transcript })
                        }
                        is SpeechEvent.Failed -> _state.update { it.copy(problem = event.error.toProblem()) }
                    }
                }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { HomeViewModel(OnDeviceSpeechToText(checkNotNull(this[APPLICATION_KEY]))) }
        }
    }
}

private fun SpeechError.toProblem(): HomeProblem = when (this) {
    SpeechError.NothingHeard -> HomeProblem.NothingHeard
    SpeechError.LanguageUnavailable -> HomeProblem.LanguageUnavailable
    SpeechError.Other -> HomeProblem.Failed
}
