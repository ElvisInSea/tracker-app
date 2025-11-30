# Daily Tracker - Native Android App

A native Android application for tracking daily tasks, time, frequency, and trends in your life. Built with Kotlin + Jetpack Compose, featuring a creamy, soft UI design.

> English | [中文](README_CN.md)

## Features

### 1. Home Screen
- Display heatmap for all tasks (last 6 weeks)
- Tap on a day in the heatmap to expand that day's records
- Task name, unit, and description shown at the top left of the heatmap
- Check-in button at the top right with checked/unchecked states and count display
- Floating action button to create new tasks

### 2. Create Task
- Enter task name (required)
- Unit (required)
- Single check-in amount (required)
- Target value (required, determines color intensity in heatmap)

### 3. Task Detail Screen
- Display task statistics (total count, active days, today's count, etc.)
- Trend chart (line chart showing last 2 weeks) below the statistics
- Detailed logs grouped by day below the trend chart
- Daily records shown in a collapsible list (collapsed by default)
- Swipe left on a record to delete (swipe reveals delete button)
- Edit task's single check-in amount and description
- Delete task with confirmation dialog

### 4. Timeline Screen
- Calendar picker at the top (showing 3 days before and after)
- Today's task records displayed below

### 5. Settings Screen
- Import/Export data
- Data format: JSON

## Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Database**: Room Database
- **Architecture**: MVVM (ViewModel + StateFlow)
- **Navigation**: Navigation Compose
- **Charts**: Vico Charts

## Project Structure

```
app/src/main/java/com/tracker/app/
├── data/              # Data layer
│   ├── Habit.kt      # Task data model
│   ├── Log.kt        # Log data model
│   ├── HabitDao.kt   # Task data access object
│   ├── LogDao.kt     # Log data access object
│   └── AppDatabase.kt # Room database
├── viewmodel/         # View models
│   └── TrackerViewModel.kt
├── ui/
│   ├── theme/        # Theme configuration
│   │   ├── Color.kt  # Color definitions
│   │   ├── Theme.kt  # Theme setup
│   │   └── Type.kt   # Typography
│   ├── screen/       # Screen components
│   │   ├── HomeScreen.kt
│   │   ├── HabitDetailScreen.kt
│   │   ├── TimelineScreen.kt
│   │   ├── SettingsScreen.kt
│   │   └── CreateHabitDialog.kt
│   └── component/    # UI components
│       ├── HabitCard.kt
│       ├── Heatmap.kt
│       └── SwipeableLogItem.kt
├── util/             # Utility classes
│   ├── DateUtils.kt
│   └── IdGenerator.kt
├── MainActivity.kt   # Main Activity
└── TrackerApp.kt     # App entry and navigation
```

## Requirements

- Android Studio Hedgehog | 2023.1.1 or higher
- JDK 17
- Android SDK 24 or higher
- Gradle 8.2 or higher

## Build Instructions

1. Clone the repository
   ```bash
   git clone <repository-url>
   cd tracker-app
   ```

2. Open the project in Android Studio
   - Launch Android Studio
   - Select "Open an Existing Project"
   - Choose the project directory

3. Wait for Gradle sync to complete
   - Android Studio will automatically download dependencies
   - If you encounter issues, run `./gradlew build`

4. Run the app
   - Connect an Android device or start an emulator
   - Click the Run button or use `Shift + F10` (Windows/Linux) or `Ctrl + R` (Mac)

## Development

### Build Debug Version
```bash
./gradlew assembleDebug
```

### Build Release Version
```bash
./gradlew assembleRelease
```

### Run Tests
```bash
./gradlew test
```

### Code Linting
```bash
./gradlew lint
```

## UI Design Philosophy

- **Soft**: Large rounded corners, subtle shadows, airy padding
- **Clean**: Minimal text, lightweight icons
- **Friendly**: Warm cards, playful icon-like elements
- **Colors**: Low saturation + creamy white

## Data Format

Example of exported JSON format:

```json
{
  "tasks": [
    {
      "id": "...",
      "name": "Drink Water",
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

## Contributing

Issues and Pull Requests are welcome!

## Release

Please check [RELEASE_CHECKLIST.md](RELEASE_CHECKLIST.md) for detailed release checklist before publishing.

### Version Information
- Current Version: 1.0
- Minimum Android Version: Android 7.0 (API 24)
- Target Android Version: Android 14 (API 34)

## License

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
