package ruano.dev.wear.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ruano.dev.wear.data.health.HeartRateDataSource
import ruano.dev.wear.domain.model.GamePhase
import ruano.dev.wear.domain.model.GameState
import ruano.dev.wear.domain.model.Player
import ruano.dev.wear.domain.usecase.GetHighScoreUseCase
import ruano.dev.wear.domain.usecase.SaveHighScoreUseCase

enum class HapticType { JUMP, HIT }

class GameViewModel(
    private val getHighScore: GetHighScoreUseCase,
    private val saveHighScore: SaveHighScoreUseCase,
    private val heartRateSource: HeartRateDataSource
) : ViewModel() {

    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state.asStateFlow()

    private val _hapticChannel = Channel<HapticType>(Channel.BUFFERED)
    val hapticEvents: Flow<HapticType> = _hapticChannel.receiveAsFlow()

    private var gameFrame = 0L
    private var gameJob: Job? = null
    private var lastLives = 3

    init {
        loadHighScore()
        observeHeartRate()
    }

    fun startGame() {
        _state.value = GameState(phase = GamePhase.PLAYING,
            highScore = _state.value.highScore)
        gameFrame = 0L
        lastLives = _state.value.lives
        gameJob = viewModelScope.launch {
            while (_state.value.phase == GamePhase.PLAYING) {
                delay(16L)
                _state.update { GameEngine.update(it, gameFrame++) }
                // Detecta si hubo colisión comparando vidas
                val currentLives = _state.value.lives
                if (currentLives < lastLives) {
                    _hapticChannel.trySend(HapticType.HIT)
                }
                lastLives = currentLives
            }
            if (_state.value.phase == GamePhase.DEAD) {
                saveHighScore(_state.value.score)
            }
        }
    }

    fun onJump() {
        val s = _state.value
        when (s.phase) {
            GamePhase.IDLE, GamePhase.DEAD -> startGame()
            GamePhase.PLAYING -> {
                if (!s.player.isJumping && s.player.y >= Player.FLOOR_Y - 5f) {
                    _state.update { it.copy(player = it.player.copy(
                        velocityY = Player.JUMP_VELOCITY, isJumping = true
                    ))}
                    _hapticChannel.trySend(HapticType.JUMP)
                }
            }
            else -> {}
        }
    }

    fun onSlide() {
        if (_state.value.phase != GamePhase.PLAYING || _state.value.player.isJumping) return
        _state.update { it.copy(player = it.player.copy(
            slideFrames = Player.SLIDE_DURATION
        ))}
    }

    private fun loadHighScore() {
        viewModelScope.launch {
            val hs = getHighScore()
            _state.update { it.copy(highScore = hs) }
        }
    }

    private fun observeHeartRate() {
        viewModelScope.launch {
            heartRateSource.heartRate.collect { bpm ->
                _state.update { it.copy(heartRate = bpm) }
            }
        }
    }

    override fun onCleared() { gameJob?.cancel() }
}