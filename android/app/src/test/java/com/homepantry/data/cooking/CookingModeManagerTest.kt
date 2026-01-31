package com.homepantry.data.cooking

import android.content.Context
import com.homepantry.data.entity.RecipeInstruction
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * 烹饪模式管理器测试
 */
class CookingModeManagerTest {

    private lateinit var voicePlaybackManager: VoicePlaybackManager
    private lateinit var cookingModeManager: CookingModeManager
    private lateinit var context: Context

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        voicePlaybackManager = mockk(relaxed = true)
        cookingModeManager = CookingModeManager(voicePlaybackManager)
    }

    @After
    fun tearDown() {
        cookingModeManager.release()
    }

    @Test
    fun `load instructions sorts by step number`() = runTest {
        val instructions = listOf(
            RecipeInstruction("recipe1", 3, "步骤3"),
            RecipeInstruction("recipe1", 1, "步骤1"),
            RecipeInstruction("recipe1", 2, "步骤2")
        )

        cookingModeManager.loadInstructions(instructions)

        assertEquals(0, cookingModeManager.currentStep?.stepNumber)
        assertEquals(3, cookingModeManager.totalSteps)
    }

    @Test
    fun `next step increments index`() = runTest {
        val instructions = listOf(
            RecipeInstruction("recipe1", 1, "步骤1"),
            RecipeInstruction("recipe1", 2, "步骤2")
        )

        every { voicePlaybackManager.speakStep(any(), any(), any(), any(), any()) }.returns(Unit)

        cookingModeManager.loadInstructions(instructions)
        cookingModeManager.nextStep()

        assertEquals(1, cookingModeManager.currentStep?.stepNumber)
        assertEquals(2, cookingModeManager.progress)

        verify { voicePlaybackManager.speakStep(2, 2, "步骤2", null, null) }
    }

    @Test
    fun `previous step decrements index`() = runTest {
        val instructions = listOf(
            RecipeInstruction("recipe1", 1, "步骤1"),
            RecipeInstruction("recipe1", 2, "步骤2"),
            RecipeInstruction("recipe1", 3, "步骤3")
        )

        every { voicePlaybackManager.speakStep(any(), any(), any(), any(), any()) }.returns(Unit)

        cookingModeManager.loadInstructions(instructions)
        cookingModeManager.nextStep() // 到步骤2
        cookingModeManager.previousStep() // 回到步骤1

        assertEquals(0, cookingModeManager.currentStep?.stepNumber)
    }

    @Test
    fun `cannot go next on last step`() = runTest {
        val instructions = listOf(
            RecipeInstruction("recipe1", 1, "步骤1")
        )

        cookingModeManager.loadInstructions(instructions)
        val originalStep = cookingModeManager.currentStep
        cookingModeManager.nextStep()

        assertEquals(originalStep, cookingModeManager.currentStep)
    }

    @Test
    fun `cannot go previous on first step`() = runTest {
        val instructions = listOf(
            RecipeInstruction("recipe1", 1, "步骤1")
        )

        cookingModeManager.loadInstructions(instructions)
        cookingModeManager.previousStep()

        assertEquals(0, cookingModeManager.currentStep?.stepNumber)
    }

    @Test
    fun `play current step calls voice manager`() = runTest {
        val instructions = listOf(
            RecipeInstruction("recipe1", 1, "切番茄", duration = 60, reminder = "要切均匀")
        )

        every { voicePlaybackManager.speakStep(any(), any(), any(), any(), any()) }.returns(Unit)

        cookingModeManager.loadInstructions(instructions)
        cookingModeManager.playCurrentStep()

        verify {
            voicePlaybackManager.speakStep(
                stepNumber = 1,
                totalSteps = 1,
                instruction = "切番茄",
                duration = 60,
                reminder = "要切均匀"
            )
        }
    }

    @Test
    fun `go to step navigates to specific step`() = runTest {
        val instructions = listOf(
            RecipeInstruction("recipe1", 1, "步骤1"),
            RecipeInstruction("recipe1", 2, "步骤2"),
            RecipeInstruction("recipe1", 3, "步骤3")
        )

        every { voicePlaybackManager.speakStep(any(), any(), any(), any(), any()) }.returns(Unit)

        cookingModeManager.loadInstructions(instructions)
        cookingModeManager.goToStep(3)

        assertEquals(2, cookingModeManager.currentStep?.stepNumber)
    }

    @Test
    fun `pause stops playback`() = runTest {
        every { voicePlaybackManager.pause() }.returns(Unit)

        cookingModeManager.pause()

        verify { voicePlaybackManager.pause() }
    }

    @Test
    fun `stop cancels all timers`() = runTest {
        every { voicePlaybackManager.stop() }.returns(Unit)

        val instructions = listOf(
            RecipeInstruction("recipe1", 1, "步骤1", duration = 120)
        )

        cookingModeManager.loadInstructions(instructions)
        cookingModeManager.playCurrentStep()
        cookingModeManager.stop()

        assertTrue(cookingModeManager.getActiveTimers().isEmpty())
        verify { voicePlaybackManager.stop() }
    }

    @Test
    fun `timer with duration is auto started`() = runTest {
        every { voicePlaybackManager.speakStep(any(), any(), any(), any(), any()) }.returns(Unit)

        val instructions = listOf(
            RecipeInstruction("recipe1", 1, "炒菜", duration = 180)
        )

        cookingModeManager.loadInstructions(instructions)
        cookingModeManager.playCurrentStep()

        val timers = cookingModeManager.getActiveTimers()
        assertEquals(1, timers.size)
        assertEquals(1, timers.first().stepNumber)
        assertEquals(180, timers.first().totalSeconds)
    }

    @Test
    fun `progress is calculated correctly`() = runTest {
        val instructions = listOf(
            RecipeInstruction("recipe1", 1, "步骤1"),
            RecipeInstruction("recipe1", 2, "步骤2"),
            RecipeInstruction("recipe1", 3, "步骤3"),
            RecipeInstruction("recipe1", 4, "步骤4")
        )

        cookingModeManager.loadInstructions(instructions)

        assertEquals(1, cookingModeManager.progress)

        cookingModeManager.nextStep()
        assertEquals(2, cookingModeManager.progress)

        cookingModeManager.nextStep()
        assertEquals(3, cookingModeManager.progress)
    }

    @Test
    fun `empty instructions returns null current step`() = runTest {
        cookingModeManager.loadInstructions(emptyList())

        assertEquals(0, cookingModeManager.totalSteps)
        assertEquals(null, cookingModeManager.currentStep)
    }
}
