# HomePantry SDD 改造方案

**日期**: 2026-02-15
**项目**: HomePantry (家常味库)
**目标**: 将存量项目改造为 SDD (Spec-Driven Development) 工作流

---

## 📊 项目现状分析

### 项目概况

| 维度 | 现状 | 说明 |
|------|------|------|
| **技术栈** | Kotlin + Jetpack Compose + Room | 现代化 Android 技术栈 |
| **架构** | MVVM + Clean Architecture | 架构清晰，分层合理 |
| **代码质量** | 高 | 有完整测试（215+ 单元测试） |
| **文档体系** | 完整但分散 | README, REQUIREMENTS, ARCHITECTURE, DEVELOPMENT, TEST_REPORT |
| **SDD 集成** | ❌ 无 | 没有 Constitution 和 Specs |
| **开发流程** | 传统方式 | 依赖文档和代码审查 |

### 现有文档体系

```
docs/
├── README.md                    # 项目概述
├── REQUIREMENTS.md              # 产品需求（2.0 版本）
├── ARCHITECTURE.md              # 架构设计
├── DEVELOPMENT.md               # 开发指南
├── TEST_REPORT.md               # 测试报告
└── 2025-01-29-homepantry-design.md  # 历史设计文档
```

### 代码结构

```
android/app/src/main/java/com/homepantry/
├── data/                        # 数据层
│   ├── dao/                    # 15 个 DAO
│   ├── entity/                 # 14 个实体
│   ├── repository/             # 仓库层
│   └── database/               # 数据库配置
├── ui/                          # UI 层
│   ├── home/                   # 首页
│   ├── recipe/                 # 菜谱页面
│   ├── ingredient/             # 食材页面
│   ├── mealplan/               # 餐食计划
│   ├── family/                 # 家庭功能
│   ├── cooking/                # 烹饪模式
│   ├── settings/               # 设置
│   └── theme/                  # 主题配置
├── viewmodel/                   # ViewModel 层
└── navigation/                  # 导航配置
```

---

## 🎯 SDD 改造目标

### 核心目标

1. **建立 Constitution**：创建项目原则文档，作为 AI 助手的永久参考
2. **迁移现有需求**：将 REQUIREMENTS.md 迁移为 SDD spec.md 格式
3. **建立文档结构**：创建 `.specify/` 目录，规范文档管理
4. **集成工作流**：为未来的开发采用 SDD 工作流

### 预期收益

| 收益 | 说明 |
|------|------|
| **可预测性** | AI 生成代码有规范约束，质量更稳定 |
| **可追溯性** | 每个功能都有完整的 spec → plan → tasks 记录 |
| **可维护性** | Constitution 确保长期代码质量 |
| **可协作性** | 团队通过文档达成共识 |
| **知识沉淀** | 系统化的文档体系，减少知识流失 |

---

## 📋 SDD 改造计划

### Phase 1: 初始化 SDD 环境（Week 1）

#### 1.1 安装 Spec-Kit

```bash
# 进入项目目录
cd /root/work/homepantry

# 检查 Python 和 uv
python --version  # 需要 Python 3.9+
uv --version      # 需要先安装 uv

# 安装 Spec-Kit
pip install spec-kit
```

#### 1.2 初始化 SDD 项目结构

```bash
# 初始化 Spec-Kit
spec-kit init

# 创建目录结构
.specify/
├── memory/
│   └── constitution.md       # 项目原则
├── specs/
│   ├── 001-existing-features/    # 现有功能规范
│   │   ├── spec.md
│   │   ├── plan.md
│   │   ├── data-model.md
│   │   ├── tasks.md
│   │   ├── contracts/
│   │   └── research.md
│   ├── 002-p1-features/         # P1 功能规范
│   └── 003-p2-features/         # P2 功能规范
└── scripts/
```

#### 1.3 创建 Constitution（核心任务）

**目标**: 定义项目原则，作为 AI 助手的永久参考

**内容框架**:
```markdown
# HomePantry Constitution

## 1. 产品价值观

### 1.1 核心价值主张
> 让每一个热爱做饭的人，都能轻松管理自己的私房菜谱，从容规划每日餐食

### 1.2 设计哲学
- **简洁至上**: 减少 3 步以内的操作
- **温暖亲切**: 使用温暖配色，柔和圆角
- **高效流畅**: 快速响应，流畅动画
- **清晰反馈**: 每个操作都有明确反馈
- **容错性强**: 可撤销，可恢复

## 2. 技术原则

### 2.1 架构原则
- **MVVM + Clean Architecture**: 严格的职责分离
- **数据层单一来源**: Repository 是唯一的数据访问点
- **UI 无业务逻辑**: ViewModel 负责所有业务逻辑
- **Flow 驱动 UI**: 所有 UI 数据通过 Flow/LiveData 传递

### 2.2 代码质量
- **Kotlin 编码规范**: 遵循 Kotlin 官方编码规范
- **4 空格缩进**
- **命名规范**:
  - 类名: PascalCase
  - 函数/变量: camelCase
  - 常量: UPPER_SNAKE_CASE
- **注释**: 解释"为什么"而非"是什么"

### 2.3 测试要求
- **单元测试覆盖率**: ≥ 70%
- **所有 Repository 必须有测试**
- **核心业务逻辑必须有测试**
- **UI 测试**: 关键用户路径必须有测试

### 2.4 性能要求
- **App 启动时间**: < 2s
- **页面切换**: < 1s
- **列表渲染**: 使用 LazyColumn 虚拟化
- **避免内存泄漏**: 使用 proper lifecycle

### 2.5 安全性要求
- **输入验证**: 所有用户输入必须验证
- **SQL 注入防护**: Room 自动防护
- **数据加密**: 敏感数据必须加密存储（未来云端同步时）

## 3. 数据原则

### 3.1 数据库设计
- **规范化设计**: 避免数据冗余
- **索引优化**: 常用查询字段必须有索引
- **迁移策略**: 版本升级必须有 Migration
- **事务处理**: 复杂操作使用 @Transaction

### 3.2 数据流
- **单向数据流**: UI → ViewModel → Repository → DAO → DB
- **StateFlow**: 所有 UI 状态使用 StateFlow
- **Flow**: 数据库查询返回 Flow
- **协程**: 所有异步操作使用协程

## 4. UI/UX 原则

### 4.1 视觉设计
- **配色**:
  - 主色调: 温暖橙 (#FF6B35)
  - 背景色: 米白 (#FAF7F2)
  - 辅助色: 蔬菜绿、肉类红、海鲜蓝、主食黄
- **圆角规范**: 大圆角 16px，中圆角 12px，小圆角 8px
- **字体规范**:
  - 大标题: 24-28sp
  - 标题: 20sp
  - 副标题: 18sp
  - 正文: 16sp

### 4.2 交互设计
- **操作反馈**: 点击、长按、滑动都有反馈
- **加载状态**: 使用骨架屏或加载动画
- **错误处理**: 友好错误提示和重试按钮
- **空状态**: 友好插图和引导文案

### 4.3 厨房模式特殊设计
- **横屏大字**: 超大字体，单屏一步
- **语音播报**: TTS 播报步骤
- **手势操作**: 手背滑动、音量键
- **防误触**: 大按钮，简单操作

## 5. AI 协作指南

### 5.1 AI 角色定义
- **代码生成**: AI 负责根据 tasks.md 生成代码
- **规范参考**: AI 永远参考 constitution.md
- **质量保证**: AI 生成的代码必须符合测试要求

### 5.2 AI 使用规则
- **永远从 Constitution 开始**: 生成代码前先阅读 constitution.md
- **遵循 Architecture**: 严格遵守 MVVM + Clean Architecture
- **代码规范**: 遵循 Kotlin 编码规范
- **测试优先**: 生成代码时同时生成测试

### 5.3 AI 禁止行为
- ❌ 不修改 Architecture
- ❌ 不更改技术栈
- ❌ 不跳过测试
- ❌ 不引入未讨论的依赖

## 6. 开发流程

### 6.1 新功能开发流程
1. `/speckit.specify` - 创建 spec.md（What + Why）
2. `/speckit.plan` - 创建 plan.md（技术方案）
3. `/speckit.tasks` - 创建 tasks.md（任务清单）
4. `/speckit.implement` - 执行实现
5. Code Review
6. 更新 docs/

### 6.2 Bug 修复流程
1. 创建 bug spec
2. 分析原因
3. 编写测试用例
4. 修复代码
5. 验证测试通过

### 6.3 重构流程
1. 创建重构 spec
2. 定义重构目标
3. 重构代码
4. 确保测试通过
5. 更新文档

## 7. 质量标准

### 7.1 代码质量
- **CircleCI / GitHub Actions**: CI 检查通过
- **代码覆盖率**: ≥ 70%
- **静态分析**: detekt 检查通过
- **Lint**: Android Lint 检查通过

### 7.2 文档质量
- **所有 spec 必须有**:
  - 明确的目标
  - 清晰的用户场景
  - 完整的验收标准
- **所有 plan 必须有**:
  - 技术栈说明
  - 架构图
  - 数据模型
- **所有 tasks 必须有**:
  - 可执行的步骤
  - 验证方法

### 7.3 测试质量
- **单元测试**: 所有业务逻辑必须有测试
- **集成测试**: 关键用户路径必须有测试
- **UI 测试**: 关键页面必须有测试

## 8. 持续改进

### 8.1 定期回顾
- **每月**: Constitution 更新回顾
- **每季度**: 架构回顾
- **每年**: 技术栈评估

### 8.2 知识管理
- **所有重要决策**必须记录在 research.md
- **所有技术选型**必须记录在 plan.md
- **所有经验教训**必须更新 Constitution

---

## 附录

### A. 参考文档
- [REQUIREMENTS.md](../docs/REQUIREMENTS.md)
- [ARCHITECTURE.md](../docs/ARCHITECTURE.md)
- [DEVELOPMENT.md](../docs/DEVELOPMENT.md)

### B. 相关规范
- [Kotlin 编码规范](https://kotlinlang.org/docs/coding-conventions.html)
- [Android 架构指南](https://developer.android.com/topic/architecture)
- [Jetpack Compose 指南](https://developer.android.com/jetpack/compose)

### C. 版本历史
- v1.0 (2026-02-15): 初始版本
```

---

### Phase 2: 迁移现有功能为 Specs（Week 2-3）

#### 2.1 功能分类

基于 REQUIREMENTS.md 的功能优先级：

| Spec ID | 功能分类 | 优先级 | 说明 |
|---------|---------|--------|------|
| 001 | 菜谱管理基础 | P0 | CRUD、搜索、收藏 |
| 002 | 食材管理 | P0 | 食材库、推荐 |
| 003 | 餐食计划 | P0 | 每日菜单、周菜单 |
| 004 | 购物清单 | P0 | 自动生成、管理 |
| 005 | 烹饪模式 | P0 | 横屏大字、语音、计时器 |
| 006 | 智能推荐 | P1 | 个性化推荐 |
| 007 | 食材库存 | P1 | 库存管理、保质期提醒 |
| 008 | 制作记录 | P1 | 版本记录、评分 |
| 009 | 周菜单生成 | P2 | AI 生成一周菜单 |
| 010 | 营养分析 | P2 | 营养信息、分析报告 |
| 011 | 宴请模式 | P2 | 多人聚餐规划 |
| 012 | 快速保存 | P2 | 链接保存、OCR |
| 013 | 语音播报 | P2 | 烹饪模式语音 |
| 014 | 家庭管理 | P3 | 家庭成员、共享 |
| 015 | 数据统计 | P3 | 烹饪数据、统计 |

#### 2.2 迁移策略

**原则**:
1. **从已实现功能开始**: 先迁移 P0 功能
2. **保持文档同步**: Specs 必须与代码同步
3. **补充缺失内容**: 补充 plan.md 和 tasks.md
4. **验收标准**: 每个功能必须有完整的 spec + plan + tasks

**迁移模板**:
```bash
# 为每个功能创建 spec
/speckit.specify "菜谱管理基础功能"

# Spec-Kit 会生成:
.specify/specs/001-recipe-management/
├── spec.md              # 从 REQUIREMENTS.md 迁移
├── plan.md              # 从 ARCHITECTURE.md 提取
├── data-model.md        # 从实体类生成
├── tasks.md             # 基于现有代码生成
├── contracts/           # API 契约
└── research.md          # 技术调研
```

---

### Phase 3: 补充 Plan 和 Tasks（Week 4）

#### 3.1 补充技术方案

对于每个已实现的 P0 功能，补充 plan.md：

**plan.md 内容框架**:
```markdown
# Plan: 菜谱管理基础功能

## 技术栈
- 语言: Kotlin 1.9+
- UI: Jetpack Compose 1.5+
- 数据库: Room 2.6+
- 异步: Coroutines + Flow
- 依赖注入: 可选 Hilt

## 架构设计

### 数据层
- Entity: Recipe, RecipeIngredient, RecipeInstruction
- DAO: RecipeDao
- Repository: RecipeRepository

### 业务逻辑层
- Use Cases:
  - CreateRecipe
  - GetRecipeById
  - SearchRecipes
  - UpdateRecipe
  - DeleteRecipe

### 表现层
- ViewModel: RecipeViewModel
- UI:
  - RecipeListScreen
  - RecipeDetailScreen
  - AddRecipeScreen
  - EditRecipeScreen

## 数据模型

### Recipe Entity
- id: String (PK)
- name: String
- description: String?
- imageUrl: String?
- cookingTime: Int
- servings: Int
- difficulty: DifficultyLevel
- categoryId: String?
- isFavorite: Boolean
- createdAt: Long
- updatedAt: Long

## 关系设计
- Recipe (1) -- (N) RecipeIngredient
- Recipe (1) -- (N) RecipeInstruction
- Category (1) -- (N) Recipe

## 关键算法
- 搜索: LIKE 查询 + Flow
- 筛选: WHERE 子句 + Flow
- 排序: ORDER BY + Flow

## 性能优化
- 索引: categoryId, name, createdAt
- 分页: LIMIT + OFFSET
- Flow: 实时数据更新
```

#### 3.2 补充任务清单

对于每个已实现的 P0 功能，补充 tasks.md：

**tasks.md 内容框架**:
```markdown
# Tasks: 菜谱管理基础功能

## 已完成任务

### 1. 数据层实现
- [x] 1.1 创建 Recipe Entity
- [x] 1.2 创建 RecipeIngredient Entity
- [x] 1.3 创建 RecipeInstruction Entity
- [x] 1.4 创建 RecipeDao
- [x] 1.5 创建 RecipeRepository

### 2. 业务逻辑层实现
- [x] 2.1 创建 CreateRecipe UseCase
- [x] 2.2 创建 GetRecipeById UseCase
- [x] 2.3 创建 SearchRecipes UseCase
- [x] 2.4 创建 UpdateRecipe UseCase
- [x] 2.5 创建 DeleteRecipe UseCase

### 3. 表现层实现
- [x] 3.1 创建 RecipeViewModel
- [x] 3.2 创建 RecipeListScreen
- [x] 3.3 创建 RecipeDetailScreen
- [x] 3.4 创建 AddRecipeScreen
- [x] 3.5 创建 EditRecipeScreen

### 4. 测试实现
- [x] 4.1 RecipeDao 测试 (12 个测试)
- [x] 4.2 RecipeRepository 测试 (47 个测试)
- [x] 4.3 RecipeViewModel 测试 (68 个测试)

### 5. 导航实现
- [x] 5.1 配置导航图
- [x] 5.2 实现页面跳转
- [x] 5.3 实现参数传递

## 验证清单

### 功能验证
- [x] 可以添加菜谱
- [x] 可以编辑菜谱
- [x] 可以删除菜谱
- [x] 可以搜索菜谱
- [x] 可以收藏菜谱
- [x] 可以查看菜谱详情

### 性能验证
- [x] 启动时间 < 2s
- [x] 页面切换 < 1s
- [x] 列表滚动流畅

### 测试验证
- [x] 单元测试通过
- [x] 集成测试通过
- [x] UI 测试通过
```

---

### Phase 4: 新功能采用 SDD 工作流（Week 5+）

#### 4.1 开发流程标准化

**新功能开发流程**:
```
1. 用户提出需求
   ↓
2. /speckit.specify
   ↓
3. 审查 spec.md (What + Why)
   ↓
4. /speckit.plan
   ↓
5. 审查 plan.md (技术方案)
   ↓
6. /speckit.tasks
   ↓
7. 审查 tasks.md (任务清单)
   ↓
8. /speckit.implement
   ↓
9. Code Review
   ↓
10. 更新 docs/
   ↓
11. 归档 spec
```

#### 4.2 AI 协作模式

**模式1: 我直接写代码（简单任务）**
- 修改一行代码
- 修复明显的 bug
- 添加简单功能

**模式2: 委托 Claude Code（复杂任务）**
- 新功能实现
- 代码重构
- 架构设计
- 完整的 SDD 工作流

**协作模式**: 我 orchestrate，Claude Code execute
- 我启动 Claude Code
- 我监控进度并通知用户
- 我回答 Claude Code 的问题
- 我帮助用户审查结果

---

## 🗂️ 目录结构（改造后）

```
homepantry/
├── .specify/                      # SDD 规范目录
│   ├── memory/
│   │   └── constitution.md        # 项目原则 ⭐ 核心
│   ├── specs/
│   │   ├── 001-recipe-management/
│   │   │   ├── spec.md
│   │   │   ├── plan.md
│   │   │   ├── data-model.md
│   │   │   ├── tasks.md
│   │   │   ├── contracts/
│   │   │   └── research.md
│   │   ├── 002-ingredient-management/
│   │   ├── 003-meal-plan/
│   │   ├── 004-shopping-list/
│   │   ├── 005-cooking-mode/
│   │   ├── 006-smart-recommendation/
│   │   ├── 007-pantry-inventory/
│   │   ├── 008-cooking-records/
│   │   ├── 009-weekly-menu/
│   │   ├── 010-nutrition-analysis/
│   │   ├── 011-banquet-mode/
│   │   ├── 012-quick-save/
│   │   ├── 013-voice-playback/
│   │   ├── 014-family-management/
│   │   └── 015-data-statistics/
│   └── scripts/
│       └── helpers/
├── android/                       # Android 应用
│   └── app/
│       └── src/
│           ├── main/
│           │   └── java/com/homepantry/
│           │       ├── data/
│           │       ├── ui/
│           │       ├── viewmodel/
│           │       └── navigation/
│           ├── test/
│           └── androidTest/
├── docs/                          # 文档（保持现有）
│   ├── README.md
│   ├── REQUIREMENTS.md
│   ├── ARCHITECTURE.md
│   ├── DEVELOPMENT.md
│   ├── TEST_REPORT.md
│   └── SDD-GUIDE.md              # 新增：SDD 使用指南
└── CLAUDE.md                      # Claude Code 工作指南
```

---

## ✅ 验收标准

### Phase 1 验收
- [ ] Spec-Kit 安装成功
- [ ] `.specify/` 目录创建
- [ ] constitution.md 创建完成
- [ ] Constitution 包含所有核心原则

### Phase 2 验收
- [ ] P0 功能全部迁移为 specs
- [ ] 每个 spec 有完整的 spec.md
- [ ] Specs 与代码同步

### Phase 3 验收
- [ ] 所有 P0 功能有 plan.md
- [ ] 所有 P0 功能有 tasks.md
- [ ] Tasks 与已实现代码匹配

### Phase 4 验收
- [ ] 新功能采用 SDD 工作流
- [ ] AI 协作模式建立
- [ ] 文档体系完整

---

## 🚀 实施步骤

### 立即行动（今天）

1. **安装 Spec-Kit**
   ```bash
   cd /root/work/homepantry
   pip install spec-kit
   ```

2. **初始化 SDD 项目**
   ```bash
   spec-kit init
   ```

3. **创建 Constitution**
   - 使用上面的内容框架
   - 基于现有文档提取
   - 作为 AI 的永久参考

### 本周完成（Week 1）

1. 完善 Constitution
2. 创建 `.specify/` 目录结构
3. 创建第一个 spec（001-recipe-management）

### 下周完成（Week 2-3）

1. 迁移所有 P0 功能为 specs
2. 补充 plan.md
3. 补充 tasks.md

### 长期（Week 4+）

1. 新功能采用 SDD 工作流
2. 建立代码审查流程
3. 持续优化 Constitution

---

## 📚 参考资料

- [SDD 学习笔记](/root/.openclaw/workspace/memory/2026-02-12-openspec-study.md)
- [Spec-Kit GitHub](https://github.com/github/spec-kit)
- [OpenSpec GitHub](https://github.com/Fission-AI/OpenSpec)
- [HomePantry 现有文档](/root/work/homepantry/docs/)

---

**文档版本**: 1.0
**创建日期**: 2026-02-15
**最后更新**: 2026-02-15
**负责人**: Jude 🦞
