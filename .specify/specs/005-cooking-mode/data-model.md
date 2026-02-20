# Data Model: 烹饪模式

**Spec ID**: 005
**功能名称**: 烹饪模式
**优先级**: P0
**状态**: ✅ 已实现
**创建日期**: 2026-02-15

---

## 核心功能

烹饪模式不需要独立的数据库实体，它主要是一个UI 和交互功能。

---

## 关键组件

### 1. CookingModeManager（烹饪模式管理器）

```kotlin
class CookingModeManager(
    private val recipeRepository: RecipeRepository,
    private val voicePlaybackManager: VoicePlaybackManager
) {
    private var currentStep: Int = 0
    private var instructions: List<RecipeInstruction> = emptyList()
    private var timers: Map<String, Long> = emptyMap()

    fun initialize(instructions: List<RecipeInstruction>) {
        this.instructions = instructions
        currentStep = 0
    }

    fun nextStep() {
        if (currentStep < instructions.size - 1) {
            currentStep++
        }
    }

    fun previousStep() {
        if (currentStep > 0) {
            currentStep--
        }
    }

    fun setStepTimer(instructionId: String, duration: Long) {
        timers[instructionId] = duration
    }
}
```

### 2. VoicePlaybackManager（语音播放管理器）

```kotlin
class VoicePlaybackManager(context: Context) {
    private val tts = TextToSpeech(context) { status ->
        if (status == TextToSpeech.SUCCESS) {
            // 初始化成功
        }
    }

    fun speak(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_ADD, null, null)
    }

    fun stop() {
        tts.stop()
    }
}
```

---

## 参考资料

- [spec.md](./spec.md)
- [plan.md](./plan.md)
- [Constitution](../memory/constitution.md)

---

**文档版本**: 1.0
**创建日期**: 2026-02-15
**负责人**: Jude 🦞
