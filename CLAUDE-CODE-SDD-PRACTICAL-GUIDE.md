# Claude Code + SDD: 存量项目改造实战指南

**文档对象**: 刘钢 (高级后端架构师)
**项目**: HomePantry (家常味库）
**改造阶段**: Phase 3 - 补充 Plan 和 Tasks (80%)
**创建日期**: 2026-02-15

---

## 🎯 核心问题

**问题**: 如何使用 claude-code 基于已经建立的 SDD 规范进行存量项目的改造？

**关键挑战**:
1. SDD 规范已经建立（Constitution + Specs）
2. 现有代码已经实现（215+ 单元测试）
3. 如何让 claude-code 遵循 SDD 规范生成代码？
4. 如何验证 claude-code 生成的代码符合规范？

---

## 📊 当前状态

### SDD 规范状态

| 组件 | 状态 | 说明 |
|------|------|------|
| Constitution | ✅ 100% | 项目原则（16,584 字节） |
| Specs | 🟡 40% | 15 个 specs，6 个完整填充 |
| Plan/Tasks | 🟡 40% | P0 功能完整，其他待填充 |

### 存量代码状态

| 组件 | 状态 | 说明 |
|------|------|------|
| 代码库 | ✅ 成熟 | MVVM + Clean Architecture |
| 测试 | ✅ 完整 | 215+ 单元测试 |
| 文档 | ✅ 完整 | README, REQUIREMENTS, ARCHITECTURE |

---

## 🚀 Claude Code + SDD 工作流

### 阶段 1: 准备阶段（确保 claude-code 能访问 SDD 规范）

#### 1.1 配置项目上下文

```bash
# 在项目根目录创建 .claude-code 目录
mkdir -p .claude-code

# 创建上下文配置文件
cat > .claude-code/context.txt << 'EOF'
# SDD Constitution
.constitution=$(cat .specify/memory/constitution.md)

# 当前功能 Spec
# 假设我们要改造 002-ingredient-management
.spec=$(cat .specify/specs/002-ingredient-management/spec.md)
.plan=$(cat .specify/specs/002-ingredient-management/plan.md)
.tasks=$(cat .specify/specs/002-ingredient-management/tasks.md)
EOF
```

#### 1.2 创建 Prompt 模板

```bash
# 创建 prompt 模板
cat > .claude-code/prompts/sdd-development.txt << 'EOF'
你是一个经验丰富的 Android 开发工程师，专精于 Kotlin + Jetpack Compose + Room。

## SDD 规范

### Constitution (项目原则)
请严格遵循以下项目原则：

<<CONSTITUTION>>

### 当前功能规范

#### Spec (需求规范)
<<SPEC>>

#### Plan (技术方案)
<<PLAN>>

#### Tasks (任务清单)
<<TASKS>>

## 开发要求

请确保：
1. 代码符合 Constitution 中的技术原则
2. 实现符合 spec.md 中的需求
3. 架构符合 plan.md 中的设计
4. 完成所有 tasks.md 中的任务

## 输出要求

请生成：
1. Entity 类（如果需要）
2. DAO 接口（如果需要）
3. Repository 类（如果需要）
4. ViewModel 类（如果需要）
5. Screen Composable（如果需要）
6. 相关的测试类

每个文件都应该：
- 遵循 Kotlin 编码规范
- 包含必要的注释
- 使用正确的注解（@Entity, @Dao, @Composable 等）
EOF
```

---

### 阶段 2: 开发阶段（使用 claude-code 基于规范开发）

#### 2.1 优化现有功能（半 SDD）

**适用场景**: 优化 P0/P1 功能

**工作流**:

```bash
# 1. 从现有代码提取信息
cd /root/work/homepantry

# 2. 阅读相关代码
# 例如：优化 002-ingredient-management
cat android/app/src/main/java/com/homepantry/data/entity/Ingredient.kt
cat android/app/src/main/java/com/homepantry/data/dao/IngredientDao.kt
cat android/app/src/main/java/com/homepantry/data/repository/IngredientRepository.kt

# 3. 使用 claude-code 优化
# 假设我们需要添加一个新的方法：getExpiringItemsInNextNDays

# 4. 准备 prompt
cat > /tmp/prompt.txt << 'EOF'
请为 002-ingredient-management 添加一个新的方法：

需求：获取 N 天内即将过期的食材

请实现以下功能：
1. 在 IngredientDao 中添加一个新方法
2. 在 IngredientRepository 中添加一个新方法
3. 在 IngredientViewModel 中添加一个新方法
4. 在 PantryScreen 中添加一个 Composable 显示即将过期的食材

请遵循以下规范：
<<CONSTITUTION>>
<<SPEC>>
<<PLAN>>
EOF

# 5. 调用 claude-code
claude-code --prompt-file /tmp/prompt.txt

# 6. Code Review
```

#### 2.2 实现新功能（完全 SDD）

**适用场景**: 实现 P2/P3 功能

**工作流**:

```bash
# 1. 创建新功能的 spec
cd /root/work/homepantry/.specify

# 2. 使用工具创建模板
./create-spec.sh 009 weekly-menu

# 3. 填充文档
# 编辑 specs/009-weekly-menu/ 下的 6 个文档

# 4. 使用 claude-code 实现
# 假设我们需要实现"周菜单生成"功能

# 5. 准备 prompt
cat > /tmp/prompt.txt << 'EOF'
请实现 009-weekly-menu 功能：

需求规范：
<<SPEC>>

技术方案：
<<PLAN>>

任务清单：
<<TASKS>>

请确保：
1. 代码符合 Constitution 中的技术原则
2. 实现符合 spec.md 中的所有需求
3. 架构符合 plan.md 中的设计
4. 完成所有 tasks.md 中的任务

请生成：
1. WeeklyMenuGenerator 类
2. WeeklyMenuRepository 类
3. WeeklyMenuViewModel 类
4. WeeklyMenuScreen Composable
5. 相关的测试类
EOF

# 6. 调用 claude-code
claude-code --prompt-file /tmp/prompt.txt

# 7. Code Review
```

---

### 阶段 3: 验证阶段（确保代码符合 SDD 规范）

#### 3.1 自动化验证

```bash
# 使用静态代码分析工具
cd /root/work/homepantry/android

# 检查代码质量
./gradlew detekt

# 检查代码格式
./gradlew ktlintCheck

# 检查代码问题
./gradlew lint

# 运行测试
./gradlew test

# 检查测试覆盖率
./gradlew jacocoTestReport
```

#### 3.2 人工 Code Review 对照表

```markdown
## Code Review 对照表

### 代码质量检查

- [ ] 代码符合 Constitution 中的技术原则
  - [ ] Kotlin 编码规范
  - [ ] MVVM + Clean Architecture
  - [ ] 测试覆盖率 ≥ 70%

- [ ] 代码符合 spec.md 中的需求
  - [ ] 所有 User Stories 都已实现
  - [ ] 所有 Non-Functional Requirements 都已满足

- [ ] 代码符合 plan.md 中的设计
  - [ ] 数据层设计正确
  - [ ] 业务逻辑层设计正确
  - [ ] 表现层设计正确

- [ ] 完成所有 tasks.md 中的任务
  - [ ] 数据层实现
  - [ ] 业务逻辑层实现
  - [ ] 表现层实现
  - [ ] 导航实现
  - [ ] 测试实现
```

---

## 📚 实战示例：优化 002-ingredient-management

### 场景：添加"获取 N 天内即将过期的食材"功能

#### 步骤 1: 分析现有代码

```kotlin
// 现有代码（IngredientDao.kt）
@Query("SELECT * FROM pantry_items WHERE expiryDate < :expiryTime")
suspend fun getExpiringItems(expiryTime: Long): List<PantryItem>
```

#### 步骤 2: 定义需求

```markdown
## 需求

添加一个新方法，获取 N 天内即将过期的食材

参数：
- days: Int (天数)

返回值：
- List<PantryItem> (即将过期的食材)
```

#### 步骤 3: 使用 claude-code 实现

```bash
# 准备 prompt
cat > /tmp/prompt.txt << 'EOF'
请为 002-ingredient-management 添加一个新方法：

需求：获取 N 天内即将过期的食材

请遵循以下规范：

### Constitution (项目原则)
<<CONSTITUTION>>

### 当前功能规范
<<SPEC>>

### 技术方案
<<PLAN>>

### 实现要求

1. 在 IngredientDao 中添加一个新方法：
   ```kotlin
   @Query("SELECT * FROM pantry_items WHERE expiryDate BETWEEN :startTime AND :endTime ORDER BY expiryDate ASC")
   fun getExpiringItemsInNextNDays(days: Int): Flow<List<PantryItem>>
   ```

2. 在 IngredientRepository 中添加一个新方法：
   ```kotlin
   fun getExpiringItemsInNextNDays(days: Int): Flow<List<PantryItem>>
   ```

3. 在 IngredientViewModel 中添加一个新方法：
   ```kotlin
   fun loadExpiringItems(days: Int)
   ```

4. 在 PantryScreen 中添加一个 Composable 显示即将过期的食材

请确保：
1. 代码符合 Constitution 中的技术原则
2. 使用 Flow 返回实时数据
3. 正确使用 Room 的 @Query 注解
4. 添加必要的测试
EOF

# 调用 claude-code
claude-code --prompt-file /tmp/prompt.txt
```

#### 步骤 4: Code Review

```markdown
## Code Review

### 代码质量检查

- [ ] 代码符合 Constitution 中的技术原则
- [ ] 使用 Flow 返回实时数据
- [ ] 正确使用 Room 的 @Query 注解
- [ ] 添加必要的测试

### 需求验证

- [ ] 可以获取 N 天内即将过期的食材
- [ ] 按过期时间排序
- [ ] 实时更新（使用 Flow）
```

---

## 🎓 学习成果

### 1. SDD + Claude Code 的协同模式

**角色分工**:
- **我 (Orchestrator)**: 建立 SDD 规范，管理文档，设计架构
- **Claude Code (Executor)**: 基于 SDD 规范生成代码

**协作模式**:
1. 我负责：Constitution, Specs, Plan, Tasks
2. Claude Code 负责：基于规范生成代码
3. Code Review: 我负责，确保质量

### 2. 存量项目改造的优先级

**P0 功能 (优化)**:
- 001: recipe-management
- 002: ingredient-management
- 003: meal-plan
- 004: shopping-list
- 005: cooking-mode

**P1 功能 (补充 SDD 规范)**:
- 006: smart-recommendation
- 007: pantry-inventory
- 008: cooking-records

**P2/P3 功能 (完全 SDD)**:
- 009-015

### 3. 验证和持续改进

**验证方法**:
1. 自动化验证（detekt, ktlint, lint, test）
2. 人工 Code Review（对照 SDD 文档）
3. 持续更新文档（基于代码变更）

**持续改进**:
1. 根据实际情况调整 SDD 规范
2. 根据代码质量调整 prompts
3. 根据反馈优化工作流

---

## 🚀 下一步行动

### 立即行动（今天/本周）

#### 选项1: 优化 P0 功能

```bash
# 选择一个 P0 功能进行优化
# 例如：优化 002-ingredient-management

cd /root/work/homepantry

# 1. 阅读相关代码
cat android/app/src/main/java/com/homepantry/data/entity/Ingredient.kt
cat android/app/src/main/java/com/homepantry/data/dao/IngredientDao.kt
cat android/app/src/main/java/com/homepantry/data/repository/IngredientRepository.kt

# 2. 使用 claude-code 优化
# 参考 CLAUDE-CODE-PLUS-SDD-GUIDE.md 中的工作流
```

#### 选项2: 实现 P1 功能

```bash
# 选择一个 P1 功能进行实现
# 例如：实现 006-smart-recommendation

cd /root/work/homepantry/.specify

# 1. 补充剩余文档（plan.md, tasks.md, research.md, README.md）
# 2. 使用 claude-code 实现
# 3. Code Review
```

#### 选项3: 建立 Claude Code + SDD 工作流

```bash
# 1. 创建 prompt 模板
mkdir -p .claude-code/prompts

# 2. 创建 context 配置
cat > .claude-code/context.txt

# 3. 测试工作流
# 优化一个简单的功能，验证工作流是否有效
```

---

## 💡 关键洞察

### 1. SDD 是 Claude Code 的"导航系统"

**作用**:
- Constitution: 指导原则（北星）
- Specs: 功能地图（详细路线）
- Plan: 技术方案（具体路径）
- Tasks: 任务清单（里程碑）

**价值**:
- 让 Claude Code 有明确的方向
- 减少代码错误
- 提高代码质量

### 2. Claude Code 是 SDD 的"执行引擎"

**作用**:
- 基于 SDD 规范生成代码
- 自动化重复工作
- 提高开发效率

**价值**:
- 快速生成代码
- 遵循项目规范
- 减少开发时间

### 3. 存量项目改造的策略

**优先级**:
1. **P0 功能优化**: 提高用户使用最频繁的核心功能
2. **P1 功能实现**: 补充 SDD 规范，然后实现
3. **P2/P3 功能实现**: 按 SDD 工作流开发

**方法**:
- **优化功能**: 半 SDD（基于现有代码 + SDD 规范）
- **新功能**: 完全 SDD（从头开始）

---

## 📊 成功指标

### 功能指标

| 指标 | 目标 | 说明 |
|------|------|------|
| 代码质量 | ≥ 80% | detekt, ktlint, lint 检查通过 |
| 测试覆盖率 | ≥ 70% | JaCoCo 报告 |
| SDD 遵循度 | ≥ 90% | Code Review 对照表 |
| 开发效率 | +50% | 相比传统开发 |

### 质量指标

| 指标 | 目标 | 说明 |
|------|------|------|
| 代码符合 Constitution | 100% | 所有原则都遵循 |
| 代码符合 Spec | ≥ 95% | 所有需求都实现 |
| 代码符合 Plan | ≥ 90% | 架构设计一致 |

---

## 📚 参考资料

### 项目文档

- [Constitution](./memory/constitution.md)
- [SDD-MIGRATION-GUIDE.md](./SDD-MIGRATION-GUIDE.md)
- [CLAUDE-CODE-PLUS-SDD-GUIDE.md](./CLAUDE-CODE-PLUS-SDD-GUIDE.md)
- [SDD-FINAL-SUMMARY-REPORT.md](./SDD-FINAL-SUMMARY-REPORT.md)

### Claude Code 文档

- [Claude Code 文档](https://docs.anthropic.com/claude/docs/claude-code/overview)
- [Claude Code 最佳实践](https://docs.anthropic.com/claude/docs/claude-code/best-practices)

### 已完成 Specs

- [001-recipe-management](./specs/001-recipe-management/)
- [002-ingredient-management](./specs/002-ingredient-management/)
- [003-meal-plan](./specs/003-meal-plan/)
- [004-shopping-list](./specs/004-shopping-list/)
- [005-cooking-mode](./specs/005-cooking-mode/)
- [006-smart-recommendation](./specs/006-smart-recommendation/)

---

## 🎯 总结

### SDD + Claude Code 的价值

**对刘钢（高级后端架构师）的价值**:
- 规范驱动开发，确保代码质量
- AI 辅助开发，提高开发效率
- 可追溯性，每个功能都有完整记录

**对项目的价值**:
- 减少技术债务
- 提高代码质量
- 加快开发进度
- 降低维护成本

---

**文档版本**: 1.0
**创建日期**: 2026-02-15
**最后更新**: 2026-02-15
**负责人**: Jude 🦞
