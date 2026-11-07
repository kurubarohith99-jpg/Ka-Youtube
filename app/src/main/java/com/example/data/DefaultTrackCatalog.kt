package com.example.data

import com.example.audio.ProceduralBeatGenerator
import com.example.model.DjTrack

object DefaultTrackCatalog {

    val initialTracks: List<DjTrack> = listOf(
        DjTrack(
            id = "track_1",
            title = "Ain't A Party Without Nirvana",
            artist = "David Guetta & GlowInTheDark Feat. Harrison",
            remix = "Original Club Mix",
            durationMs = 246_000L,
            bpm = 132.13f,
            musicalKey = "07B",
            genre = "Electro House",
            coverColorHex = 0xFF0284C7,
            waveformPoints = ProceduralBeatGenerator.generateWaveformPoints(250, 132.13f),
            defaultCues = mapOf(1 to 0L, 2 to 14530L, 3 to 29060L, 4 to 58120L)
        ),
        DjTrack(
            id = "track_2",
            title = "Used to Know Metropolis",
            artist = "David Guetta, Nicky Romero, Gotye",
            remix = "Festival Bootleg",
            durationMs = 337_000L,
            bpm = 132.13f,
            musicalKey = "08A",
            genre = "Progressive EDM",
            coverColorHex = 0xFFE11D48,
            waveformPoints = ProceduralBeatGenerator.generateWaveformPoints(250, 132.13f),
            defaultCues = mapOf(1 to 0L, 2 to 15000L, 3 to 30000L, 4 to 60000L)
        ),
        DjTrack(
            id = "track_3",
            title = "Can't You See",
            artist = "Shermanology & GRX",
            remix = "Xantra & David Hopperman Remix",
            durationMs = 274_000L,
            bpm = 128.0f,
            musicalKey = "10B",
            genre = "Big Room",
            coverColorHex = 0xFF10B981,
            waveformPoints = ProceduralBeatGenerator.generateWaveformPoints(250, 128.0f),
            defaultCues = mapOf(1 to 0L, 2 to 15000L, 3 to 37500L, 4 to 75000L)
        ),
        DjTrack(
            id = "track_4",
            title = "La La Land",
            artist = "Green Velvet",
            remix = "DJ Niki Remix",
            durationMs = 246_000L,
            bpm = 129.0f,
            musicalKey = "09B",
            genre = "Tech House",
            coverColorHex = 0xFF8B5CF6,
            waveformPoints = ProceduralBeatGenerator.generateWaveformPoints(250, 129.0f),
            defaultCues = mapOf(1 to 0L, 2 to 18600L, 3 to 37200L)
        ),
        DjTrack(
            id = "track_5",
            title = "Bad",
            artist = "David Guetta, Showtek Feat. Vassy",
            remix = "Original Mix",
            durationMs = 270_000L,
            bpm = 128.0f,
            musicalKey = "05A",
            genre = "Bounce / EDM",
            coverColorHex = 0xFFF59E0B,
            waveformPoints = ProceduralBeatGenerator.generateWaveformPoints(250, 128.0f),
            defaultCues = mapOf(1 to 0L, 2 to 15000L, 3 to 30000L, 4 to 60000L)
        ),
        DjTrack(
            id = "track_6",
            title = "As The Skylarking Comes",
            artist = "BT Vs. Motorcycle Vs. Ilan Bluestone",
            remix = "Anarchy Edit",
            durationMs = 420_000L,
            bpm = 130.0f,
            musicalKey = "10A",
            genre = "Trance",
            coverColorHex = 0xFF06B6D4,
            waveformPoints = ProceduralBeatGenerator.generateWaveformPoints(250, 130.0f),
            defaultCues = mapOf(1 to 0L, 2 to 29500L, 3 to 59000L)
        ),
        DjTrack(
            id = "track_7",
            title = "Rock The Boat",
            artist = "Bob Sinclar ft Pitbull & DragonFly",
            remix = "DJ Nejtrino & DJ Stranger Remix",
            durationMs = 305_000L,
            bpm = 129.0f,
            musicalKey = "08A",
            genre = "Club House",
            coverColorHex = 0xFFEC4899,
            waveformPoints = ProceduralBeatGenerator.generateWaveformPoints(250, 129.0f),
            defaultCues = mapOf(1 to 0L, 2 to 14880L, 3 to 29760L)
        ),
        DjTrack(
            id = "track_8",
            title = "Feeling Good",
            artist = "Nina Simone",
            remix = "Bassnectar Remix",
            durationMs = 226_000L,
            bpm = 160.0f,
            musicalKey = "02B",
            genre = "Glitch Hop / Bass",
            coverColorHex = 0xFFD946EF,
            waveformPoints = ProceduralBeatGenerator.generateWaveformPoints(250, 160.0f),
            defaultCues = mapOf(1 to 0L, 2 to 12000L, 3 to 24000L)
        ),
        DjTrack(
            id = "track_9",
            title = "Breakin' A Sweat",
            artist = "Skrillex & The Doors",
            remix = "DJ Nejtrino Remix",
            durationMs = 284_000L,
            bpm = 130.0f,
            musicalKey = "07B",
            genre = "Dubstep / Electro",
            coverColorHex = 0xFF14B8A6,
            waveformPoints = ProceduralBeatGenerator.generateWaveformPoints(250, 130.0f),
            defaultCues = mapOf(1 to 0L, 2 to 14760L, 3 to 29530L)
        ),
        DjTrack(
            id = "track_10",
            title = "Happy",
            artist = "Pharrell Williams",
            remix = "Craig Vanity VS Benny Benassi",
            durationMs = 337_000L,
            bpm = 150.0f,
            musicalKey = "05B",
            genre = "Electro Pop",
            coverColorHex = 0xFFF97316,
            waveformPoints = ProceduralBeatGenerator.generateWaveformPoints(250, 150.0f),
            defaultCues = mapOf(1 to 0L, 2 to 16000L, 3 to 32000L)
        )
    )

    val onlineStreamPresets: List<Pair<String, String>> = listOf(
        "Tomorrowland Live Stream" to "https://stream.tomorrowland.com/live.aac",
        "BBC Radio 1 Dance DJ Mix" to "https://stream.live.vc.bbcmedia.co.uk/bbc_radio_one",
        "Defected Radio Show" to "https://streaming.defected.com/radio.mp3",
        "Ibiza Global Radio Club Mix" to "https://live.ibizaglobalradio.com/stream"
    )
}
