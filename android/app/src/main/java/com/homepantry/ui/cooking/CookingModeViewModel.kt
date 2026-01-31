package com.homepantry.ui.cooking

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homepantry.data.cooking.CookingModeManager
import com.homepantry.data.cooking.VoicePlaybackManager
import com.homepantry.data.entity.RecipeInstruction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 烹饪模式 ViewModel
 */
class CookingModeViewModel(
    private val context: Context
) : ViewModel() {

    private val voicePlaybackManager = VoicePlaybackManager(context)
    private val cookingModeManager = CookingModeManager(voicePlaybackManager)

    // UI State
    private val _uiState = MutableStateFlow<CookingModeUiState>(CookingModeUiState.Loading)
    val uiState: StateFlow<CookingModeUiState> = _uiState.asStateFlow()

    // 语音设置
    private val _isVoiceEnabled = MutableStateFlow(true)
    val isVoiceEnabled: StateFlow<Boolean> = _isVoiceEnabled.asStateFlow()

    private val _speechRate = MutableStateFlow(1.0f)
    val speechRate: StateFlow<Float> = _speechRate.asStateFlow()

    init {
        // 收集语音播放状态
        viewModelScope.launch {
            voicePlaybackManager.isPlaying.collect { isPlaying ->
                (_uiState.value as? CookingModeUiState.Active)?.let { currentState ->
                    _uiState.value = currentState.copy(isPlaying = isPlaying)
                }
            }
        }

        viewModelScope.launch {
            voicePlaybackManager.isSpeaking.collect { isSpeaking ->
                (_uiState.value as? CookingModeUiState.Active)?.let { currentState ->
                    _uiState.value = currentState.copy(isSpeaking = isSpeaking)
                }
            }
        }
    }

    /**
     * 初始化烹饪模式
     */
    fun initialize(instructions: List<RecipeInstruction>) {
        if (instructions.isEmpty()) {
            _uiState.value = CookingModeUiState.Error("没有找到制作步骤")
            return
        }

        cookingModeManager.loadInstructions(instructions)

        _uiState.value = CookingModeUiState.Active(
            currentStep = cookingModeManager.currentStep,
            totalSteps = cookingModeManager.totalSteps,
            progress = cookingModeManager.progress,
            isPlaying = cookingModeManager.isPlaying,
            isSpeaking = cookingModeManager.isSpeaking,
            activeTimers = cookingModeManager.getActiveTimers().map { status ->
                CookingTimerStatus(
                    stepNumber = status.stepNumber,
                    stepName = status.stepName,
                    totalSeconds = status.totalSeconds,
                    remainingSeconds = status.remainingSeconds,
                    isRunning = status.isRunning,
                    progress = if (status.totalSeconds > 0) {
                        1f - (status.remainingSeconds.toFloat() / status.totalSeconds.toFloat())
                    } else {
                        0f
                    }
                )
            }
        )
    }

    /**
     * 下一步
     */
    fun nextStep() {
        cookingModeManager.nextStep()
        updateStateFromManager()
    }

    /**
     * 上一步
     */
    fun previousStep() {
        cookingModeManager.previousStep()
        updateStateFromManager()
    }

    /**
     * 跳转到指定步骤
     */
    fun goToStep(stepNumber: Int) {
        cookingModeManager.goToStep(stepNumber)
        updateStateFromManager()
    }

    /**
     * 播放/暂停
     */
    fun togglePlayPause() {
        if (cookingModeManager.isPlaying) {
            cookingModeManager.pause()
        } else {
            cookingModeManager.resume()
        }
        updateStateFromManager()
    }

    /**
     * 切换语音
     */
    fun toggleVoice() {
        _isVoiceEnabled.value = !_isVoiceEnabled.value
    }

    /**
     * 设置语速
     */
    fun setSpeechRate(rate: Float) {
        _speechRate.value = rate
        voicePlaybackManager.speechRate = rate
    }

    /**
     * 停止并标记完成
     */
    fun stop() {
        cookingModeManager.stop()
        _uiState.value = CookingModeUiState.Completed
    }

    /**
     * 重新开始
     */
    fun restart() {
        cookingModeManager.loadInstructions(cookingModeManager.instructions ?: emptyList())
        updateStateFromManager()
    }

    /**
     * 切换计时器
     */
    fun toggleTimer(stepNumber: Int) {
        val status = cookingModeManager.getTimerStatus(stepNumber)
        if (status?.isRunning == true) {
            cookingModeManager.cancelTimer(stepNumber)
        } else {
            // 重新开始计时
            cookingModeManager.playCurrentStep()
        }
        updateStateFromManager()
    }

    /**
     * 打开设置
     */
    fun toggleSettings() {
        // 可以打开设置对话框
    }

    /**
     * 更新状态
     */
    private fun updateStateFromManager() {
        val current = _uiState.value
        if (current is CookingModeUiState.Active || current is CookingModeUiState.Loading) {
            _uiState.value = CookingModeUiState.Active(
                currentStep = cookingModeManager.currentStep,
                totalSteps = cookingModeManager.totalSteps,
                progress = cookingModeManager.progress,
                isPlaying = cookingModeManager.isPlaying,
                isSpeaking = cookingModeManager.isSpeaking,
                activeTimers = cookingModeManager.getActiveTimers().map { status ->
                    CookingTimerStatus(
                        stepNumber = status.stepNumber,
                        stepName = status.stepName,
                        totalSeconds = status.totalSeconds,
                        remainingSeconds = status.remainingSeconds,
                        isRunning = status.isRunning,
                        progress = if (status.totalSeconds > 0) {
                            1f - (status.remainingSeconds.toFloat() / status.totalSeconds.toFloat())
                        } else {
                            0f
                        }
                    )
                }
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        cookingModeManager.release()
    }

    companion object {
        fun factory(context: Context): () -> CookingModeViewModel {
            return { CookingModeViewModel(context) }
        }
    }
}

/**
 * UI State
 */
sealed class CookingModeUiState {
    object Loading : CookingModeUiState()
    data class Active(
        val currentStep: com.homepantry.data.entity.RecipeInstruction?,
        val totalSteps: Int,
        val progress: Int,
        val isPlaying: Boolean,
        val isSpeaking: Boolean,
        val activeTimers: List<CookingTimerStatus>
    ) : CookingModeUiState()
    object Completed : CookingModeUiState()
    data class Error(val message: String?) : CookingModeUiState()
}

/**
 * 计时器状态
 */
data class CookingTimerStatus(
    val stepNumber: Int,
    val stepName: String,
    val totalSeconds: Int,
    val remainingSeconds: Int,
    val isRunning: Boolean,
    val progress: Float = if (totalSeconds > 0) {
        1f - (remainingSeconds.toFloat() / totalSeconds.toFloat())
    } else {
        0f
    }
)
