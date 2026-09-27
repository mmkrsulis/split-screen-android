// SPDX-License-Identifier: AGPL-3.0-only
package io.github.mmkrsulis.splitscreenandroid.launcher

import io.github.mmkrsulis.splitscreenandroid.model.AppTarget
import org.junit.Assert.assertEquals
import org.junit.Test

class AppDiscoveryLogicTest {
    @Test fun `discovery deduplicates package and excludes own package`() {
        val targets = listOf(
            AppTarget("mine", "mine/Main", "Mine"),
            AppTarget("other", "other/Zed", "Zed"),
            AppTarget("other", "other/Alpha", "Alpha"),
        )
        val result = normalizeDiscoveredApps(targets, "mine")
        assertEquals(listOf("other"), result.map { it.packageName })
        assertEquals("Alpha", result.single().label)
    }
}
