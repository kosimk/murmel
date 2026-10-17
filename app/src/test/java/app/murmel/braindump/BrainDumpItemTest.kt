package app.murmel.braindump

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BrainDumpItemTest {

    @Test
    fun `every known type value maps to its ItemType`() {
        assertEquals(ItemType.Question, "question".toItemType())
        assertEquals(ItemType.Todo, "todo".toItemType())
        assertEquals(ItemType.Appointment, "appointment".toItemType())
        assertEquals(ItemType.Answer, "answer".toItemType())
        assertEquals(ItemType.Note, "note".toItemType())
    }

    @Test
    fun `type values tolerate case and surrounding spaces`() {
        assertEquals(ItemType.Question, " Question ".toItemType())
    }

    @Test
    fun `unknown type value gives null`() {
        assertNull("reminder".toItemType())
    }

    @Test
    fun `every known person value maps to its Person`() {
        assertEquals(Person.Arzt, "arzt".toPerson())
        assertEquals(Person.Arzt, "Ärztin".toPerson())
        assertEquals(Person.Hebamme, "hebamme".toPerson())
        assertEquals(Person.Partner, "partner".toPerson())
        assertEquals(Person.Self, "self".toPerson())
    }

    @Test
    fun `missing or unknown person gives null`() {
        assertNull(null.toPerson())
        assertNull("oma".toPerson())
    }

    @Test
    fun `items with the same values are equal, copy changes only what is named`() {
        val question = BrainDumpItem(ItemType.Question, "Darf ich noch schwimmen?", forPerson = Person.Hebamme)

        assertEquals(question, BrainDumpItem(ItemType.Question, "Darf ich noch schwimmen?", forPerson = Person.Hebamme))

        val forDoctor = question.copy(forPerson = Person.Arzt)
        assertEquals("Darf ich noch schwimmen?", forDoctor.text)
        assertNotEquals(question, forDoctor)
    }
}
