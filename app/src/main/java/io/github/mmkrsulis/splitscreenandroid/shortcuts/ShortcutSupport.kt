// SPDX-License-Identifier: AGPL-3.0-only
package io.github.mmkrsulis.splitscreenandroid.shortcuts

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import io.github.mmkrsulis.splitscreenandroid.R
import io.github.mmkrsulis.splitscreenandroid.model.AppPair

class ShortcutSupport(private val context: Context) {
    fun supported() = ShortcutManagerCompat.isRequestPinShortcutSupported(context)

    fun request(pair: AppPair): Boolean {
        if (!supported()) return false
        val intent = Intent(context, ShortcutTrampolineActivity::class.java)
            .setAction(ACTION)
            .setData(Uri.Builder().scheme(SCHEME).authority(HOST).appendPath(pair.id).build())
            .putExtra(EXTRA_ID, pair.id)
        val info = ShortcutInfoCompat.Builder(context, pair.id)
            .setShortLabel(pair.name)
            .setLongLabel("Launch ${pair.appA.label} and ${pair.appB.label}")
            .setIcon(IconCompat.createWithResource(context, R.drawable.ic_launcher))
            .setIntent(intent)
            .build()
        return ShortcutManagerCompat.requestPinShortcut(context, info, null)
    }

    fun disable(id: String) = ShortcutManagerCompat.disableShortcuts(context, listOf(id), "Pair was deleted")

    companion object {
        const val ACTION = "io.github.mmkrsulis.splitscreenandroid.LAUNCH_PAIR"
        const val EXTRA_ID = "pair_id"
        const val SCHEME = "splitscreen"
        const val HOST = "pair"
    }
}
