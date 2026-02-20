# Plan: 烹饪模式

**Spec ID**: 005
**功能名称**: 烹饪模式
**优先级**: P0
**状态**: ✅ 已实现
**创建日期**: 2026-02-15
**最后更新**: 2026-02-15

---

## 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Kotlin | 1.9+ | 开发语言 |
| Jetpack Compose | 1.5+ | UI 框架 |
| Room | 2.6+ | 本地数据库 |
| TextToSpeech | Android | 语音播报 |
| Coroutines | 1.7+ | 异步处理 |
| Flow | Kotlin | 数据流 |

---

## 核心功能

### 1. 横屏大字模式
```kotlin
@Composable
fun EnhancedCookingModeScreen(
    instructions: List<RecipeInstruction>,
    currentIndex: Int,
    onNext: () -> Unit,
    onPrevious: () -> Unit
) {
    // 横屏布局，超大字体
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = instructions[currentIndex].instruction,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold
            )

            Row(horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = onPrevious) {
                    Icon(Icons.Default.ArrowBack, null)
                }
                Button(onClick = onNext) {
                    Icon(Icons.Default.ArrowForward, null)
                }
            }
        }
    }
}
```

---

## 参考资料

- [spec.md](./spec.md)
- [Constitution](../memory/constitution.md)

---

**文档版本**: 1.0
**创建日期**: 2026-02-15
**负责人**: Jude 🦞
