# 家常味库 (HomePantry)

> 面向家庭主厨的温馨实用菜谱管理应用

## 项目概述

家常味库是一款帮助家庭用户管理菜谱、食材和餐食计划的移动应用。支持菜谱收藏、基于现有食材的智能推荐、餐食计划管理等功能。

**平台：** Android (Kotlin + Jetpack Compose)
**数据存储：** Room Database (本地存储)
**架构模式：** MVVM

---

## 功能特性

### 已实现功能

- **菜谱管理**：添加、编辑、删除、收藏菜谱
- **食材管理**：管理个人食材库，追踪过期日期
- **餐食计划**：周视图餐食计划，安排每日三餐
- **智能推荐**：基于现有食材推荐可做菜谱
- **高级搜索**：按难度、烹饪时间、份数筛选菜谱
- **菜谱评分**：对做过的菜谱进行评分和评论
- **烹饪计时器**：内置计时器，帮助掌握烹饪时间
- **数据导出**：导出菜谱数据为 JSON 格式

### 计划中功能

- 家庭成员管理和共享
- 菜谱图片管理
- 购物清单自动生成
- 菜谱导入功能

---

## 项目结构

```
homepantry/
├── android/                          # Android 应用模块
│   └── app/
│       └── src/
│           ├── main/
│           │   ├── java/com/homepantry/
│           │   │   ├── data/
│           │   │   │   ├── dao/          # 数据访问对象
│           │   │   │   ├── entity/       # 数据库实体
│           │   │   │   ├── repository/   # 仓库层
│           │   │   │   ├── database/     # 数据库配置
│           │   │   │   ├── recommendation/ # 推荐算法
│           │   │   │   ├── search/       # 搜索过滤
│           │   │   │   ├── export/       # 数据导出
│           │   │   │   └── shopping/     # 购物清单
│           │   │   ├── ui/
│           │   │   │   ├── home/         # 主页面
│           │   │   │   ├── recipe/       # 菜谱相关页面
│           │   │   │   ├── ingredient/   # 食材页面
│           │   │   │   ├── mealplan/     # 餐食计划页面
│           │   │   │   ├── family/       # 家庭页面
│           │   │   │   ├── cooking/      # 烹饪计时器
│           │   │   │   ├── settings/     # 设置页面
│           │   │   │   └── theme/        # 主题配置
│           │   │   ├── viewmodel/        # 视图模型
│           │   │   ├── navigation/       # 导航配置
│           │   │   └── MainActivity.kt   # 主活动
│           │   ├── test/                 # 单元测试
│           │   └── androidTest/          # UI 测试
│           ├── build.gradle.kts          # 应用构建配置
│           └── build.gradle.kts          # 项目构建配置
└── docs/                             # 设计文档
```

---

## 技术栈

### 核心技术

| 技术 | 版本 | 用途 |
|------|------|------|
| Kotlin | 1.9+ | 开发语言 |
| Jetpack Compose | 1.5+ | UI 框架 |
| Room | 2.6+ | 本地数据库 |
| Navigation Compose | 2.7+ | 页面导航 |
| Coroutines | 1.7+ | 异步处理 |
| ViewModel | 2.6+ | 状态管理 |
| LiveData/StateFlow | 2.6+ | 数据流 |

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

// Navigation
implementation("androidx.navigation:navigation-compose:2.7.5")

// Testing
testImplementation("junit:junit:4.13.2")
testImplementation("org.mockito:mockito-core:5.7.0")
androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.5.4")
```

---

## 开发环境设置

### 前置要求

- JDK 17 或更高版本
- Android Studio Hedgehog (2023.1.1) 或更高版本
- Android SDK 24+ (最低支持 Android 7.0)
- Gradle 8.0+

### 构建步骤

1. **克隆仓库**
   ```bash
   git clone <repository-url>
   cd homepantry
   ```

2. **打开项目**
   - 在 Android Studio 中打开 `android` 目录

3. **同步 Gradle**
   - Android Studio 会自动提示同步 Gradle
   - 等待依赖下载完成

4. **运行应用**
   - 选择模拟器或连接真机
   - 点击 Run 按钮 (或按 Shift+F10)

### 清理构建

```bash
cd android
./gradlew clean
./gradlew build
```

---

## 架构设计

### MVVM 架构

```
┌─────────────────────────────────────────────────────┐
│                      UI Layer                       │
│              (Jetpack Compose Screens)              │
│  ┌─────────────────────────────────────────────┐   │
│  │  Observes StateFlow / LiveData               │   │
│  │  Calls ViewModel functions                   │   │
│  └─────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────┐
│                   ViewModel Layer                   │
│  ┌─────────────────────────────────────────────┐   │
│  │  - Holds UI state                            │   │
│  │  - Exposes functions for UI events           │   │
│  │  - Communicates with Repository              │   │
│  └─────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────┐
│                  Repository Layer                   │
│  ┌─────────────────────────────────────────────┐   │
│  │  - Single source of truth for data          │   │
│  │  - Abstracts data sources                    │   │
│  │  - Handles business logic                    │   │
│  └─────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────┐
│                    Data Layer                        │
│  ┌─────────────────────────────────────────────┐   │
│  │  Room Database (DAOs)                        │   │
│  │  Local Storage (Files)                       │   │
│  └─────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────┘
```

### 数据流

1. **用户操作** → UI 调用 ViewModel 函数
2. **ViewModel** → 调用 Repository 获取/更新数据
3. **Repository** → 通过 DAO 访问数据库
4. **数据库** → 返回数据给 Repository
5. **Repository** → 处理数据并返回给 ViewModel
6. **ViewModel** → 更新 StateFlow/LiveData
7. **UI** → 观察状态变化并重新渲染

---

## 数据模型

### 核心实体

#### Recipe (菜谱)
```kotlin
@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey val id: String,
    val name: String,
    val description: String?,
    val imageUrl: String?,
    val cookingTime: Int,        // 分钟
    val servings: Int,            // 份数
    val difficulty: DifficultyLevel,
    val isFavorite: Boolean,
    val categoryId: String?,
    val createdAt: Long
)
```

#### Ingredient (食材)
```kotlin
@Entity(tableName = "ingredients")
data class Ingredient(
    @PrimaryKey val id: String,
    val name: String,
    val unit: String,             // 克、毫升、个等
    val category: IngredientCategory
)
```

#### RecipeIngredient (菜谱食材关联)
```kotlin
@Entity(tableName = "recipe_ingredients")
data class RecipeIngredient(
    @PrimaryKey val id: String,
    val recipeId: String,
    val ingredientId: String,
    val quantity: Double,
    val notes: String?
)
```

#### MealPlan (餐食计划)
```kotlin
@Entity(tableName = "meal_plans")
data class MealPlan(
    @PrimaryKey val id: String,
    val date: Long,               // 时间戳
    val mealType: MealType,       // 早餐/午餐/晚餐
    val recipeId: String,
    val servings: Int,
    val notes: String?
)
```

---

## 测试

### 运行测试

```bash
# 单元测试
cd android
./gradlew test

# UI 测试
./gradlew connectedAndroidTest

# 特定测试类
./gradlew test --tests RecipeRepositoryTest
```

### 测试覆盖率

```bash
./gradlew jacocoTestReport
```

---

## 贡献指南

### 代码规范

- 遵循 Kotlin 官方编码规范
- 使用 4 空格缩进
- 函数命名使用 camelCase
- 类名使用 PascalCase
- 常量使用 UPPER_SNAKE_CASE

### 提交规范

```
<type>(<scope>): <subject>

<body>

<footer>
```

**类型 (type):**
- `feat`: 新功能
- `fix`: 修复 bug
- `docs`: 文档更新
- `style`: 代码格式调整
- `refactor`: 重构
- `test`: 测试相关
- `chore`: 构建/工具相关

**示例:**
```
feat(recipe): add image upload functionality

Implement recipe image upload with:
- Image picker integration
- Local storage caching
- Coil image loading in UI

Co-Authored-By: Claude <noreply@anthropic.com>
```

---

## 许可证

Copyright © 2025 HomePantry Team. All rights reserved.

---

## 联系方式

- 项目维护者: HomePantry Team
- 邮箱: bleakplain@163.com
- 问题反馈: GitHub Issues

---

*最后更新: 2025-01-30*
