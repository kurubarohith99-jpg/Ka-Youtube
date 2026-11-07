package com.example

import com.example.data.DefaultTrackCatalog
import com.example.model.DeckId
import com.example.model.DeckState
import com.example.model.DjSkin
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun `verify default track catalog loaded correctly`() {
        val tracks = DefaultTrackCatalog.initialTracks
        assertTrue("Catalog should contain tracks", tracks.size >= 10)

        val track1 = tracks[0]
        assertEquals("Ain't A Party Without Nirvana", track1.title)
        assertEquals(132.13f, track1.bpm, 0.01f)
        assertEquals("07B", track1.musicalKey)
        assertTrue("Waveform points should be populated", track1.waveformPoints.isNotEmpty())
    }

    @Test
    fun `verify dj skins configuration`() {
        assertEquals(4, DjSkin.allSkins.size)
        val classic = DjSkin.ClassicDark
        assertEquals("Virtual DJ Classic", classic.name)
        assertEquals(0xFF00E5FF, classic.deckAColor)
        assertEquals(0xFFFF1744, classic.deckBColor)
    }

    @Test
    fun `verify deck state BPM calculation with pitch`() {
        val state = DeckState(
            deckId = DeckId.DECK_A,
            baseBpm = 128.0f,
            pitchPercent = 8.0f
        )
        // 128 * 1.08 = 138.24
        assertEquals(138.24f, state.currentBpm, 0.01f)
    }
}
