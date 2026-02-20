package com.homepantry.utils

import android.util.Log

/**
 * 统一日志管理器
 */
object Logger {

    private const val TAG = "HomePantry"

    private const val DEBUG = true
    private const val VERBOSE = false

    /**
     * Debug 日志
     */
    fun d(message: String, throwable: Throwable? = null) {
        if (DEBUG) {
            Log.d(TAG, message, throwable)
        }
    }

    /**
     * Debug 日志（带标签）
     */
    fun d(tag: String, message: String, throwable: Throwable? = null) {
        if (DEBUG) {
            Log.d(tag, message, throwable)
        }
    }

    /**
     * Info 日志
     */
    fun i(message: String, throwable: Throwable? = null) {
        if (DEBUG) {
            Log.i(TAG, message, throwable)
        }
    }

    /**
     * Info 日志（带标签）
     */
    fun i(tag: String, message: String, throwable: Throwable? = null) {
        if (DEBUG) {
            Log.i(tag, message, throwable)
        }
    }

    /**
     * Warning 日志
     */
    fun w(message: String, throwable: Throwable? = null) {
        if (DEBUG) {
            Log.w(TAG, message, throwable)
        }
    }

    /**
     * Warning 日志（带标签）
     */
    fun w(tag: String, message: String, throwable: Throwable? = null) {
        if (DEBUG) {
            Log.w(tag, message, throwable)
        }
    }

    /**
     * Error 日志
     */
    fun e(message: String, throwable: Throwable? = null) {
        Log.e(TAG, message, throwable)
    }

    /**
     * Error 日志（带标签）
     */
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        Log.e(tag, message, throwable)
    }

    /**
     * Verbose 日志
     */
    fun v(message: String, throwable: Throwable? = null) {
        if (VERBOSE && DEBUG) {
            Log.v(TAG, message, throwable)
        }
    }

    /**
     * Verbose 日志（带标签）
     */
    fun v(tag: String, message: String, throwable: Throwable? = null) {
        if (VERBOSE && DEBUG) {
            Log.v(tag, message, throwable)
        }
    }

    /**
     * 创建性能日志
     */
    fun performance(message: String, block: () -> Unit) {
        if (DEBUG) {
            val startTime = System.currentTimeMillis()
            block()
            val elapsedTime = System.currentTimeMillis() - startTime
            d("Performance: $message took $elapsedTime ms")
        } else {
            block()
        }
    }

    /**
     * 创建性能日志（返回值）
     */
    fun <T> performance(message: String, block: () -> T): T {
        return if (DEBUG) {
            val startTime = System.currentTimeMillis()
            val result = block()
            val elapsedTime = System.currentTimeMillis() - startTime
            d("Performance: $message took $elapsedTime ms")
            result
        } else {
            block()
        }
    }

    /**
     * 记录方法进入
     */
    fun enter(methodName: String) {
        if (VERBOSE && DEBUG) {
            d("Enter: $methodName")
        }
    }

    /**
     * 记录方法退出
     */
    fun exit(methodName: String) {
        if (VERBOSE && DEBUG) {
            d("Exit: $methodName")
        }
    }

    /**
     * 记录方法进入（带参数）
     */
    fun enter(methodName: String, vararg params: Any?) {
        if (VERBOSE && DEBUG) {
            d("Enter: $methodName ${params.joinToString(", ")}")
        }
    }

    /**
     * 记录方法退出（带返回值）
     */
    fun <T> exit(methodName: String, returnValue: T): T {
        if (VERBOSE && DEBUG) {
            d("Exit: $methodName return $returnValue")
        }
        return returnValue
    }
}
