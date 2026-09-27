// SPDX-License-Identifier: AGPL-3.0-only
package io.github.mmkrsulis.splitscreenandroid.launcher

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.annotation.RequiresApi
import io.github.mmkrsulis.splitscreenandroid.model.AppTarget
import java.text.Collator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

fun normalizeDiscoveredApps(targets: List<AppTarget>, ownPackage: String): List<AppTarget> {
    val collator = Collator.getInstance()
    return targets
        .filterNot { it.packageName == ownPackage }
        .groupBy { it.packageName }
        .mapNotNull { (_, activities) -> activities.minWithOrNull(compareBy(collator) { it.label }) }
        .sortedWith(compareBy(collator) { it.label })
}

class AppDiscovery(private val context: Context) {
    suspend fun discover(): List<AppTarget> = withContext(Dispatchers.IO) {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val found = if (android.os.Build.VERSION.SDK_INT >= 33) queryModern(intent) else queryLegacy(intent)
        normalizeDiscoveredApps(
            found.mapNotNull { result ->
                val activity = result.activityInfo ?: return@mapNotNull null
                AppTarget(
                    packageName = activity.packageName,
                    componentName = ComponentName(activity.packageName, activity.name).flattenToString(),
                    label = result.loadLabel(context.packageManager).toString(),
                )
            },
            context.packageName,
        )
    }

    @RequiresApi(33)
    private fun queryModern(intent: Intent) = context.packageManager.queryIntentActivities(
        intent,
        PackageManager.ResolveInfoFlags.of(0),
    )

    @Suppress("DEPRECATION")
    private fun queryLegacy(intent: Intent) = context.packageManager.queryIntentActivities(intent, 0)
}
