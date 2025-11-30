package com.tracker.app.util

import java.util.UUID

object IdGenerator {
    fun generateId(): String {
        return UUID.randomUUID().toString()
    }
}

