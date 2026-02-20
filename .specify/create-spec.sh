#!/bin/bash
# 快速创建 SDD spec 模板
# 用法: ./create-spec.sh <spec-id> <feature-name>

SPEC_ID=$1
FEATURE_NAME=$2

if [ -z "$SPEC_ID" ] || [ -z "$FEATURE_NAME" ]; then
    echo "用法: ./create-spec.sh <spec-id> <feature-name>"
    echo "示例: ./create-spec.sh 002 ingredient-management"
    exit 1
fi

SOURCE_DIR="specs/001-recipe-management"
TARGET_DIR="specs/${SPEC_ID}-${FEATURE_NAME}"

# 创建目录
mkdir -p "${TARGET_DIR}/contracts"

# 复制模板文件
cp "${SOURCE_DIR}/spec.md" "${TARGET_DIR}/spec.md"
cp "${SOURCE_DIR}/plan.md" "${TARGET_DIR}/plan.md"
cp "${SOURCE_DIR}/data-model.md" "${TARGET_DIR}/data-model.md"
cp "${SOURCE_DIR}/tasks.md" "${TARGET_DIR}/tasks.md"
cp "${SOURCE_DIR}/research.md" "${TARGET_DIR}/research.md"
cp "${SOURCE_DIR}/README.md" "${TARGET_DIR}/README.md"

echo "✅ 已创建: ${TARGET_DIR}/"
echo "   - spec.md"
echo "   - plan.md"
echo "   - data-model.md"
echo "   - tasks.md"
echo "   - research.md"
echo "   - README.md"
echo ""
echo "下一步: 编辑这些文件，填充 ${FEATURE_NAME} 的具体内容"
