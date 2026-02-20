package com.homepantry.viewmodel

import androidx.lifecycle.ViewModel
import com.homepantry.utils.Logger
import com.homepantry.utils.PerformanceMonitor

/**
 * 基础视图模型
 */
abstract class BaseViewModel : ViewModel() {

    companion object {
        private const val TAG = "BaseViewModel"
    }

    init {
        Logger.d(TAG, "${this::class.simpleName} init")
    }

    /**
     * 清除错误
     */
    open fun clearError() {
        Logger.d("${this::class.simpleName}.clearError")
    }

    /**
     * 清除成功消息
     */
    open fun clearSuccessMessage() {
        Logger.d("${this::class.simpleName}.clearSuccessMessage")
    }

    override fun onCleared() {
        super.onCleared()
        Logger.d(TAG, "${this::class.simpleName} onCleared")
    }
}
