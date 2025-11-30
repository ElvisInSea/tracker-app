package com.tracker.app.ui.theme

import androidx.compose.ui.graphics.Color

// 奶油色系配色方案
val CreamyBackground = Color(0xFFFDFBF7) // 暖米白背景
val CreamyCard = Color(0xFFFFFFFF)
val CreamyText = Color(0xFF44403C) // stone-700
val CreamySubText = Color(0xFFA8A29E) // stone-400
val CreamyPrimary = Color(0xFFFFB7B2) // 柔和粉
val CreamySecondary = Color(0xFFE2F0CB) // 抹茶绿
val CreamyAccent = Color(0xFFFFDAC1) // 杏色
val CreamyHighlight = Color(0xFFC7CEEA) // 雾霾蓝

// 预设事务颜色
val TaskColors = listOf(
    TaskColor(
        bg = Color(0xFFFFEDD5), // orange-100
        text = Color(0xFFEA580C), // orange-600
        stroke = Color(0xFFEA580C)
    ),
    TaskColor(
        bg = Color(0xFFDBEAFE), // blue-100
        text = Color(0xFF2563EB), // blue-600
        stroke = Color(0xFF2563EB)
    ),
    TaskColor(
        bg = Color(0xFFDCFCE7), // green-100
        text = Color(0xFF16A34A), // green-600
        stroke = Color(0xFF16A34A)
    ),
    TaskColor(
        bg = Color(0xFFFFE4E6), // rose-100
        text = Color(0xFFE11D48), // rose-600
        stroke = Color(0xFFE11D48)
    ),
    TaskColor(
        bg = Color(0xFFF3E8FF), // purple-100
        text = Color(0xFF9333EA), // purple-600
        stroke = Color(0xFF9333EA)
    )
)

data class TaskColor(
    val bg: Color,
    val text: Color,
    val stroke: Color
)

