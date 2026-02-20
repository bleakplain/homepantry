package com.homepantry.utils

import android.os.Trace
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace

/**
 * 性能监控工具
 */
object PerformanceMonitor {

    /**
     * 记录性能（简单版本）
     */
    inline fun <T> recordPerformance(
        name: String,
        block: () -> T
    ): T {
        val startTime = System.currentTimeMillis()
        val result = block()
        val elapsedTime = System.currentTimeMillis() - startTime

        Logger.d("Performance: $name took ${elapsedTime}ms")
        return result
    }

    /**
     * 记录性能（使用 Trace API）
     */
    inline fun <T> recordTrace(
        name: String,
        block: () -> T
    ): T {
        val traceName = "homepantry_$name"
        Trace.beginSection(traceName)

        return try {
            recordPerformance(name, block)
        } finally {
            Trace.endSection()
        }
    }

    /**
     * 记录性能（使用 Firebase Performance）
     */
    inline fun <T> recordFirebasePerformance(
        name: String,
        block: () -> T
    ): T {
        val trace = FirebasePerformance.getInstance().newTrace(name)
        trace.start()

        return try {
            recordPerformance(name, block)
        } finally {
            trace.stop()
        }
    }

    /**
     * 记录性能（返回值）
     */
    inline fun <T> recordPerformanceWithResult(
        name: String,
        block: () -> T
    ): T {
        return recordPerformance(name, block)
    }

    /**
     * 记录性能（使用 Trace API 和返回值）
     */
    inline fun <T> recordTraceWithResult(
        name: String,
        block: () -> T
    ): T {
        return recordTrace(name, block)
    }

    /**
     * 记录性能（使用 Firebase Performance 和返回值）
     */
    inline fun <T> recordFirebasePerformanceWithResult(
        name: String,
        block: () -> T
    ): T {
        return recordFirebasePerformance(name, block)
    }

    /**
     * 记录方法性能
     */
    inline fun <T> recordMethodPerformance(
        methodName: String,
        block: () -> T
    ): T {
        Logger.enter(methodName)
        val result = recordTrace(methodName, block)
        Logger.exit(methodName, result)
        return result
    }

    /**
     * 记录方法性能（带参数）
     */
    inline fun <T> recordMethodPerformanceWithParams(
        methodName: String,
        vararg params: Any?,
        block: () -> T
    ): T {
        Logger.enter(methodName, *params)
        val result = recordTrace(methodName, block)
        Logger.exit(methodName, result)
        return result
    }
}
