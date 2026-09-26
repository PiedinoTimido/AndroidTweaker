package com.android.tweaker.model

import java.io.File

data class SavedLogItem(
    val id: String,
    val fileName: String,
    val timestamp: Long,
    val logContent: String,
    val isSystemLog: Boolean,
    val targetPackage: String = "",
    var isStarred: Boolean = false
)
