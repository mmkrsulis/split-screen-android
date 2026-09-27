// SPDX-License-Identifier: AGPL-3.0-only
package io.github.mmkrsulis.splitscreenandroid.shortcuts

import android.app.Activity
import android.os.Bundle
import io.github.mmkrsulis.splitscreenandroid.SplitScreenApplication
import io.github.mmkrsulis.splitscreenandroid.launcher.NativeSplitScreenLauncher
import io.github.mmkrsulis.splitscreenandroid.launcher.shouldRecordAttempt
import java.util.UUID
import kotlinx.coroutines.launch

internal fun validateShortcutRequest(
    action: String?,
    scheme: String?,
    host: String?,
    pathSegments: List<String>,
    extraId: String?,
): String? {
    if (action != ShortcutSupport.ACTION || scheme != ShortcutSupport.SCHEME || host != ShortcutSupport.HOST) return null
    if (pathSegments.size != 1 || pathSegments.single() != extraId) return null
    val id = extraId ?: return null
    return try {
        if (UUID.fromString(id).toString() == id) id else null
    } catch (_: IllegalArgumentException) {
        null
    }
}

class ShortcutTrampolineActivity : Activity() {
    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        val data = intent.data
        val id = validateShortcutRequest(
            intent.action,
            data?.scheme,
            data?.host,
            data?.pathSegments.orEmpty(),
            intent.getStringExtra(ShortcutSupport.EXTRA_ID),
        )
        if (id == null) {
            finish()
            return
        }
        val app = application as SplitScreenApplication
        app.appScope.launch {
            val pair = runCatching { app.repository.find(id) }.getOrNull()
            if (pair != null) {
                val result = NativeSplitScreenLauncher(this@ShortcutTrampolineActivity).launch(pair)
                if (shouldRecordAttempt(result)) app.repository.markLaunched(id)
            }
            runOnUiThread { finish() }
        }
    }
}
