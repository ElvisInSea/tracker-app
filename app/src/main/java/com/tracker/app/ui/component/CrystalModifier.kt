package com.tracker.app.ui.component

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 为"今天"的格子添加"极简水晶"高亮样式
 * 
 * 视觉上由内向外分为三层：
 * 1. 主体：原本的颜色块（圆形）
 * 2. 间隔线：主体外有一圈 2dp 宽的纯白色描边
 * 3. 外光环：白色描边外，还有一圈 4dp 宽的半透明淡蓝色光环
 * 
 * @param baseColor 主体颜色（例如蓝色）
 * @param whiteBorderWidth 白色间隔线宽度，默认 2dp
 * @param haloWidth 外光环宽度，默认 4dp
 * @param haloColor 外光环颜色，默认半透明淡蓝色
 */
fun Modifier.crystalHighlight(
    baseColor: Color,
    whiteBorderWidth: Dp = 2.dp,
    haloWidth: Dp = 4.dp,
    haloColor: Color = Color(0x333B82F6) // Blue 500 with 20% alpha
): Modifier = this.drawBehind {
    val size = size.minDimension
    val centerX = size / 2f
    val centerY = size / 2f
    
    // 主体半径（14.dp，与其他日期保持一致）
    val baseSize = 14.dp.toPx()
    val baseRadius = baseSize / 2f
    
    // 白色描边的半径（在主体外）
    val whiteBorderRadius = baseRadius
    
    // 外光环的半径（在白色描边外）
    // 确保外光环在 Box 边界内（留出一些边距）
    val maxHaloRadius = (size / 2f) - 1.dp.toPx() // 留出 1dp 边距
    val desiredHaloRadius = baseRadius + whiteBorderWidth.toPx() + haloWidth.toPx() / 2f
    val haloRadius = desiredHaloRadius.coerceAtMost(maxHaloRadius)
    
    // 1. 绘制外光环（最外层）
    drawCircle(
        color = haloColor,
        radius = haloRadius,
        center = androidx.compose.ui.geometry.Offset(centerX, centerY),
        style = Stroke(width = haloWidth.toPx())
    )
    
    // 2. 绘制白色间隔线（中间层）
    drawCircle(
        color = Color.White,
        radius = whiteBorderRadius,
        center = androidx.compose.ui.geometry.Offset(centerX, centerY),
        style = Stroke(width = whiteBorderWidth.toPx())
    )
    
    // 3. 主体颜色块（最内层，由 background modifier 处理，这里不需要绘制）
    // 注意：主体颜色块应该通过 background modifier 在外部设置
}

