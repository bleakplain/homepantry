package com.homepantry.data.cooking

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.*

/**
 * 语音播报管理器 - 用于烹饪模式
 */
class VoicePlaybackManager(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    // 播放状态
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking

    // 当前文本
    private val _currentText = MutableStateFlow("")
    val currentText: StateFlow<String> = _currentText

    // 语言设置
    var language: Locale = Locale.CHINA
        set(value) {
            field = value
            tts?.language = value
        }

    // 语速 (0.5 - 2.0, 默认 1.0)
    var speechRate: Float = 1.0f
        set(value) {
            field = value.coerceIn(0.5f, 2.0f)
            tts?.setSpeechRate(field)
        }

    // 音高 (0.5 - 2.0, 默认 1.0)
    var pitch: Float = 1.0f
        set(value) {
            field = value.coerceIn(0.5f, 2.0f)
            tts?.setPitch(field)
        }

    init {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.apply {
                // 设置中文
                val result = setLanguage(Locale.CHINA)
                isInitialized = result != TextToSpeech.LANG_MISSING_DATA &&
                               result != TextToSpeech.LANG_NOT_SUPPORTED

                if (isInitialized) {
                    setSpeechRate(speechRate)
                    setPitch(pitch)

                    // 设置播放进度监听
                    setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {
                            _isSpeaking.value = true
                        }

                        override fun onDone(utteranceId: String?) {
                            _isSpeaking.value = false
                            _isPlaying.value = false
                        }

                        override fun onError(utteranceId: String?) {
                            _isSpeaking.value = false
                            _isPlaying.value = false
                        }

                        override fun onStop(utteranceId: String?, interrupted: Boolean) {
                            _isSpeaking.value = false
                            _isPlaying.value = false
                        }
                    })
                }
            }
        }
    }

    /**
     * 播放文本
     */
    fun speak(text: String) {
        if (!isInitialized) return

        _currentText.value = text
        _isPlaying.value = true

        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "utterance_${System.currentTimeMillis()}")
    }

    /**
     * 播放下一个（追加到队列）
     */
    fun speakNext(text: String) {
        if (!isInitialized) return

        tts?.speak(text, TextToSpeech.QUEUE_ADD, null, "utterance_${System.currentTimeMillis()}")
    }

    /**
     * 停止播放
     */
    fun stop() {
        tts?.stop()
        _isPlaying.value = false
        _isSpeaking.value = false
    }

    /**
     * 暂停播放
     */
    fun pause() {
        tts?.stop()
        _isPlaying.value = false
    }

    /**
     * 继续播放
     */
    fun resume() {
        _currentText.value?.let { speak(it) }
    }

    /**
     * 播放烹饪步骤
     */
    fun speakStep(
        stepNumber: Int,
        totalSteps: Int,
        instruction: String,
        duration: Int? = null,
        reminder: String? = null
    ) {
        val text = buildString {
            append("步骤")
            append(stepNumber)
            append("，共")
            append(totalSteps)
            append("步。")
            append(instruction)

            if (reminder != null) {
                append("注意，")
                append(reminder)
            }

            if (duration != null && duration > 0) {
                val minutes = duration / 60
                val seconds = duration % 60
                if (minutes > 0) {
                    append("大约需要")
                    append(minutes)
                    append("分钟")
                    if (seconds > 0) {
                        append(seconds)
                        append("秒")
                    }
                } else {
                    append("大约需要")
                    append(seconds)
                    append("秒")
                }
            }
        }

        speak(text)
    }

    /**
     * 播放计时提醒
     */
    fun speakTimerReminder(remainingSeconds: Int, stepName: String? = null) {
        val text = buildString {
            if (stepName != null) {
                append(stepName)
            }
            append("还有")
            val minutes = remainingSeconds / 60
            val seconds = remainingSeconds % 60
            if (minutes > 0) {
                append(minutes)
                append("分")
            }
            if (seconds > 0) {
                append(seconds)
                append("秒")
            }
            append("完成")
        }

        speak(text)
    }

    /**
     * 播放计时结束提醒
     */
    fun speakTimerComplete(stepName: String? = null) {
        val text = if (stepName != null) {
            "$stepName 完成了，可以进行下一步"
        } else {
            "时间到了，可以进行下一步"
        }

        speak(text)
    }

    /**
     * 播放关键提醒
     */
    fun speakKeyReminder(reminder: String) {
        speak("注意，$reminder")
    }

    /**
     * 释放资源
     */
    fun release() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }

    /**
     * 检查是否可用
     */
    fun isAvailable(): Boolean = isInitialized

    /**
     * 获取可用语言
     */
    fun getAvailableLanguages(): Set<Locale> {
        return if (isInitialized) {
            tts?.availableLanguages ?: emptySet()
        } else {
            emptySet()
        }
    }

    companion object {
        // 预定义的语音提示
        const val PROMPT_START = "开始烹饪模式"
        const val PROMPT_NEXT_STEP = "下一步"
        const val PROMPT_PREVIOUS_STEP = "上一步"
        const val PROMPT_PAUSE = "暂停"
        const val PROMPT_RESUME = "继续"
        const val PROMPT_COMPLETE = "烹饪完成"
    }
}
