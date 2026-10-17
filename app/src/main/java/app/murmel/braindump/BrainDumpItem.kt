package app.murmel.braindump

/**
 * One thing extracted from a spoken brain dump.
 *
 * Mirrors the item schema in FEATURES.md (Milestone 1). The JSON field names `for` and
 * `when` are Kotlin keywords, so the properties are named after what they hold instead.
 */
data class BrainDumpItem(
    val type: ItemType,
    /** The cleaned-up wording, staying close to what was said. */
    val text: String,
    /** Who the item is for. Null when nobody was named. */
    val forPerson: Person? = null,
    /**
     * Date or time as the model wrote it (ISO 8601), e.g. "2026-11-03T10:00".
     * Kept as text here; turning it into java.time is the parser's job.
     */
    val mentionedTime: String? = null,
    /** Who an appointment is with, in the user's words, e.g. "Dr. Weber". */
    val appointmentWith: String? = null,
)

enum class ItemType { Question, Todo, Appointment, Answer, Note }

enum class Person { Arzt, Hebamme, Partner, Self }

/** The model's `type` value, e.g. "question", as an [ItemType]. Null if it isn't one we know. */
fun String.toItemType(): ItemType? = when (trim().lowercase()) {
    "question" -> ItemType.Question
    "todo" -> ItemType.Todo
    "appointment" -> ItemType.Appointment
    "answer" -> ItemType.Answer
    "note" -> ItemType.Note
    else -> null
}

/**
 * The model's `for` value, e.g. "hebamme", as a [Person].
 * Null if the value is missing or isn't one we know.
 */
fun String?.toPerson(): Person? = when (this?.trim()?.lowercase()) {
    "arzt", "ärztin" -> Person.Arzt
    "hebamme" -> Person.Hebamme
    "partner" -> Person.Partner
    "self" -> Person.Self
    else -> null
}
