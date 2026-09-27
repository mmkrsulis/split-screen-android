// SPDX-License-Identifier: AGPL-3.0-only
package io.github.mmkrsulis.splitscreenandroid.model

import java.util.UUID
import kotlinx.serialization.Serializable

@Serializable
data class AppTarget(val packageName: String, val componentName: String, val label: String)

@Serializable
data class AppPair(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val appA: AppTarget,
    val appB: AppTarget,
    val position: Float = 0.5f,
    val favorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = createdAt,
    val lastUsedAt: Long? = null,
)
