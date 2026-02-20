package com.homepantry.scheduler

import android.content.Context
import androidx.hilt.work.HiltViewModel
import androidx.work.*
import com.homepantry.work.ExpirationCheckWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * 过期任务调度器
 */
@HiltViewModel
class ExpirationScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val TAG = "ExpirationScheduler"
        private const val EXPIRATION_CHECK_WORK_NAME = "expiration_check_work"
    }

    /**
     * 调度每日过期检查
     */
    fun scheduleDailyCheck() {
        val expirationCheckWorkRequest = PeriodicWorkRequestBuilder<ExpirationCheckWorker>(
            EXPIRATION_CHECK_WORK_NAME,
            repeatInterval = 1, // 每天执行一次
            repeatIntervalTimeUnit = TimeUnit.DAYS
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            EXPIRATION_CHECK_WORK_NAME,
            expirationCheckWorkRequest,
            ExistingPeriodicWorkPolicy.REPLACE, // 替换现有的任务
            ExpirationCheckWorker::class.java
        )

        android.util.Log.d(TAG, "已调度每日过期检查任务")
    }

    /**
     * 取消每日过期检查
     */
    fun cancelDailyCheck() {
        WorkManager.getInstance(context).cancelUniqueWork(
            EXPIRATION_CHECK_WORK_NAME
        )

        android.util.Log.d(TAG, "已取消每日过期检查任务")
    }

    /**
     * 立即执行过期检查
     */
    fun triggerExpirationCheckNow() {
        val expirationCheckWorkRequest = OneTimeWorkRequestBuilder<ExpirationCheckWorker>(
            EXPIRATION_CHECK_WORK_NAME
        ).build()

        WorkManager.getInstance(context).enqueueUniqueOneTimeWork(
            EXPIRATION_CHECK_WORK_NAME,
            expirationCheckWorkRequest,
            ExistingWorkPolicy.REPLACE,
            ExpirationCheckWorker::class.java
        )

        android.util.Log.d(TAG, "已触发立即过期检查")
    }

    /**
     * 检查任务状态
     */
    fun getTaskStatus(): Listenable<WorkInfo> {
        val workInfos = WorkManager.getInstance(context).getWorkInfosByTag(
            WorkQuery.fromTags(arrayListOf(EXPIRATION_CHECK_WORK_NAME))
        )

        android.util.Log.d(TAG, "任务状态：${workInfos.size} 个任务")
        workInfos.forEach { workInfo ->
            android.util.Log.d(TAG, "任务 ${workInfo.id}: ${workInfo.state}")
        }

        return workInfos
    }

    /**
     * 判断任务是否在运行
     */
    fun isTaskRunning(): Boolean {
        val workInfos = getTaskStatus()
        return workInfos.any { it.state == WorkInfo.State.RUNNING }
    }

    /**
     * 重新调度任务
     */
    fun rescheduleDailyCheck() {
        cancelDailyCheck()
        scheduleDailyCheck()
        android.util.Log.d(TAG, "已重新调度每日过期检查任务")
    }
}
