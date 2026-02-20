#!/bin/bash

# 批量修复 P0 严重问题脚本
# 使用方法：在项目根目录下运行 ./fix-all-p0-issues.sh

echo "========================================"
echo "P0 严重问题批量修复脚本"
echo "========================================"

# 遍历所有 Kotlin 文件
find android/app/src/main/java/com/homepantry -name "*.kt" -type f | while read file; do
    echo "修复文件: $file"

    # 1. 添加 Logger 导入（如果没有）
    if ! grep -q "import com.homepantry.utils.Logger" "$file"; then
        echo "  - 添加 Logger 导入"
        sed -i '/^package com.homepantry/a import com.homepantry.utils.Logger' "$file"
    fi

    # 2. 添加 PerformanceMonitor 导入（如果没有）
    if ! grep -q "import com.homepantry.utils.PerformanceMonitor" "$file"; then
        echo "  - 添加 PerformanceMonitor 导入"
        sed -i '/^import com.homepantry.utils.Logger$/a import com.homepantry.utils.PerformanceMonitor' "$file"
    fi

    # 3. 添加 TAG 常量（对于 Repository 和 ViewModel 类）
    if [[ "$file" == *"Repository.kt"* ]] || [[ "$file" == *"ViewModel.kt"* ]]; then
        if ! grep -q "companion object {" "$file"; then
            echo "  - 添加 TAG 常量"
            class_name=$(basename "$file" .kt)
            sed -i "/^class $class_name/a\    companion object {\n        private const val TAG = \"$class_name\"\n    }" "$file"
        fi
    fi

    # 4. 修复 Logger 使用
    # 将 Log.d 替换为 Logger.d
    sed -i 's/Log\.d(/Logger.d(/g' "$file"
    # 将 Log.e 替换为 Logger.e
    sed -i 's/Log\.e(/Logger.e(/g' "$file"
    # 将 Log.i 替换为 Logger.i
    sed -i 's/Log\.i(/Logger.i(/g' "$file"
    # 将 Log.w 替换为 Logger.w
    sed -i 's/Log\.w(/Logger.w(/g' "$file"

    echo "  - 修复完成"
done

echo "========================================"
echo "P0 严重问题批量修复完成！"
echo "========================================"
echo ""
echo "下一步："
echo "1. 在 Android Studio 中打开项目"
echo "2. 同步 Gradle"
echo "3. 验证编译成功"
echo "4. 运行应用"
echo "5. 验证所有功能正常"
echo ""
echo "详细指南：android/app/android-studio-fix-guide.md"
