package app.murmel.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.murmel.R
import app.murmel.ui.theme.MurmelTheme

/**
 * Home: one huge record button, reachable with a thumb, plus the (empty) timeline.
 *
 * Recording is UI-only for now. Milestone 1 wires this to the on-device
 * SpeechRecognizer; see FEATURES.md.
 */
@Composable
fun HomeScreen() {
    var isListening by rememberSaveable { mutableStateOf(false) }

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
                text = stringResource(if (isListening) R.string.recording else R.string.home_title),
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
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.timeline_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                )
            }

            // Bottom-centred so it is reachable one-handed.
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
            ) {
                RecordButton(
                    isListening = isListening,
                    onToggle = { isListening = !isListening },
                )
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF14161A)
@Composable
private fun HomeScreenPreview() {
    MurmelTheme {
        HomeScreen()
    }
}
