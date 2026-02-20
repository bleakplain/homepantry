# 现有功能迁移指南

**日期**: 2026-02-15
**目的**: 指导如何将现有功能迁移为 SDD specs

---

## 📋 迁移步骤概览

对于每个功能（002-015），执行以下步骤：

### Step 1: 创建目录
```bash
cd /root/work/homepantry/.specify/specs
mkdir -p 00X-feature-name/contracts
```

### Step 2: 创建 spec.md（需求规范）

**参考模板**: `.specify/specs/001-recipe-management/spec.md`

**核心内容**:
1. **What**: 功能描述、核心功能、用户场景
2. **Why**: 业务价值、问题解决、成功指标
3. **User Stories**: 用户故事和验收标准
4. **Non-Functional Requirements**: 性能、可用性、兼容性
5. **Constraints**: 技术约束、业务约束
6. **Dependencies**: 依赖关系
7. **Out of Scope**: 不在本次实现的功能

**信息来源**:
- `REQUIREMENTS.md`: 提取用户场景和功能需求
- 代码：提取已实现的功能

### Step 3: 创建 plan.md（技术方案）

**参考模板**: `.specify/specs/001-recipe-management/plan.md`

**核心内容**:
1. **技术栈**: 技术版本和用途
2. **架构设计**: 整体架构、数据层、业务逻辑层、表现层
3. **数据模型**: Entity、DAO、Repository
4. **关键算法**: 核心算法说明
5. **性能优化**: 数据库优化、UI 优化
6. **测试策略**: 单元测试、UI 测试
7. **部署策略**: 数据库迁移、Gradle 配置

**信息来源**:
- `ARCHITECTURE.md`: 提取架构设计
- 代码：提取 Entity、DAO、Repository、ViewModel、Screen

### Step 4: 创建 data-model.md（数据模型）

**参考模板**: `.specify/specs/001-recipe-management/data-model.md`

**核心内容**:
1. **实体定义**: Entity 字段、索引、关系
2. **关系设计**: 一对多、多对多关系
3. **枚举定义**: 相关的枚举类型

**信息来源**:
- 代码：`android/app/src/main/java/com/homepantry/data/entity/`

### Step 5: 创建 tasks.md（任务清单）

**参考模板**: `.specify/specs/001-recipe-management/tasks.md`

**核心内容**:
1. **已完成任务**: 数据层、业务逻辑层、表现层、导航、测试、性能优化、文档更新
2. **验证清单**: 功能验证、性能验证、测试验证、文档验证

**信息来源**:
- 代码：提取已实现的类和方法
- 测试代码：提取已实现的测试

### Step 6: 创建 research.md（技术调研，可选）

**参考模板**: `.specify/specs/001-recipe-management/research.md`

**核心内容**:
1. **技术调研**: 技术选型对比
2. **关键技术问题**: 问题分析和解决方案
3. **性能测试结果**: 测试数据和结果
4. **已知问题和限制**: 问题和优化方向

**信息来源**:
- `ARCHITECTURE.md`: 提取技术选型原因
- `TEST_REPORT.md`: 提取性能测试结果

### Step 7: 创建 README.md（文档总结）

**参考模板**: `.specify/specs/001-recipe-management/README.md`

**核心内容**:
1. **文档列表**: 所有文档的字符数
2. **核心内容**: What、Why、技术实现、数据模型、实现状态
3. **验收标准**: 功能、性能、测试
4. **性能测试结果**: 测试数据
5. **已知问题和优化方向**

---

## 🚀 快速迁移示例

### 示例1: 002-ingredient-management（已完成）

**已创建**:
- ✅ spec.md (2,913 字符)

**待创建**:
- ⬜ plan.md
- ⬜ data-model.md
- ⬜ tasks.md
- ⬜ research.md
- ⬜ README.md

**代码来源**:
- Entity: `android/app/src/main/java/com/homepantry/data/entity/Ingredient.kt`
- DAO: `android/app/src/main/java/com/homepantry/data/dao/IngredientDao.kt`
- Repository: `android/app/src/main/java/com/homepantry/data/repository/IngredientRepository.kt`

### 示例2: 003-meal-plan（待创建）

**代码来源**:
- Entity: `android/app/src/main/java/com/homepantry/data/entity/MealPlan.kt`
- DAO: `android/app/src/main/java/com/homepantry/data/dao/MealPlanDao.kt`
- Repository: `android/app/src/main/java/com/homepantry/data/repository/MealPlanRepository.kt`
- ViewModel: `android/app/src/main/java/com/homepantry/viewmodel/MealPlanViewModel.kt`
- Screen: `android/app/src/main/java/com/homepantry/ui/mealplan/MealPlanScreen.kt`

### 示例3: 004-shopping-list（待创建）

**代码来源**:
- Entity: `android/app/src/main/java/com/homepantry/data/entity/ShoppingList.kt`
- Entity: `android/app/src/main/java/com/homepantry/data/entity/ShoppingItem.kt`
- DAO: `android/app/src/main/java/com/homepantry/data/dao/ShoppingListDao.kt`
- Repository: `android/app/src/main/java/com/homepantry/data/repository/ShoppingListRepository.kt`

### 示例4: 005-cooking-mode（待创建）

**代码来源**:
- Manager: `android/app/src/main/java/com/homepantry/ui/cooking/CookingModeManager.kt`
- Screen: `android/app/src/main/java/com/homepantry/ui/cooking/EnhancedCookingModeScreen.kt`
- Voice: `android/app/src/main/java/com/homepantry/ui/cooking/VoicePlaybackManager.kt`

---

## 📊 代码文件映射表

| Spec ID | 功能名称 | Entity | DAO | Repository | ViewModel | Screen |
|---------|---------|--------|-----|-----------|-----------|--------|
| 002 | 食材管理 | Ingredient | IngredientDao | IngredientRepository | IngredientViewModel | IngredientScreen |
| 003 | 餐食计划 | MealPlan | MealPlanDao | MealPlanRepository | MealPlanViewModel | MealPlanScreen |
| 004 | 购物清单 | ShoppingList, ShoppingItem | ShoppingListDao | ShoppingListRepository | ShoppingListViewModel | ShoppingListScreen |
| 005 | 烹饪模式 | - | - | - | - | EnhancedCookingModeScreen |
| 006 | 智能推荐 | - | - | - | - | - |
| 007 | 食材库存 | PantryItem | IngredientDao (pantry) | - | - | PantryScreen |
| 008 | 制作记录 | RecipeNote | RecipeNoteDao | - | - | - |
| 009 | 周菜单生成 | - | - | WeeklyMealPlanGenerator | - | - |
| 010 | 营养分析 | NutritionInfo | NutritionInfoDao | NutritionAnalyzer | - | - |
| 011 | 宴请模式 | - | - | BanquetMenuGenerator | - | - |
| 012 | 快速保存 | - | - | - | - | - |
| 013 | 语音播报 | - | - | VoicePlaybackManager | - | - |
| 014 | 家庭管理 | FamilyMember | FamilyMemberDao | - | - | FamilyScreen |
| 015 | 数据统计 | - | - | - | - | - |

---

## 💡 迁移技巧

### 1. 批量创建目录
```bash
cd /root/work/homepantry/.specify/specs
for i in {002..015}; do
    mkdir -p $i-feature/contracts
done
```

### 2. 复制模板
```bash
# 复制 spec.md 模板
cp 001-recipe-management/spec.md 002-feature/spec.md

# 复制 plan.md 模板
cp 001-recipe-management/plan.md 002-feature/plan.md

# 复制 data-model.md 模板
cp 001-recipe-management/data-model.md 002-feature/data-model.md

# 复制 tasks.md 模板
cp 001-recipe-management/tasks.md 002-feature/tasks.md

# 复制 research.md 模板
cp 001-recipe-management/research.md 002-feature/research.md

# 复制 README.md 模板
cp 001-recipe-management/README.md 002-feature/README.md
```

### 3. 查看代码结构
```bash
# 查看 Entity
find android/app/src/main/java/com/homepantry/data/entity/ -type f -name "*.kt"

# 查看 DAO
find android/app/src/main/java/com/homepantry/data/dao/ -type f -name "*.kt"

# 查看 Repository
find android/app/src/main/java/com/homepantry/data/repository/ -type f -name "*.kt"

# 查看 ViewModel
find android/app/src/main/java/com/homepantry/viewmodel/ -type f -name "*.kt"

# 查看 Screen
find android/app/src/main/java/com/homepantry/ui/ -type f -name "*Screen.kt"
```

### 4. 提取 Entity 信息
```bash
# 提取 Entity 字段
grep -A 20 "data class" android/app/src/main/java/com/homepantry/data/entity/Ingredient.kt
```

### 5. 提取 DAO 方法
```bash
# 提取 DAO 方法
grep -E "@Query|@Insert|@Update|@Delete" android/app/src/main/java/com/homepantry/data/dao/IngredientDao.kt
```

---

## 🎯 迁移检查清单

对于每个功能，检查以下项目：

### spec.md
- [ ] What: 功能描述、核心功能、用户场景
- [ ] Why: 业务价值、问题解决、成功指标
- [ ] User Stories: 至少 2-3 个用户故事
- [ ] Non-Functional Requirements: 性能、可用性、兼容性
- [ ] Constraints: 技术约束、业务约束
- [ ] Dependencies: 依赖关系
- [ ] Out of Scope: 不在本次实现的功能

### plan.md
- [ ] 技术栈: 技术版本和用途
- [ ] 架构设计: 整体架构、数据层、业务逻辑层、表现层
- [ ] 数据模型: Entity、DAO、Repository
- [ ] 关键算法: 核心算法说明
- [ ] 性能优化: 数据库优化、UI 优化
- [ ] 测试策略: 单元测试、UI 测试
- [ ] 部署策略: 数据库迁移、Gradle 配置

### data-model.md
- [ ] 实体定义: Entity 字段、索引、关系
- [ ] 关系设计: 一对多、多对多关系
- [ ] 枚举定义: 相关的枚举类型

### tasks.md
- [ ] 已完成任务: 数据层、业务逻辑层、表现层、导航、测试
- [ ] 验证清单: 功能验证、性能验证、测试验证、文档验证

### research.md
- [ ] 技术调研: 技术选型对比
- [ ] 关键技术问题: 问题分析和解决方案
- [ ] 性能测试结果: 测试数据和结果
- [ ] 已知问题和限制: 问题和优化方向

### README.md
- [ ] 文档列表: 所有文档的字符数
- [ ] 核心内容: What、Why、技术实现、数据模型、实现状态
- [ ] 验收标准: 功能、性能、测试
- [ ] 性能测试结果: 测试数据

---

## 📚 参考资料

- [001-recipe-management/spec.md](../001-recipe-management/spec.md)
- [001-recipe-management/plan.md](../001-recipe-management/plan.md)
- [001-recipe-management/data-model.md](../001-recipe-management/data-model.md)
- [001-recipe-management/tasks.md](../001-recipe-management/tasks.md)
- [001-recipe-management/research.md](../001-recipe-management/research.md)
- [001-recipe-management/README.md](../001-recipe-management/README.md)
- [REQUIREMENTS.md](../../../docs/REQUIREMENTS.md)
- [ARCHITECTURE.md](../../../docs/ARCHITECTURE.md)
- [Constitution](../memory/constitution.md)

---

**文档版本**: 1.0
**创建日期**: 2026-02-15
**最后更新**: 2026-02-15
**负责人**: Jude 🦞
