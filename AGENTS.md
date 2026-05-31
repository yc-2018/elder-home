# AGENTS.md

本文件给后续代码代理使用。代码文件按 UTF-8 处理；如果本文件或其他文档与实际代码不一致，以实际代码为准，并在合适时更新文档。

## 项目概览

- 这是一个纯原生 Android Java 应用，包名为 `com.local.elderhomehelper`，面向老人桌面快捷入口和 MIUI/HyperOS 小组件使用。
- 根工程只包含 `:app` 模块。当前 Android Gradle Plugin 为 `9.2.0`，`compileSdk`/`targetSdk` 为 `36`，`minSdk` 为 `26`，Java 版本为 17。
- 仓库不提交 Gradle Wrapper；CI 在 `.github/workflows/android.yml` 中使用 JDK 17、Android SDK 36、Gradle `9.4.1` 执行 `gradle :app:assembleDebug` 并上传 debug APK。
- 当前没有 `src/test`、`src/androidTest` 或测试依赖。修改后优先执行 `gradle :app:assembleDebug`；如果本地没有 Gradle，应说明未能本地构建，并参考 GitHub Actions 的同等构建命令。

## 现有架构

- `MainActivity` 负责选择 1x1 到 4x4 的桌面格子尺寸，并通过 `AppWidgetManager.requestPinAppWidget` 添加空入口。
- `ShortcutConfigureActivity` 负责从桌面小组件进入配置页，选择应用入口或 URL/deeplink，设置显示名和图标。
- `ShortcutWidgetProvider` 统一处理小组件更新、点击、启动应用或打开 URL；16 个 `ShortcutWidgetNxM` 类只是不同尺寸 receiver 的轻量子类。
- `ShortcutPrefs` 使用 `SharedPreferences` 保存每个 `appWidgetId` 的入口配置。
- `AppLoader` 枚举可启动应用，并按中文排序；`InstalledAppAdapter` 渲染应用列表；`IconStore` 负责图标裁剪、转换和持久化。
- `WidgetComponentMap` 维护宽高到具体小组件 receiver 类的映射。新增或删除尺寸时，必须同步更新 Java receiver、`AndroidManifest.xml`、`res/xml/app_shortcut_*.xml`、字符串资源和映射表。

## 编码与注释要求

- 所有新增或修改的代码、Markdown、XML、Gradle 文件必须用 UTF-8 编码保存。Windows PowerShell/CMD 读取或写入中文时，应显式使用 UTF-8，避免 GBK 导致乱码。
- 生成代码时，每个新增类和新增方法都必须写简短注释，说明职责或关键行为。修改已有类/方法且行为发生变化时，也要补齐必要注释。
- 新增的重要字段、常量或局部变量，应在声明行尾添加简短尾注释，说明变量用途；避免给明显的一次性临时变量写无意义注释。
- 编写完成后必须检查中文显示是否正常，确认中文没有整段变成问号，也没有被写成反斜杠加 `u` 和十六进制数字组成的 Unicode 转义。
- 不要把中文 UI 文案硬改成英文；现有界面面向中文用户，文案应继续简洁、清楚、适合老人使用。

## 开发风格

- 优先遵循项目现有风格、架构和验证方式。保持原生 Android Java、XML layout、`Activity`、`AppWidgetProvider`、`SharedPreferences` 的实现路线，除非用户明确要求引入新框架。
- 保持改动小而准，只触碰完成任务必需的文件。不要顺手重构相邻代码、重排无关格式或删除无关旧代码。
- 不要为单次需求添加过度抽象、额外依赖、复杂配置或未经请求的功能。
- UI 继续使用现有大字号、高对比、简单按钮和卡片风格。新增颜色、尺寸、字符串应放入对应 `res/values` 文件，避免在 Java 中散落重复资源。
- URL/deeplink 入口要继续走 `Intent.ACTION_VIEW`，并保留打不开 URL 时的用户提示。
- 图标相关逻辑要注意 bitmap 内存、文件持久化和失败兜底；保存失败时不能阻塞入口配置。
- 提供 URL 链接前，必须先验证链接有效性。本文追加翻译所用链接 `https://raw.githubusercontent.com/forrestchang/andrej-karpathy-skills/main/CLAUDE.md` 已于 2026-05-31 验证为 HTTP 200，内容类型为 `text/plain; charset=utf-8`。

## 常用验证

- 列文件：`rg --files`。
- 搜索文本：`rg -n "关键词" .`。
- 本地构建：`gradle :app:assembleDebug`。
- UTF-8 读取示例：`Get-Content -Encoding UTF8 -LiteralPath <path>`。
- 中文完整性检查示例：先运行 `rg -n "\?{2,}" .` 查找连续问号；再用正则查找反斜杠加 `u` 和四位十六进制数字组成的转义串，并人工确认命中是否为合法内容。

---

## 追加翻译：CLAUDE.md

来源：`https://raw.githubusercontent.com/forrestchang/andrej-karpathy-skills/main/CLAUDE.md`。

用于减少常见 LLM 编码错误的行为准则。可按需与项目特定说明合并。

**取舍：** 这些准则更偏向谨慎，而不是速度。对于非常简单的任务，应结合判断。

### 1. 编码前先思考

**不要假设。不要隐藏困惑。把取舍说出来。**

实现前：

- 明确说出你的假设。如果不确定，就提问。
- 如果存在多种理解，把它们列出来，不要默默选择其中一种。
- 如果有更简单的做法，要说出来。该反对时要反对。
- 如果事情不清楚，就停下来。说清楚哪里困惑，并提问。

### 2. 简洁优先

**用能解决问题的最少代码。不要写猜测性的东西。**

- 不要添加用户没有要求的功能。
- 不要为只使用一次的代码创建抽象。
- 不要添加未被要求的“灵活性”或“可配置性”。
- 不要为不可能发生的场景写错误处理。
- 如果你写了 200 行，而 50 行就能完成，就重写。

问问自己：“资深工程师会不会觉得这过度复杂？”如果答案是会，就简化。

### 3. 外科手术式修改

**只修改必须修改的地方。只清理你自己造成的问题。**

编辑现有代码时：

- 不要“改进”相邻代码、注释或格式。
- 不要重构没有坏掉的东西。
- 匹配现有风格，即使你会用不同方式实现。
- 如果发现无关的死代码，提出来，不要删除。

当你的改动制造了孤立代码时：

- 移除由你的改动导致未使用的 import、变量或函数。
- 除非用户要求，否则不要删除原本就存在的死代码。

检验标准：每一行改动都应该能直接追溯到用户的请求。

### 4. 目标驱动执行

**定义成功标准。循环直到验证通过。**

把任务转换为可验证的目标：

- “添加校验” -> “为非法输入写测试，然后让测试通过”
- “修复 bug” -> “写一个能复现 bug 的测试，然后让测试通过”
- “重构 X” -> “确保重构前后测试都通过”

对于多步骤任务，写一个简短计划：

```text
1. [步骤] -> 验证：[检查]
2. [步骤] -> 验证：[检查]
3. [步骤] -> 验证：[检查]
```

清晰的成功标准能让你独立循环推进。模糊标准（例如“让它能用”）需要不断澄清。

---

**如果这些准则有效，应该表现为：** diff 中不必要的改动更少，因过度复杂导致的重写更少，澄清问题发生在实现之前，而不是错误之后。
