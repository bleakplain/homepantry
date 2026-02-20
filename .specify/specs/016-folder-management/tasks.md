# Tasks: 收藏分类管理

**Spec ID**: 016
**功能名称**: 收藏分类管理
**优先级**: P1
**状态**: 🚧 规划中
**创建日期**: 2026-02-20
**最后更新**: 2026-02-20

---

## 待办任务

### 数据层

- [ ] **Folder Entity** (`data/entity/Folder.kt`)
  - 字段定义
  - 表配置
  - Room 注解

- [ ] **RecipeFolder Entity** (`data/entity/RecipeFolder.kt`)
  - 字段定义
  - 表配置
  - 外键和索引

- [ ] **FolderDao** (`data/dao/FolderDao.kt`)
  - insert(Folder)
  - update(Folder)
  - deleteById(String)
  - updateSortOrder(String, Int)
  - getMaxSortOrder()
  - getAllFolders()
  - getFolderById(String)

- [ ] **RecipeFolderDao** (`data/dao/RecipeFolderDao.kt`)
  - insert(RecipeFolder)
  - delete(String, String)
  - deleteByFolderId(String)
  - deleteByRecipeId(String)
  - exists(String, String)
  - getRecipesByFolderId(String)
  - getFoldersByRecipeId(String)

- [ ] **FolderRepository** (`data/repository/FolderRepository.kt`)
  - createFolder(name, icon, color)
  - updateFolder(folder)
  - deleteFolder(folderId)
  - reorderFolders(folderIds)
  - getFolders()
  - getFolderById(folderId)

- [ ] **RecipeFolderRepository** (`data/repository/RecipeFolderRepository.kt`)
  - addToFolder(recipeId, folderId)
  - batchAddToFolder(recipeIds, folderId)
  - removeFromFolder(recipeId, folderId)
  - getRecipesByFolderId(folderId)
  - getFoldersByRecipeId(recipeId)

### 业务逻辑层

- [ ] **FolderViewModel** (`viewmodel/FolderViewModel.kt`)
  - folders: StateFlow<List<Folder>>
  - createFolder(name, icon, color)
  - updateFolder(folder)
  - deleteFolder(folderId)
  - reorderFolders(folderIds)

- [ ] **FolderDetailViewModel** (`viewmodel/FolderDetailViewModel.kt`)
  - folderWithRecipes: StateFlow<FolderWithRecipes?>
  - loadFolder(folderId)
  - addToFolder(recipeId, folderId)
  - removeFromFolder(recipeId, folderId)

### 表现层

- [ ] **FolderListScreen** (`ui/folder/FolderListScreen.kt`)
  - 收藏夹列表
  - 创建收藏夹
  - 拖拽排序
  - 搜索收藏夹

- [ ] **FolderDetailScreen** (`ui/folder/FolderDetailScreen.kt`)
  - 收藏夹详情
  - 菜谱列表
  - 排序和筛选
  - 收藏/取消收藏

- [ ] **CreateFolderDialog** (`ui/folder/CreateFolderDialog.kt`)
  - 创建收藏夹对话框
  - 名称输入
  - 图标选择
  - 颜色选择

- [ ] **EditFolderDialog** (`ui/folder/EditFolderDialog.kt`)
  - 编辑收藏夹对话框
  - 名称修改
  - 图标和颜色更新

### 导航

- [ ] 收藏夹列表路由
- [ ] 收藏夹详情路由

### 测试

- [ ] **FolderDaoTest** (`test/dao/FolderDaoTest.kt`)
  - 插入测试
  - 更新测试
  - 删除测试
  - 查询测试
  - 排序测试

- [ ] **RecipeFolderDaoTest** (`test/dao/RecipeFolderDaoTest.kt`)
  - 插入测试
  - 删除测试
  - 查询测试
  - 唯一约束测试

- [ ] **FolderRepositoryTest** (`test/repository/FolderRepositoryTest.kt`)
  - 创建收藏夹测试
  - 更新收藏夹测试
  - 删除收藏夹测试
  - 排序测试

- [ ] **RecipeFolderRepositoryTest** (`test/repository/RecipeFolderRepositoryTest.kt`)
  - 收藏菜谱测试
  - 批量收藏测试
  - 移除菜谱测试
  - 查询测试

- [ ] **FolderViewModelTest** (`test/viewmodel/FolderViewModelTest.kt`)
  - 创建收藏夹测试
  - 更新收藏夹测试
  - 删除收藏夹测试
  - 排序测试

---

## 验收清单

### 功能验收

- [ ] 可以创建收藏夹
- [ ] 可以编辑收藏夹
- [ ] 可以删除收藏夹
- [ ] 可以收藏菜谱到收藏夹
- [ ] 可以批量收藏菜谱
- [ ] 可以从收藏夹移除菜谱
- [ ] 可以拖拽调整收藏夹顺序
- [ ] 可以搜索收藏夹

### 性能验收

- [ ] 收藏夹列表加载 < 500ms
- [ ] 收藏夹详情加载 < 500ms
- [ ] 收藏操作响应 < 500ms
- [ ] 批量收藏 < 2s（10个菜谱）

### 测试验收

- [ ] 单元测试覆盖率 ≥ 70%
- [ ] 收藏夹创建成功率 ≥ 99%
- [ ] 菜谱收藏成功率 ≥ 99%
- [ ] 批量操作成功率 ≥ 99%

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

1. 创建 Folder 实体
2. 创建 RecipeFolder 实体
3. 创建 FolderDao
4. 创建 RecipeFolderDao
5. 创建 FolderRepository
6. 创建 RecipeFolderRepository

### 阶段 2：业务逻辑层（0.5 天）

7. 创建 FolderViewModel
8. 创建 FolderDetailViewModel

### 阶段 3：表现层（1 天）

9. 创建 FolderListScreen
10. 创建 FolderDetailScreen
11. 创建 CreateFolderDialog
12. 创建 EditFolderDialog

### 阶段 4：导航和集成（0.5 天）

13. 配置导航
14. 集成到现有页面

### 阶段 5：测试（1 天）

15. 编写单元测试
16. 编写集成测试
17. 测试覆盖率检查

**总预计时间**: 2-3 天

---

## 参考资料

- [Folder.kt](../../../android/app/src/main/java/com/homepantry/data/entity/Folder.kt)
- [RecipeFolder.kt](../../../android/app/src/main/java/com/homepantry/data/entity/RecipeFolder.kt)
- [FolderDao.kt](../../../android/app/src/main/java/com/homepantry/data/dao/FolderDao.kt)
- [FolderRepository.kt](../../../android/app/src/main/java/com/homepantry/data/repository/FolderRepository.kt)

---

**文档版本**: 1.0
**创建日期**: 2026-02-20
**最后更新**: 2026-02-20
**负责人**: Jude 🦞
