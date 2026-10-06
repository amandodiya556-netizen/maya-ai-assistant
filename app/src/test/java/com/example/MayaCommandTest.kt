package com.example

import com.example.data.local.PersonalityMode
import com.example.data.local.TaskCategory
import com.example.data.local.TaskPriority
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MayaCommandTest {

    @Test
    fun testPersonalityModes() {
        assertEquals("JARVIS Protocol", PersonalityMode.JARVIS.displayName)
        assertEquals("Companion Mode", PersonalityMode.COMPANION.displayName)
    }

    @Test
    fun testTaskPriorityEnum() {
        val priority = TaskPriority.HIGH
        assertEquals("HIGH", priority.name)
        assertTrue(TaskPriority.values().contains(TaskPriority.CRITICAL))
    }

    @Test
    fun testTaskCategoryEnum() {
        val cat = TaskCategory.CONTENT
        assertEquals("CONTENT", cat.name)
        assertTrue(TaskCategory.values().contains(TaskCategory.WORK))
    }
}
