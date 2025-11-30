package com.tracker.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey
    val id: String,
    val name: String,
    val unit: String,
    val step: Int, // 单次打卡量
    val target: Int, // 每日目标
    val description: String = "",
    val colorIndex: Int = 0, // 颜色索引
    val createdAt: Long = System.currentTimeMillis()
)

