package app.murmel.ui.home

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.murmel.R
import app.murmel.ui.theme.MurmelTheme

/**
 * The big record button: a microphone while idle, a stop square while listening.
 *
 * Stateless on purpose: it only shows [isListening] and reports taps through [onToggle].
 * The caller owns the state, so the same value can also drive the title text, and
 * later come from a ViewModel without changing this button.
 */
@Composable
fun RecordButton(
    isListening: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LargeFloatingActionButton(
        onClick = onToggle,
        shape = CircleShape,
        // CLAUDE.md: record button at least 96 dp, for one-handed use at night.
        modifier = modifier.size(112.dp),
    ) {
        Icon(
            imageVector = if (isListening) Icons.Filled.Stop else Icons.Filled.Mic,
            contentDescription = stringResource(
                if (isListening) R.string.record_stop else R.string.record_start,
            ),
            modifier = Modifier.size(48.dp),
        )
    }
}

@Preview(name = "Idle", showBackground = true, backgroundColor = 0xFF14161A)
@Composable
private fun RecordButtonIdlePreview() {
    MurmelTheme {
        RecordButton(isListening = false, onToggle = {})
    }
}

@Preview(name = "Listening", showBackground = true, backgroundColor = 0xFF14161A)
@Composable
private fun RecordButtonListeningPreview() {
    MurmelTheme {
        RecordButton(isListening = true, onToggle = {})
    }
}
