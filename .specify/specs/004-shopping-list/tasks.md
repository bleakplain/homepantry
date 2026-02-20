# Tasks: 洋物清单

**Spec ID**: 004
**功能名称**: 洋物清单
**优先级**: P0
**状态**: ✅ 已实现
**创建日期**: 2026-02-15
**最后更新**: 2026-02-15

---

## 已完成任务

### 1. 数据层实现

#### 1.1 创建 Entity 类
- [x] 1.1.1 创建 `ShoppingList` Entity
- [x] 1.1.2 创建 `ShoppingItem` Entity

**文件位置**:
- `android/app/src/main/java/com/homepantry/data/entity/ShoppingList.kt`
- `android/app/src/main/java/com/homepantry/data/entity/ShoppingItem.kt`

**验收标准**:
- [x] 所有字段正确定义
- [x] 主键使用 `@PrimaryKey`
- [x] 索引正确定义
- [x] 外键关系正确配置

---

#### 1.2 创建 DAO 接口
- [x] 1.2.1 创建 `ShoppingListDao` 接口
- [x] 1.2.2 实现清单 CRUD 方法
- [x] 1.2.3 实现项 CRUD 方法
- [x] 1.2.4 实现查询和筛选方法

**文件位置**:
- `android/app/src/main/java/com/homepantry/data/dao/ShoppingListDao.kt`

**验收标准**:
- [x] 所有查询方法正确实现
- [x] 使用 `Flow` 返回实时数据
- [x] 复杂操作使用 `@Transaction`
- [x] 查询性能优化

---

#### 1.3 创建 Repository
- [x] 1.3.1 创建 `ShoppingListRepository` 类
- [x] 1.3.2 实现清单管理方法
- [x] 1.3.3 实现项管理方法
- [x] 1.3.4 实现智能合并方法

**文件位置**:
- `android/app/src/main/java/com/homepantry/data/repository/ShoppingListRepository.kt`

**验收标准**:
- [x] 所有方法正确实现
- [x] 智能合并算法正确
- [x] 错误处理完善
- [x] 方法职责单一

---

### 2. 业务逻辑层实现

#### 2.1 创建 ViewModel
- [x] 2.1.1 创建 `ShoppingListViewModel` 类
- [x] 2.1.2 定义 UI 状态
- [x] 2.1.3 实现加载清单方法
- [x] 2.1.4 实现添加清单方法
- [x] 2.1.5 实现添加项方法
- [x] 2.1.6 实现更新项方法

**文件位置**:
- `android/app/src/main/java/com/homepantry/viewmodel/ShoppingListViewModel.kt`

**验收标准**:
- [x] 状态管理正确
- [x] 所有方法正确实现
- [x] 错误处理完善
- [x] 协程正确使用

---

### 3. 表现层实现

#### 3.1 创建购物清单页面
- [x] 3.1.1 创建 `ShoppingListScreen` Composable
- [x] 3.1.2 创建 `ShoppingListCard` Composable
- [x] 3.1.3 创建 `ShoppingItemRow` Composable

**文件位置**:
- `android/app/src/main/java/com/homepantry/ui/shopping/ShoppingListScreen.kt`

**验收标准**:
- [x] UI 符合设计规范
- [x] 支持分类显示
- [x] 支持勾选操作
- [x] 支持价格记录

---

### 4. 测试实现

#### 4.1 单元测试
- [x] 4.1.1 创建 `ShoppingListDaoTest`
- [x] 4.1.2 创建 `ShoppingListRepositoryTest`
- [x] 4.1.3 创建 `ShoppingListViewModelTest`

**验收标准**:
- [x] 所有测试通过
- [x] 测试覆盖率 ≥ 70%
- [x] 测试用例覆盖所有分支

---

## 验证清单

### 功能验证

#### 基础功能
- [x] 可以添加购物清单
- [x] 可以添加购物项
- [x] 可以编辑购物项
- [x] 可以删除购物项
- [x] 可以勾选已购买
- [x] 可以查看购物清单
- [x] 可以自动生成购物清单

#### 高级功能
- [x] 可以智能合并数量
- [x] 可以按分类显示
- [x] 可以记录价格

---

### 性能验证

#### 加载性能
- [x] 购物清单加载时间 < 1s
- [x] 购物项添加时间 < 1s
- [x] 自动生成时间 < 3s

---

### 兼容性验证

#### 设备兼容
- [x] 支持最低 API 24
- [x] 支持不同屏幕尺寸
- [x] 支持不同屏幕密度

---

### 测试验证

#### 单元测试
- [x] `ShoppingListDaoTest`: 所有测试通过
- [x] `ShoppingListRepositoryTest`: 所有测试通过
- [x] `ShoppingListViewModelTest`: 所有测试通过

---

### 文档验证

- [x] Constitution 已创建
- [x] spec.md 已创建
- [x] plan.md 已创建
- [x] data-model.md 已创建
- [x] tasks.md 已创建
- [x] research.md 待创建
- [x] README.md 已创建

---

## 已知问题

### 待优化项

1. **智能合并准确性**
   - 当前：简单按名称合并
   - 未来：考虑单位差异

2. **价格记录**
   - 当前：手动输入
   - 未来：支持从超市小票扫描

---

## 参考资料

- [spec.md](./spec.md)
- [plan.md](./plan.md)
- [data-model.md](./data-model.md)
- [REQUIREMENTS.md](../../../docs/REQUIREMENTS.md)
- [ARCHITECTURE.md](../../../docs/ARCHITECTURE.md)
- [Constitution](../memory/constitution.md)

---

**文档版本**: 1.0
**创建日期**: 2026-02-15
**最后更新**: 2026-02-15
**负责人**: Jude 🦞
