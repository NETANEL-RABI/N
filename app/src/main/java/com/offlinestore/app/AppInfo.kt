package com.offlinestore.app

import android.graphics.drawable.Drawable

data class AppInfo(
    val assetFileName: String,
    val label: String,
    val packageName: String,
    val versionName: String?,
    val icon: Drawable?
)
