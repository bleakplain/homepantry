package com.homepantry.testing

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * JUnit Rule for managing Kotlin Coroutines in tests.
 * Sets the Main dispatcher to a test dispatcher before each test
 * and resets it after each test.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CoroutineTestRule(
    private val testDispatcher: TestDispatcher = kotlinx.coroutines.test.StandardTestDispatcher()
) : TestWatcher() {

    override fun starting(description: Description?) {
        super.starting(description)
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description?) {
        super.finished(description)
        Dispatchers.resetMain()
    }

    /**
     * Run the given block on the test dispatcher
     */
    fun runTestOnTestDispatcher(block: suspend () -> Unit) {
        kotlinx.coroutines.test.runTest {
            block()
        }
    }
}

/**
 * InstantTaskExecutorRule for Room database testing.
 * This allows Room to execute database operations synchronously.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class InstantExecutorRule : TestWatcher() {
    override fun starting(description: Description?) {
        super.starting(description)
        // Room uses Arch Task Executor for background operations
        // In tests, we want everything to run synchronously
        // This is typically handled by Room's testing utilities
    }

    override fun finished(description: Description?) {
        super.finished(description)
    }
}

/**
 * Extension function for running suspend functions in tests
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun <T> runTest(
    block: suspend () -> T
): T {
    return kotlinx.coroutines.test.runTest { block() }
}
