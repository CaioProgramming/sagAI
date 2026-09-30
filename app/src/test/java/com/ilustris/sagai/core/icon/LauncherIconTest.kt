package com.ilustris.sagai.core.icon

import com.ilustris.sagai.features.newsaga.data.model.Genre
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class LauncherIconTest {
    @Test
    fun `every genre has its own icon`() {
        Genre.entries.forEach { genre ->
            assertEquals("$genre should map to its own icon", genre, LauncherIcon.forGenre(genre).genre)
        }
    }

    @Test
    fun `no genre falls back to the default icon`() {
        assertEquals(LauncherIcon.DEFAULT, LauncherIcon.forGenre(null))
    }

    @Test
    fun `every icon has an alias in the manifest`() {
        val manifest = File("src/main/AndroidManifest.xml").readText()
        LauncherIcon.entries.forEach { icon ->
            assertTrue("${icon.aliasName} is missing from the manifest", manifest.contains("android:name=\".launcher.${icon.aliasName}\""))
        }
    }

    @Test
    fun `only the default alias starts enabled`() {
        val manifest = File("src/main/AndroidManifest.xml").readText()
        val enabled = Regex("""<activity-alias\s+android:name="\.launcher\.(\w+)"\s+android:enabled="true"""").findAll(manifest).map { it.groupValues[1] }.toList()
        assertEquals(listOf(LauncherIcon.DEFAULT.aliasName), enabled)
    }
}
