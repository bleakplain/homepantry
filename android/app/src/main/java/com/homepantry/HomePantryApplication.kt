package com.homepantry

import android.app.Application
import android.content.Context
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.squareup.leakcanary.LeakCanary
import com.squareup.leakcanary.RefWatcher
import dagger.hilt.android.HiltAndroidApp

/**
 * HomePantry Application
 */
@HiltAndroidApp
class HomePantryApplication : Application() {

    companion object {
        private const val TAG = "HomePantryApplication"
        private const val LEAK_CANARY_WATCH_DELAY_MILLIS = 5000L
    }

    private var refWatcher: RefWatcher? = null

    override fun onCreate() {
        super.onCreate()

        // 设置全局异常处理器
        GlobalExceptionHandler.setup(this)

        // 初始化 LeakCanary（仅 Debug 构建）
        setupLeakCanary()

        // 初始化 Firebase Crashlytics
        setupFirebaseCrashlytics()

        // 初始化其他应用级别的组件
        initializeOtherComponents()

        Log.d(TAG, "HomePantryApplication onCreate 完成")
    }

    /**
     * 设置 LeakCanary
     */
    private fun setupLeakCanary() {
        if (!BuildConfig.DEBUG) {
            Log.d(TAG, "LeakCanary 仅在 Debug 构建中启用")
            return
        }

        try {
            refWatcher = LeakCanary.install(
                this,
                LEAK_CANARY_WATCH_DELAY_MILLIS,
                LeakCanaryConfig().apply {
                    enableDumpHeap = true
                    enableDisplayHeapValue = true
                    enableDisplayLeakCount = true
                }
            )

            Log.d(TAG, "LeakCanary 安装成功")
        } catch (e: Exception) {
            Log.e(TAG, "LeakCanary 安装失败", e)
        }
    }

    /**
     * 设置 Firebase Crashlytics
     */
    private fun setupFirebaseCrashlytics() {
        try {
            FirebaseCrashlytics.getInstance().apply {
                setCrashlyticsCollectionEnabled(true)
                setCustomKey("app_version", BuildConfig.VERSION_NAME)
                setCustomKey("app_build", BuildConfig.VERSION_CODE.toString())
            }

            Log.d(TAG, "Firebase Crashlytics 初始化成功")
        } catch (e: Exception) {
            Log.e(TAG, "Firebase Crashlytics 初始化失败", e)
        }
    }

    /**
     * 初始化其他应用级别的组件
     */
    private fun initializeOtherComponents() {
        // 初始化其他组件，如：
        // - WorkManager
        // - NotificationManager
        // - Database
        // - 其他单例
    }

    override fun onTerminate() {
        super.onTerminate()

        // 清理 LeakCanary
        refWatcher?.let { watcher ->
            LeakCanary.installedRefWatcher?.uninstall(watcher)
        }

        Log.d(TAG, "HomePantryApplication onTerminate")
    }

    /**
     * 获取全局上下文
     */
    companion object {
        private var instance: HomePantryApplication? = null

        fun getInstance(): HomePantryApplication {
            return instance ?: throw IllegalStateException("Application 未初始化")
        }

        fun getInstanceOrNull(): HomePantryApplication? {
            return instance
        }
    }

    init {
        instance = this
    }
}
