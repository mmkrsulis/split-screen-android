// SPDX-License-Identifier: AGPL-3.0-only
package io.github.mmkrsulis.splitscreenandroid.launcher

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import io.github.mmkrsulis.splitscreenandroid.model.AppPair
import io.github.mmkrsulis.splitscreenandroid.model.AppTarget

sealed interface LaunchResult {
    data object Success : LaunchResult
    data class PartialSuccess(val message: String) : LaunchResult
    data class Unsupported(val reason: String) : LaunchResult
    data class PermissionRequired(val reason: String) : LaunchResult
    data class AppUnavailable(val label: String) : LaunchResult
    data class Failed(val message: String) : LaunchResult
}

interface SplitScreenLauncher { suspend fun launch(pair: AppPair): LaunchResult }

internal fun launchSequentially(first: () -> Unit, second: () -> Unit): LaunchResult {
    try {
        first()
    } catch (error: SecurityException) {
        return LaunchResult.PermissionRequired(error.message ?: "First app launch was blocked")
    } catch (error: ActivityNotFoundException) {
        return LaunchResult.Failed(error.message ?: "First app is unavailable")
    } catch (error: RuntimeException) {
        return LaunchResult.Failed(error.message ?: "First app launch failed")
    }
    return try {
        second()
        LaunchResult.PartialSuccess("Both app launches were requested. Android and each app decide whether split screen is entered.")
    } catch (error: RuntimeException) {
        LaunchResult.PartialSuccess("The first app was requested, but the second app could not be launched: ${error.message ?: "launch failed"}")
    }
}

fun shouldRecordAttempt(result: LaunchResult): Boolean =
    result is LaunchResult.Success || result is LaunchResult.PartialSuccess

class NativeSplitScreenLauncher(private val context: Context) : SplitScreenLauncher {
    override suspend fun launch(pair: AppPair): LaunchResult {
        if (pair.appA.packageName == pair.appB.packageName) {
            return LaunchResult.Unsupported("Choose apps from different packages")
        }
        val first = explicit(pair.appA) ?: return LaunchResult.AppUnavailable(pair.appA.label)
        val second = explicit(pair.appB) ?: return LaunchResult.AppUnavailable(pair.appB.label)
        return launchSequentially(
            first = { context.startActivity(first) },
            second = { context.startActivity(second) },
        )
    }

    private fun explicit(target: AppTarget): Intent? {
        val component = ComponentName.unflattenFromString(target.componentName) ?: return null
        if (component.packageName != target.packageName) return null
        val intent = Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_LAUNCHER)
            .setComponent(component)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT)
        return intent.takeIf {
            context.packageManager.resolveActivity(it, PackageManager.MATCH_DEFAULT_ONLY) != null
        }
    }
}
