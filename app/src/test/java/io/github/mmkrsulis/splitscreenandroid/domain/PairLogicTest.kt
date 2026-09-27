// SPDX-License-Identifier: AGPL-3.0-only
package io.github.mmkrsulis.splitscreenandroid.domain

import io.github.mmkrsulis.splitscreenandroid.model.AppPair
import io.github.mmkrsulis.splitscreenandroid.model.AppTarget
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PairLogicTest {
    private val a = AppTarget("one.pkg", "one.pkg/Main", "One")
    private val b = AppTarget("two.pkg", "two.pkg/Main", "Two")

    @Test fun `valid pair requires distinct complete targets`() {
        assertNull(validatePair(AppPair(name = "Work", appA = a, appB = b)))
        assertEquals(PairValidation.SAME_APP, validatePair(AppPair(name = "Work", appA = a, appB = a)))
        assertEquals(PairValidation.BLANK_NAME, validatePair(AppPair(name = " ", appA = a, appB = b)))
    }

    @Test fun `strategy reflects Android capability bands`() {
        assertEquals(LaunchStrategy.UNSUPPORTED, selectStrategy(27, true))
        assertEquals(LaunchStrategy.NATIVE_BEST_EFFORT, selectStrategy(28, true))
        assertEquals(LaunchStrategy.UNSUPPORTED, selectStrategy(30, false))
        assertEquals(LaunchStrategy.NATIVE_STRONGEST, selectStrategy(32, true))
        assertEquals(LaunchStrategy.NATIVE_STRONGEST, selectStrategy(36, true))
    }
}
