package app.murmel.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Warm, low-glare palette: most logging happens at night with a baby in one arm.
private val Apricot = Color(0xFFF2C29B)
private val ApricotDeep = Color(0xFF9A5B33)
private val Night = Color(0xFF14161A)
private val NightSurface = Color(0xFF1E2128)
private val Mist = Color(0xFFE6E2DC)

private val DarkColors = darkColorScheme(
    primary = Apricot,
    onPrimary = Night,
    background = Night,
    onBackground = Mist,
    surface = NightSurface,
    onSurface = Mist,
)

private val LightColors = lightColorScheme(
    primary = ApricotDeep,
    onPrimary = Color.White,
    background = Color(0xFFFBF8F4),
    onBackground = Night,
    surface = Color.White,
    onSurface = Night,
)

/**
 * Dark is the default on purpose (3 AM feeds). A light mode toggle can come later.
 */
@Composable
fun MurmelTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
