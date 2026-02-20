# Data Model: 保质期提醒

**Spec ID**: 018
**功能名称**: 保质期提醒
**创建日期**: 2026-02-20
**最后更新**: 2026-02-20

---

## 实体定义

### ExpirationReminder（过期提醒）

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
    ],
    indices = [
        Index(value = ["pantryItemId"]),
        Index(value = ["isEnabled", "lastNotifiedDate"])
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

---

### ExpirationNotification（过期通知）

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
    val notificationType: NotificationType = NotificationType.EXPIRED,
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

## 关系设计

### 与其他实体的关系

```
expiration_reminders (过期提醒)
    │
    └── PantryItem (食材库存) ── expiryDate (过期日期)

expiration_notifications (过期通知)
    │
    └── PantryItem (食材库存)
```

**说明**:
- 过期提醒通过外键关联食材库存
- 过期通知通过外键关联食材库存

---

## 数据类

### ExpirationCheckResult（过期检查结果）

```kotlin
data class ExpirationCheckResult(
    val pantryItem: PantryItem,
    val expirationDate: Long,
    val daysUntilExpiration: Int,
    val status: ExpirationStatus
)

enum class ExpirationStatus {
    EXPIRED,      // 已过期
    EXPIRED_TODAY, // 今天过期
    EXPIRING_SOON, // 即将过期（N 天内）
    FRESH          // 新鲜（超过 N 天）
}
```

### ExpirationSummary（过期汇总）

```kotlin
data class ExpirationSummary(
    val totalCount: Int,
    val expiredCount: Int,
    val expiringSoonCount: Int,
    val freshCount: Int,
    val expiredItems: List<PantryItem>,
    val expiringSoonItems: List<PantryItem>
)
```

### ReminderConfig（提醒配置）

```kotlin
data class ReminderConfig(
    val reminderDays: Int = 3,
    val reminderTime: String = "08:00",
    val reminderFrequency: ExpirationReminder.ReminderFrequency = ExpirationReminder.ReminderFrequency.DAILY,
    val notificationEnabled: Boolean = true
)
```

---

## 索引设计

### expiration_reminders 表索引

```sql
-- 主键自动创建索引
CREATE INDEX idx_expiration_reminders_pantry_item_id ON expiration_reminders(pantry_item_id);
CREATE INDEX idx_expiration_reminders_is_enabled_last_notified ON expiration_reminders(is_enabled, last_notified_date);
```

### expiration_notifications 表索引

```sql
-- 主键自动创建索引
CREATE INDEX idx_expiration_notifications_pantry_item_notification_date ON expiration_notifications(pantry_item_id, notification_date);
CREATE INDEX idx_expiration_notifications_is_read_is_handled ON expiration_notifications(is_read, is_handled);
```

---

## 数据流向

### 过期检查流程

```
用户操作（手动触发/定时任务）
    ↓
ExpirationRepository.checkExpiringItems()
    ↓
查询食材（WHERE expiry_date <= today + N days）
    ↓
分类过期状态（已过期、即将过期、新鲜）
    ↓
生成检查结果列表
    ↓
更新 UI
```

### 通知发送流程

```
WorkManager 触发定时任务
    ↓
ExpirationWorker.doWork()
    ↓
获取启用的过期提醒
    ↓
对每个提醒检查过期食材
    ↓
根据过期状态发送通知
    ↓
保存通知记录
    ↓
更新最后通知日期
```

---

## 数据验证

### 过期提醒验证

1. **提前天数验证**
   ```kotlin
   fun validateReminderDays(days: Int): Result<Unit> {
       return when {
           days < 0 -> Result.failure(Exception("提前天数不能小于 0"))
           days > 30 -> Result.failure(Exception("提前天数不能超过 30 天"))
           else -> Result.success(Unit)
       }
   }
   ```

2. **提醒时间验证**
   ```kotlin
   fun validateReminderTime(time: String): Result<Unit> {
       return if (time.matches(Regex("^([01]?[0-9]|2[0-3]):([0-5][0-9])$"))) {
           Result.success(Unit)
       } else {
           Result.failure(Exception("时间格式不正确，应为 HH:mm"))
       }
   }
   ```

---

## 默认数据

### 默认过期提醒

```kotlin
object DefaultExpirationReminder {
    val DEFAULT_REMINDER = ExpirationReminder(
        id = "default",
        pantryItemId = "",  // 需要设置
        reminderDays = 3,
        reminderTime = "08:00",
        reminderFrequency = ExpirationReminder.ReminderFrequency.DAILY,
        isEnabled = true,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )
}
```

---

## 参考资料

- [Room Database](https://developer.android.com/training/data-storage/room)
- [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager)
- [NotificationManager](https://developer.android.com/guide/topics/ui/notifiers/notifications)
- [Kotlin Data Classes](https://kotlinlang.org/docs/data-classes.html)
- [SQLite Indexes](https://www.sqlite.org/lang_createindex.html)

---

**文档版本**: 1.0
**创建日期**: 2026-02-20
**最后更新**: 2026-02-20
**负责人**: Jude 🦞
