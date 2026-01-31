package com.homepantry.data.cooking

import com.homepantry.data.entity.RecipeInstruction

/**
 * 烹饪模式管理器 - 管理烹饪步骤、计时器、语音播报
 */
class CookingModeManager(
    private val voicePlaybackManager: VoicePlaybackManager
) {
    // 当前菜谱的步骤
    private var instructions: List<RecipeInstruction> = emptyList()
    private var currentStepIndex: Int = 0

    // 计时器
    private var activeTimers: MutableMap<Int, StepTimer> = mutableMapOf()

    // 状态
    val isPlaying: Boolean
        get() = voicePlaybackManager.isPlaying.value

    val isSpeaking: Boolean
        get() = voicePlaybackManager.isSpeaking.value

    val currentStep: RecipeInstruction?
        get() = instructions.getOrNull(currentStepIndex)

    val totalSteps: Int
        get() = instructions.size

    val progress: Int
        get() = if (instructions.isEmpty()) 0 else currentStepIndex + 1

    /**
     * 加载菜谱步骤
     */
    fun loadInstructions(instructionList: List<RecipeInstruction>) {
        instructions = instructionList.sortedBy { it.stepNumber }
        currentStepIndex = 0
    }

    /**
     * 跳转到指定步骤
     */
    fun goToStep(stepNumber: Int) {
        val index = instructions.indexOfFirst { it.stepNumber == stepNumber }
        if (index >= 0) {
            currentStepIndex = index
            playCurrentStep()
        }
    }

    /**
     * 下一步
     */
    fun nextStep() {
        if (currentStepIndex < instructions.size - 1) {
            currentStepIndex++
            playCurrentStep()
        }
    }

    /**
     * 上一步
     */
    fun previousStep() {
        if (currentStepIndex > 0) {
            currentStepIndex--
            playCurrentStep()
        }
    }

    /**
     * 播放当前步骤
     */
    fun playCurrentStep() {
        val step = currentStep ?: return
        voicePlaybackManager.speakStep(
            stepNumber = step.stepNumber,
            totalSteps = instructions.size,
            instruction = step.instruction,
            duration = step.duration,
            reminder = step.reminder
        )

        // 如果步骤有时长，自动启动计时器
        step.duration?.let { duration ->
            startTimer(step.stepNumber, duration, step.instruction)
        }
    }

    /**
     * 播放指定步骤
     */
    fun playStep(stepNumber: Int) {
        goToStep(stepNumber)
    }

    /**
     * 暂停播放
     */
    fun pause() {
        voicePlaybackManager.pause()
    }

    /**
     * 继续播放
     */
    fun resume() {
        voicePlaybackManager.resume()
    }

    /**
     * 停止播放
     */
    fun stop() {
        voicePlaybackManager.stop()
        cancelAllTimers()
    }

    /**
     * 开始计时
     */
    fun startTimer(stepNumber: Int, durationSeconds: Int, stepName: String? = null) {
        val timer = StepTimer(
            stepNumber = stepNumber,
            durationSeconds = durationSeconds,
            stepName = stepName ?: "步骤$stepNumber",
            onTick = { remaining ->
                // 每分钟提醒一次剩余时间
                if (remaining % 60 == 0 && remaining > 0) {
                    voicePlaybackManager.speakTimerReminder(remaining, stepName)
                }
            },
            onComplete = {
                voicePlaybackManager.speakTimerComplete(stepName)
                activeTimers.remove(stepNumber)
            }
        )
        activeTimers[stepNumber] = timer
        timer.start()
    }

    /**
     * 取消计时器
     */
    fun cancelTimer(stepNumber: Int) {
        activeTimers[stepNumber]?.cancel()
        activeTimers.remove(stepNumber)
    }

    /**
     * 取消所有计时器
     */
    fun cancelAllTimers() {
        activeTimers.values.forEach { it.cancel() }
        activeTimers.clear()
    }

    /**
     * 获取计时器状态
     */
    fun getTimerStatus(stepNumber: Int): TimerStatus? {
        return activeTimers[stepNumber]?.getStatus()
    }

    /**
     * 获取所有活跃计时器
     */
    fun getActiveTimers(): List<TimerStatus> {
        return activeTimers.values.map { it.getStatus() }
    }

    /**
     * 释放资源
     */
    fun release() {
        stop()
        voicePlaybackManager.release()
    }

    /**
     * 步骤计时器
     */
    class StepTimer(
        private val stepNumber: Int,
        private val durationSeconds: Int,
        private val stepName: String,
        private val onTick: (Int) -> Unit = {},
        private val onComplete: () -> Unit = {}
    ) {
        private var remainingSeconds = durationSeconds
        private var isRunning = false
        private var isCancelled = false

        private val thread = Thread {
            isRunning = true
            while (remainingSeconds > 0 && !isCancelled) {
                try {
                    Thread.sleep(1000)
                } catch (e: InterruptedException) {
                    break
                }
                remainingSeconds--
                if (!isCancelled) {
                    onTick(remainingSeconds)
                }
            }
            isRunning = false
            if (!isCancelled) {
                onComplete()
            }
        }

        fun start() {
            if (!isRunning) {
                thread.start()
            }
        }

        fun cancel() {
            isCancelled = true
        }

        fun getStatus(): TimerStatus {
            return TimerStatus(
                stepNumber = stepNumber,
                stepName = stepName,
                totalSeconds = durationSeconds,
                remainingSeconds = remainingSeconds,
                isRunning = isRunning
            )
        }
    }

    /**
     * 计时器状态
     */
    data class TimerStatus(
        val stepNumber: Int,
        val stepName: String,
        val totalSeconds: Int,
        val remainingSeconds: Int,
        val isRunning: Boolean
    ) {
        val progress: Float
            get() = if (totalSeconds > 0) {
                (totalSeconds - remainingSeconds).toFloat() / totalSeconds
            } else 0f

        val isComplete: Boolean
            get() = remainingSeconds <= 0
    }
}
