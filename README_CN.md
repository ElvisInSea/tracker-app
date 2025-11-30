# Daily Tracker - Android 原生应用

一个用于记录生活中日常事务的时间、频率、趋势等的 Android 原生应用。采用 Kotlin + Jetpack Compose 开发，具有奶油感的 UI 设计。

> [English](README.md) | 中文

## 功能特性

### 1. 主页面
- 显示所有事务的热力图（最近 6 周）
- 点击热力图的某一天，展开当天该项的记录时间记录
- 热力图左上方显示事务的名称、单位、描述等
- 热力图右上方是打卡按钮，分为未打卡/打卡状态，打卡状态旁边显示次数
- 悬浮创建按钮用于创建新事务

### 2. 创建事务
- 输入事务名称（必填）
- 单位（必填）
- 单次打卡量（必填）
- 目标值（必填，决定在热力图中的颜色深浅）

### 3. 事务详情页面
- 上方展示事务信息（总次数、活跃天数、今日次数等）
- 事务信息下方是趋势图（折线图，显示近两周趋势）
- 趋势图下方是详细日志，以日为组
- 当日的详细记录以列表展示，列表默认收缩状态（可展开）
- 列表展开时，可向左滑动单次记录删除（滑动出现删除按钮，点击删除按钮删除）
- 详情页可以编辑事务的单次打卡量、描述
- 可以删除这个事务，删除之前有二次确认提示

### 4. 时间线页面
- 上方是日历选择器（显示前后 3 天）
- 下方是当日的事务的记录时间

### 5. 设置页面
- 导入导出数据
- 数据是 JSON 格式

## 技术栈

- **语言**: Kotlin
- **UI 框架**: Jetpack Compose
- **数据库**: Room Database
- **架构**: MVVM (ViewModel + StateFlow)
- **导航**: Navigation Compose
- **图表**: Vico Charts

## 项目结构

```
app/src/main/java/com/tracker/app/
├── data/              # 数据层
│   ├── Habit.kt      # 事务数据模型
│   ├── Log.kt        # 日志数据模型
│   ├── HabitDao.kt   # 事务数据访问对象
│   ├── LogDao.kt     # 日志数据访问对象
│   └── AppDatabase.kt # Room 数据库
├── viewmodel/         # 视图模型
│   └── TrackerViewModel.kt
├── ui/
│   ├── theme/        # 主题配置
│   │   ├── Color.kt  # 颜色定义
│   │   ├── Theme.kt  # 主题配置
│   │   └── Type.kt   # 字体配置
│   ├── screen/       # 屏幕组件
│   │   ├── HomeScreen.kt
│   │   ├── HabitDetailScreen.kt
│   │   ├── TimelineScreen.kt
│   │   ├── SettingsScreen.kt
│   │   └── CreateHabitDialog.kt
│   └── component/    # UI 组件
│       ├── HabitCard.kt
│       ├── Heatmap.kt
│       └── SwipeableLogItem.kt
├── util/             # 工具类
│   ├── DateUtils.kt
│   └── IdGenerator.kt
├── MainActivity.kt   # 主 Activity
└── TrackerApp.kt     # 应用入口和导航

```

## 构建要求

- Android Studio Hedgehog | 2023.1.1 或更高版本
- JDK 17
- Android SDK 24 或更高版本
- Gradle 8.2 或更高版本

## 构建步骤

1. 克隆项目
   ```bash
   git clone <repository-url>
   cd tracker-app
   ```

2. 使用 Android Studio 打开项目
   - 打开 Android Studio
   - 选择 "Open an Existing Project"
   - 选择项目目录

3. 等待 Gradle 同步完成
   - Android Studio 会自动下载依赖
   - 如果遇到问题，可以运行 `./gradlew build`

4. 运行应用
   - 连接 Android 设备或启动模拟器
   - 点击运行按钮或使用快捷键 `Shift + F10` (Windows/Linux) 或 `Ctrl + R` (Mac)

## 开发

### 构建 Debug 版本
```bash
./gradlew assembleDebug
```

### 构建 Release 版本
```bash
./gradlew assembleRelease
```

### 运行测试
```bash
./gradlew test
```

### 代码检查
```bash
./gradlew lint
```

## UI 设计特点

- **Soft**: 大圆角、浅阴影、空气垫
- **Clean**: 极少文字、图标轻量
- **Friendly**: 卡片有温度、图标像小玩具
- **颜色**: 低饱和+奶油白

## 数据格式

导出的 JSON 格式示例：

```json
{
  "tasks": [
    {
      "id": "...",
      "name": "喝水",
      "unit": "ml",
      "step": 200,
      "target": 2000,
      "description": "",
      "colorIndex": 0,
      "createdAt": 1234567890
    }
  ],
  "logs": [
    {
      "id": "...",
      "taskId": "...",
      "amount": 200,
      "timestamp": 1234567890
    }
  ]
}
```

## 贡献

欢迎提交 Issue 和 Pull Request！

## 发布

发布前请查看 [RELEASE_CHECKLIST.md](RELEASE_CHECKLIST.md) 了解详细的发布检查清单。

### 版本信息
- 当前版本: 1.0
- 最低支持 Android 版本: Android 7.0 (API 24)
- 目标 Android 版本: Android 14 (API 34)

## 许可证

MIT License

Copyright (c) 2024 Daily Tracker

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

