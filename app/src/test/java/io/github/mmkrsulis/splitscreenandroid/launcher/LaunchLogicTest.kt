// SPDX-License-Identifier: AGPL-3.0-only
package io.github.mmkrsulis.splitscreenandroid.launcher

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LaunchLogicTest {
    @Test fun `second launch failure after first attempt is partial success`() {
        var firstStarted = false
        val result = launchSequentially(
            first = { firstStarted = true },
            second = { error("second rejected") },
        )
        assertTrue(firstStarted)
        assertTrue(result is LaunchResult.PartialSuccess)
        assertTrue(shouldRecordAttempt(result))
    }

    @Test fun `first launch failure is not an attempted pair launch`() {
        var secondStarted = false
        val result = launchSequentially(
            first = { error("first rejected") },
            second = { secondStarted = true },
        )
        assertFalse(secondStarted)
        assertTrue(result is LaunchResult.Failed)
        assertFalse(shouldRecordAttempt(result))
    }

    @Test fun `two accepted requests are still only partial success`() {
        val result = launchSequentially(first = {}, second = {})
        assertTrue(result is LaunchResult.PartialSuccess)
        assertTrue(shouldRecordAttempt(result))
        assertEquals(false, result is LaunchResult.Success)
    }
}
