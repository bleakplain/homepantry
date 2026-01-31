package com.homepantry.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Base ViewModel that provides common state management for loading, error, and success states.
 * Reduces code duplication across ViewModels.
 */
abstract class BaseViewModel : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    /**
     * Execute a suspend function with loading state, error handling, and optional success message.
     * This is the preferred way to perform operations in ViewModels.
     *
     * @param block The suspend function to execute
     * @param successMessage Optional message to show on success
     */
    protected fun execute(
        successMessage: String? = null,
        block: suspend () -> Unit
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                block()
                successMessage?.let { _successMessage.value = it }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Execute a suspend function with custom error handling.
     */
    protected fun executeWithErrorHandling(
        onError: (String) -> Unit = { _error.value = it },
        block: suspend () -> Unit
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                block()
            } catch (e: Exception) {
                onError(e.message ?: "Unknown error occurred")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Launch a coroutine without showing loading state (for background operations).
     */
    protected fun launchInBackground(
        onError: (String) -> Unit = { _error.value = it },
        block: suspend () -> Unit
    ) {
        viewModelScope.launch {
            try {
                block()
            } catch (e: Exception) {
                onError(e.message ?: "Unknown error occurred")
            }
        }
    }

    /**
     * Clear the current error message.
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Clear the current success message.
     */
    fun clearSuccessMessage() {
        _successMessage.value = null
    }

    /**
     * Set loading state manually (useful for Flow collectors).
     */
    protected fun setLoading(isLoading: Boolean) {
        _isLoading.value = isLoading
    }

    /**
     * Set error message manually.
     */
    protected fun setError(message: String?) {
        _error.value = message
    }

    /**
     * Set success message manually.
     */
    protected fun setSuccess(message: String?) {
        _successMessage.value = message
    }
}
