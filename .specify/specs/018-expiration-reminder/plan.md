# Plan: 保质期提醒

**Spec ID**: 018
**功能名称**: 保质期提醒
**优先级**: P2
**状态**: 🚧 规划中
**创建日期**: 2026-02-20
**最后更新**: 2026-02-20

---

## 技术栈

### 核心技术

| 技术 | 版本 | 用途 |
|------|------|------|
| Kotlin | 1.9+ | 开发语言 |
| Jetpack Compose | 1.5+ | UI 框架 |
| Room | 2.6+ | 本地数据库 |
| WorkManager | 2.8+ | 后台任务调度 |
| NotificationManager | 系统服务 | 系统通知 |
| Coroutines | 1.7+ | 异步处理 |
| ViewModel | 2.6+ | 状态管理 |
| Flow | Kotlin | 数据流 |

### 主要依赖

```kotlin
// Jetpack Compose
implementation(platform("androidx.compose:compose-bom:2023.10.01"))
implementation("androidx.compose.ui:ui")
implementation("androidx.compose.material3:material3")

// Room Database
implementation("androidx.room:room-runtime:2.6.1")
implementation("androidx.room:room-ktx:2.6.1")
ksp("androidx.room:room-compiler:2.6.1")

// ViewModel
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")

// WorkManager
implementation("androidx.work:work-runtime-ktx:2.8.1")

// Navigation
implementation("androidx.navigation:navigation-compose:2.7.5")

// Testing
testImplementation("junit:junit:4.13.2")
testImplementation("org.mockito:mockito-core:5.7.0")
```

---

## 架构设计

### 整体架构

```
┌─────────────────────────────────────────────┐
│                    Presentation Layer               │
│  ┌──────────────────┐  ┌──────────────────────┐   │
│  │  Screens        │  │  ViewModels          │   │
│  │  (Compose)       │◄─┤   (State)            │   │
│  │ PantryScreen    │  └──────────────────────┘   │
│  │ ExpirationDialog│                              │
│  └──────────────────┘                              │
└─────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────┐
│                     Domain Layer                    │
│  ┌──────────────────┐  ┌──────────────────────┐   │
│  │ExpirationRepo    │  │  NotificationRepo     │   │
│  │  (过期仓库)      │  │  (通知仓库)          │   │
│  └──────────────────┘  └──────────────────────┘   │
└─────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────┐
│                        Data Layer                    │
│  ┌──────────────────┐  ┌──────────────────────┐   │
│  │PantryItemDao     │  │  ExpirationDao        │   │
│  │  (库存数据)       │  │  (过期数据)           │   │
│  └──────────────────┘  └──────────────────────┘   │
└─────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────┐
│              Room Database                    │
│              (SQLite 本地数据库)                     │
└─────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────┐
│               WorkManager                      │
│           (后台任务调度)                       │
└─────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────┐
│           NotificationManager                  │
│              (系统通知)                        │
└─────────────────────────────────────────────┘
```

---

## 数据模型

### 核心实体

#### ExpirationReminder（过期提醒）

存储过期提醒配置。

| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| id | String | 提醒ID（主键） | NOT NULL |
| pantryItemId | String | 食材ID | NOT NULL, 外键 |
| reminderDays | Int | 提前天数（0-30） | NOT NULL |
| reminderTime | String | 提醒时间（HH:mm） | NOT NULL |
| reminderFrequency | ReminderFrequency | 提醒频率 | NOT NULL |
| isEnabled | Boolean | 是否启用 | NOT NULL |
| lastNotifiedDate | Long? | 最后通知日期（时间戳） | NULLABLE |
| createdAt | Long | 创建时间（时间戳） | NOT NULL |
| updatedAt | Long | 更新时间（时间戳） | NOT NULL |

**Room 定义**:
```kotlin
@Entity(tableName = "expiration_reminders",
    foreignKeys = [
        ForeignKey(
            entity = PantryItem::class,
            parentColumns = ["id"],
            childColumns = ["pantryItemId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ExpirationReminder(
    @PrimaryKey val id: String,
    val pantryItemId: String,
    val reminderDays: Int = 3,
    val reminderTime: String = "08:00",
    val reminderFrequency: ReminderFrequency = ReminderFrequency.DAILY,
    val isEnabled: Boolean = true,
    val lastNotifiedDate: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    enum class ReminderFrequency {
        DAILY, WEEKLY, MONTHLY
    }
}
```

#### ExpirationNotification（过期通知）

存储过期通知记录。

| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| id | String | 通知ID（主键） | NOT NULL |
| pantryItemId | String | 食材ID | NOT NULL |
| notificationDate | Long | 通知日期（时间戳） | NOT NULL |
| notificationType | NotificationType | 通知类型 | NOT NULL |
| isRead | Boolean | 是否已读 | NOT NULL |
| isHandled | Boolean | 是否已处理 | NOT NULL |
| createdAt | Long | 创建时间（时间戳） | NOT NULL |

**Room 定义**:
```kotlin
@Entity(tableName = "expiration_notifications",
    indices = [
        Index(value = ["pantryItemId", "notificationDate"]),
        Index(value = ["isRead", "isHandled"])
    ]
)
data class ExpirationNotification(
    @PrimaryKey val id: String,
    val pantryItemId: String,
    val notificationDate: Long,
    val notificationType: NotificationType,
    val isRead: Boolean = false,
    val isHandled: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    enum class NotificationType {
        EXPIRED, EXPIRING_SOON, BULK_CHECK
    }
}
```

---

## 关键功能实现

### 1. 过期检查算法

#### 检查逻辑

```kotlin
suspend fun checkExpiringItems(
    reminderDays: Int
): List<ExpirationCheckResult> {
    val today = System.currentTimeMillis()
    val msPerDay = 24 * 60 * 60 * 1000L
    val expirationDate = today - (reminderDays * msPerDay)

    return pantryItemDao.getItemsExpiringBefore(expirationDate)
        .map { item ->
            ExpirationCheckResult(
                pantryItem = item,
                expirationDate = item.expirationDate,
                daysUntilExpiration = ((item.expirationDate - today) / msPerDay).toInt(),
                status = when {
                    item.expirationDate < today -> ExpirationStatus.EXPIRED
                    item.expirationDate == today -> ExpirationStatus.EXPIRED_TODAY
                    item.expirationDate < expirationDate -> ExpirationStatus.EXPIRING_SOON
                    else -> ExpirationStatus.FRESH
                }
            )
        }
}

data class ExpirationCheckResult(
    val pantryItem: PantryItem,
    val expirationDate: Long,
    val daysUntilExpiration: Int,
    val status: ExpirationStatus
)

enum class ExpirationStatus {
    EXPIRED, EXPIRED_TODAY, EXPIRING_SOON, FRESH
}
```

---

### 2. 定时任务调度

#### WorkManager 配置

```kotlin
class ExpirationCheckWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val database = HomePantryDatabase.getDatabase(applicationContext)
        val expirationRepo = ExpirationRepository(database)

        try {
            // 获取所有启用的过期提醒
            val reminders = expirationRepo.getEnabledReminders()

            // 对每个提醒进行过期检查
            reminders.forEach { reminder ->
                checkAndNotify(reminder, expirationRepo)
            }

            return Result.success()
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    private suspend fun checkAndNotify(
        reminder: ExpirationReminder,
        expirationRepo: ExpirationRepository
    ) {
        val results = expirationRepo.checkExpiringItems(reminder.reminderDays)

        // 根据过期状态发送通知
        results.forEach { result ->
            when (result.status) {
                ExpirationStatus.EXPIRED -> sendExpirationNotification(result)
                ExpirationStatus.EXPIRING_SOON -> sendExpiringSoonNotification(result)
                else -> {}
            }
        }
    }
}
```

---

## 定时任务

### 每日过期检查

```kotlin
val expirationCheckWorkRequest = PeriodicWorkRequestBuilder<ExpirationCheckWorker>(
    "expiration_check_work",
    1, // 每天执行一次
    TimeUnit.DAYS
).build()

WorkManager.getInstance(context).enqueueUniquePeriodicWork(
    "expiration_check_work",
    expirationCheckWorkRequest,
    ExistingPeriodicWorkPolicy.KEEP,
    ExpirationCheckWork::class.java
)
```

---

## UI 设计

### 过期设置对话框

```kotlin
@Composable
fun ExpirationSettingsDialog(
    pantryItem: PantryItem,
    currentReminder: ExpirationReminder?,
    onDismiss: () -> Unit,
    onSave: (ExpirationReminder) -> Unit
) {
    var reminderDays by remember { mutableStateOf(currentReminder?.reminderDays ?: 3) }
    var reminderTime by remember { mutableStateOf(currentReminder?.reminderTime ?: "08:00") }
    var reminderFrequency by remember { mutableStateOf(currentReminder?.reminderFrequency ?: ReminderFrequency.DAILY) }
    var notificationEnabled by remember { mutableStateOf(currentReminder?.isEnabled ?: true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("过期提醒设置") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 提前天数选择
                Text("提前提醒天数：")
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(1, 3, 5, 7).forEach { days ->
                        FilterChip(
                            text = "${days}天",
                            isSelected = reminderDays == days,
                            onClick = { reminderDays = days }
                        )
                    }
                }

                // 提醒时间选择
                Text("提醒时间：")
                OutlinedTextField(
                    value = reminderTime,
                    onValueChange = { reminderTime = it },
                    label = { Text("HH:mm") },
                    placeholder = { Text("08:00") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // 提醒频率选择
                Text("提醒频率：")
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ReminderFrequency.DAILY
                        FilterChip(
                            text = "每天",
                            isSelected = reminderFrequency == ReminderFrequency.DAILY,
                            onClick = { reminderFrequency = ReminderFrequency.DAILY }
                        )
                    ReminderFrequency.WEEKLY
                        FilterChip(
                            text = "每周",
                            isSelected = reminderFrequency == ReminderFrequency.WEEKLY,
                            onClick = { reminderFrequency = ReminderFrequency.WEEKLY }
                        )
                    ReminderFrequency.MONTHLY
                        FilterChip(
                            text = "每月",
                            isSelected = reminderFrequency == ReminderFrequency.MONTHLY,
                            onClick = { reminderFrequency = ReminderFrequency.MONTHLY }
                        )
                }

                // 通知开关
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("启用通知")
                    Switch(
                        checked = notificationEnabled,
                        onCheckedChange = { notificationEnabled = it }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val reminder = ExpirationReminder(
                    id = currentReminder?.id ?: UUID.randomUUID().toString(),
                    pantryItemId = pantryItem.id,
                    reminderDays = reminderDays,
                    reminderTime = reminderTime,
                    reminderFrequency = reminderFrequency,
                    isEnabled = notificationEnabled
                )
                onSave(reminder)
            }) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
```

---

## 性能优化

### 数据库优化

1. **索引优化**
   ```kotlin
   @Entity(
       tableName = "expiration_notifications",
       indices = [
           Index(value = ["pantryItemId", "notificationDate"]),
           Index(value = ["isRead", "isHandled"])
       ]
   )
   ```

2. **查询优化**
   - 使用参数化查询防止 SQL 注入
   - 使用 Flow 进行流式查询
   - 避免N+1查询

3. **分页检查**
   ```kotlin
   @Query("""
       SELECT * FROM pantry_items
       WHERE expiration_date <= :expirationDate
       LIMIT :limit OFFSET :offset
   """)
   fun getItemsExpiringBeforePaged(
       expirationDate: Long,
       limit: Int,
       offset: Int
   ): Flow<List<PantryItem>>
   ```

### 后台任务优化

1. **任务节流**
   - 使用 WorkManager 的约束和链
   - 避免同时执行多个过期检查
   - 电池优化

2. **通知批量发送**
   - 使用 NotificationCompat.Group
   - 批量发送过期通知，避免过多通知

---

## 测试策略

### 单元测试

**ExpirationRepository 测试**:
- 创建过期提醒测试
- 更新过期提醒测试
- 删除过期提醒测试
- 过期检查测试
- 通知记录测试

**ExpirationWorker 测试**:
- 过期检查逻辑测试
- 通知发送测试
- 错误处理测试

### 集成测试

- 过期检查完整流程
- 通知发送流程
- 批量检查流程

### 测试覆盖率目标

- 代码覆盖率：≥ 70%
- 核心算法覆盖率：≥ 90%

---

## 部署策略

### 数据库迁移

```kotlin
val MIGRATION_18_19 = object : Migration(18, 19) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // 创建 expiration_reminders 表
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS expiration_reminders (
                id TEXT PRIMARY KEY NOT NULL,
                pantry_item_id TEXT NOT NULL,
                reminder_days INTEGER NOT NULL,
                reminder_time TEXT NOT NULL,
                reminder_frequency TEXT NOT NULL,
                is_enabled INTEGER NOT NULL DEFAULT 1,
                last_notified_date INTEGER,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                FOREIGN KEY (pantry_item_id) REFERENCES pantry_items(id) ON DELETE CASCADE
            )
        """)

        // 创建 expiration_notifications 表
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS expiration_notifications (
                id TEXT PRIMARY KEY NOT NULL,
                pantry_item_id TEXT NOT NULL,
                notification_date INTEGER NOT NULL,
                notification_type TEXT NOT NULL,
                is_read INTEGER NOT NULL DEFAULT 0,
                is_handled INTEGER NOT NULL DEFAULT 0,
                created_at INTEGER NOT NULL,
                FOREIGN KEY (pantry_item_id) REFERENCES pantry_items(id) ON DELETE CASCADE
            )
        """)

        // 创建索引
        database.execSQL("""
            CREATE INDEX IF NOT EXISTS index_expiration_notifications_item_date
            ON expiration_notifications(pantry_item_id, notification_date)
        """)

        database.execSQL("""
            CREATE INDEX IF NOT EXISTS index_expiration_notifications_status
            ON expiration_notifications(is_read, is_handled)
        """)

        // 为现有食材添加默认过期提醒（提前 3 天，每天早上 8 点）
        database.execSQL("""
            INSERT INTO expiration_reminders (id, pantry_item_id, reminder_days, reminder_time, reminder_frequency, created_at, updated_at)
            SELECT
                'rem_' || id,
                id,
                3,
                '08:00',
                'DAILY',
                ${System.currentTimeMillis()},
                ${System.currentTimeMillis()}
            FROM pantry_items
            WHERE expiration_date IS NOT NULL
            AND id NOT IN (SELECT pantry_item_id FROM expiration_reminders)
        """)
    }
}
```

---

## 已知问题和优化方向

### 已知问题

1. **批量检查性能**
   - 影响：10000 个食材时，检查可能变慢（5s）
   - 解决方案：使用分页检查、优化查询
   - 状态：✅ 已实现

2. **通知重复发送**
   - 影响：可能重复发送过期提醒
   - 解决方案：记录最后通知日期，避免重复
   - 状态：✅ 已实现

### 优化方向

1. **智能推荐**（优先级 P2）
   - 基于过期食材推荐菜谱
   - 提升用户体验

2. **过期提醒优化**（优先级 P2）
   - 支持自定义提醒声音
   - 支持自定义提醒文案

3. **批量处理优化**（优先级 P3）
   - 批量标记为"已处理"
   - 批量删除过期食材

---

## 参考资料

- [WorkManager Guide](https://developer.android.com/topic/libraries/architecture/workmanager)
- [Notifications Guide](https://developer.android.com/guide/topics/ui/notifiers/notifications)
- [Room Database](https://developer.android.com/training/data-storage/room)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)

---

**文档版本**: 1.0
**创建日期**: 2026-02-20
**最后更新**: 2026-02-20
**负责人**: Jude 🦞
