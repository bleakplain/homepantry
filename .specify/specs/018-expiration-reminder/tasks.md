# Tasks: 保质期提醒

**Spec ID**: 018
**功能名称**: 保质期提醒
**优先级**: P2
**状态**: 🚧 规划中
**创建日期**: 2026-02-20
**最后更新**: 2026-02-20

---

## 待办任务

### 数据层

- [ ] **ExpirationReminder Entity** (`data/entity/ExpirationReminder.kt`)
  - 字段定义
  - 表配置
  - Room 注解

- [ ] **ExpirationNotification Entity** (`data/entity/ExpirationNotification.kt`)
  - 字段定义
  - 表配置
  - Room 注解

- [ ] **ExpirationReminderDao** (`data/dao/ExpirationReminderDao.kt`)
  - insert(ExpirationReminder)
  - update(ExpirationReminder)
  - delete(ExpirationReminder)
  - deleteById(String)
  - deleteByPantryItemId(String)
  - getAllReminders()
  - getReminderByPantryItemId(String)
  - getEnabledReminders()
  - getLatestReminder()

- [ ] **ExpirationNotificationDao** (`data/dao/ExpirationNotificationDao.kt`)
  - insert(ExpirationNotification)
  - update(ExpirationNotification)
  - delete(ExpirationNotification)
  - deleteById(String)
  - deleteByPantryItemId(String)
  - getAllNotifications()
  - getNotificationsByPantryItemId(String)
  - getUnreadNotifications()
  - getUnhandledNotifications()
  - markAsRead(String)
  - markAsHandled(String)
  - markAllAsRead()
  - markAllAsHandled()

- [ ] **ExpirationRepository** (`data/repository/ExpirationRepository.kt`)
  - createReminder(pantryItemId, config)
  - updateReminder(reminder)
  - deleteReminder(reminderId)
  - getAllReminders()
  - getEnabledReminders()
  - checkExpiringItems(reminderDays)
  - sendExpirationNotification(pantryItem)
  - sendExpiringSoonNotification(pantryItem, days)
  - saveNotification(notification)
  - getNotificationHistory()
  - markNotificationAsRead(notificationId)
  - markNotificationAsHandled(notificationId)

### 业务逻辑层

- [ ] **ExpirationViewModel** (`viewmodel/ExpirationViewModel.kt`)
  - expirationReminders: StateFlow<List<ExpirationReminder>>
  - expirationNotifications: StateFlow<List<ExpirationNotification>>
  - loadReminders()
  - loadNotifications()
  - checkExpiration()
  - markNotificationAsRead(notificationId)
  - markNotificationAsHandled(notificationId)

- [ ] **ExpirationSettingsViewModel** (`viewmodel/ExpirationSettingsViewModel.kt`)
  - currentConfig: StateFlow<ReminderConfig>
  - updateReminderDays(days)
  - updateReminderTime(time)
  - updateReminderFrequency(frequency)
  - enableNotifications(enable)
  - saveSettings()

- [ ] **ExpirationWorker** (`work/ExpirationWorker.kt`)
  - doWork()
  - checkAndNotify()

### 表现层

- [ ] **ExpirationDialog** (`ui/pantry/ExpirationDialog.kt`)
  - 过期检查报告
  - 批量操作（标记已处理、删除）
  - 分类显示（已过期、即将过期、新鲜）

- [ ] **ExpirationSettingsScreen** (`ui/pantry/ExpirationSettingsScreen.kt`)
  - 过期提醒设置
  - 提前天数选择（1-30）
  - 提醒时间选择
  - 提醒频率选择（每天、每周、每月）
  - 通知声音和振动设置

- [ ] **ExpirationNotificationItem** (`ui/components/ExpirationNotificationItem.kt`)
  - 单个过期通知
  - 显示食材信息、过期时间、处理状态
  - 跳转到食材详情

- [ ] **ExpirationBadge** (`ui/components/ExpirationBadge.kt`)
  - 显示过期食材数量
  - 点击打开过期检查

### 工作层

- [ ] **ExpirationScheduler** (`scheduler/ExpirationScheduler.kt`)
  - scheduleDailyCheck()
  - cancelDailyCheck()
  - scheduleCheck(reminderFrequency)
  - updateSchedule(reminderFrequency)

- [ ] **ExpirationNotifier** (`notification/ExpirationNotifier.kt`)
  - sendExpirationNotification(notification)
  - sendExpiringSoonNotification(notifications)
  - createNotificationChannel()
  - createNotificationCompat(notification)

### 导航

- [ ] 过期检查对话框路由配置
- [ ] 过期设置页面路由配置
- [ ] 集成到食材库存页面

### 测试

- [ ] **ExpirationReminderDaoTest** (`test/dao/ExpirationReminderDaoTest.kt`)
  - 插入测试
  - 更新测试
  - 删除测试
  - 查询测试
  - 关联测试

- [ ] **ExpirationRepositoryTest** (`test/repository/ExpirationRepositoryTest.kt`)
  - 创建提醒测试
  - 更新提醒测试
  - 删除提醒测试
  - 过期检查测试
  - 通知发送测试

- [ ] **ExpirationWorkerTest** (`test/work/ExpirationWorkerTest.kt`)
  - 过期检查逻辑测试
  - 通知发送测试
  - 错误处理测试

---

## 验收清单

### 功能验收

- [ ] 可以设置过期提醒（提前 1-30 天）
- [ ] 可以设置提醒时间（每天早上 8 点）
- [ ] 可以设置提醒频率（每天、每周、每月）
- [ ] 可以手动触发过期检查
- [ ] 可以查看过期检查报告（已过期、即将过期、新鲜）
- [ ] 可以批量操作过期食材（标记已处理、删除）
- [ ] 可以查看过期通知历史
- [ ] 可以标记通知为已读/已处理
- [ ] 可以清除已处理通知

### 性能验收

- [ ] 过期检查响应时间 < 500ms
- [ ] 1000 个食材检查 < 500ms
- [ ] 10000 个食材检查 < 1s
- [ ] 通知发送成功率 ≥ 99%
- [ ] 批量操作成功率 ≥ 99%

### 测试验收

- [ ] 单元测试覆盖率 ≥ 70%
- [ ] 过期提醒 DAO 测试 ≥ 6 个测试用例
- [ ] 过期通知 DAO 测试 ≥ 8 个测试用例
- [ ] 过期仓库测试 ≥ 6 个测试用例
- [ ] 过期 Worker 测试 ≥ 3 个测试用例

### 文档验收

- [ ] spec.md - 需求规范
- [ ] plan.md - 技术方案
- [ ] data-model.md - 数据模型
- [ ] tasks.md - 任务清单
- [ ] research.md - 技术调研
- [ ] README.md - 文档总结

---

## 开发顺序

### 阶段 1：数据层（1 天）

1. 创建 ExpirationReminder Entity
2. 创建 ExpirationNotification Entity
3. 创建 ExpirationReminderDao
4. 创建 ExpirationNotificationDao
5. 创建 ExpirationRepository
6. 编写单元测试

### 阶段 2：业务逻辑层（0.5 天）

7. 创建 ExpirationViewModel
8. 创建 ExpirationSettingsViewModel
9. 编写单元测试

### 阶段 3：工作层（0.5 天）

10. 创建 ExpirationWorker
11. 创建 ExpirationScheduler
12. 创建 ExpirationNotifier
13. 编写单元测试

### 阶段 4：表现层（1.5 天）

14. 创建 ExpirationDialog
15. 创建 ExpirationSettingsScreen
16. 创建 ExpirationNotificationItem
17. 创建 ExpirationBadge
18. 编写 UI 测试

### 阶段 5：导航和集成（0.5 天）

19. 配置导航路由
20. 集成到食材库存页面
21. 集成到设置页面
22. 测试导航流程

### 阶段 6：测试（1 天）

23. 运行所有单元测试
24. 运行集成测试
25. 测试覆盖率检查

**总预计时间**: 5 天

---

## 参考资料

- [ExpirationReminder.kt](../../../android/app/src/main/java/com/homepantry/data/entity/ExpirationReminder.kt)
- [ExpirationNotification.kt](../../../android/app/src/main/java/com/homepantry/data/entity/ExpirationNotification.kt)
- [ExpirationReminderDao.kt](../../../android/app/src/main/java/com/homepantry/data/dao/ExpirationReminderDao.kt)
- [ExpirationNotificationDao.kt](../../../android/app/src/main/java/com/homepantry/data/dao/ExpirationNotificationDao.kt)
- [ExpirationRepository.kt](../../../android/app/src/main/java/com/homepantry/data/repository/ExpirationRepository.kt)

---

**文档版本**: 1.0
**创建日期**: 2026-02-20
**最后更新**: 2026-02-20
**负责人**: Jude 🦞
