package app.murmel.ui.home

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.murmel.R
import app.murmel.braindump.Person
import app.murmel.ui.theme.MurmelTheme

/** What the header shows. Real values come from the database in Milestone 2. */
data class AtAGlance(
    /** Schwangerschaftswoche (SSW). */
    val pregnancyWeek: Int,
    val nextAppointment: NextAppointment?,
    val openQuestions: Int,
)

data class NextAppointment(
    /** Already formatted, e.g. "28.10., 10:00". Formatting dates properly is unit T1. */
    val dateAndTime: String,
    val person: Person,
)

/** Shown until real data exists (Milestone 2). */
val PlaceholderAtAGlance = AtAGlance(
    pregnancyWeek = 24,
    nextAppointment = NextAppointment(dateAndTime = "28.10., 10:00", person = Person.Midwife),
    openQuestions = 3,
)

/** Current SSW, the next appointment and how many questions are still open. */
@Composable
fun AtAGlanceHeader(
    glance: AtAGlance,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = stringResource(R.string.glance_pregnancy_week, glance.pregnancyWeek),
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = glance.nextAppointment?.let {
                    stringResource(R.string.glance_next_appointment, it.dateAndTime, stringResource(it.person.label))
                } ?: stringResource(R.string.glance_no_appointment),
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = pluralStringResource(R.plurals.glance_open_questions, glance.openQuestions, glance.openQuestions),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )
        }
    }
}

@get:StringRes
private val Person.label: Int
    get() = when (this) {
        Person.Doctor -> R.string.person_doctor
        Person.Midwife -> R.string.person_midwife
        Person.Partner -> R.string.person_partner
        Person.Self -> R.string.person_self
    }

@Preview(name = "With appointment", showBackground = true, backgroundColor = 0xFF14161A)
@Composable
private fun AtAGlanceHeaderPreview() {
    MurmelTheme {
        AtAGlanceHeader(glance = PlaceholderAtAGlance)
    }
}

@Preview(name = "No appointment, one question", showBackground = true, backgroundColor = 0xFF14161A)
@Composable
private fun AtAGlanceHeaderEmptyPreview() {
    MurmelTheme {
        AtAGlanceHeader(glance = AtAGlance(pregnancyWeek = 12, nextAppointment = null, openQuestions = 1))
    }
}
