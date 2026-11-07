package com.example.audio

import android.content.Context
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.net.Uri
import com.example.model.DeckId
import com.example.model.DeckState
import com.example.model.DjTrack
import com.example.model.MixerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.abs
import kotlin.math.sin

class DjAudioEngine(private val context: Context) {

    private var playerA: MediaPlayer? = null
    private var playerB: MediaPlayer? = null

    private val scratchSynthA = ScratchSynthesizer()
    private val scratchSynthB = ScratchSynthesizer()

    private val _deckStateA = MutableStateFlow(DeckState(deckId = DeckId.DECK_A))
    val deckStateA: StateFlow<DeckState> = _deckStateA.asStateFlow()

    private val _deckStateB = MutableStateFlow(DeckState(deckId = DeckId.DECK_B))
    val deckStateB: StateFlow<DeckState> = _deckStateB.asStateFlow()

    private val _mixerState = MutableStateFlow(MixerState())
    val mixerState: StateFlow<MixerState> = _mixerState.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Main)
    private var syncTickerJob: Job? = null

    init {
        startSyncTicker()
    }

    private fun startSyncTicker() {
        syncTickerJob = scope.launch {
            var step = 0
            while (isActive) {
                delay(33) // ~30 fps state updates
                step++

                // Update Deck A position & loop check
                updatePlayerPosition(DeckId.DECK_A)
                // Update Deck B position & loop check
                updatePlayerPosition(DeckId.DECK_B)

                // Update VU levels
                updateVuMeters(step)
            }
        }
    }

    private fun updatePlayerPosition(deckId: DeckId) {
        val player = if (deckId == DeckId.DECK_A) playerA else playerB
        val stateFlow = if (deckId == DeckId.DECK_A) _deckStateA else _deckStateB
        val currentState = stateFlow.value

        if (player != null && player.isPlaying) {
            val currentPos = player.currentPosition.toLong()
            val duration = player.duration.toLong().coerceAtLeast(180_000L)

            // Check if loop is active
            if (currentState.isLoopActive && currentState.loopEndMs > currentState.loopStartMs) {
                if (currentPos >= currentState.loopEndMs || currentPos < currentState.loopStartMs) {
                    player.seekTo(currentState.loopStartMs.toInt())
                    stateFlow.value = currentState.copy(
                        positionMs = currentState.loopStartMs,
                        durationMs = duration
                    )
                    return
                }
            }

            // Jog spin animation when playing
            val newJogAngle = (currentState.jogAngleDeg + (currentState.currentBpm / 128f) * 4.5f) % 360f
            stateFlow.value = currentState.copy(
                positionMs = currentPos,
                durationMs = duration,
                jogAngleDeg = newJogAngle
            )
        }
    }

    private fun updateVuMeters(step: Int) {
        val stA = _deckStateA.value
        val stB = _deckStateB.value
        val mx = _mixerState.value

        val baseBeatA = if (stA.isPlaying) {
            val beatPhase = (stA.positionMs.toFloat() / (60000f / stA.currentBpm)) % 1f
            if (beatPhase < 0.25f) 0.92f else 0.45f + 0.15f * sin(step * 0.4f)
        } else 0f

        val baseBeatB = if (stB.isPlaying) {
            val beatPhase = (stB.positionMs.toFloat() / (60000f / stB.currentBpm)) % 1f
            if (beatPhase < 0.25f) 0.90f else 0.42f + 0.15f * sin(step * 0.4f + 1f)
        } else 0f

        val vuA = (baseBeatA * stA.volume).coerceIn(0f, 1f)
        val vuB = (baseBeatB * stB.volume).coerceIn(0f, 1f)

        val crossA = if (mx.crossfader <= 0.5f) 1f else (1f - mx.crossfader) * 2f
        val crossB = if (mx.crossfader >= 0.5f) 1f else mx.crossfader * 2f

        val leftMaster = ((vuA * crossA + vuB * crossB * 0.85f) * mx.masterVolume).coerceIn(0f, 1f)
        val rightMaster = ((vuA * crossA * 0.85f + vuB * crossB) * mx.masterVolume).coerceIn(0f, 1f)

        _mixerState.value = mx.copy(
            vuLevelA = vuA,
            vuLevelB = vuB,
            vuLevelLeft = leftMaster,
            vuLevelRight = rightMaster
        )
    }

    fun loadTrack(deckId: DeckId, track: DjTrack) {
        scope.launch(Dispatchers.IO) {
            val file = if (track.audioPath != null) {
                File(track.audioPath)
            } else {
                ProceduralBeatGenerator.generateTrackWav(
                    context = context,
                    fileName = track.id,
                    bpm = track.bpm,
                    keyFreq = if (deckId == DeckId.DECK_A) 130.81f else 146.83f, // C3 vs D3
                    styleVariant = if (deckId == DeckId.DECK_A) 0 else 1
                )
            }

            scope.launch(Dispatchers.Main) {
                val player = if (deckId == DeckId.DECK_A) {
                    playerA?.release()
                    MediaPlayer().also { playerA = it }
                } else {
                    playerB?.release()
                    MediaPlayer().also { playerB = it }
                }

                try {
                    if (track.isStreamOrYouTube && track.audioPath != null) {
                        player.setDataSource(context, Uri.parse(track.audioPath))
                    } else {
                        player.setDataSource(file.absolutePath)
                    }
                    player.prepare()
                    player.isLooping = true

                    val duration = player.duration.toLong().coerceAtLeast(track.durationMs)
                    val stateFlow = if (deckId == DeckId.DECK_A) _deckStateA else _deckStateB

                    stateFlow.value = stateFlow.value.copy(
                        track = track,
                        baseBpm = track.bpm,
                        durationMs = duration,
                        positionMs = 0L,
                        isPlaying = false,
                        hotCues = track.defaultCues,
                        cuePositionMs = 0L
                    )

                    updateAudioVolumes()
                    applyPlaybackParams(deckId)
                } catch (_: Exception) {
                    // Fallback track info set
                    val stateFlow = if (deckId == DeckId.DECK_A) _deckStateA else _deckStateB
                    stateFlow.value = stateFlow.value.copy(
                        track = track,
                        baseBpm = track.bpm,
                        durationMs = track.durationMs,
                        positionMs = 0L,
                        hotCues = track.defaultCues
                    )
                }
            }
        }
    }

    fun togglePlayPause(deckId: DeckId) {
        val player = if (deckId == DeckId.DECK_A) playerA else playerB
        val stateFlow = if (deckId == DeckId.DECK_A) _deckStateA else _deckStateB
        val currentState = stateFlow.value

        if (player == null) {
            // If no track loaded, load default catalog track
            val defaultTrack = if (deckId == DeckId.DECK_A) {
                com.example.data.DefaultTrackCatalog.initialTracks[0]
            } else {
                com.example.data.DefaultTrackCatalog.initialTracks[1]
            }
            loadTrack(deckId, defaultTrack)
            return
        }

        try {
            if (player.isPlaying) {
                player.pause()
                stateFlow.value = currentState.copy(isPlaying = false)
            } else {
                updateAudioVolumes()
                applyPlaybackParams(deckId)
                player.start()
                stateFlow.value = currentState.copy(isPlaying = true)
            }
        } catch (_: Exception) {
        }
    }

    fun cuePress(deckId: DeckId) {
        val player = if (deckId == DeckId.DECK_A) playerA else playerB
        val stateFlow = if (deckId == DeckId.DECK_A) _deckStateA else _deckStateB
        val currentState = stateFlow.value

        if (player == null) return

        try {
            if (player.isPlaying) {
                // Pause and jump to cue
                player.pause()
                player.seekTo(currentState.cuePositionMs.toInt())
                stateFlow.value = currentState.copy(
                    isPlaying = false,
                    positionMs = currentState.cuePositionMs
                )
            } else {
                // Update cue point to current position
                val current = player.currentPosition.toLong()
                stateFlow.value = currentState.copy(cuePositionMs = current)
            }
        } catch (_: Exception) {
        }
    }

    fun syncDecks(deckId: DeckId) {
        // Sync target deck to match the other deck's BPM
        val otherState = if (deckId == DeckId.DECK_A) _deckStateB.value else _deckStateA.value
        val targetFlow = if (deckId == DeckId.DECK_A) _deckStateA else _deckStateB

        val targetBpm = otherState.currentBpm
        val baseBpm = targetFlow.value.baseBpm
        val neededPitchPercent = ((targetBpm / baseBpm) - 1.0f) * 100f

        targetFlow.value = targetFlow.value.copy(
            pitchPercent = neededPitchPercent.coerceIn(-50f, 50f),
            isSyncActive = true
        )
        applyPlaybackParams(deckId)
    }

    fun setPitchPercent(deckId: DeckId, percent: Float) {
        val targetFlow = if (deckId == DeckId.DECK_A) _deckStateA else _deckStateB
        targetFlow.value = targetFlow.value.copy(
            pitchPercent = percent.coerceIn(-50f, 50f),
            isSyncActive = false
        )
        applyPlaybackParams(deckId)
    }

    fun nudgePitch(deckId: DeckId, deltaPercent: Float) {
        val targetFlow = if (deckId == DeckId.DECK_A) _deckStateA else _deckStateB
        val newPercent = (targetFlow.value.pitchPercent + deltaPercent).coerceIn(-50f, 50f)
        targetFlow.value = targetFlow.value.copy(pitchPercent = newPercent)
        applyPlaybackParams(deckId)
    }

    fun toggleKeyLock(deckId: DeckId) {
        val targetFlow = if (deckId == DeckId.DECK_A) _deckStateA else _deckStateB
        targetFlow.value = targetFlow.value.copy(isKeyLocked = !targetFlow.value.isKeyLocked)
        applyPlaybackParams(deckId)
    }

    fun toggleVinylMode(deckId: DeckId) {
        val targetFlow = if (deckId == DeckId.DECK_A) _deckStateA else _deckStateB
        targetFlow.value = targetFlow.value.copy(isVinylMode = !targetFlow.value.isVinylMode)
    }

    fun toggleSlipMode(deckId: DeckId) {
        val targetFlow = if (deckId == DeckId.DECK_A) _deckStateA else _deckStateB
        targetFlow.value = targetFlow.value.copy(isSlipMode = !targetFlow.value.isSlipMode)
    }

    fun setHotCue(deckId: DeckId, cueNumber: Int) {
        val stateFlow = if (deckId == DeckId.DECK_A) _deckStateA else _deckStateB
        val current = stateFlow.value.positionMs
        val updated = stateFlow.value.hotCues.toMutableMap()
        updated[cueNumber] = current
        stateFlow.value = stateFlow.value.copy(hotCues = updated)
    }

    fun jumpToHotCue(deckId: DeckId, cueNumber: Int) {
        val player = if (deckId == DeckId.DECK_A) playerA else playerB
        val stateFlow = if (deckId == DeckId.DECK_A) _deckStateA else _deckStateB
        val cueMs = stateFlow.value.hotCues[cueNumber] ?: return

        try {
            player?.seekTo(cueMs.toInt())
            stateFlow.value = stateFlow.value.copy(positionMs = cueMs)
        } catch (_: Exception) {
        }
    }

    fun seekTo(deckId: DeckId, targetMs: Long) {
        val player = if (deckId == DeckId.DECK_A) playerA else playerB
        val stateFlow = if (deckId == DeckId.DECK_A) _deckStateA else _deckStateB
        val duration = stateFlow.value.durationMs
        val clamped = targetMs.coerceIn(0L, duration)

        try {
            player?.seekTo(clamped.toInt())
            stateFlow.value = stateFlow.value.copy(positionMs = clamped)
        } catch (_: Exception) {
        }
    }

    fun setLoopLength(deckId: DeckId, beats: Float) {
        val stateFlow = if (deckId == DeckId.DECK_A) _deckStateA else _deckStateB
        stateFlow.value = stateFlow.value.copy(selectedLoopBeats = beats)
        if (stateFlow.value.isLoopActive) {
            startLoop(deckId, beats)
        }
    }

    fun toggleLoop(deckId: DeckId) {
        val stateFlow = if (deckId == DeckId.DECK_A) _deckStateA else _deckStateB
        if (stateFlow.value.isLoopActive) {
            stateFlow.value = stateFlow.value.copy(isLoopActive = false)
        } else {
            startLoop(deckId, stateFlow.value.selectedLoopBeats)
        }
    }

    fun loopIn(deckId: DeckId) {
        val stateFlow = if (deckId == DeckId.DECK_A) _deckStateA else _deckStateB
        stateFlow.value = stateFlow.value.copy(
            loopStartMs = stateFlow.value.positionMs,
            isLoopActive = false
        )
    }

    fun loopOut(deckId: DeckId) {
        val stateFlow = if (deckId == DeckId.DECK_A) _deckStateA else _deckStateB
        val start = stateFlow.value.loopStartMs
        val end = stateFlow.value.positionMs.coerceAtLeast(start + 500L)
        stateFlow.value = stateFlow.value.copy(
            loopEndMs = end,
            isLoopActive = true
        )
    }

    private fun startLoop(deckId: DeckId, beats: Float) {
        val stateFlow = if (deckId == DeckId.DECK_A) _deckStateA else _deckStateB
        val current = stateFlow.value.positionMs
        val bpm = stateFlow.value.currentBpm
        val loopDurationMs = ((60000f / bpm) * beats).toLong()

        stateFlow.value = stateFlow.value.copy(
            isLoopActive = true,
            loopStartMs = current,
            loopEndMs = current + loopDurationMs
        )
    }

    // Scratch handling
    fun onJogTouchStart(deckId: DeckId) {
        val stateFlow = if (deckId == DeckId.DECK_A) _deckStateA else _deckStateB
        val synth = if (deckId == DeckId.DECK_A) scratchSynthA else scratchSynthB
        stateFlow.value = stateFlow.value.copy(isScratching = true)
        synth.startScratch(1.0f)
    }

    fun onJogScratchMove(deckId: DeckId, angleDeltaDeg: Float, velocity: Float) {
        val player = if (deckId == DeckId.DECK_A) playerA else playerB
        val stateFlow = if (deckId == DeckId.DECK_A) _deckStateA else _deckStateB
        val synth = if (deckId == DeckId.DECK_A) scratchSynthA else scratchSynthB
        val current = stateFlow.value

        val newAngle = (current.jogAngleDeg + angleDeltaDeg) % 360f
        synth.updateVelocity(velocity)

        // Scrub audio position
        val msDelta = (angleDeltaDeg * 15f).toLong()
        val newPos = (current.positionMs + msDelta).coerceIn(0L, current.durationMs)

        stateFlow.value = current.copy(
            jogAngleDeg = newAngle,
            positionMs = newPos
        )

        try {
            player?.seekTo(newPos.toInt())
        } catch (_: Exception) {
        }
    }

    fun onJogTouchEnd(deckId: DeckId) {
        val stateFlow = if (deckId == DeckId.DECK_A) _deckStateA else _deckStateB
        val synth = if (deckId == DeckId.DECK_A) scratchSynthA else scratchSynthB
        stateFlow.value = stateFlow.value.copy(isScratching = false)
        synth.stopScratch()
    }

    // Mixer & EQ Controls
    fun setCrossfader(value: Float) {
        _mixerState.value = _mixerState.value.copy(crossfader = value.coerceIn(0f, 1f))
        updateAudioVolumes()
    }

    fun setMasterVolume(value: Float) {
        _mixerState.value = _mixerState.value.copy(masterVolume = value.coerceIn(0f, 1f))
        updateAudioVolumes()
    }

    fun setDeckVolume(deckId: DeckId, volume: Float) {
        val stateFlow = if (deckId == DeckId.DECK_A) _deckStateA else _deckStateB
        stateFlow.value = stateFlow.value.copy(volume = volume.coerceIn(0f, 1f))
        updateAudioVolumes()
    }

    fun setDeckGain(deckId: DeckId, gainDb: Float) {
        val stateFlow = if (deckId == DeckId.DECK_A) _deckStateA else _deckStateB
        stateFlow.value = stateFlow.value.copy(gainDb = gainDb.coerceIn(-12f, 12f))
        updateAudioVolumes()
    }

    fun setDeckEq(deckId: DeckId, high: Float? = null, mid: Float? = null, low: Float? = null) {
        val stateFlow = if (deckId == DeckId.DECK_A) _deckStateA else _deckStateB
        stateFlow.value = stateFlow.value.copy(
            eqHighDb = high?.coerceIn(-12f, 12f) ?: stateFlow.value.eqHighDb,
            eqMidDb = mid?.coerceIn(-12f, 12f) ?: stateFlow.value.eqMidDb,
            eqLowDb = low?.coerceIn(-12f, 12f) ?: stateFlow.value.eqLowDb
        )
    }

    fun toggleEqKill(deckId: DeckId, band: String) {
        val stateFlow = if (deckId == DeckId.DECK_A) _deckStateA else _deckStateB
        when (band.uppercase()) {
            "HIGH" -> stateFlow.value = stateFlow.value.copy(isHighKill = !stateFlow.value.isHighKill)
            "MID" -> stateFlow.value = stateFlow.value.copy(isMidKill = !stateFlow.value.isMidKill)
            "LOW" -> stateFlow.value = stateFlow.value.copy(isLowKill = !stateFlow.value.isLowKill)
        }
    }

    fun setFilterCutoff(deckId: DeckId, cutoff: Float) {
        val stateFlow = if (deckId == DeckId.DECK_A) _deckStateA else _deckStateB
        stateFlow.value = stateFlow.value.copy(filterCutoff = cutoff.coerceIn(-1f, 1f))
    }

    fun toggleHeadphoneCue(deckId: DeckId) {
        val mx = _mixerState.value
        _mixerState.value = if (deckId == DeckId.DECK_A) {
            mx.copy(headphoneCueA = !mx.headphoneCueA)
        } else {
            mx.copy(headphoneCueB = !mx.headphoneCueB)
        }
    }

    fun setMixerTab(tab: com.example.model.MixerTab) {
        _mixerState.value = _mixerState.value.copy(activeTab = tab)
    }

    private fun updateAudioVolumes() {
        val cross = _mixerState.value.crossfader
        val master = _mixerState.value.masterVolume

        // Equal-power crossfader curve
        val crossA = if (cross <= 0.5f) 1.0f else (1.0f - cross) * 2.0f
        val crossB = if (cross >= 0.5f) 1.0f else cross * 2.0f

        val volA = _deckStateA.value.volume
        val gainA = Math.pow(10.0, (_deckStateA.value.gainDb / 20.0)).toFloat()
        val finalA = (crossA * volA * master * gainA).coerceIn(0f, 1f)

        val volB = _deckStateB.value.volume
        val gainB = Math.pow(10.0, (_deckStateB.value.gainDb / 20.0)).toFloat()
        val finalB = (crossB * volB * master * gainB).coerceIn(0f, 1f)

        try {
            playerA?.setVolume(finalA, finalA)
            playerB?.setVolume(finalB, finalB)
        } catch (_: Exception) {
        }
    }

    private fun applyPlaybackParams(deckId: DeckId) {
        val player = if (deckId == DeckId.DECK_A) playerA else playerB
        val state = if (deckId == DeckId.DECK_A) _deckStateA.value else _deckStateB.value

        if (player == null) return

        try {
            val speedFactor = (1.0f + state.pitchPercent / 100.0f).coerceIn(0.25f, 2.0f)
            val params = PlaybackParams().apply {
                speed = speedFactor
                pitch = if (state.isKeyLocked) 1.0f else speedFactor
            }
            player.playbackParams = params
        } catch (_: Exception) {
        }
    }

    fun release() {
        syncTickerJob?.cancel()
        try {
            playerA?.stop()
            playerA?.release()
            playerB?.stop()
            playerB?.release()
            scratchSynthA.release()
            scratchSynthB.release()
        } catch (_: Exception) {
        }
    }
}
