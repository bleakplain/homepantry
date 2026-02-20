# Tasks: 烹饪模式

**Spec ID**: 005
**功能名称**: 烹饪模式
**优先级**: P0
**状态**: ✅ 已实现
**创建日期**: 2026-02-15
**最后更新**: 2026-02-15

---

## 已完成任务

### 1. 表现层实现

#### 1.1 创建横屏大字页面
- [x] 1.1.1 创建 `EnhancedCookingModeScreen`
- [x] 1.1.2 实现横屏布局
- [x] 1.1.3 实现超大字体显示
- [x] 1.1.4 实现步骤信息显示

**文件位置**:
- `android/app/src/main/java/com/homepantry/ui/cooking/EnhancedCookingModeScreen.kt`

**验收标准**:
- [x] 横屏显示正常
- [x] 超大字体清晰
- [x] 步骤信息完整

---

#### 1.2 创建语音播报功能
- [x] 1.2.1 创建 `VoicePlaybackManager`
- [x] 1.2.2 实现 TTS 初始化
- [x] 1.2.3 实现语音播报步骤
- [x] 1.2.4 实现播报控制（播放、暂停、停止）
- [x] 1.2.5 实现语速和音高调节
- [x] 1.2.6 实现中文支持

**文件位置**:
- `android/app/src/main/java/com/homepantry/data/cooking/VoicePlaybackManager.kt`

**验收标准**:
- [x] TTS 初始化成功
- [x] 语音播报清晰
- [x] 播报控制正常
- [x] 支持中文

---

#### 1.3 创建烹饪模式管理器
- [x] 1.3.1 创建 `CookingModeManager`
- [x] 1.3.2 实现步骤管理（加载、下一步、上一步、跳转）
- [x] 1.3.3 实现语音播放协调
- [x] 1.3.4 实现计时器管理
- [x] 1.3.5 实现状态管理（播放中、说话中）

**文件位置**:
- `android/app/src/main/java/com/homepantry/data/cooking/CookingModeManager.kt`

**验收标准**:
- [x] 步骤管理正确
- [x] 语音播放协调正常
- [x] 计时器准确
- [x] 状态更新及时

---

#### 1.4 创建手势操作
- [x] 1.4.1 实现手背滑动检测
- [x] 1.4.2 实现左右滑动切换步骤
- [x] 1.4.3 实现音量键控制

**文件位置**:
- `android/app/src/main/java/com/homepantry/ui/cooking/EnhancedCookingModeScreen.kt`

**验收标准**:
- [x] 手势识别准确
- [x] 滑动响应快速
- [x] 音量键控制正常

---

#### 1.5 创建步骤内计时器
- [x] 1.5.1 实现倒计时显示
- [x] 1.5.2 实现计时器线程
- [x] 1.5.3 实现每分钟语音提醒
- [x] 1.5.4 实现计时结束提醒

**文件位置**:
- `android/app/src/main/java/com/homepantry/data/cooking/CookingModeManager.kt`

**验收标准**:
- [x] 计时器准确
- [x] 语音提醒及时
- [x] 计时结束提醒正确

---

### 2. 业务逻辑层实现

#### 2.1 创建 ViewModel
- [x] 2.1.1 创建 `CookingModeViewModel`
- [x] 2.1.2 定义 UI 状态
- [x] 2.1.3 实现初始化方法
- [x] 2.1.4 实现步骤控制方法
- [x] 2.1.5 实现语音控制方法
- [x] 2.1.6 实现计时器控制方法

**文件位置**:
- `android/app/src/main/java/com/homepantry/ui/cooking/CookingModeViewModel.kt`

**验收标准**:
- [x] 状态管理正确
- [x] 所有方法正确实现
- [x] 协程正确使用

---

### 3. 测试实现

#### 3.1 单元测试
- [x] 3.1.1 创建 `VoicePlaybackManagerTest`
- [x] 3.1.2 创建 `CookingModeManagerTest`

**验收标准**:
- [x] 所有测试通过
- [x] 测试覆盖率 ≥ 70%
- [x] 测试用例覆盖所有分支

---

## 验证清单

### 功能验证

#### 基础功能
- [x] 可以进入烹饪模式
- [x] 可以横屏显示
- [x] 可以语音播报步骤
- [x] 可以手势切换步骤
- [x] 可以设置计时器

#### 高级功能
- [x] 可以多菜并行
- [x] 可以查看计时器状态
- [x] 可以查看播放状态

---

### 性能验证

#### 加载性能
- [x] 烹饪模式加载时间 < 1s
- [x] 语音播报延迟 < 500ms

#### 运行性能
- [x] 手势响应时间 < 100ms
- [x] 计时器准确度 ±1s

---

### 兼容性验证

#### 设备兼容
- [x] 支持最低 API 24
- [x] 支持横屏显示
- [x] 支持 TTS API

---

### 测试验证

#### 单元测试
- [x] `VoicePlaybackManagerTest`: 所有测试通过
- [x] `CookingModeManagerTest`: 所有测试通过

---

## 已知问题和优化方向

### 已知问题

1. **TTS 语音播报**
   - 当前：基础播报功能
   - 未来：支持语速调节、方言选择

2. **隔空手势**
   - 当前：需要前置摄像头
   - 未来：优化检测算法

---

## 参考资料

- [spec.md](./spec.md)
- [plan.md](./plan.md)
- [REQUIREMENTS.md](../../../docs/REQUIREMENTS.md)
- [ARCHITECTURE.md](../../../docs/ARCHITECTURE.md)
- [Constitution](../memory/constitution.md)

---

**文档版本**: 1.0
**创建日期**: 2026-02-15
**最后更新**: 2026-02-15
**负责人**: Jude 🦞
