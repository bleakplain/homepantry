# ktlint 代码格式化配置指南

**日期**: 2026-02-20
**时间**: 19:20 GMT+8
**任务**: ktlint 代码格式化工具配置
**工具**: ktlint（Kotlin 代码格式化工具）

---

## 📋 ktlint 简介

### 什么是 ktlint？

**ktlint** 是一个针对 Kotlin 编程语言的代码格式化工具，用于统一代码风格、检查代码格式、自动格式化代码。

**主要功能**:
- 代码格式化
- 代码风格检查
- 自动格式化
- 支持自定义规则
- 支持 EditorConfig
- 支持多模块项目

**优点**:
- 专为 Kotlin 设计
- 支持自定义规则
- 支持 EditorConfig（与 IDE 同步）
- 支持增量分析
- 支持多模块项目
- 支持 CI/CD 集成

---

## 🚀 安装 ktlint

### 方法 1：使用 Gradle 插件（推荐）

在项目根目录的 `build.gradle.kts` 中添加：

```kotlin
plugins {
    id("org.jlleitsch.ktlin-gradle") version "11.5.1"
}

ktlint {
    version = "1.0.1"
    debug = false
    verbose = true
    android = true
    outputToConsole = true
    outputColorName = "RED"
    ignoreFailures = false
    reporters {
        reporter("plain")
        reporter("checkstyle")
    }
}
```

### 方法 2：使用 Homebrew（macOS）

```bash
brew install ktlint
```

### 方法 3：使用 SDKMAN

```bash
sdk install ktlint
```

---

## 📋 ktlint 配置文件

### 创建 ktlint 配置文件

在项目根目录创建 `.editorconfig`：

```ini
# EditorConfig is awesome: https://EditorConfig.org
root = true

[*]
indent_size = 4
continuation_indent_size = 4
insert_final_newline = true
charset = utf-8
trim_trailing_whitespace = true
ij_kotlin_allow_trailing_comma = true
ij_kotlin_allow_trailing_comma_on_call_site = true
ij_kotlin_allow_trailing_comma_on_multiplicative_chain = false

[*.{kt,kts}]
ij_kotlin_imports_layout = *,java.**,javax.**,kotlin.**,androidx.**
ij_kotlin_allow_trailing_comma = true
ij_kotlin_allow_trailing_comma_on_call_site = true
ij_kotlin_allow_trailing_comma_on_multiplicative_chain = false
indent_size = 4
continuation_indent_size = 8
ij_kotlin_name_count_to_use_star_import = 999
ij_kotlin_name_count_to_use_star_import_for_members = 999
ij_kotlin_packages_to_use_import_on_demand = unset
ij_kotlin_packages_to_use_import_on_demand = unset
ij_kotlin_add_import_alias = false
ij_kotlin_packages_to_use_import_on_demand = unset
```

---

## 🚀 运行 ktlint

### 方法 1：使用 Gradle 插件（推荐）

```bash
# 运行 ktlint 检查
./gradlew ktlintCheck

# 运行 ktlint 格式化
./gradlew ktlintFormat

# 运行 ktlint 检查并格式化
./gradlew ktlintApplyToIdea

# 生成 ktlint 报告
./gradlew ktlintCheckstyleReport
```

### 方法 2：使用命令行工具

```bash
# 运行 ktlint 检查
ktlint "android/app/src/main/java/**/*.kt"

# 运行 ktlint 格式化
ktlint -F "android/app/src/main/java/**/*.kt"

# 运行 ktlint 检查并生成报告
ktlint --reporter=plain --reporter=checkstyle "android/app/src/main/java/**/*.kt" > build/reports/ktlint/ktlint-report.xml
```

---

## 📊 ktlint 报告

### HTML 报告

**生成命令**:
```bash
./gradlew ktlintCheckstyleReport
```

**报告路径**:
```
build/reports/ktlint/ktlint-report.xml
```

**使用方法**:
1. 在 Android Studio 中打开 `build/reports/ktlint/ktlint-report.xml`
2. 查看所有代码风格问题
3. 点击每个问题查看详情
4. 修复所有问题

---

## 📋 ktlint 规则配置

### 基本规则

```yaml
# ktlint 规则配置
# 路径：.editorconfig

# 缩进
indent_size = 4

# 换行
insert_final_newline = true

# 字符集
charset = utf-8

# 尾随空格
trim_trailing_whitespace = true

# 逗号
ij_kotlin_allow_trailing_comma = true
ij_kotlin_allow_trailing_comma_on_call_site = true
ij_kotlin_allow_trailing_comma_on_multiplicative_chain = false
```

### 导入规则

```yaml
# 导入规则
ij_kotlin_imports_layout = *,java.**,javax.**,kotlin.**,androidx.**
ij_kotlin_packages_to_use_import_on_demand = unset
ij_kotlin_add_import_alias = false
```

---

## 📋 CI/CD 集成

### GitHub Actions

在 `.github/workflows/ktlint.yml` 中创建：

```yaml
name: ktlint

on:
  pull_request:
    branches: [ master ]

jobs:
  ktlint:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      
      - name: Set up JDK 11
        uses: actions/setup-java@v2
        with:
          java-version: '11'
          distribution: 'adopt'

      - name: Run ktlint
        run: ./gradlew ktlintCheck

      - name: Upload ktlint Report
        uses: actions/upload-artifact@v2
        with:
          name: ktlint Report
          path: build/reports/ktlint/ktlint-report.xml
```

---

## 📋 代码审查清单

### 代码风格问题（P1）

- [ ] 缩进不正确（Indentation）
- [ ] 换行不正确（Line length）
- [ ] 尾随空格（Trailing whitespace）
- [ ] 字符集不正确（Charset）
- [ ] 导入顺序不正确（Imports order）
- [ ] 通配符导入（Wildcard import）
- [ ] 逗号使用不正确（Comma）

---

## 📊 总结

### ktlint 代码格式化工具

**工具**: ktlint（Kotlin 代码格式化工具）
**主要功能**: 代码格式化、代码风格检查、自动格式化
**优点**: 专为 Kotlin 设计、支持 EditorConfig、支持 CI/CD 集成
**配置文件**: `.editorconfig`
**报告路径**: `build/reports/ktlint/ktlint-report.xml`

---

## 🚀 开始使用 ktlint

### 立即执行（推荐）

1. **在项目根目录中运行 ktlint**
   ```bash
   ./gradlew ktlintCheck
   ```

2. **查看 ktlint 报告**
   - 在 Android Studio 中打开 `build/reports/ktlint/ktlint-report.xml`
   - 查看所有代码风格问题

3. **自动格式化代码**
   ```bash
   ./gradlew ktlintFormat
   ```

4. **验证格式化**
   ```bash
   ./gradlew ktlintCheck
   ```

**预计时间**: 1 小时

---

## 📝 备注

### 重要提示

1. **EditorConfig 同步**:
   - 使用 `.editorconfig` 文件统一代码风格
   - 确保 IDE（Android Studio）的代码风格与 ktlint 一致
   - 在设置中启用 EditorConfig 支持

2. **CI/CD 集成**:
   - 使用 GitHub Actions 运行 ktlint
   - 在每次 pull request 时自动运行 ktlint
   - 只允许格式化正确的代码合并

3. **持续改进**:
   - 定期运行 ktlint
   - 修复所有发现的代码风格问题
   - 改进代码风格和可维护性

---

**ktlint 代码格式化工具配置和使用指南完成！**

**工具**: ktlint
**配置文件**: `.editorconfig`
**报告路径**: `build/reports/ktlint/ktlint-report.xml`

---

**准备好使用 ktlint 进行系统 review 了吗？预计时间：1 小时** 🚀
