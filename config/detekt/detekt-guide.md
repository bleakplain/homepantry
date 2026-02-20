# Detekt 代码审查配置指南

**日期**: 2026-02-20
**时间**: 19:15 GMT+8
**任务**: Detekt 代码审查工具配置
**工具**: Detekt（Kotlin 静态代码分析工具）

---

## 📋 Detekt 简介

### 什么是 Detekt？

**Detekt** 是一个针对 Kotlin 编程语言的静态代码分析工具，用于改进代码质量、检查代码风格和发现潜在的 bug。

**主要功能**:
- 静态代码分析
- 代码风格检查
- 潜在的 bug 发现
- 代码复杂度检查
- 代码重复检查
- 自定义规则

**优点**:
- 专为 Kotlin 设计
- 支持自定义规则
- 支持 Kotlin DSL 配置
- 支持增量分析
- 支持多模块项目
- 支持 CI/CD 集成

---

## 🚀 安装 Detekt

### 方法 1：使用 Gradle 插件（推荐）

在项目根目录的 `build.gradle.kts` 中添加：

```kotlin
plugins {
    id("io.gitlab.arturbosch.detekt") version "1.23.3"
}

detekt {
    toolVersion = "1.23.3"
    config = files("$rootDir/config/detekt/detekt.yml")
    parallel = true
    buildUponDefaultConfig = prebuiltConfigurations + configurations
    allRules = false
}
```

### 方法 2：使用 Homebrew（macOS）

```bash
brew install detekt
```

### 方法 3：使用 SDKMAN

```bash
sdk install detekt
```

---

## 📋 Detekt 配置文件

### 创建 Detekt 配置目录

```bash
mkdir -p config/detekt
```

### 创建 Detekt 配置文件

在 `config/detekt/detekt.yml` 中创建：

```yaml
# Detekt 配置文件
# 路径：config/detekt/detekt.yml

build:
  maxIssues: 0
  excludeCorrectable: false
  weights:
    complexity: 2
    LongParameterList: 1
    style: 1
    comments: 1

config:
  validation: true
  warningsAsErrors: false
  checkExhaustiveness: false

processors:
  active: true

complexity:
  active: true
  threshold: 15

LongParameterList:
  active: true
  functionThreshold: 6
  constructorThreshold: 7

style:
  active: true
  maxLineLength: 120

comments:
  active: true
  excludeDefault: true
```

---

## 🚀 运行 Detekt

### 方法 1：使用 Gradle 插件（推荐）

```bash
# 运行 Detekt 分析
./gradlew detekt

# 运行 Detekt 分析并生成 HTML 报告
./gradlew detektBuild

# 运行 Detekt 分析并生成 XML 报告
./gradlew detektMain
```

### 方法 2：使用命令行工具

```bash
# 运行 Detekt 分析
detekt android/app/src/main/java/com/homepantry -c config/detekt/detekt.yml

# 运行 Detekt 分析并生成 HTML 报告
detekt android/app/src/main/java/com/homepantry -c config/detekt/detekt.yml -r html

# 运行 Detekt 分析并生成 XML 报告
detekt android/app/src/main/java/com/homepantry -c config/detekt/detekt.yml -r xml
```

---

## 📊 Detekt 报告

### HTML 报告

**生成命令**:
```bash
./gradlew detektBuild
```

**报告路径**:
```
build/reports/detekt/detekt.html
```

**使用方法**:
1. 在浏览器中打开 `build/reports/detekt/detekt.html`
2. 查看所有代码问题
3. 按严重程度和类别筛选
4. 修复所有问题

---

### XML 报告

**生成命令**:
```bash
./gradlew detektMain
```

**报告路径**:
```
build/reports/detekt/detekt.xml
```

**使用方法**:
1. 在 Android Studio 中打开 `build/reports/detekt/detekt.xml`
2. 查看所有代码问题
3. 点击每个问题查看详情
4. 修复所有问题

---

## 📋 Detekt 规则配置

### 复杂度规则

```yaml
complexity:
  active: true
  threshold: 15
  ignoreSimpleWhenEntries: true
  ignoreNestingFunctions: true
```

### 风格规则

```yaml
style:
  active: true
  autoCorrect: true
  MaxLineLength:
    active: true
    maxLineLength: 120
  WildcardImport:
    active: true
  UnusedImports:
    active: true
```

### 注释规则

```yaml
comments:
  active: true
  excludeDefault: true
  UndocumentedPublicClass:
    active: true
  UndocumentedPublicFunction:
    active: true
```

---

## 🚀 CI/CD 集成

### GitHub Actions

在 `.github/workflows/detekt.yml` 中创建：

```yaml
name: Detekt

on:
  pull_request:
    branches: [ master ]

jobs:
  detekt:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Set up JDK 11
        uses: actions/setup-java@v2
        with:
          java-version: '11'
          distribution: 'adopt'

      - name: Run Detekt
        run: ./gradlew detekt

      - name: Upload Detekt Report
        uses: actions/upload-artifact@v2
        with:
          name: Detekt Report
          path: build/reports/detekt/detekt.html
```

---

## 📋 代码审查清单

### 必须修复的问题（P0）

- [ ] 代码复杂度过高（Complexity > 15）
- [ ] 参数列表过长（LongParameterList > 6）
- [ ] 未使用的导入（UnusedImports）
- [ ] 通配符导入（WildcardImport）
- [ ] 未公开的公共类（UndocumentedPublicClass）
- [ ] 未公开的公共函数（UndocumentedPublicFunction）

### 建议修复的问题（P1）

- [ ] 魔术魔法数（MagicNumber）
- [ ] 不必要的集合创建（UnnecessaryTemporaryInstantiation）
- [ ] 不必要的抽象类（UnnecessaryAbstractClass）
- [ ] 不必要的接口（UnnecessaryInterface）
- [ ] 不必要的泛型（UnnecessaryGeneric）

---

## 📊 总结

### Detekt 代码审查工具

**工具**: Detekt（Kotlin 静态代码分析工具）
**主要功能**: 静态代码分析、代码风格检查、潜在的 bug 发现
**优点**: 专为 Kotlin 设计、支持自定义规则、支持 CI/CD 集成
**配置文件**: `config/detekt/detekt.yml`
**HTML 报告**: `build/reports/detekt/detekt.html`
**XML 报告**: `build/reports/detekt/detekt.xml`

---

## 🚀 开始使用 Detekt

### 立即执行（推荐）

1. **安装 Detekt**
   ```bash
   ./gradlew detekt
   ```

2. **查看 Detekt 报告**
   - 在浏览器中打开 `build/reports/detekt/detekt.html`
   - 查看所有代码问题

3. **修复所有 P0 问题**
   - 修复代码复杂度过高的问题
   - 修复参数列表过长的问题
   - 修复未使用的导入
   - 修复未公开的公共类和函数

4. **验证修复**
   ```bash
   ./gradlew detekt
   ```

5. **提交和推送**
   ```bash
   git add .
   git commit -m "fix: 修复所有 Detekt P0 问题"
   git push origin master
   ```

**预计时间**: 2 小时

---

## 📝 备注

### 重要提示

1. **CI/CD 集成**:
   - 使用 GitHub Actions 运行 Detekt
   - 在每次 pull request 时自动运行 Detekt
   - 只允许修复了所有 P0 问题的代码合并

2. **代码审查流程**:
   - 在每次提交前运行 Detekt
   - 修复所有 P0 问题
   - 在每次 pull request 时自动运行 Detekt
   - 代码审查者检查所有 P0 和 P1 问题

3. **持续改进**:
   - 定期运行 Detekt
   - 修复所有发现的问题
   - 改进代码质量和可维护性

---

**准备好使用 Detekt 进行系统 review 了吗？预计时间：2 小时** 🚀
