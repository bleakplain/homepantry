#!/bin/bash

# 自动化代码审查脚本
# 使用方法：在项目根目录下运行 ./code-review.sh

echo "========================================"
echo "自动化代码审查脚本"
echo "========================================"

# 1. 运行 Detekt 静态代码分析
echo ""
echo "Step 1: 运行 Detekt 静态代码分析..."
echo "========================================"

./gradlew detekt

if [ $? -ne 0 ]; then
    echo "Detekt 静态代码分析失败！"
    exit 1
else
    echo "Detekt 静态代码分析成功！"
fi

# 2. 运行 ktlint 代码格式化
echo ""
echo "Step 2: 运行 ktlint 代码格式化..."
echo "========================================"

./gradlew ktlintCheck

if [ $? -ne 0 ]; then
    echo "ktlint 代码格式化检查失败！"
    exit 1
else
    echo "ktlint 代码格式化检查成功！"
fi

# 3. 运行 Android Lint
echo ""
echo "Step 3: 运行 Android Lint..."
echo "========================================"

./gradlew lint

if [ $? -ne 0 ]; then
    echo "Android Lint 检查失败！"
    exit 1
else
    echo "Android Lint 检查成功！"
fi

# 4. 运行所有测试
echo ""
echo "Step 4: 运行所有测试..."
echo "========================================"

./gradlew test

if [ $? -ne 0 ]; then
    echo "所有测试运行失败！"
    exit 1
else
    echo "所有测试运行成功！"
fi

# 5. 生成代码审查报告
echo ""
echo "Step 5: 生成代码审查报告..."
echo "========================================"

./gradlew detektBuild

if [ $? -ne 0 ]; then
    echo "Detekt 报告生成失败！"
    exit 1
else
    echo "Detekt 报告生成成功！"
fi

./gradlew ktlintCheckstyleReport

if [ $? -ne 0 ]; then
    echo "ktlint 报告生成失败！"
    exit 1
else
    echo "ktlint 报告生成成功！"
fi

echo "========================================"
echo "自动化代码审查完成！"
echo "========================================"
echo ""
echo "下一步："
echo "1. 在 Android Studio 中打开项目"
echo "2. 查看代码审查报告"
echo "3. 修复所有发现的问题"
echo "4. 运行自动化代码审查脚本：./code-review.sh"
echo "5. 验证所有测试通过"
echo ""
echo "代码审查报告："
echo "- Detekt 报告：build/reports/detekt/detekt.html"
echo "- ktlint 报告：build/reports/ktlint/ktlint-report.xml"
echo "- Android Lint 报告：build/reports/lint/lint-results.html"
echo ""
echo "详细指南："
echo "- Detekt 配置指南：config/detekt/detekt-guide.md"
echo "- ktlint 配置指南：config/ktlint/ktlint-guide.md"
echo "- Android Lint 配置指南：config/android-lint/android-lint-guide.md"
