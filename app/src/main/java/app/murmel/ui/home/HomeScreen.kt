package app.murmel.ui.home

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import app.murmel.R
import app.murmel.ui.theme.MurmelTheme

/**
 * Home: one huge record button, reachable with a thumb, plus the latest transcript.
 *
 * Asks for the microphone on the first tap, then hands over to [HomeViewModel].
 */
@Composable
fun HomeScreen(viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory)) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    val requestMicrophone = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) viewModel.onRecordTapped() else viewModel.onMicrophoneDenied()
    }

    HomeContent(
        state = state,
        onRecordClick = {
            val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED
            if (granted) viewModel.onRecordTapped() else requestMicrophone.launch(Manifest.permission.RECORD_AUDIO)
        },
    )
}

@Composable
private fun HomeContent(state: HomeUiState, onRecordClick: () -> Unit) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AssistChip(
                onClick = {},
                label = { Text(stringResource(R.string.privacy_badge)) },
                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
            )

            Spacer(Modifier.height(32.dp))

            Text(
                text = stringResource(if (state.isListening) R.string.recording else R.string.home_title),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.home_hint),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                contentAlignment = Alignment.Center,
            ) {
                when {
                    state.transcript.isNotEmpty() -> Text(
                        text = state.transcript,
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 24.dp),
                    )
                    state.problem != null -> Text(
                        text = stringResource(state.problem.message),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 24.dp),
                    )
                    !state.isListening -> Text(
                        text = stringResource(R.string.timeline_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        modifier = Modifier.padding(vertical = 24.dp),
                    )
                }
            }

            // Bottom-centred so it is reachable one-handed.
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
            ) {
                LargeFloatingActionButton(
                    onClick = onRecordClick,
                    shape = CircleShape,
                    modifier = Modifier.size(112.dp),
                ) {
                    Icon(
                        imageVector = if (state.isListening) Icons.Filled.Stop else Icons.Filled.Mic,
                        contentDescription = stringResource(
                            if (state.isListening) R.string.record_stop else R.string.record_start,
                        ),
                        modifier = Modifier.size(48.dp),
                    )
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@get:StringRes
private val HomeProblem.message: Int
    get() = when (this) {
        HomeProblem.MicrophoneDenied -> R.string.problem_microphone_denied
        HomeProblem.RecognitionUnavailable -> R.string.problem_recognition_unavailable
        HomeProblem.LanguageUnavailable -> R.string.problem_language_unavailable
        HomeProblem.NothingHeard -> R.string.problem_nothing_heard
        HomeProblem.Failed -> R.string.problem_failed
    }

@Preview(showBackground = true, backgroundColor = 0xFF14161A)
@Composable
private fun HomeIdlePreview() {
    MurmelTheme {
        HomeContent(state = HomeUiState(), onRecordClick = {})
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF14161A)
@Composable
private fun HomeTranscriptPreview() {
    MurmelTheme {
        HomeContent(
            state = HomeUiState(isListening = true, transcript = "Um drei gestillt, links, dann eine Stunde geschlafen"),
            onRecordClick = {},
        )
    }
}
