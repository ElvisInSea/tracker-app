package com.tracker.app.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.delay

// Creamy UI 颜色定义 - 低饱和、奶油感
private val CreamyBackground = Color(0xFFFFFBF0) // Warm Cream
private val CreamyCard = Color.White.copy(alpha = 0.8f) // 毛玻璃效果
private val CreamyText = Color(0xFF57534E) // Stone 600
private val CreamySubText = Color(0xFFA8A29E) // Stone 400
private val CreamyLightText = Color(0xFFD6D3D1) // Stone 300
private val CreamyAccent = Color(0xFF818CF8) // Soft Indigo
private val CreamyHighlight = Color(0xFFF5F5F4) // Stone 100

/**
 * Creamy UI 风格的时间选择器
 * 修复版：解决了 0 吸附错误和初始值偏差问题
 */
@Composable
fun CreamyTimePicker(
    initialHour: Int = 0,
    initialMinute: Int = 0,
    onTimeSelected: (Int, Int) -> Unit
) {
    var selectedHour by remember { mutableStateOf(initialHour.coerceIn(0, 23)) }
    var selectedMinute by remember { mutableStateOf(initialMinute.coerceIn(0, 59)) }
    
    // 当选择改变时，通知外部
    LaunchedEffect(selectedHour, selectedMinute) {
        onTimeSelected(selectedHour, selectedMinute)
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
            // 小时滚轮
            ScrollColumn(
                label = "HOUR",
                items = (0..23).toList(),
                selected = selectedHour,
                onSelect = { selectedHour = it },
                modifier = Modifier.weight(1f)
            )
            
            // 分隔符
            Text(
                text = ":",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE7E5E4),
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            
            // 分钟滚轮
            ScrollColumn(
                label = "MINUTE",
                items = (0..59).toList(),
                selected = selectedMinute,
                onSelect = { selectedMinute = it },
                modifier = Modifier.weight(1f)
            )
        }
}

/**
 * 3D 滚轮组件
 */
@Composable
private fun ScrollColumn(
    label: String,
    items: List<Int>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val itemHeight = 48.dp
    val visibleItems = 3 // 可见3项（上下各1项 + 中间选中项）
    val density = LocalDensity.current
    val itemHeightPx = with(density) { itemHeight.toPx() }
    
    // 修复 1: 初始化时直接使用 selected 索引。
    // LazyColumn 的 contentPadding 会自动处理第一项的位置，使其正好处于中间（Lens）位置。
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = selected
    )
    
    var isDragging by remember { mutableStateOf(false) }
    var dragOffset by remember { mutableStateOf(0f) }
    var clickedIndex by remember { mutableStateOf<Int?>(null) }
    
    // 处理滚动吸附逻辑
    LaunchedEffect(listState.isScrollInProgress, isDragging) {
        if (!listState.isScrollInProgress && !isDragging) {
            // 滚动结束，吸附到最近的项
            val firstVisibleIndex = listState.firstVisibleItemIndex
            val scrollOffset = listState.firstVisibleItemScrollOffset
            
            // 计算中心项索引
            // 逻辑：当前视觉中心位置 = (TopIndex * H + TopOffset) + LensOffset(H)
            val centerOffset = itemHeightPx
            val centerPosition = firstVisibleIndex * itemHeightPx + scrollOffset + centerOffset
            val centerItemIndex = ((centerPosition - centerOffset) / itemHeightPx).roundToInt().coerceIn(0, items.size - 1)
            
            // 修复 2: 目标索引直接为 centerItemIndex，不需要 -1。
            // 目标偏移量为 0。scrollToItem(index, 0) 会把该项置于 contentPadding 之下，也就是正中间。
            val targetFirstIndex = centerItemIndex
            val targetScrollOffset = 0
            
            val needsSnap = listState.firstVisibleItemIndex != targetFirstIndex || 
                           abs(scrollOffset - targetScrollOffset) > itemHeightPx * 0.1f
            
            if (needsSnap) {
                listState.animateScrollToItem(targetFirstIndex, scrollOffset = targetScrollOffset)
                delay(200) // 等待动画完成
            }
            
            // 更新选中项
            if (items[centerItemIndex] != selected) {
                onSelect(items[centerItemIndex])
            }
        }
    }
    
    // 处理点击项时的滚动
    LaunchedEffect(clickedIndex) {
        clickedIndex?.let { index ->
            // 修复 3: 点击时直接滚动到 index，offset 为 0
            listState.animateScrollToItem(index, scrollOffset = 0)
            clickedIndex = null
        }
    }
    
    // 处理拖拽结束后的吸附
    LaunchedEffect(isDragging) {
        if (!isDragging && dragOffset != 0f) {
            val firstVisibleIndex = listState.firstVisibleItemIndex
            val scrollOffset = listState.firstVisibleItemScrollOffset
            val centerOffset = itemHeightPx
            val centerPosition = firstVisibleIndex * itemHeightPx + scrollOffset + centerOffset
            val centerItemIndex = ((centerPosition - centerOffset) / itemHeightPx).roundToInt().coerceIn(0, items.size - 1)
            
            // 修复 4: 拖拽结束直接吸附到 centerItemIndex
            listState.animateScrollToItem(centerItemIndex, scrollOffset = 0)
            delay(50) 
            dragOffset = 0f
        }
    }
    
    // 同步滚动位置到选中索引（仅在外部改变 selected 时触发）
    LaunchedEffect(selected) {
        if (!isDragging && !listState.isScrollInProgress) {
            val targetIndex = items.indexOf(selected)
            if (targetIndex != -1) {
                val currentFirstIndex = listState.firstVisibleItemIndex
                // 简单的近似判断是否需要滚动
                if (currentFirstIndex != targetIndex) {
                    // 修复 5: 外部更新时，直接滚动到 targetIndex
                    listState.animateScrollToItem(targetIndex, scrollOffset = 0)
                }
            }
        }
    }
    
    // 实时计算当前中心项（用于视觉反馈）
    val currentCenterIndex = remember(
        listState.firstVisibleItemIndex,
        listState.firstVisibleItemScrollOffset,
        isDragging,
        dragOffset
    ) {
        val firstVisibleIndex = listState.firstVisibleItemIndex
        val scrollOffset = listState.firstVisibleItemScrollOffset
        val centerOffset = itemHeightPx
        val centerPosition = firstVisibleIndex * itemHeightPx + scrollOffset + dragOffset + centerOffset
        ((centerPosition - centerOffset) / itemHeightPx).roundToInt().coerceIn(0, items.size - 1)
    }
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 标签
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = CreamySubText,
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Box(
            modifier = Modifier
                .height(itemHeight * visibleItems)
                .width(96.dp)
                .clip(RoundedCornerShape(24.dp))
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragStart = {
                            isDragging = true
                            dragOffset = 0f
                        },
                        onDragEnd = {
                            isDragging = false
                        }
                    ) { _, dragAmount ->
                        dragOffset -= dragAmount // 注意：拖拽方向与滚动偏移方向相反
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            // 选中项的高亮背景（透镜效果）
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(itemHeight)
                    .background(
                        color = CreamyHighlight,
                        shape = RoundedCornerShape(12.dp)
                    )
            )
            
            // 滚轮列表
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .offset { IntOffset(0, -dragOffset.roundToInt()) }, // 修正拖拽视觉方向
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(vertical = itemHeight), // 上下各留1个item高度
                verticalArrangement = Arrangement.spacedBy(0.dp),
                userScrollEnabled = true
            ) {
                itemsIndexed(items) { index, item ->
                    val isSelected = index == currentCenterIndex
                    val offsetFromCenter = calculateOffsetFromCenter(
                        listState = listState,
                        itemIndex = index,
                        itemHeight = itemHeight,
                        itemHeightPx = itemHeightPx,
                        dragOffset = if (isDragging) dragOffset else 0f
                    )
                    
                    ScrollColumnItem(
                        text = String.format("%02d", item),
                        isSelected = isSelected,
                        itemHeight = itemHeight,
                        offsetFromCenter = offsetFromCenter,
                        onClick = {
                            if (!isDragging) {
                                clickedIndex = index
                            }
                        }
                    )
                }
            }
            
            // 顶部渐变遮罩
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(itemHeight)
                    .align(Alignment.TopCenter)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                CreamyCard,
                                Color.Transparent
                            )
                        )
                    )
            )
            
            // 底部渐变遮罩
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(itemHeight)
                    .align(Alignment.BottomCenter)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                CreamyCard
                            )
                        )
                    )
            )
        }
    }
}

/**
 * 滚轮单个项目
 */
@Composable
private fun ScrollColumnItem(
    text: String,
    isSelected: Boolean,
    itemHeight: androidx.compose.ui.unit.Dp,
    offsetFromCenter: Float,
    onClick: () -> Unit
) {
    // 根据距离中心的距离计算透明度和缩放
    val distance = abs(offsetFromCenter)
    val maxDistance = 1f
    
    val alpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else (1f - (distance / maxDistance).coerceIn(0f, 1f) * 0.6f).coerceIn(0.4f, 1f),
        animationSpec = tween(100),
        label = "alpha"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else (1f - (distance / maxDistance).coerceIn(0f, 1f) * 0.1f).coerceIn(0.9f, 1f),
        animationSpec = tween(100),
        label = "scale"
    )
    
    val fontSize = if (isSelected) 24.sp else 18.sp
    val fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
    val textColor = if (isSelected) CreamyText else CreamyLightText
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(itemHeight)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = fontSize,
            fontWeight = fontWeight,
            color = textColor.copy(alpha = alpha),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .scale(scale)
                .then(
                    if (!isSelected && distance > 0.5f) {
                        Modifier.blur(radius = 0.5.dp)
                    } else {
                        Modifier
                    }
                )
        )
    }
}

/**
 * 计算项目距离中心的偏移量（以item高度为单位）
 */
private fun calculateOffsetFromCenter(
    listState: androidx.compose.foundation.lazy.LazyListState,
    itemIndex: Int,
    itemHeight: androidx.compose.ui.unit.Dp,
    itemHeightPx: Float,
    dragOffset: Float
): Float {
    val firstVisibleIndex = listState.firstVisibleItemIndex
    val scrollOffset = listState.firstVisibleItemScrollOffset
    
    // 简化计算：
    // 因为有 dragOffset (且应用于 LazyColumn 的 offset)，相当于临时改变了 scrollOffset
    // dragOffset 为负时（向上拖），相当于 scroll 增加。
    // 所以 effectiveScroll = scrollOffset + dragOffset
    
    val effectiveScrollIndex = firstVisibleIndex + (scrollOffset + dragOffset) / itemHeightPx
    
    return (itemIndex - effectiveScrollIndex)
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFBF0)
@Composable
private fun CreamyTimePickerPreview() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = CreamyBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CreamyTimePicker(
                initialHour = 14,
                initialMinute = 30,
                onTimeSelected = { hour, minute ->
                    // Preview callback
                }
            )
        }
    }
}
