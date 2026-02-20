# Tasks: 高级筛选

**Spec ID**: 017
**功能名称**: 高级筛选
**优先级**: P1
**状态**: 🚧 规划中
**创建日期**: 2026-02-20
**最后更新**: 2026-02-20

---

## 待办任务

### 数据层

- [ ] **RecipeFilter Entity** (`data/entity/RecipeFilter.kt`)
  - 字段定义
  - 表配置
  - Room 注解

- [ ] **RecipeFilterDao** (`data/dao/RecipeFilterDao.kt`)
  - insert(RecipeFilter)
  - update(RecipeFilter)
  - delete(RecipeFilter)
  - getAllFilters()
  - getFilterById(String)
  - filterRecipes(criteria)
  - filterRecipesPaged(criteria, limit, offset)

- [ ] **RecipeFilterRepository** (`data/repository/RecipeFilterRepository.kt`)
  - createFilter(criteria)
  - updateFilter(filter)
  - deleteFilter(filterId)
  - getFilters()
  - getActiveFilter()
  - applyFilter(criteria)

### 业务逻辑层

- [ ] **FilterViewModel** (`viewmodel/FilterViewModel.kt`)
  - filterCriteria: StateFlow<RecipeFilterCriteria>
  - filterResult: StateFlow<RecipeFilterResult?>
  - applyCookingTimeRange(min, max)
  - applyDifficultyRange(min, max)
  - addIngredient(ingredientId, type)
  - removeIngredient(ingredientId)
  - addCategory(categoryId)
  - removeCategory(categoryId)
  - clearFilters()
  - saveFilterAsPreset(name)

- [ ] **FilterDialogViewModel** (`viewmodel/FilterDialogViewModel.kt`)
  - currentFilter: StateFlow<RecipeFilterCriteria>
  - ingredients: StateFlow<List<Ingredient>>
  - categories: StateFlow<List<Category>>
  - onApply(filter)
  - onClear()

### 表现层

- [ ] **FilterDialog** (`ui/recipe/FilterDialog.kt`)
  - 烹饪时间筛选 UI
  - 难度筛选 UI
  - 食材筛选 UI
  - 分类筛选 UI
  - 预设筛选 UI
  - 应用/清除/保存按钮

- [ ] **CookingTimeFilterSection** (`ui/recipe/components/CookingTimeFilterSection.kt`)
  - 时间范围选择（<15、15-30、30-60、>60）
  - 自定义时间范围输入

- [ ] **DifficultyFilterSection** (`ui/recipe/components/DifficultyFilterSection.kt`)
  - 难度选择（简单、中等、困难）
  - 多选支持

- [ ] **IngredientFilterSection** (`ui/recipe/components/IngredientFilterSection.kt`)
  - 食材列表（可搜索）
  - 包含/排除切换
  - "使用现有食材"按钮

- [ ] **CategoryFilterSection** (`ui/recipe/components/CategoryFilterSection.kt`)
  - 分类列表（可搜索）
  - 多选支持

- [ ] **PresetsSection** (`ui/recipe/components/PresetsSection.kt`)
  - 预设筛选列表
  - 快速应用预设

- [ ] **FilterBadge** (`ui/recipe/components/FilterBadge.kt`)
  - 显示当前筛选数量
  - 点击打开筛选对话框

### 导航

- [ ] 筛选对话框路由配置
- [ ] 菜谱列表页集成筛选按钮

### 测试

- [ ] **RecipeFilterDaoTest** (`test/dao/RecipeFilterDaoTest.kt`)
  - 插入筛选器测试
  - 更新筛选器测试
  - 删除筛选器测试
  - 查询筛选器测试
  - 筛选菜谱测试
  - 分页筛选测试

- [ ] **RecipeFilterRepositoryTest** (`test/repository/RecipeFilterRepositoryTest.kt`)
  - 创建筛选器测试
  - 更新筛选器测试
  - 删除筛选器测试
  - 应用筛选器测试
  - 清除筛选器测试

- [ ] **FilterViewModelTest** (`test/viewmodel/FilterViewModelTest.kt`)
  - 应用时间范围测试
  - 应用难度范围测试
  - 添加食材测试
  - 删除食材测试
  - 添加分类测试
  - 删除分类测试
  - 清除筛选测试
  - 保存预设测试

---

## 验收清单

### 功能验收

- [ ] 可以按烹饪时间筛选（<15、15-30、30-60、>60 分钟）
- [ ] 可以按难度筛选（简单、中等、困难）
- [ ] 可以按食材筛选（包含/排除）
- [ ] 可以按分类筛选（多选）
- [ ] 可以组合多个筛选条件（AND 关系）
- [ ] 实时更新筛选结果
- [ ] 可以保存筛选条件为预设

### 性能验收

- [ ] 筛选响应时间 < 500ms（单条件）
- [ ] 筛选响应时间 < 1s（多条件、食材）
- [ ] 1000 个菜谱筛选 < 500ms
- [ ] 10000 个菜谱筛选 < 1s
- [ ] 内存占用 < 100MB（筛选操作）

### 测试验收

- [ ] 单元测试覆盖率 ≥ 70%
- [ ] RecipeFilterDao 测试 ≥ 8 个测试用例
- [ ] RecipeFilterRepository 测试 ≥ 6 个测试用例
- [ ] FilterViewModel 测试 ≥ 10 个测试用例

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

1. 创建 RecipeFilter Entity
2. 创建 RecipeFilterDao
3. 创建 RecipeFilterRepository
4. 编写单元测试

### 阶段 2：业务逻辑层（0.5 天）

5. 创建 FilterViewModel
6. 创建 FilterDialogViewModel
7. 编写单元测试

### 阶段 3：表现层（1.5 天）

8. 创建 FilterDialog
9. 创建 CookingTimeFilterSection
10. 创建 DifficultyFilterSection
11. 创建 IngredientFilterSection
12. 创建 CategoryFilterSection
13. 创建 PresetsSection
14. 创建 FilterBadge
15. 编写 UI 测试

### 阶段 4：导航和集成（0.5 天）

16. 配置筛选对话框路由
17. 集成到菜谱列表页
18. 测试导航流程

### 阶段 5：测试（0.5 天）

19. 运行所有单元测试
20. 运行集成测试
21. 测试覆盖率检查

**总预计时间**: 4 天

---

## 参考资料

- [RecipeFilter.kt](../../../android/app/src/main/java/com/homepantry/data/entity/RecipeFilter.kt)
- [RecipeFilterDao.kt](../../../android/app/src/main/java/com/homepantry/data/dao/RecipeFilterDao.kt)
- [RecipeFilterRepository.kt](../../../android/app/src/main/java/com/homepantry/data/repository/RecipeFilterRepository.kt)
- [FilterViewModel.kt](../../../android/app/src/main/java/com/homepantry/viewmodel/FilterViewModel.kt)
- [FilterDialog.kt](../../../android/app/src/main/java/com/homepantry/ui/recipe/FilterDialog.kt)

---

**文档版本**: 1.0
**创建日期**: 2026-02-20
**最后更新**: 2026-02-20
**负责人**: Jude 🦞
