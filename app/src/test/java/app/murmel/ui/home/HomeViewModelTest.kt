package app.murmel.ui.home

import app.murmel.speech.SpeechError
import app.murmel.speech.SpeechEvent
import app.murmel.speech.SpeechToText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val speech = FakeSpeechToText()
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = HomeViewModel(speech)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `partial and final results end up in the transcript`() {
        viewModel.onRecordTapped()
        assertTrue(viewModel.state.value.isListening)

        speech.events.trySend(SpeechEvent.Partial("Um drei"))
        assertEquals("Um drei", viewModel.state.value.transcript)

        speech.finish(SpeechEvent.Final("Um drei gestillt, links"))
        val state = viewModel.state.value
        assertEquals("Um drei gestillt, links", state.transcript)
        assertFalse(state.isListening)
        assertNull(state.problem)
    }

    @Test
    fun `empty final result keeps the last partial text`() {
        viewModel.onRecordTapped()
        speech.events.trySend(SpeechEvent.Partial("Windel nass"))
        speech.finish(SpeechEvent.Final(""))

        assertEquals("Windel nass", viewModel.state.value.transcript)
    }

    @Test
    fun `tapping while listening stops the recognizer`() {
        viewModel.onRecordTapped()
        viewModel.onRecordTapped()

        assertEquals(1, speech.stopCalls)
    }

    @Test
    fun `missing on-device recognition is reported, nothing starts`() {
        speech.available = false
        viewModel.onRecordTapped()

        val state = viewModel.state.value
        assertFalse(state.isListening)
        assertEquals(HomeProblem.RecognitionUnavailable, state.problem)
    }

    @Test
    fun `recognizer errors are reported`() {
        viewModel.onRecordTapped()
        speech.finish(SpeechEvent.Failed(SpeechError.LanguageUnavailable))

        val state = viewModel.state.value
        assertFalse(state.isListening)
        assertEquals(HomeProblem.LanguageUnavailable, state.problem)
    }

    @Test
    fun `a new recording clears the previous transcript and problem`() {
        viewModel.onRecordTapped()
        speech.finish(SpeechEvent.Failed(SpeechError.NothingHeard))

        speech.reset()
        viewModel.onRecordTapped()

        val state = viewModel.state.value
        assertTrue(state.isListening)
        assertEquals("", state.transcript)
        assertNull(state.problem)
    }

    @Test
    fun `denied microphone is reported`() {
        viewModel.onMicrophoneDenied()

        assertEquals(HomeProblem.MicrophoneDenied, viewModel.state.value.problem)
    }
}

private class FakeSpeechToText : SpeechToText {
    var available = true
    var stopCalls = 0
    var events = Channel<SpeechEvent>(Channel.UNLIMITED)
        private set

    override fun isAvailable() = available
    override fun listen(): Flow<SpeechEvent> = events.receiveAsFlow()
    override fun stop() {
        stopCalls++
    }

    /** Send the last event and end the flow, like the real recognizer does. */
    fun finish(event: SpeechEvent) {
        events.trySend(event)
        events.close()
    }

    fun reset() {
        events = Channel(Channel.UNLIMITED)
    }
}
