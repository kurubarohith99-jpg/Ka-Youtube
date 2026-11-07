package com.example.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.DjAudioEngine
import com.example.audio.MixRecorder
import com.example.audio.ProceduralBeatGenerator
import com.example.audio.SamplerManager
import com.example.data.DefaultTrackCatalog
import com.example.model.DeckId
import com.example.model.DeckState
import com.example.model.DjSkin
import com.example.model.DjTrack
import com.example.model.MixerState
import com.example.model.MixerTab
import com.example.model.RecordedMix
import com.example.model.SampleType
import com.example.model.SamplerPad
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DjViewModel(application: Application) : AndroidViewModel(application) {

    private val audioEngine = DjAudioEngine(application)
    private val samplerManager = SamplerManager(application)
    private val mixRecorder = MixRecorder(application)

    val deckA: StateFlow<DeckState> = audioEngine.deckStateA
    val deckB: StateFlow<DeckState> = audioEngine.deckStateB
    val mixerState: StateFlow<MixerState> = audioEngine.mixerState

    private val _allTracks = MutableStateFlow<List<DjTrack>>(DefaultTrackCatalog.initialTracks)
    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow("All Tracks")

    val filteredTracks: StateFlow<List<DjTrack>> = combine(
        _allTracks,
        searchQuery,
        selectedCategory
    ) { tracks, query, category ->
        var list = tracks
        if (category != "All Tracks") {
            list = when (category) {
                "Club / EDM" -> list.filter { it.genre.contains("EDM", ignoreCase = true) || it.genre.contains("House", ignoreCase = true) }
                "Remixes" -> list.filter { it.remix.contains("Remix", ignoreCase = true) || it.remix.contains("Bootleg", ignoreCase = true) }
                "Cloud Streams" -> list.filter { it.isStreamOrYouTube }
                else -> list
            }
        }
        if (query.isNotBlank()) {
            list = list.filter {
                it.title.contains(query, ignoreCase = true) ||
                    it.artist.contains(query, ignoreCase = true) ||
                    it.remix.contains(query, ignoreCase = true)
            }
        }
        list
    }.stateIn(viewModelScope, SharingStarted.Lazily, DefaultTrackCatalog.initialTracks)

    private val _selectedTrackForInspect = MutableStateFlow<DjTrack?>(DefaultTrackCatalog.initialTracks[2])
    val selectedTrackForInspect: StateFlow<DjTrack?> = _selectedTrackForInspect.asStateFlow()

    private val _activeSkin = MutableStateFlow(DjSkin.ClassicDark)
    val activeSkin: StateFlow<DjSkin> = _activeSkin.asStateFlow()

    val recordedMixes: StateFlow<List<RecordedMix>> = mixRecorder.recordedMixes
    val isRecording: StateFlow<Boolean> = mixRecorder.isRecording
    val recordDurationSec: StateFlow<Int> = mixRecorder.durationSec

    private val _samplerPads = MutableStateFlow(
        listOf(
            SamplerPad(1, "1. Saxo", SampleType.SAXO),
            SamplerPad(2, "2. This This", SampleType.THIS_THIS),
            SamplerPad(3, "3. Shots Shots", SampleType.SHOTS),
            SamplerPad(4, "4. Hands Up", SampleType.HANDS_UP),
            SamplerPad(5, "5. Airhorn", SampleType.AIRHORN),
            SamplerPad(6, "6. Siren", SampleType.SIREN),
            SamplerPad(7, "7. Laser", SampleType.LASER),
            SamplerPad(8, "8. Vinyl Drop", SampleType.VINYL_DROP)
        )
    )
    val samplerPads: StateFlow<List<SamplerPad>> = _samplerPads.asStateFlow()

    init {
        // Load default Deck A and Deck B tracks
        audioEngine.loadTrack(DeckId.DECK_A, DefaultTrackCatalog.initialTracks[0])
        audioEngine.loadTrack(DeckId.DECK_B, DefaultTrackCatalog.initialTracks[1])
    }

    fun loadTrackToDeck(deckId: DeckId, track: DjTrack) {
        audioEngine.loadTrack(deckId, track)
        _selectedTrackForInspect.value = track
    }

    fun togglePlayPause(deckId: DeckId) {
        audioEngine.togglePlayPause(deckId)
    }

    fun cue(deckId: DeckId) {
        audioEngine.cuePress(deckId)
    }

    fun sync(deckId: DeckId) {
        audioEngine.syncDecks(deckId)
    }

    fun setPitchPercent(deckId: DeckId, percent: Float) {
        audioEngine.setPitchPercent(deckId, percent)
    }

    fun nudgePitch(deckId: DeckId, delta: Float) {
        audioEngine.nudgePitch(deckId, delta)
    }

    fun toggleKeyLock(deckId: DeckId) {
        audioEngine.toggleKeyLock(deckId)
    }

    fun toggleVinylMode(deckId: DeckId) {
        audioEngine.toggleVinylMode(deckId)
    }

    fun toggleSlipMode(deckId: DeckId) {
        audioEngine.toggleSlipMode(deckId)
    }

    fun setHotCue(deckId: DeckId, cueNumber: Int) {
        audioEngine.setHotCue(deckId, cueNumber)
    }

    fun jumpToHotCue(deckId: DeckId, cueNumber: Int) {
        audioEngine.jumpToHotCue(deckId, cueNumber)
    }

    fun seekTo(deckId: DeckId, positionMs: Long) {
        audioEngine.seekTo(deckId, positionMs)
    }

    fun toggleLoop(deckId: DeckId) {
        audioEngine.toggleLoop(deckId)
    }

    fun setLoopLength(deckId: DeckId, beats: Float) {
        audioEngine.setLoopLength(deckId, beats)
    }

    fun loopIn(deckId: DeckId) {
        audioEngine.loopIn(deckId)
    }

    fun loopOut(deckId: DeckId) {
        audioEngine.loopOut(deckId)
    }

    // Scratching
    fun onJogTouchStart(deckId: DeckId) {
        audioEngine.onJogTouchStart(deckId)
    }

    fun onJogScratchMove(deckId: DeckId, angleDeltaDeg: Float, velocity: Float) {
        audioEngine.onJogScratchMove(deckId, angleDeltaDeg, velocity)
    }

    fun onJogTouchEnd(deckId: DeckId) {
        audioEngine.onJogTouchEnd(deckId)
    }

    // Mixer
    fun setCrossfader(value: Float) {
        audioEngine.setCrossfader(value)
    }

    fun setMasterVolume(value: Float) {
        audioEngine.setMasterVolume(value)
    }

    fun setDeckVolume(deckId: DeckId, volume: Float) {
        audioEngine.setDeckVolume(deckId, volume)
    }

    fun setDeckGain(deckId: DeckId, gainDb: Float) {
        audioEngine.setDeckGain(deckId, gainDb)
    }

    fun setDeckEq(deckId: DeckId, high: Float? = null, mid: Float? = null, low: Float? = null) {
        audioEngine.setDeckEq(deckId, high, mid, low)
    }

    fun toggleEqKill(deckId: DeckId, band: String) {
        audioEngine.toggleEqKill(deckId, band)
    }

    fun setFilterCutoff(deckId: DeckId, cutoff: Float) {
        audioEngine.setFilterCutoff(deckId, cutoff)
    }

    fun toggleHeadphoneCue(deckId: DeckId) {
        audioEngine.toggleHeadphoneCue(deckId)
    }

    fun setMixerTab(tab: MixerTab) {
        audioEngine.setMixerTab(tab)
    }

    // Sampler
    fun triggerSamplerPad(pad: SamplerPad) {
        samplerManager.play(pad.sampleType)
        viewModelScope.launch {
            _samplerPads.value = _samplerPads.value.map {
                if (it.id == pad.id) it.copy(isPlaying = true) else it
            }
            kotlinx.coroutines.delay(300)
            _samplerPads.value = _samplerPads.value.map {
                if (it.id == pad.id) it.copy(isPlaying = false) else it
            }
        }
    }

    // Live Recording
    fun toggleRecording(customTitle: String? = null) {
        if (mixRecorder.isRecording.value) {
            mixRecorder.stopRecording()
        } else {
            mixRecorder.startRecording(customTitle)
        }
    }

    fun deleteMix(mix: RecordedMix) {
        mixRecorder.deleteMix(mix)
    }

    // YouTube / Cloud Stream Import
    fun loadYouTubeOrStream(url: String, title: String, targetDeck: DeckId) {
        val streamTrack = DjTrack(
            id = "stream_${System.currentTimeMillis()}",
            title = title.ifBlank { "YouTube / Web Stream Mix" },
            artist = "Online Stream",
            remix = "Live Mix",
            durationMs = 600_000L,
            bpm = 128.0f,
            musicalKey = "08B",
            genre = "Online Stream",
            audioPath = url,
            isStreamOrYouTube = true,
            waveformPoints = ProceduralBeatGenerator.generateWaveformPoints(250, 128.0f),
            coverColorHex = 0xFFEF4444
        )
        _allTracks.value = listOf(streamTrack) + _allTracks.value
        loadTrackToDeck(targetDeck, streamTrack)
    }

    // Local Device Audio Import
    fun loadLocalAudioFile(uri: Uri, displayName: String, targetDeck: DeckId) {
        val localTrack = DjTrack(
            id = "local_${System.currentTimeMillis()}",
            title = displayName.substringBeforeLast("."),
            artist = "My Music",
            remix = "Local Track",
            durationMs = 210_000L,
            bpm = 128.0f,
            musicalKey = "08A",
            genre = "Device Audio",
            audioPath = uri.toString(),
            isStreamOrYouTube = true,
            waveformPoints = ProceduralBeatGenerator.generateWaveformPoints(250, 128.0f),
            coverColorHex = 0xFF10B981
        )
        _allTracks.value = listOf(localTrack) + _allTracks.value
        loadTrackToDeck(targetDeck, localTrack)
    }

    fun setSkin(skin: DjSkin) {
        _activeSkin.value = skin
    }

    fun inspectTrack(track: DjTrack) {
        _selectedTrackForInspect.value = track
    }

    override fun onCleared() {
        super.onCleared()
        audioEngine.release()
        samplerManager.release()
    }
}
