package com.homepantry

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.lang.Thread

/**
 * 全局异常处理器
 */
class GlobalExceptionHandler : Thread.UncaughtExceptionHandler {

    companion object {
        private const val TAG = "GlobalExceptionHandler"

        /**
         * 设置全局异常处理器
         */
        fun setup(application: HomePantryApplication) {
            Thread.setDefaultUncaughtExceptionHandler(
                GlobalExceptionHandler(application)
            )
        }
    }

    private var application: HomePantryApplication? = null

    constructor(application: HomePantryApplication) {
        this.application = application
    }

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        Log.e(TAG, "未捕获的异常", throwable)

        // 上报到 Firebase Crashlytics
        com.google.firebase.crashlytics.FirebaseCrashlytics.getInstance()
            .recordException(throwable)

        // 显示 Toast 通知
        showCrashToast(throwable)

        // 保存崩溃信息
        saveCrashInfo(thread, throwable)

        // 杀死应用
        killProcess()
    }

    /**
     * 显示崩溃 Toast 通知
     */
    private fun showCrashToast(throwable: Throwable) {
        Handler(Looper.getMainLooper()).post {
            val message = if (throwable.message != null) {
                "应用崩溃：${throwable.message}"
            } else {
                "应用崩溃：未知错误"
            }

            Toast.makeText(
                application?.applicationContext,
                message,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    /**
     * 保存崩溃信息
     */
    private fun saveCrashInfo(thread: Thread, throwable: Throwable) {
        try {
            val timestamp = System.currentTimeMillis()
            val stackTrace = android.util.Log.getStackTraceString(throwable)

            // 保存到 SharedPreferences 或本地文件
            val prefs = application?.getSharedPreferences(
                "crash_info",
                Context.MODE_PRIVATE
            )

            prefs?.edit {
                putLong("last_crash_timestamp", timestamp)
                putString("last_crash_thread", thread.name)
                putString("last_crash_exception", throwable.toString())
                putString("last_crash_stacktrace", stackTrace)
            }?.apply()
        } catch (e: Exception) {
            Log.e(TAG, "保存崩溃信息失败", e)
        }
    }

    /**
     * 杀死应用
     */
    private fun killProcess() {
        // 等待 Toast 显示
        Handler(Looper.getMainLooper()).postDelayed({
            android.os.Process.killProcess(android.os.Process.myPid())
        }, 2000)
    }
}
