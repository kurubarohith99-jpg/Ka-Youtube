package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import com.example.model.DeckId
import com.example.ui.components.BottomBrowserSection
import com.example.ui.components.DeckControlPanel
import com.example.ui.components.MixerSection
import com.example.ui.components.RecordingStudioDialog
import com.example.ui.components.VirtualDjHeader
import com.example.ui.components.VirtualSkinDialog
import com.example.ui.components.YouTubeStreamDialog
import com.example.ui.theme.ConsoleBackground
import com.example.viewmodel.DjViewModel

@Composable
fun DjMainScreen(
    viewModel: DjViewModel,
    modifier: Modifier = Modifier
) {
    val deckA by viewModel.deckA.collectAsState()
    val deckB by viewModel.deckB.collectAsState()
    val mixerState by viewModel.mixerState.collectAsState()
    val filteredTracks by viewModel.filteredTracks.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedInspectTrack by viewModel.selectedTrackForInspect.collectAsState()
    val activeSkin by viewModel.activeSkin.collectAsState()
    val samplerPads by viewModel.samplerPads.collectAsState()
    val recordedMixes by viewModel.recordedMixes.collectAsState()
    val isRecording by viewModel.isRecording.collectAsState()
    val recordDurationSec by viewModel.recordDurationSec.collectAsState()

    var showYouTubeDialog by remember { mutableStateOf(false) }
    var showRecordingDialog by remember { mutableStateOf(false) }
    var showSkinDialog by remember { mutableStateOf(false) }
    var pendingImportDeck by remember { mutableStateOf(DeckId.DECK_A) }

    // Android Storage Access Framework (SAF) audio file picker
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val fileName = uri.lastPathSegment ?: "Local Track"
            viewModel.loadLocalAudioFile(uri, fileName, pendingImportDeck)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ConsoleBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("dj_main_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. Top Section: Virtual DJ Header with Dual Waveforms
            VirtualDjHeader(
                deckA = deckA,
                deckB = deckB,
                isRecording = isRecording,
                recordDurationSec = recordDurationSec,
                onOpenRecording = { showRecordingDialog = true },
                onOpenYouTubeDialog = { showYouTubeDialog = true },
                onOpenSkinDialog = { showSkinDialog = true }
            )

            // 2. Middle Section: Deck A (Left) + Mixer (Center) + Deck B (Right)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                // Deck A Surface (Blue Neon / Active Skin)
                DeckControlPanel(
                    deck = deckA,
                    deckId = DeckId.DECK_A,
                    accentColor = Color(activeSkin.deckAColor),
                    ledRingColors = activeSkin.jogLedColors.map { Color(it) },
                    onPlayPause = { viewModel.togglePlayPause(DeckId.DECK_A) },
                    onCue = { viewModel.cue(DeckId.DECK_A) },
                    onSync = { viewModel.sync(DeckId.DECK_A) },
                    onPitchChange = { viewModel.setPitchPercent(DeckId.DECK_A, it) },
                    onNudgePitch = { viewModel.nudgePitch(DeckId.DECK_A, it) },
                    onToggleKeyLock = { viewModel.toggleKeyLock(DeckId.DECK_A) },
                    onToggleVinylMode = { viewModel.toggleVinylMode(DeckId.DECK_A) },
                    onToggleSlipMode = { viewModel.toggleSlipMode(DeckId.DECK_A) },
                    onSetHotCue = { viewModel.setHotCue(DeckId.DECK_A, it) },
                    onJumpHotCue = { viewModel.jumpToHotCue(DeckId.DECK_A, it) },
                    onSeek = { viewModel.seekTo(DeckId.DECK_A, it) },
                    onToggleLoop = { viewModel.toggleLoop(DeckId.DECK_A) },
                    onSetLoopLength = { viewModel.setLoopLength(DeckId.DECK_A, it) },
                    onLoopIn = { viewModel.loopIn(DeckId.DECK_A) },
                    onLoopOut = { viewModel.loopOut(DeckId.DECK_A) },
                    onJogTouchStart = { viewModel.onJogTouchStart(DeckId.DECK_A) },
                    onJogScratchMove = { delta, vel -> viewModel.onJogScratchMove(DeckId.DECK_A, delta, vel) },
                    onJogTouchEnd = { viewModel.onJogTouchEnd(DeckId.DECK_A) },
                    modifier = Modifier.weight(1.35f)
                )

                // Center Mixer Console
                MixerSection(
                    deckA = deckA,
                    deckB = deckB,
                    mixer = mixerState,
                    onCrossfaderChange = { viewModel.setCrossfader(it) },
                    onDeckVolumeChange = { id, vol -> viewModel.setDeckVolume(id, vol) },
                    onDeckGainChange = { id, gain -> viewModel.setDeckGain(id, gain) },
                    onDeckEqChange = { id, h, m, l -> viewModel.setDeckEq(id, h, m, l) },
                    onFilterChange = { id, cutoff -> viewModel.setFilterCutoff(id, cutoff) },
                    onHeadphoneCueToggle = { id -> viewModel.toggleHeadphoneCue(id) },
                    onTabSelect = { viewModel.setMixerTab(it) },
                    modifier = Modifier.weight(1.1f)
                )

                // Deck B Surface (Red Neon / Active Skin)
                DeckControlPanel(
                    deck = deckB,
                    deckId = DeckId.DECK_B,
                    accentColor = Color(activeSkin.deckBColor),
                    ledRingColors = activeSkin.jogLedColors.reversed().map { Color(it) },
                    onPlayPause = { viewModel.togglePlayPause(DeckId.DECK_B) },
                    onCue = { viewModel.cue(DeckId.DECK_B) },
                    onSync = { viewModel.sync(DeckId.DECK_B) },
                    onPitchChange = { viewModel.setPitchPercent(DeckId.DECK_B, it) },
                    onNudgePitch = { viewModel.nudgePitch(DeckId.DECK_B, it) },
                    onToggleKeyLock = { viewModel.toggleKeyLock(DeckId.DECK_B) },
                    onToggleVinylMode = { viewModel.toggleVinylMode(DeckId.DECK_B) },
                    onToggleSlipMode = { viewModel.toggleSlipMode(DeckId.DECK_B) },
                    onSetHotCue = { viewModel.setHotCue(DeckId.DECK_B, it) },
                    onJumpHotCue = { viewModel.jumpToHotCue(DeckId.DECK_B, it) },
                    onSeek = { viewModel.seekTo(DeckId.DECK_B, it) },
                    onToggleLoop = { viewModel.toggleLoop(DeckId.DECK_B) },
                    onSetLoopLength = { viewModel.setLoopLength(DeckId.DECK_B, it) },
                    onLoopIn = { viewModel.loopIn(DeckId.DECK_B) },
                    onLoopOut = { viewModel.loopOut(DeckId.DECK_B) },
                    onJogTouchStart = { viewModel.onJogTouchStart(DeckId.DECK_B) },
                    onJogScratchMove = { delta, vel -> viewModel.onJogScratchMove(DeckId.DECK_B, delta, vel) },
                    onJogTouchEnd = { viewModel.onJogTouchEnd(DeckId.DECK_B) },
                    modifier = Modifier.weight(1.35f)
                )
            }

            // 3. Bottom Section: Music Library, Cover Flow, Sampler, and Inspector
            BottomBrowserSection(
                tracks = filteredTracks,
                selectedCategory = selectedCategory,
                searchQuery = searchQuery,
                selectedTrackForInspect = selectedInspectTrack,
                samplerPads = samplerPads,
                onSelectCategory = { viewModel.selectedCategory.value = it },
                onSearchChange = { viewModel.searchQuery.value = it },
                onLoadTrackToDeck = { id, track -> viewModel.loadTrackToDeck(id, track) },
                onInspectTrack = { viewModel.inspectTrack(it) },
                onTriggerSampler = { viewModel.triggerSamplerPad(it) },
                onOpenLocalFilePicker = {
                    pendingImportDeck = DeckId.DECK_A
                    audioPickerLauncher.launch("audio/*")
                },
                onOpenYouTubeDialog = { showYouTubeDialog = true }
            )
        }

        // Dialogs
        if (showYouTubeDialog) {
            YouTubeStreamDialog(
                onDismiss = { showYouTubeDialog = false },
                onLoadStream = { url, title, targetDeck ->
                    viewModel.loadYouTubeOrStream(url, title, targetDeck)
                }
            )
        }

        if (showRecordingDialog) {
            RecordingStudioDialog(
                isRecording = isRecording,
                recordDurationSec = recordDurationSec,
                recordedMixes = recordedMixes,
                onToggleRecording = { customTitle -> viewModel.toggleRecording(customTitle) },
                onDeleteMix = { viewModel.deleteMix(it) },
                onDismiss = { showRecordingDialog = false }
            )
        }

        if (showSkinDialog) {
            VirtualSkinDialog(
                currentSkin = activeSkin,
                onSelectSkin = { viewModel.setSkin(it) },
                onDismiss = { showSkinDialog = false }
            )
        }
    }
}
